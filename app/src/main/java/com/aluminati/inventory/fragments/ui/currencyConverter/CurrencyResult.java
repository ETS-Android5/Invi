package com.aluminati.inventory.fragments.ui.currencyConverter;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class CurrencyResult {

    @Expose
    @SerializedName("base")
    private String base;
    @Expose
    @SerializedName("date")
    private String date;
    @Expose
    @SerializedName("rates")
    private Map<String, Float> currencyRates;

    public CurrencyResult(String base, String date, Map<String, Float> currencyRates) {
        this.base = base;
        this.date = date;
        this.currencyRates = currencyRates;
    }

    public String getBase() {
        return base;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Float> getCurrencyRates() {
        return currencyRates;
    }

}
