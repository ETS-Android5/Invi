package com.aluminati.inventory.model;

import java.util.List;

public class PurchaseItem extends BaseItem {
    private int quantity;

    public PurchaseItem() {/*for FireStore */}

    public PurchaseItem(String storeID, String storeCity,
                        String storeCountry, String title,
                        String description, double price,
                        String imgLink, List<String> tags, int quantity) {
        super(storeID, storeCity, storeCountry, title, description, price, imgLink,tags);
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
