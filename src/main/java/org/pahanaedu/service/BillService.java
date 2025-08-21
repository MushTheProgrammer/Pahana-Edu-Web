package org.pahanaedu.service;

import org.pahanaedu.model.Bill;
import org.pahanaedu.model.BillItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BillService {
    // Bill CRUD operations

    boolean deleteBillByInvoiceNumber(String invoiceNumber);
    Bill getBillById(Integer billId);
    Bill getBillByInvoiceNumber(String invoiceNumber);
    List<Bill> getAllBills();
    
    // Bill search and pagination
    List<Bill> searchBills(String invoiceNumber, String customer, String status, 
                          LocalDate fromDate, LocalDate toDate, 
                          int page, int size, String sort, String dir);
    int countBills(String invoiceNumber, String customer, String status, 
                   LocalDate fromDate, LocalDate toDate);
    

    
    // Invoice number generation
    String getNextInvoiceNumber();
    
    // Bill calculations and management
    BigDecimal calculateBillSubtotal(Integer billId);
    boolean updateBillTotals(Integer billId, BigDecimal discountRate);
    boolean updateBillStatus(Integer billId, String status);
    
    // Complete bill operations
    Bill createBillWithItems(Bill bill, List<BillItem> items);
    Bill getBillWithItems(Integer billId);
}
