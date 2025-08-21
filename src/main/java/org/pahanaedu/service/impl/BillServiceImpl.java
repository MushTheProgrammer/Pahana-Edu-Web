package org.pahanaedu.service.impl;

import org.pahanaedu.dao.BillDAO;
import org.pahanaedu.dao.impl.BillDAOImpl;
import org.pahanaedu.model.Bill;
import org.pahanaedu.model.BillItem;
import org.pahanaedu.service.BillService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BillServiceImpl implements BillService {
    private final BillDAO dao = new BillDAOImpl();


    @Override
    public boolean deleteBillByInvoiceNumber(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            return false;
        }
        return dao.deleteBillByInvoiceNumber(invoiceNumber);
    }

    @Override
    public Bill getBillById(Integer billId) {
        return dao.getBillById(billId);
    }

    @Override
    public Bill getBillByInvoiceNumber(String invoiceNumber) {
        return dao.getBillByInvoiceNumber(invoiceNumber);
    }

    @Override
    public List<Bill> getAllBills() {
        return dao.getAllBills();
    }

    @Override
    public List<Bill> searchBills(String invoiceNumber, String customer, String status, 
                                 LocalDate fromDate, LocalDate toDate, 
                                 int page, int size, String sort, String dir) {
        int limit = Math.max(1, size);
        int offset = Math.max(0, (page - 1) * limit);
        String orderCol = sort == null ? "bill_date" : sort;
        String orderDir = dir == null ? "desc" : dir;
        return dao.searchBills(invoiceNumber, customer, status, fromDate, toDate, offset, limit, orderCol, orderDir);
    }

    @Override
    public int countBills(String invoiceNumber, String customer, String status, 
                         LocalDate fromDate, LocalDate toDate) {
        return dao.countBills(invoiceNumber, customer, status, fromDate, toDate);
    }

    @Override
    public String getNextInvoiceNumber() {
        return dao.getNextInvoiceNumber();
    }

    @Override
    public BigDecimal calculateBillSubtotal(Integer billId) {
        return dao.calculateBillSubtotal(billId);
    }

    @Override
    public boolean updateBillTotals(Integer billId, BigDecimal discountRate) {
        BigDecimal subtotal = dao.calculateBillSubtotal(billId);
        BigDecimal discountAmount = subtotal.multiply(discountRate).divide(new BigDecimal("100"));
        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        
        return dao.updateBillTotals(billId, subtotal, discountRate, discountAmount, totalAmount);
    }

    @Override
    public boolean updateBillStatus(Integer billId, String status) {
        return dao.updateBillStatus(billId, status);
    }

    @Override
    public Bill createBillWithItems(Bill bill, List<BillItem> items) {
        System.out.println("BillService - Creating bill with invoice: " + bill.getInvoiceNumber());

        // Initialize bill amounts if they are null
        if (bill.getSubtotal() == null) {
            bill.setSubtotal(BigDecimal.ZERO);
        }
        if (bill.getDiscountAmount() == null) {
            bill.setDiscountAmount(BigDecimal.ZERO);
        }
        if (bill.getTotalAmount() == null) {
            bill.setTotalAmount(BigDecimal.ZERO);
        }
        if (bill.getStatus() == null) {
            bill.setStatus("Paid");
        }

        // Add the bill first
        boolean billAdded = dao.addBill(bill);
        System.out.println("BillService - Bill added: " + billAdded);

        if (billAdded) {
            Integer billId = bill.getBillId();
            System.out.println("BillService - Generated bill ID: " + billId);

            // Add all items
            for (BillItem item : items) {
                item.setBillId(billId);
                item.calculateLineTotal();
                boolean itemAdded = dao.addBillItem(item);
                System.out.println("BillService - Item added: " + item.getItemCode() + " = " + itemAdded);
            }

            // Calculate and update totals
            boolean totalsUpdated = updateBillTotals(billId, bill.getDiscountRate());
            System.out.println("BillService - Totals updated: " + totalsUpdated);

            return getBillById(billId);
        }
        System.out.println("BillService - Failed to add bill");
        return null;
    }

    @Override
    public Bill getBillWithItems(Integer billId) {
        Bill bill = dao.getBillById(billId);
        if (bill != null) {
            List<BillItem> items = dao.getBillItems(billId);
            bill.setBillItems(items);
        }
        return bill;
    }
}
