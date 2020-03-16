package com.aluminati.inventory.fragments.receipt;

public class ReceiptItem {
    private String timestamp;
    private int quantity;
    private String itemref;
    private double total;
    private boolean rental;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getItemref() {
        return itemref;
    }

    public void setItemref(String itemref) {
        this.itemref = itemref;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public boolean isRental() {
        return rental;
    }

    public void setRental(boolean rental) {
        this.rental = rental;
    }
}
