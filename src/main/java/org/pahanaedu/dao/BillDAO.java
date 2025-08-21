package org.pahanaedu.dao;

import org.pahanaedu.model.Bill;
import org.pahanaedu.model.BillItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BillDAO {
    // Bill CRUD operations
    boolean addBill(Bill bill);
    boolean updateBill(Bill bill);
    boolean deleteBill(Integer billId);
    boolean deleteBillByInvoiceNumber(String invoiceNumber);
    Bill getBillById(Integer billId);
    Bill getBillByInvoiceNumber(String invoiceNumber);
    List<Bill> getAllBills();
    
    // Bill search and pagination
    List<Bill> searchBills(String invoiceNumber, String customer, String status, 
                          LocalDate fromDate, LocalDate toDate, 
                          int offset, int limit, String orderByCol, String orderDir);
    int countBills(String invoiceNumber, String customer, String status, 
                   LocalDate fromDate, LocalDate toDate);
    
    // Bill item operations
    boolean addBillItem(BillItem billItem);
    boolean updateBillItem(BillItem billItem);
    boolean deleteBillItem(Integer billItemId);
    List<BillItem> getBillItems(Integer billId);
    boolean deleteBillItems(Integer billId);
    
    // Invoice number generation
    String getNextInvoiceNumber();
    
    // Bill calculations
    BigDecimal calculateBillSubtotal(Integer billId);
    boolean updateBillTotals(Integer billId, BigDecimal subtotal, BigDecimal discountRate, 
                            BigDecimal discountAmount, BigDecimal totalAmount);
    
    // Status management
    boolean updateBillStatus(Integer billId, String status);
}
