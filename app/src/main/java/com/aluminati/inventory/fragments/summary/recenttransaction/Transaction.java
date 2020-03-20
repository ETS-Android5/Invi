package com.aluminati.inventory.fragments.summary.recenttransaction;

public class Transaction {

    private String amount;
    private String date;
    private String type;

    public Transaction(String amount, String date, String type) {
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
