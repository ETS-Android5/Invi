package com.aluminati.inventory.fragments.receipt;

public class ReceiptListItem {
    //{"title":"Coca Cola 1.5Ltr","imgurl":"http://img.tesco.com/Groceries/pi/439/5449000000439/IDShot_90x90.jpg","price":2.00,"quantity":1}
    private String title;
    private String imgurl;
    private double price;
    private int quantity;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
