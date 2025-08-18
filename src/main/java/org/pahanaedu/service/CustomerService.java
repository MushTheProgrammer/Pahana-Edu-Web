package org.pahanaedu.service;

import org.pahanaedu.model.Customer;
import java.util.List;

public interface CustomerService {
        boolean addCustomer(Customer customer);
        boolean updateCustomer(Customer customer);
        boolean deleteCustomer(String accountNo);
        Customer getCustomerByAccountNo(String accountNo);
        List<Customer> getAllCustomers();
        List<Customer> searchCustomers(String acc, String name, String email, String phone, int page,
                                       int size, String sort, String dir);
        int countCustomers(String acc, String name, String email, String phone);
        String getNextAccountNo();
}
