package org.pahanaedu.service.impl;

import org.pahanaedu.dao.DashboardDAO;
import org.pahanaedu.dao.impl.DashboardDAOImpl;
import org.pahanaedu.model.DashboardStats;
import org.pahanaedu.service.DashboardService;

import java.math.BigDecimal;

public class DashboardServiceImpl implements DashboardService {

    private final DashboardDAO dashboardDAO;

    public DashboardServiceImpl() {
        this.dashboardDAO = new DashboardDAOImpl();
    }

    public DashboardServiceImpl(DashboardDAO dashboardDAO) {
        this.dashboardDAO = dashboardDAO;
    }

    @Override
    public DashboardStats getAllStats() {
        return dashboardDAO.getAllStats();
    }

    @Override
    public BigDecimal getTotalRevenue() {
        return dashboardDAO.getTotalRevenue();
    }

    @Override
    public int getTotalCustomers() {
        return dashboardDAO.getTotalCustomers();
    }

    @Override
    public int getTotalOrders() {
        return dashboardDAO.getTotalOrders();
    }

    @Override
    public int getActiveItemsCount() {
        return dashboardDAO.getActiveItemsCount();
    }
}
