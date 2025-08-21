package org.pahanaedu.controller;

import org.pahanaedu.model.Item;
import org.pahanaedu.service.ItemService;
import org.pahanaedu.service.impl.ItemServiceImpl;

import javax.json.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/items")
public class ItemServlet extends HttpServlet {

    private final ItemService service = new ItemServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        String codeParam = req.getParameter("code");
        if (codeParam != null && codeParam.length() > 0 && req.getParameter("single") != null) {
            Item i = service.getItemByCode(codeParam);
            if (i != null) resp.getWriter().write(toJson(i).toString());
            else { resp.setStatus(HttpServletResponse.SC_NOT_FOUND); resp.getWriter().write("{\"error\":\"Item not found\"}"); }
            return;
        }

        String code = req.getParameter("code");
        String name = req.getParameter("name");
        BigDecimal minPrice = parseDecimal(req.getParameter("minPrice"));
        BigDecimal maxPrice = parseDecimal(req.getParameter("maxPrice"));
        Integer minQty = parseInt(req.getParameter("minQty"));
        Integer maxQty = parseInt(req.getParameter("maxQty"));
        int page = parseInt(req.getParameter("page")) == null ? 1 : parseInt(req.getParameter("page"));
        int size = parseInt(req.getParameter("size")) == null ? 10 : parseInt(req.getParameter("size"));
        String sort = req.getParameter("sort");
        String dir = req.getParameter("dir");

        List<Item> items = service.searchItems(nn(code), nn(name), minPrice, maxPrice, minQty, maxQty, page, size, sort, dir);
        int total = service.countItems(nn(code), nn(name), minPrice, maxPrice, minQty, maxQty);
        JsonObjectBuilder out = Json.createObjectBuilder();
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Item it : items) arr.add(toJson(it));
        out.add("data", arr).add("page", page).add("size", size).add("total", total);
        resp.getWriter().write(out.build().toString());
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // expose next item code via HEAD /items?next=true
        String next = req.getParameter("next");
        if ("true".equalsIgnoreCase(next)) {
            String code = service.getNextItemCode();
            resp.setHeader("X-Next-Item-Code", code);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = readJsonBody(req);
        Item i = new Item();
        String code = body.containsKey("itemCode") ? body.getString("itemCode") : "";
        if (code == null || code.trim().isEmpty()) code = service.getNextItemCode();
        i.setItemCode(code);
        i.setName(body.getString("name"));
        i.setUnitPrice(new BigDecimal(body.getJsonNumber("unitPrice").doubleValue()));
        i.setQtyOnHand(body.getJsonNumber("qtyOnHand").intValue());

        String error = validate(i);
        resp.setContentType("application/json");
        if (error != null) {
            System.out.println("Validation error: " + error);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"error\":\""+error+"\"}");
            return;
        }
        System.out.println("Attempting to add item: " + i.getItemCode());
        boolean success = service.addItem(i);
        System.out.println("Add item result: " + success);
        resp.getWriter().write("{\"success\":"+success+"}");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonObject body = readJsonBody(req);
        Item i = new Item();
        i.setItemCode(body.getString("itemCode"));
        i.setName(body.getString("name"));
        i.setUnitPrice(new BigDecimal(body.getJsonNumber("unitPrice").doubleValue()));
        i.setQtyOnHand(body.getJsonNumber("qtyOnHand").intValue());

        String error = validate(i);
        if (error != null) { resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); resp.getWriter().write("{\"success\":false,\"error\":\""+error+"\"}"); return; }
        boolean success = service.updateItem(i);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\":"+success+"}");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        boolean success = false;
        if (code != null) success = service.deleteItem(code);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\":"+success+"}");
    }

    private String validate(Item i) {
        if (i.getItemCode() == null || i.getItemCode().trim().isEmpty()) return "Item code is required";
        if (i.getName() == null || i.getName().trim().isEmpty()) return "Name is required";
        if (i.getUnitPrice() == null || i.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) return "Price must be >= 0";
        if (i.getQtyOnHand() == null || i.getQtyOnHand() < 0) return "Qty must be >= 0";
        return null;
    }

    private JsonObject toJson(Item it) {
        return Json.createObjectBuilder()
                .add("itemCode", it.getItemCode())
                .add("name", it.getName())
                .add("unitPrice", it.getUnitPrice())
                .add("qtyOnHand", it.getQtyOnHand())
                .build();
    }

    private JsonObject readJsonBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        try (JsonReader jsonReader = Json.createReader(new StringReader(sb.toString()))) {
            return jsonReader.readObject();
        }
    }

    private String nn(String s) { return s == null ? "" : s; }
    private Integer parseInt(String s) { try { return s==null?null:Integer.parseInt(s);} catch (Exception e) { return null; } }
    private BigDecimal parseDecimal(String s) { try { return s==null?null:new BigDecimal(s);} catch (Exception e) { return null; } }
}
