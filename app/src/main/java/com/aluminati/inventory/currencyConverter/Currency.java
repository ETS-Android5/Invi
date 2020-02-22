package com.aluminati.inventory.currencyConverter;

public class Currency {

    private int image;
    private String currencySymbol;
    private String currencyRate;
    private String currencyName;
    private String currencyDisplayName;

    public Currency(int image, String currencySymbol, String currencyRate, String currencyDisplayName) {
        this.image = image;
        this.currencySymbol = currencySymbol;
        this.currencyRate = currencyRate;
        this.currencyDisplayName = currencyDisplayName;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(String currencyRate) {
        this.currencyRate = currencyRate;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyDisplayName() {
        return currencyDisplayName;
    }

    public void setCurrencyDisplayName(String currencyDisplayName) {
        this.currencyDisplayName = currencyDisplayName;
    }
}
