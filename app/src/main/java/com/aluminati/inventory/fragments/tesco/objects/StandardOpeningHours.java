package com.aluminati.inventory.fragments.tesco.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StandardOpeningHours {

    private String day;

    @Expose
    @SerializedName("open")
    private String openTime;

    @Expose
    @SerializedName("close")
    private String closeTime;

    @Expose
    @SerializedName("isOpen")
    private String isOpen;

    public String getDay() {
        switch (day){
            case "mo": return "Monday";
            case "tu": return "Tuesday";
            case "we": return "Wednesday";
            case "th": return "Thursday";
            case "fr": return "Friday";
            case "sa": return "Saturday";
            case "su": return "Sunday";
        }
        return "";
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(String isOpen) {
        this.isOpen = isOpen;
    }

    public StandardOpeningHours(){

    }

    public StandardOpeningHours(String day, String openTime, String closeTime) {
        this.day = day;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public StandardOpeningHours(String day, String openTime, String closeTime, String isOpen) {
        this.day = day;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
    }
}
