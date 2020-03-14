package com.aluminati.inventory.fragments.tesco;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Product {

    @Expose
    @SerializedName("image")
    private String image;
    @Expose
    @SerializedName("tpnb")
    private String tpnb;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("description")
    private String description;
    @Expose
    @SerializedName("price")
    private  String price;
    @Expose
    @SerializedName("id")
    private  String id;
    @Expose
    @SerializedName("exactMatch")
    private  boolean exactMatch;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTpnb() {
        return tpnb;
    }

    public void setTpnb(String tpnb) {
        this.tpnb = tpnb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setExactMatch(boolean exactMatch) {this.exactMatch = exactMatch;}

    public boolean getExactMatch() {return exactMatch; }

    public Product(String image, String tpnb, String name, String description, String price, String id) {
        this.image = image;
        this.tpnb = tpnb;
        this.name = name;
        this.description = description;
        this.price = price;
        this.id = id;
    }

    public Product(String img, String name, String price){
        this.image = img;
        this.name = name;
        this.price = price;
    }

    public Product(){

    }

    public String toString() {
        return String.format("{image: %s, name: %s, description: %s, price: %s}",
                image, name, description, price);
    }
}

/*
    "image": "http://img.tesco.com/Groceries/pi/164/4060800128164/IDShot_90x90.jpg",
          "superDepartment": "Drinks",
          "tpnb": 76206044,
          "ContentsMeasureType": "ML",
          "name": "Pepsi Max 12 X 330Ml",
          "UnitOfSale": 1,
          "AverageSellingUnitWeight": 4.4,
          "description": ["Low Calorie Cola Flavoured Soft Drink with Sweeteners"],
          "UnitQuantity": "100ML",
          "id": 282775492,
          "ContentsQuantity": 3960,
          "department": "Fizzy Drinks & Cola",
          "price": 4.75,
          "unitprice": 0.12
 */