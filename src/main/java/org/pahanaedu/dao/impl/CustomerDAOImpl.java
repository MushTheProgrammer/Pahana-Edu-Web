package org.pahanaedu.dao.impl;

import org.pahanaedu.dao.CustomerDAO;
import org.pahanaedu.model.Customer;
import org.pahanaedu.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {

    @Override
    public String getNextAccountNo() {
        String last = null;
        String sql = "SELECT account_no FROM customers ORDER BY account_no DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) last = rs.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (last == null || last.isEmpty()) return "ACC0001";
        try {
            String numStr = last.replaceAll("[^0-9]", "");
            int num = Integer.parseInt(numStr);
            return String.format("ACC%04d", num + 1);
        } catch (Exception e) {
            return "ACC0001";
        }
    }


    @Override
    public List<Customer> searchCustomers(String acc, String name, String email, String phone, int offset, int limit, String orderByCol, String orderDir) {
        List<Customer> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM customers WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (acc != null && !acc.isEmpty()) {
            sql.append(" AND LOWER(account_no) LIKE ?");
            params.add("%" + acc.toLowerCase() + "%");
        }
        if (name != null && !name.isEmpty()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + name.toLowerCase() + "%");
        }

        if (email != null && !email.isEmpty()) {
            sql.append(" AND LOWER(email) LIKE ?");
            params.add("%" + email.toLowerCase() + "%");
        }

        if (phone != null && !phone.isEmpty()) {
            sql.append(" AND phone LIKE ?");
            params.add("%" + phone + "%");
        }

        // Sorting - whitelist columns to avoid SQL injection
        String col = "account_no";
        if ("account_no".equalsIgnoreCase(orderByCol)) col = "account_no";
        else if ("name".equalsIgnoreCase(orderByCol)) col = "name";
        else if ("email".equalsIgnoreCase(orderByCol)) col = "email";
        else if ("phone".equalsIgnoreCase(orderByCol)) col = "phone";

        String dir = "ASC";
        if ("desc".equalsIgnoreCase(orderDir)) dir = "DESC";
        sql.append(" ORDER BY ").append(col).append(" ").append(dir);

        // Pagination
        if (limit > 0) {
            sql.append(" LIMIT ? OFFSET ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) ps.setObject(idx++, p);
            if (limit > 0) {
                ps.setInt(idx++, limit);
                ps.setInt(idx, offset);
            }
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Customer c = new Customer();
                c.setAccountNo(rs.getString("account_no"));
                c.setName(rs.getString("name"));
                c.setEmail(rs.getString("email"));
                c.setAddress(rs.getString("address"));
                c.setPhone(rs.getString("phone"));
                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    @Override
    public int countCustomers(String acc, String name, String email, String phone) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM customers WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (acc != null && !acc.isEmpty()) {
            sql.append(" AND LOWER(account_no) LIKE ?");
            params.add("%" + acc.toLowerCase() + "%");
        }
        if (name != null && !name.isEmpty()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + name.toLowerCase() + "%");
        }
        if (email != null && !email.isEmpty()) {
            sql.append(" AND LOWER(email) LIKE ?");
            params.add("%" + email.toLowerCase() + "%");
        }
        if (phone != null && !phone.isEmpty()) {
            sql.append(" AND phone LIKE ?");
            params.add("%" + phone + "%");
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (account_no, name, email, address, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getAccountNo());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getPhone());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name=?, email=?, address=?, phone=? WHERE account_no=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getAddress());
            ps.setString(4, customer.getPhone());
            ps.setString(5, customer.getAccountNo());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteCustomer(String accountNo) {
        String sql = "DELETE FROM customers WHERE account_no=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Customer getCustomerByAccountNo(String accountNo) {
        String sql = "SELECT * FROM customers WHERE account_no=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = new Customer();
                c.setAccountNo(rs.getString("account_no"));
                c.setName(rs.getString("name"));
                c.setEmail(rs.getString("email"));
                c.setAddress(rs.getString("address"));
                c.setPhone(rs.getString("phone"));
                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Customer c = new Customer();
                c.setAccountNo(rs.getString("account_no"));
                c.setName(rs.getString("name"));
                c.setEmail(rs.getString("email"));
                c.setAddress(rs.getString("address"));
                c.setPhone(rs.getString("phone"));
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
