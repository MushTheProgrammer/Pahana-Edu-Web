package org.pahanaedu.dao.impl;

import org.pahanaedu.dao.BillDAO;
import org.pahanaedu.model.Bill;
import org.pahanaedu.model.BillItem;
import org.pahanaedu.util.DBConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class BillDAOImpl implements BillDAO {

    @Override
    public boolean deleteBill(Integer billId) {
        // First delete all bill items to maintain referential integrity
        deleteBillItems(billId);
        
        // Then delete the bill
        String sql = "DELETE FROM bills WHERE bill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }
    
    @Override
    public boolean deleteBillByInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            return false;
        }
        
        // First get the bill ID to delete its items
        String getBillIdSql = "SELECT bill_id FROM bills WHERE invoice_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(getBillIdSql)) {
            
            ps.setString(1, invoiceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int billId = rs.getInt("bill_id");
                    // Delete the bill using the existing deleteBill method which handles referential integrity
                    return deleteBill(billId);
                }
            }
            return false; // No bill found with this invoice number
            
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public List<Bill> getAllBills() {
        return searchBills(null, null, null, null, null, 0, 0, "bill_date", "desc");
    }

    @Override
    public Bill getBillById(Integer billId) {
        String sql = "SELECT b.*, c.name as customer_name, c.email as customer_email, c.phone as customer_phone " +
                    "FROM bills b LEFT JOIN customers c ON b.customer_account_no = c.account_no " +
                    "WHERE b.bill_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        } catch (SQLException e) {
            // Log error
        }
        return null;
    }

    @Override
    public Bill getBillByInvoiceNumber(String invoiceNumber) {
        String sql = "SELECT b.*, c.name as customer_name, c.email as customer_email, c.phone as customer_phone " +
                    "FROM bills b LEFT JOIN customers c ON b.customer_account_no = c.account_no " +
                    "WHERE b.invoice_number=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBill(rs);
                }
            }
        } catch (SQLException e) {
            // Log error
        }
        return null;
    }

    @Override
    public String getNextInvoiceNumber() {
        String currentDate = java.time.LocalDate.now().toString(); // YYYY-MM-DD
        String sql = "SELECT MAX(CAST(SUBSTRING_INDEX(invoice_number, '-', -1) AS UNSIGNED)) as last_sequence " +
                   "FROM bills WHERE invoice_number LIKE ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Get the maximum sequence number for the current date
            ps.setString(1, currentDate + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getObject(1) != null) {
                    // If there are invoices for today, increment the sequence
                    int lastSequence = rs.getInt(1);
                    return String.format("%s-%05d", currentDate, lastSequence + 1);
                } else {
                    // No invoices for today, start from 1
                    return String.format("%s-%05d", currentDate, 1);
                }
            }
        } catch (SQLException e) {
            // Log the error
            e.printStackTrace();
            // Fallback: return current date with timestamp as sequence
            return String.format("%s-%05d", currentDate, (int)(System.currentTimeMillis() % 100000));
        }
    }

    @Override
    public boolean addBillItem(BillItem billItem) {
        String sql = "INSERT INTO bill_items (bill_id, item_code, item_name, unit_price, quantity, line_total) " +
                   "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, billItem.getBillId());
            ps.setString(2, billItem.getItemCode());
            ps.setString(3, billItem.getItemName());
            ps.setBigDecimal(4, billItem.getUnitPrice());
            ps.setInt(5, billItem.getQuantity());
            ps.setBigDecimal(6, billItem.getLineTotal());

            int result = ps.executeUpdate();
            if (result > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        billItem.setBillItemId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            // Log error
        }
        return false;
    }

    @Override
    public boolean updateBillItem(BillItem billItem) {
        String sql = "UPDATE bill_items SET item_code=?, item_name=?, unit_price=?, " +
                   "quantity=?, line_total=? WHERE bill_item_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, billItem.getItemCode());
            ps.setString(2, billItem.getItemName());
            ps.setBigDecimal(3, billItem.getUnitPrice());
            ps.setInt(4, billItem.getQuantity());
            ps.setBigDecimal(5, billItem.getLineTotal());
            ps.setInt(6, billItem.getBillItemId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public boolean deleteBillItem(Integer billItemId) {
        String sql = "DELETE FROM bill_items WHERE bill_item_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public List<BillItem> getBillItems(Integer billId) {
        List<BillItem> items = new ArrayList<>();
        String sql = "SELECT * FROM bill_items WHERE bill_id=? ORDER BY bill_item_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillItem item = new BillItem();
                    item.setBillItemId(rs.getInt("bill_item_id"));
                    item.setBillId(rs.getInt("bill_id"));
                    item.setItemCode(rs.getString("item_code"));
                    item.setItemName(rs.getString("item_name"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setLineTotal(rs.getBigDecimal("line_total"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            // Log error
        }
        return items;
    }

    @Override
    public boolean deleteBillItems(Integer billId) {
        String sql = "DELETE FROM bill_items WHERE bill_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public BigDecimal calculateBillSubtotal(Integer billId) {
        String sql = "SELECT COALESCE(SUM(line_total), 0) AS subtotal FROM bill_items WHERE bill_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("subtotal");
                }
            }
        } catch (SQLException e) {
            // Log error
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean updateBillTotals(Integer billId, BigDecimal subtotal,
                                   BigDecimal discountRate, BigDecimal discountAmount,
                                   BigDecimal totalAmount) {
        String sql = "UPDATE bills SET subtotal=?, discount_rate=?, " +
                   "discount_amount=?, total_amount=? WHERE bill_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, subtotal);
            ps.setBigDecimal(2, discountRate);
            ps.setBigDecimal(3, discountAmount);
            ps.setBigDecimal(4, totalAmount);
            ps.setInt(5, billId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public boolean updateBillStatus(Integer billId, String status) {
        String sql = "UPDATE bills SET status=? WHERE bill_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, billId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log error
            return false;
        }
    }

    @Override
    public List<Bill> searchBills(String invoiceNumber, String customer, String status,
                                 LocalDate fromDate, LocalDate toDate,
                                 int offset, int limit, String orderByCol, String orderDir) {
        List<Bill> bills = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT b.*, c.name as customer_name, c.email as customer_email, c.phone as customer_phone, " +
            "COUNT(bi.bill_item_id) as item_count " +
            "FROM bills b " +
            "LEFT JOIN customers c ON b.customer_account_no = c.account_no " +
            "LEFT JOIN bill_items bi ON b.bill_id = bi.bill_id " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
            sql.append(" AND b.invoice_number LIKE ?");
            params.add("%" + invoiceNumber + "%");
        }
        if (customer != null && !customer.isEmpty()) {
            sql.append(" AND (c.name LIKE ? OR b.customer_account_no LIKE ?)");
            params.add("%" + customer + "%");
            params.add("%" + customer + "%");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND b.status = ?");
            params.add(status);
        }
        if (fromDate != null) {
            sql.append(" AND DATE(b.bill_date) >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND DATE(b.bill_date) <= ?");
            params.add(toDate);
        }

        sql.append(" GROUP BY b.bill_id, b.invoice_number, b.customer_account_no, c.name, c.email, c.phone, " +
                  "b.bill_date, b.subtotal, b.discount_rate, b.discount_amount, b.total_amount, b.status, " +
                  "b.notes, b.created_at, b.updated_at");

        // Sorting
        String col = "b.bill_date";
        if ("invoice_number".equalsIgnoreCase(orderByCol)) col = "b.invoice_number";
        else if ("customer_name".equalsIgnoreCase(orderByCol)) col = "c.name";
        else if ("total_amount".equalsIgnoreCase(orderByCol)) col = "b.total_amount";
        else if ("status".equalsIgnoreCase(orderByCol)) col = "b.status";

        String dir = "DESC";
        if ("asc".equalsIgnoreCase(orderDir)) dir = "ASC";

        sql.append(" ORDER BY ").append(col).append(" ").append(dir);

        if (limit > 0) {
            sql.append(" LIMIT ? OFFSET ?");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            for (Object param : params) {
                ps.setObject(idx++, param);
            }
            if (limit > 0) {
                ps.setInt(idx++, limit);
                ps.setInt(idx, offset);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Bill bill = mapResultSetToBill(rs);
                bills.add(bill);
            }
        } catch (Exception e) {
            // Log error
        }

        return bills;
    }

    @Override
    public int countBills(String invoiceNumber, String customer, String status,
                         LocalDate fromDate, LocalDate toDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(DISTINCT b.bill_id) FROM bills b " +
            "LEFT JOIN customers c ON b.customer_account_no = c.account_no " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
            sql.append(" AND b.invoice_number LIKE ?");
            params.add("%" + invoiceNumber + "%");
        }
        if (customer != null && !customer.isEmpty()) {
            sql.append(" AND (c.name LIKE ? OR b.customer_account_no LIKE ?)");
            params.add("%" + customer + "%");
            params.add("%" + customer + "%");
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND b.status = ?");
            params.add(status);
        }
        if (fromDate != null) {
            sql.append(" AND DATE(b.bill_date) >= ?");
            params.add(fromDate);
        }
        if (toDate != null) {
            sql.append(" AND DATE(b.bill_date) <= ?");
            params.add(toDate);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            // Log error
        }

        return 0;
    }

    @Override
    public boolean addBill(Bill bill) {
        String sql = "INSERT INTO bills (invoice_number, customer_account_no, bill_date, subtotal, " +
                    "discount_rate, discount_amount, total_amount, status, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, bill.getInvoiceNumber());
            ps.setString(2, bill.getCustomerAccountNo());
            ps.setTimestamp(3, Timestamp.valueOf(bill.getBillDate()));
            ps.setBigDecimal(4, bill.getSubtotal());
            ps.setBigDecimal(5, bill.getDiscountRate());
            ps.setBigDecimal(6, bill.getDiscountAmount());
            ps.setBigDecimal(7, bill.getTotalAmount());
            ps.setString(8, bill.getStatus());
            ps.setString(9, bill.getNotes());

            int result = ps.executeUpdate();

            if (result > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys != null && keys.next()) {
                        int billId = keys.getInt(1);
                        bill.setBillId(billId);
                    }
                } catch (SQLException e) {
                    // Log error
                }
                return true;
            }
        } catch (SQLException e) {
            // Log error
            // Check for common constraint violations
            if (e.getSQLState() != null && e.getSQLState().equals("23000")) {
                if (e.getMessage().toLowerCase().contains("foreign key constraint")) {
                    // Log referential integrity constraint error
                } else if (e.getMessage().toLowerCase().contains("duplicate entry")) {
                    // Log duplicate entry error
                } else if (e.getMessage().toLowerCase().contains("cannot be null") || e.getMessage().contains("NULL")) {
                    // Log null value error
                }
            }
        } catch (Exception e) {
            // Log error
        }
        return false;
    }

    @Override
    public boolean updateBill(Bill bill) {
        String sql = "UPDATE bills SET customer_account_no=?, bill_date=?, subtotal=?, discount_rate=?, discount_amount=?, total_amount=?, status=?, notes=? WHERE bill_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bill.getCustomerAccountNo());
            ps.setTimestamp(2, Timestamp.valueOf(bill.getBillDate()));
            ps.setBigDecimal(3, bill.getSubtotal());
            ps.setBigDecimal(4, bill.getDiscountRate());
            ps.setBigDecimal(5, bill.getDiscountAmount());
            ps.setBigDecimal(6, bill.getTotalAmount());
            ps.setString(7, bill.getStatus());
            ps.setString(8, bill.getNotes());
            ps.setInt(9, bill.getBillId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log error
        }
        return false;
    }

    private Bill mapResultSetToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setInvoiceNumber(rs.getString("invoice_number"));
        bill.setCustomerAccountNo(rs.getString("customer_account_no"));
        Timestamp billDateTs = rs.getTimestamp("bill_date");
        if (billDateTs != null) {
            bill.setBillDate(billDateTs.toLocalDateTime());
        }
        bill.setSubtotal(rs.getBigDecimal("subtotal"));
        bill.setDiscountRate(rs.getBigDecimal("discount_rate"));
        bill.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        bill.setTotalAmount(rs.getBigDecimal("total_amount"));
        bill.setStatus(rs.getString("status"));
        bill.setNotes(rs.getString("notes"));
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            bill.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        if (updatedAtTs != null) {
            bill.setUpdatedAt(updatedAtTs.toLocalDateTime());
        }
        try { bill.setCustomerName(rs.getString("customer_name")); } catch (SQLException ignored) {}
        try { bill.setCustomerEmail(rs.getString("customer_email")); } catch (SQLException ignored) {}
        try { bill.setCustomerPhone(rs.getString("customer_phone")); } catch (SQLException ignored) {}
        try { bill.setItemCount(rs.getInt("item_count")); } catch (SQLException ignored) {}
        return bill;
    }
}
