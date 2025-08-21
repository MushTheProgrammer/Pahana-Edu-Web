package org.pahanaedu.model;

import java.math.BigDecimal;

public class BillItem {
    private Integer billItemId;
    private Integer billId;
    private String itemCode;
    private String itemName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;

    // Constructors
    public BillItem() {}

    public BillItem(Integer billId, String itemCode, String itemName, BigDecimal unitPrice, Integer quantity) {
        this.billId = billId;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = unitPrice.multiply(new BigDecimal(quantity));
    }

    // Getters and setters
    public Integer getBillItemId() { return billItemId; }
    public void setBillItemId(Integer billItemId) { this.billItemId = billItemId; }

    public Integer getBillId() { return billId; }
    public void setBillId(Integer billId) { this.billId = billId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

    // Utility method
    public void calculateLineTotal() {
        if (unitPrice != null && quantity != null) {
            this.lineTotal = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
}
