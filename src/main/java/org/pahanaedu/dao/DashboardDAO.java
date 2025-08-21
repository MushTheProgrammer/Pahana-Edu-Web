package org.pahanaedu.dao;

import org.pahanaedu.model.DashboardStats;

import java.math.BigDecimal;

public interface DashboardDAO {
    /**
     * Get all dashboard statistics in a single call
     * @return DashboardStats object containing all statistics
     */
    DashboardStats getAllStats();
    
    /**
     * Get total revenue from paid bills
     * @return total revenue as BigDecimal
     */
    BigDecimal getTotalRevenue();
    
    /**
     * Get total number of customers
     * @return count of all customers
     */
    int getTotalCustomers();
    
    /**
     * Get total number of orders (bills)
     * @return count of all bills
     */
    int getTotalOrders();
    
    /**
     * Get count of active items (quantity > 0)
     * @return count of active items
     */
    int getActiveItemsCount();
}
