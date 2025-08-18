package org.pahanaedu.service.impl;

import org.pahanaedu.dao.CustomerDAO;
import org.pahanaedu.dao.impl.CustomerDAOImpl;
import org.pahanaedu.model.Customer;
import org.pahanaedu.service.CustomerService;

import java.util.List;

public class CustomerServiceImpl implements CustomerService {
    private final CustomerDAO dao = new CustomerDAOImpl();

    @Override
    public boolean addCustomer(Customer customer) { return dao.addCustomer(customer); }

    @Override
    public boolean updateCustomer(Customer customer) { return dao.updateCustomer(customer); }

    @Override
    public boolean deleteCustomer(String accountNo) { return dao.deleteCustomer(accountNo); }

    @Override
    public Customer getCustomerByAccountNo(String accountNo) { return dao.getCustomerByAccountNo(accountNo); }

    @Override
    public List<Customer> getAllCustomers() { return dao.getAllCustomers(); }

    @Override
    public List<Customer> searchCustomers(String acc, String name, String email, String phone, int page, int size, String sort, String dir) {
        int limit = Math.max(1, size);
        int offset = Math.max(0, (page-1) * limit);
        String orderCol = sort == null ? "customer_id" : sort;
        String orderDir = dir == null ? "asc" : dir;
        return dao.searchCustomers(acc, name, email, phone, offset, limit, orderCol, orderDir);
    }

    @Override
    public int countCustomers(String acc, String name, String email, String phone) {
        return dao.countCustomers(acc, name, email, phone);
    }

    // Legacy signature removed; use paged search instead

    public String getNextAccountNo() { return dao.getNextAccountNo(); }

}
