package org.pahanaedu.dao;

import org.pahanaedu.model.Customer;
import java.util.List;

public interface CustomerDAO {
        boolean addCustomer(Customer customer);
        boolean updateCustomer(Customer customer);
        boolean deleteCustomer(String accountNo);
        Customer getCustomerByAccountNo(String accountNo);
        List<Customer> getAllCustomers();
        // Paged and sorted search
        List<Customer> searchCustomers(String acc, String name, String email, String phone, int offset, int limit, String orderByCol, String orderDir);
        int countCustomers(String acc, String name, String email, String phone);
        String getNextAccountNo();
}
