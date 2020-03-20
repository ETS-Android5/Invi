package com.aluminati.inventory.fragments.tesco.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Totals {
    @Expose
    @SerializedName("all")
    private String all;
    @Expose
    @SerializedName("new")
    private String newp;
    @Expose
    @SerializedName("offer")
    private String offer;

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }

    public String getNewp() {
        return newp;
    }

    public void setNewp(String newp) {
        this.newp = newp;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public Totals(String all, String newp, String offer) {
        this.all = all;
        this.newp = newp;
        this.offer = offer;
    }
}
