package com.aluminati.inventory.model;

import java.util.List;

public abstract class BaseItem {
    protected String docID;
    protected String storeID;
    protected String storeCity;
    protected String storeCountry;
    protected String title;
    protected String description;
    protected double price;
    protected String imgLink;
    protected List<String> tags;

    public BaseItem() {}

    public BaseItem(String storeID, String storeCity,
                    String storeCountry, String title,
                    String description, double price,
                    String imgLink, List<String> tags) {
        this.storeID = storeID;
        this.storeCity = storeCity;
        this.storeCountry = storeCountry;
        this.title = title;
        this.description = description;
        this.price = price;
        this.imgLink = imgLink;
        this.tags = tags;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getStoreID() {
        return storeID;
    }

    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    public String getStoreCity() {
        return storeCity;
    }

    public void setStoreCity(String storeCity) {
        this.storeCity = storeCity;
    }

    public String getStoreCountry() {
        return storeCountry;
    }

    public void setStoreCountry(String storeCountry) {
        this.storeCountry = storeCountry;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImgLink() {
        return imgLink;
    }

    public void setImgLink(String imgLink) {
        this.imgLink = imgLink;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
