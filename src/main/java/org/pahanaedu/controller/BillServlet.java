package org.pahanaedu.controller;

import org.pahanaedu.model.Bill;
import org.pahanaedu.model.BillItem;
import org.pahanaedu.service.BillService;
import org.pahanaedu.service.impl.BillServiceImpl;

import javax.json.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "BillServlet", urlPatterns = {"/bills", "/bills/*"})
public class BillServlet extends HttpServlet {

    private final BillService service = new BillServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        // Add debug logging
        System.out.println("BillServlet - doGet called with parameters: " + req.getQueryString());

        // Simple test endpoint
        String test = req.getParameter("test");
        if ("true".equals(test)) {
            resp.getWriter().write("{\"status\":\"BillServlet is working\",\"timestamp\":\"" + new java.util.Date() + "\"}");
            return;
        }

        String billIdParam = req.getParameter("billId");
        String invoiceNumberParam = req.getParameter("invoiceNumber");

        // Get single bill by ID or invoice number
        if (billIdParam != null || invoiceNumberParam != null) {
            Bill bill = null;
            if (billIdParam != null) {
                bill = service.getBillWithItems(Integer.parseInt(billIdParam));
            } else {
                bill = service.getBillByInvoiceNumber(invoiceNumberParam);
                if (bill != null) {
                    bill = service.getBillWithItems(bill.getBillId());
                }
            }
            
            if (bill != null) {
                resp.getWriter().write(billToJson(bill).toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Bill not found\"}");
            }
            return;
        }

        // Search bills with pagination
        String invoiceNumber = req.getParameter("invoiceNumber");
        String customer = req.getParameter("customer");
        String status = req.getParameter("status");
        LocalDate fromDate = parseDate(req.getParameter("fromDate"));
        LocalDate toDate = parseDate(req.getParameter("toDate"));
        int page = parseInt(req.getParameter("page")) == null ? 1 : parseInt(req.getParameter("page"));
        int size = parseInt(req.getParameter("size")) == null ? 10 : parseInt(req.getParameter("size"));
        String sort = req.getParameter("sort");
        String dir = req.getParameter("dir");

        List<Bill> bills = service.searchBills(nn(invoiceNumber), nn(customer), nn(status), 
                                              fromDate, toDate, page, size, sort, dir);
        int total = service.countBills(nn(invoiceNumber), nn(customer), nn(status), fromDate, toDate);

        JsonObjectBuilder out = Json.createObjectBuilder();
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Bill bill : bills) {
            arr.add(billToJson(bill));
        }
        out.add("data", arr).add("page", page).add("size", size).add("total", total);
        resp.getWriter().write(out.build().toString());
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Get next invoice number
        String next = req.getParameter("next");
        if ("true".equalsIgnoreCase(next)) {
            String invoiceNumber = service.getNextInvoiceNumber();
            resp.setHeader("X-Next-Invoice-Number", invoiceNumber);
        }
    }

    // Add CORS headers to the response
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Max-Age", "3600");
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsHeaders(resp);
        System.out.println("BillServlet - doPost called");
        JsonObject body = readJsonBody(req);
        System.out.println("BillServlet - Request body parsed: " + (body != null));
        
        try {
            // Create bill
            Bill bill = new Bill();
            String invoiceNumber = body.containsKey("invoiceNumber") ? body.getString("invoiceNumber") : "";
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
                invoiceNumber = service.getNextInvoiceNumber();
            }
            bill.setInvoiceNumber(invoiceNumber);
            bill.setCustomerAccountNo(body.getString("customerAccountNo"));
            bill.setBillDate(LocalDateTime.parse(body.getString("billDate"), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            bill.setDiscountRate(new BigDecimal(body.getJsonNumber("discountRate").doubleValue()));
            bill.setStatus(body.containsKey("status") ? body.getString("status") : "Paid");
            bill.setNotes(body.containsKey("notes") ? body.getString("notes") : "");

            // Initialize totals to zero (will be calculated later)
            bill.setSubtotal(BigDecimal.ZERO);
            bill.setDiscountAmount(BigDecimal.ZERO);
            bill.setTotalAmount(BigDecimal.ZERO);

            // Create bill items
            List<BillItem> items = new ArrayList<>();
            JsonArray itemsArray = body.getJsonArray("items");
            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject itemObj = itemsArray.getJsonObject(i);
                BillItem item = new BillItem();
                item.setItemCode(itemObj.getString("itemCode"));
                item.setItemName(itemObj.getString("itemName"));
                item.setUnitPrice(new BigDecimal(itemObj.getJsonNumber("unitPrice").doubleValue()));
                item.setQuantity(itemObj.getJsonNumber("quantity").intValue());
                item.calculateLineTotal();
                items.add(item);
            }

            String error = validateBill(bill, items);
            if (error != null) {
                System.out.println("BillServlet - Validation error: " + error);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.getWriter().write("{\"success\":false,\"error\":\"" + error + "\"}");
                return;
            }

            System.out.println("BillServlet - Creating bill with invoice number: " + bill.getInvoiceNumber());
            System.out.println("BillServlet - Customer account: " + bill.getCustomerAccountNo());
            System.out.println("BillServlet - Items count: " + items.size());

            Bill createdBill = service.createBillWithItems(bill, items);
            resp.setContentType("application/json");
            if (createdBill != null) {
                System.out.println("BillServlet - Bill created successfully with ID: " + createdBill.getBillId());
                resp.getWriter().write("{\"success\":true,\"billId\":" + createdBill.getBillId() +
                                     ",\"invoiceNumber\":\"" + createdBill.getInvoiceNumber() + "\"}");
            } else {
                System.out.println("BillServlet - Failed to create bill");
                resp.getWriter().write("{\"success\":false,\"error\":\"Failed to create bill\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":false,\"error\":\"Invalid request data\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = readJsonBody(req);
        
        try {
            Integer billId = body.getInt("billId");
            String status = body.getString("status");
            
            boolean success = service.updateBillStatus(billId, status);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":" + success + "}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"success\":false,\"error\":\"Invalid request data\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Log the incoming request
        System.out.println("\n=== DELETE Request Received ===");
        System.out.println("Request Method: " + req.getMethod());
        System.out.println("Request URL: " + req.getRequestURL());
        System.out.println("Request URI: " + req.getRequestURI());
        System.out.println("Context Path: " + req.getContextPath());
        System.out.println("Servlet Path: " + req.getServletPath());
        System.out.println("Path Info: " + req.getPathInfo());
        System.out.println("Query String: " + req.getQueryString());
        
        // Log all headers for debugging
        System.out.println("\nHeaders:");
        java.util.Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + req.getHeader(headerName));
        }
        
        setCorsHeaders(resp);
        resp.setContentType("application/json");
        
        try {
            // First try to get from path parameter
            String pathInfo = req.getPathInfo();
            String invoiceNumber = null;
            
            // Extract invoice number from URL path if available
            if (pathInfo != null && pathInfo.length() > 1) {
                // Remove leading '/' and any trailing slashes
                invoiceNumber = pathInfo.replaceAll("^/|/$", "");
                System.out.println("Extracted invoice number from path: " + invoiceNumber);
            } else {
                System.out.println("No path info found in URL");
            }
            
            // Fallback to query parameter if not in path
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
                invoiceNumber = req.getParameter("invoiceNumber");
                System.out.println("Extracted invoice number from query param: " + invoiceNumber);
            } else {
                System.out.println("Using invoice number from path");
            }
            
            if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
                String errorMsg = "Invoice number is required";
                System.out.println(errorMsg);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"error\":\"" + errorMsg + "\"}");
                return;
            }
            
            // Delete the bill by invoice number
            System.out.println("Attempting to delete invoice: " + invoiceNumber);
            boolean success = service.deleteBillByInvoiceNumber(invoiceNumber);
            
            if (success) {
                String successMsg = "Invoice " + invoiceNumber + " has been deleted successfully";
                System.out.println(successMsg);
                JsonObject response = Json.createObjectBuilder()
                    .add("success", true)
                    .add("message", successMsg)
                    .build();
                resp.getWriter().write(response.toString());
            } else {
                String errorMsg = "Invoice " + invoiceNumber + " not found or could not be deleted";
                System.out.println(errorMsg);
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"success\":false,\"error\":\"" + errorMsg + "\"}");
            }
            
        } catch (Exception e) {
            String errorMsg = "Error in doDelete: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = Json.createObjectBuilder()
                .add("success", false)
                .add("error", errorMsg)
                .build();
            resp.getWriter().write(error.toString());
        } finally {
            System.out.println("=== End of DELETE Request ===\n");
        }
    }

    private String validateBill(Bill bill, List<BillItem> items) {
        if (bill.getInvoiceNumber() == null || bill.getInvoiceNumber().trim().isEmpty()) {
            return "Invoice number is required";
        }
        if (bill.getCustomerAccountNo() == null || bill.getCustomerAccountNo().trim().isEmpty()) {
            return "Customer is required";
        }
        if (bill.getBillDate() == null) {
            return "Bill date is required";
        }
        if (bill.getDiscountRate() == null || bill.getDiscountRate().compareTo(BigDecimal.ZERO) < 0 || 
            bill.getDiscountRate().compareTo(new BigDecimal("100")) > 0) {
            return "Discount rate must be between 0 and 100";
        }
        if (items == null || items.isEmpty()) {
            return "At least one item is required";
        }
        for (BillItem item : items) {
            if (item.getItemCode() == null || item.getItemCode().trim().isEmpty()) {
                return "Item code is required for all items";
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                return "Item quantity must be greater than 0";
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                return "Item unit price must be >= 0";
            }
        }
        return null;
    }

    private JsonObject billToJson(Bill bill) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("billId", bill.getBillId())
                .add("invoiceNumber", bill.getInvoiceNumber())
                .add("customerAccountNo", bill.getCustomerAccountNo())
                .add("billDate", bill.getBillDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .add("subtotal", bill.getSubtotal())
                .add("discountRate", bill.getDiscountRate())
                .add("discountAmount", bill.getDiscountAmount())
                .add("totalAmount", bill.getTotalAmount())
                .add("status", bill.getStatus())
                .add("notes", bill.getNotes() != null ? bill.getNotes() : "")
                .add("createdAt", bill.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .add("updatedAt", bill.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (bill.getCustomerName() != null) {
            builder.add("customerName", bill.getCustomerName());
        }
        if (bill.getCustomerEmail() != null) {
            builder.add("customerEmail", bill.getCustomerEmail());
        }
        if (bill.getCustomerPhone() != null) {
            builder.add("customerPhone", bill.getCustomerPhone());
        }
        if (bill.getItemCount() != null) {
            builder.add("itemCount", bill.getItemCount());
        }

        if (bill.getBillItems() != null) {
            JsonArrayBuilder itemsArray = Json.createArrayBuilder();
            for (BillItem item : bill.getBillItems()) {
                itemsArray.add(Json.createObjectBuilder()
                        .add("billItemId", item.getBillItemId())
                        .add("itemCode", item.getItemCode())
                        .add("itemName", item.getItemName())
                        .add("unitPrice", item.getUnitPrice())
                        .add("quantity", item.getQuantity())
                        .add("lineTotal", item.getLineTotal()));
            }
            builder.add("items", itemsArray);
        }

        return builder.build();
    }

    private JsonObject readJsonBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        try (JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()))) {
            return jsonReader.readObject();
        }
    }

    private String nn(String s) { return s == null ? "" : s; }
    private Integer parseInt(String s) { try { return s == null ? null : Integer.parseInt(s); } catch (Exception e) { return null; } }
    private LocalDate parseDate(String s) { try { return s == null ? null : LocalDate.parse(s); } catch (Exception e) { return null; } }
}
