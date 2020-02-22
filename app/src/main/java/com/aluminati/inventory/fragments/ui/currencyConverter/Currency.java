package com.aluminati.inventory.fragments.ui.currencyConverter;

public class Currency {

    private int image;
    private String currencySymbol;
    private String currencyRate;
    private String currencyName;
    private String currencyDisplayName;
    private String currencyCCode;

    public Currency(int image, String currencySymbol, String currencyRate, String currencyDisplayName, String currencyCCode) {
        this.image = image;
        this.currencySymbol = currencySymbol;
        this.currencyRate = currencyRate;
        this.currencyDisplayName = currencyDisplayName;
        this.currencyCCode = currencyCCode;
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

    public String getCurrencyCCode() {
        return currencyCCode;
    }

    public void setCurrencyCCode(String currencyCCode) {
        this.currencyCCode = currencyCCode;
    }
}
