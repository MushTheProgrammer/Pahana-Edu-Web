package org.pahanaedu.controller;

import org.pahanaedu.model.DashboardStats;
import org.pahanaedu.service.DashboardService;
import org.pahanaedu.service.impl.DashboardServiceImpl;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/dashboard/*")
public class DashboardServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DashboardServlet.class.getName());
    private final DashboardService dashboardService;

    public DashboardServlet() {
        LOGGER.log(Level.INFO, "DashboardServlet initialized");
        this.dashboardService = new DashboardServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.log(Level.INFO, "Received GET request: {0}", request.getRequestURI());
        LOGGER.log(Level.INFO, "Path info: {0}", request.getPathInfo());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try (PrintWriter out = response.getWriter()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Return all stats
                DashboardStats stats = dashboardService.getAllStats();
                JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("totalRevenue", stats.getTotalRevenue())
                    .add("totalCustomers", stats.getTotalCustomers())
                    .add("totalOrders", stats.getTotalOrders())
                    .add("activeItems", stats.getActiveItems())
                    .build();
                
                try (JsonWriter jsonWriter = Json.createWriter(out)) {
                    jsonWriter.writeObject(jsonResponse);
                }
            } else {
                // Handle specific stat requests
                JsonObject jsonResponse;
                switch (pathInfo) {
                    case "/revenue":
                        BigDecimal revenue = dashboardService.getTotalRevenue();
                        jsonResponse = Json.createObjectBuilder()
                            .add("revenue", revenue)
                            .build();
                        break;
                    case "/customers":
                        int customers = dashboardService.getTotalCustomers();
                        jsonResponse = Json.createObjectBuilder()
                            .add("customers", customers)
                            .build();
                        break;
                    case "/orders":
                        int orders = dashboardService.getTotalOrders();
                        jsonResponse = Json.createObjectBuilder()
                            .add("orders", orders)
                            .build();
                        break;
                    case "/items":
                        int items = dashboardService.getActiveItemsCount();
                        jsonResponse = Json.createObjectBuilder()
                            .add("activeItems", items)
                            .build();
                        break;
                    default:
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid endpoint");
                        return;
                }
                
                try (JsonWriter jsonWriter = Json.createWriter(out)) {
                    jsonWriter.writeObject(jsonResponse);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = Json.createObjectBuilder()
                .add("error", "Error processing request: " + e.getMessage())
                .build();
            try (JsonWriter jsonWriter = Json.createWriter(response.getWriter())) {
                jsonWriter.writeObject(error);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
