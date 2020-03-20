package com.aluminati.inventory.fragments.rental;

import com.aluminati.inventory.model.BaseItem;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class RentalItem extends BaseItem {
    private String unitType;//price per hour, day, week, month
    private Date checkedOutDate;

    public RentalItem() {}
    //Update
    public RentalItem(String storeID, String storeCity,
                      String storeCountry, String title,
                      String description, double price,
                      String imgLink, List<String> tags, boolean isRestricted, String unitType) {
        super(storeID, storeCity, storeCountry, title, description, price, imgLink, tags, isRestricted, true);
        this.unitType = unitType;
    }

    public RentalItem(DocumentSnapshot documentSnapshot){

        this.storeID = documentSnapshot.getString("storedId");
        this.storeCity = documentSnapshot.getString("storeCity");
        this.storeCountry = documentSnapshot.getString("storeCountry");
        this.title = documentSnapshot.getString("title");
        this.description = documentSnapshot.getString("description");
        this.price = (Double) documentSnapshot.get("price");
        this.imgLink = documentSnapshot.getString("imgLink");
        this.tags = (List<String>)documentSnapshot.get("tags");
        this.isRestricted = documentSnapshot.getBoolean("restricted");
        this.unitType = documentSnapshot.getString("unitType");

        new RentalItem(storeID,storeCity,storeCountry,title,description,price,imgLink,tags,isRestricted,unitType);
    }

    public void setCheckedOutDate(Date checkedOutDate) {
        this.checkedOutDate = checkedOutDate;
    }

    public Date getCheckedOutDate() {
        return checkedOutDate;
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
