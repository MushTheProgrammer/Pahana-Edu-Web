package org.pahanaedu.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Bill {
    private Integer billId;
    private String invoiceNumber;
    private String customerAccountNo;
    private LocalDateTime billDate;
    private BigDecimal subtotal;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for display purposes
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private List<BillItem> billItems;
    private Integer itemCount;

    // Constructors
    public Bill() {}

    public Bill(String invoiceNumber, String customerAccountNo, LocalDateTime billDate) {
        this.invoiceNumber = invoiceNumber;
        this.customerAccountNo = customerAccountNo;
        this.billDate = billDate;
        this.subtotal = BigDecimal.ZERO;
        this.discountRate = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
        this.status = "draft";
    }

    // Getters and setters
    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getCustomerAccountNo() { return customerAccountNo; }
    public void setCustomerAccountNo(String customerAccountNo) { this.customerAccountNo = customerAccountNo; }

    public LocalDateTime getBillDate() { return billDate; }
    public void setBillDate(LocalDateTime billDate) { this.billDate = billDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountRate() { return discountRate; }
    public void setDiscountRate(BigDecimal discountRate) { this.discountRate = discountRate; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Display fields
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public List<BillItem> getBillItems() { return billItems; }
    public void setBillItems(List<BillItem> billItems) { this.billItems = billItems; }

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

    // Utility methods
    public void calculateTotals() {
        if (subtotal != null && discountRate != null) {
            this.discountAmount = subtotal.multiply(discountRate).divide(new BigDecimal("100"));
            this.totalAmount = subtotal.subtract(discountAmount);
        }
    }
}
