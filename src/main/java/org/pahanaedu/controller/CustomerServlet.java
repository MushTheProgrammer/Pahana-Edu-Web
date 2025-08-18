package org.pahanaedu.controller;

import org.pahanaedu.model.Customer;
import org.pahanaedu.service.CustomerService;
import org.pahanaedu.service.impl.CustomerServiceImpl;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

@WebServlet("/customers")
public class CustomerServlet extends HttpServlet {

    private final CustomerService service = new CustomerServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String accParam = req.getParameter("accNo");

        if (accParam != null) {
            Customer c = service.getCustomerByAccountNo(accParam);
            if (c != null) {
                JsonObject json = toJson(c);
                resp.getWriter().write(json.toString());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Customer not found\"}");
            }
        } else {
            // Optional server-side search filters
            String acc = req.getParameter("acc");
            String name = req.getParameter("name");
            String email = req.getParameter("email");
            String phone = req.getParameter("phone");
            String pageStr = req.getParameter("page");
            String sizeStr = req.getParameter("size");
            String sort = req.getParameter("sort");
            String dir = req.getParameter("dir");
            int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
            int size = sizeStr != null ? Integer.parseInt(sizeStr) : 10;

            List<Customer> customers = (acc!=null || name!=null || email!=null || phone!=null || sort!=null)
                    ? service.searchCustomers(nonNull(acc), nonNull(name), nonNull(email), nonNull(phone), page, size, sort, dir)
                    : service.searchCustomers("","","","", page, size, sort, dir);

            int total = service.countCustomers(nonNull(acc), nonNull(name), nonNull(email), nonNull(phone));

            JsonObjectBuilder out = Json.createObjectBuilder();
            JsonArrayBuilder arr = Json.createArrayBuilder();
            for (Customer c : customers) arr.add(toJson(c));
            out.add("data", arr);
            out.add("page", page).add("size", size).add("total", total);
            resp.getWriter().write(out.build().toString());
        }
    }


    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // expose next account via HEAD /customers?next=true
        String next = req.getParameter("next");
        if ("true".equalsIgnoreCase(next)) {
            String acc = service.getNextAccountNo();
            resp.setHeader("X-Next-Account-No", acc);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = readJsonBody(req);

        Customer c = new Customer();
        String accNo = body.containsKey("accountNo") ? body.getString("accountNo") : "";
        if (accNo == null || accNo.trim().isEmpty()) {
            // Server-side safeguard: auto-generate if client didn't provide
            accNo = service.getNextAccountNo();
        }
        c.setAccountNo(accNo);
        c.setName(body.getString("name"));
        c.setEmail(body.containsKey("email") ? body.getString("email") : "");
        c.setAddress(body.containsKey("address") ? body.getString("address") : "");
        c.setPhone(body.containsKey("phone") ? body.getString("phone") : "");

        String error = validateCustomer(c, false);
        resp.setContentType("application/json");
        if (error != null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"error\":\"" + error + "\"}");
            return;
        }

        boolean success = service.addCustomer(c);
        resp.getWriter().write("{\"success\":" + success + "}");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = readJsonBody(req);

        Customer c = new Customer();
        c.setAccountNo(body.getString("accountNo"));
        c.setName(body.getString("name"));
        c.setEmail(body.containsKey("email") ? body.getString("email") : "");
        c.setAddress(body.containsKey("address") ? body.getString("address") : "");
        c.setPhone(body.containsKey("phone") ? body.getString("phone") : "");

        String error = validateCustomer(c, true);
        resp.setContentType("application/json");
        if (error != null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"error\":\"" + error + "\"}");
            return;
        }

        boolean success = service.updateCustomer(c);
        resp.getWriter().write("{\"success\":" + success + "}");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String accParam = req.getParameter("accNo");
        boolean success = false;
        if (accParam != null) {
            success = service.deleteCustomer(accParam);
        }
        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\":" + success + "}");
    }

    private String validateCustomer(Customer c, boolean isUpdate) {
        if (c.getAccountNo() == null || c.getAccountNo().trim().isEmpty()) return "Account No is required";
        if (c.getName() == null || c.getName().trim().isEmpty()) return "Name is required";
        String email = c.getEmail() == null ? "" : c.getEmail().trim();
        if (email.isEmpty() || !email.contains("@")) return "Valid email is required";
        String phone = c.getPhone() == null ? "" : c.getPhone().trim();
        if (!phone.isEmpty() && !phone.matches("[0-9]{10}")) return "Phone must be 10 digits";
        return null;
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

    /**
     * This method converts a Customer object to a JSON representation.
     *
     * @param c
     * @return
     */
    private JsonObject toJson(Customer c) {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("accountNo", c.getAccountNo());
        builder.add("name", c.getName());
        builder.add("email", c.getEmail() == null ? "" : c.getEmail());
        builder.add("address", c.getAddress() == null ? "" : c.getAddress());
        builder.add("phone", c.getPhone() == null ? "" : c.getPhone());
        return builder.build();
    }

    private String nonNull(String s) { return s == null ? "" : s; }

}
