package org.pahanaedu.dao.impl;

import org.pahanaedu.dao.BillDAO;
import org.pahanaedu.dao.CustomerDAO;
import org.pahanaedu.dao.DashboardDAO;
import org.pahanaedu.dao.ItemDAO;
import org.pahanaedu.model.DashboardStats;
import org.pahanaedu.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DashboardDAOImpl implements DashboardDAO {

    private final BillDAO billDAO;
    private final CustomerDAO customerDAO;
    private final ItemDAO itemDAO;

    public DashboardDAOImpl() {
        this.billDAO = new BillDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
        this.itemDAO = new ItemDAOImpl();
    }

    @Override
    public DashboardStats getAllStats() {
        return new DashboardStats(
            getTotalRevenue(),
            getTotalCustomers(),
            getTotalOrders(),
            getActiveItemsCount()
        );
    }

    @Override
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total FROM bills WHERE status = 'PAID'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
            return BigDecimal.ZERO;
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @Override
    public int getTotalCustomers() {
        String sql = "SELECT COUNT(*) as total FROM customers";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting total customers count: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) as total FROM bills";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting total orders count: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int getActiveItemsCount() {
        String sql = "SELECT COUNT(*) as total FROM items WHERE qty_on_hand > 0";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting active items count: " + e.getMessage());
            return 0;
        }
    }
}
