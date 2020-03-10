package com.aluminati.inventory.model;

import java.util.List;
import java.util.Map;

public class RentalItem extends BaseItem {
    private String unitType;//price per hour, day, week, month

    public RentalItem() {}

    public RentalItem(String storeID, String storeCity,
                      String storeCountry, String title,
                      String description, double price,
                      String imgLink, List<String> tags, boolean isRestricted, String unitType) {
        super(storeID, storeCity, storeCountry, title, description, price, imgLink, tags, isRestricted);
        this.unitType = unitType;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }


    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("unitType", unitType);

        return map;
    }
}
