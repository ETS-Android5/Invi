package com.aluminati.inventory.fragments.purchase;

import com.aluminati.inventory.model.BaseItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PurchaseItem extends BaseItem {
    private int quantity;

    public PurchaseItem() {/*for FireStore */}

    public PurchaseItem(String storeID, String storeCity,
                        String storeCountry, String title,
                        String description, double price,
                        String imgLink, List<String> tags, boolean isRestricted, int quantity) {
        super(storeID, storeCity, storeCountry, title, description, price, imgLink,tags, isRestricted);
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("quantity", quantity);

        return map;
    }
}
