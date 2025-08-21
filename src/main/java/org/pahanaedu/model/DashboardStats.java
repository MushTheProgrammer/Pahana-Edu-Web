package org.pahanaedu.model;

import java.math.BigDecimal;
import java.util.Objects;

public class DashboardStats {
    private BigDecimal totalRevenue;
    private int totalCustomers;
    private int totalOrders;
    private int activeItems;

    public DashboardStats() {
        this.totalRevenue = BigDecimal.ZERO;
    }

    public DashboardStats(BigDecimal totalRevenue, int totalCustomers, int totalOrders, int activeItems) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.totalCustomers = totalCustomers;
        this.totalOrders = totalOrders;
        this.activeItems = activeItems;
    }

    // Getters and Setters
    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getActiveItems() {
        return activeItems;
    }

    public void setActiveItems(int activeItems) {
        this.activeItems = activeItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DashboardStats that = (DashboardStats) o;
        return totalCustomers == that.totalCustomers &&
               totalOrders == that.totalOrders &&
               activeItems == that.activeItems &&
               Objects.equals(totalRevenue, that.totalRevenue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalRevenue, totalCustomers, totalOrders, activeItems);
    }

    @Override
    public String toString() {
        return "DashboardStats{" +
                "totalRevenue=" + totalRevenue +
                ", totalCustomers=" + totalCustomers +
                ", totalOrders=" + totalOrders +
                ", activeItems=" + activeItems +
                '}';
    }
}
