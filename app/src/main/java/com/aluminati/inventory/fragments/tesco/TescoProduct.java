package com.aluminati.inventory.fragments.tesco;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class TescoProduct {

    @Expose
    @SerializedName("gtin")
    private String gtin;
    @Expose
    @SerializedName("tpnb")
    private String tpnb;
    @Expose
    @SerializedName("tpnc")
    private String tpnc;
    @Expose
    @SerializedName("description")
    private String description;
    @Expose
    @SerializedName("brand")
    private String brand;
    @Expose
    @SerializedName("qtyContents")
    private Map<String, Object> qtyContents;
    @Expose
    @SerializedName("productCharacteristics")
    private Map<String,Object> productCharacteristics;
    @Expose
    @SerializedName("pkgDimensions")
    private PkgDimensions pkgDimensions;

    public TescoProduct(){

    }

    public TescoProduct(String gtin, String tpnb, String tpnc, String description
            , String brand, Map<String, Object> qtyContents, Map<String, Object> productCharacteristics, PkgDimensions pkgDimensions){

        this.gtin = gtin;
        this.tpnb = tpnb;
        this.tpnc = tpnc;
        this.description = description;
        this.brand = brand;
        this.qtyContents = qtyContents;
        this.productCharacteristics = productCharacteristics;
        this.pkgDimensions = new PkgDimensions();
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public String getTpnb() {
        return tpnb;
    }

    public void setTpnb(String tpnb) {
        this.tpnb = tpnb;
    }

    public String getTpnc() {
        return tpnc;
    }

    public void setTpnc(String tpnc) {
        this.tpnc = tpnc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Map<String, Object> getQtyContents() {
        return qtyContents;
    }

    public void setQtyContents(Map<String, Object> qtyContents) {
        this.qtyContents = qtyContents;
    }

    public Map<String, Object> getProductCharacteristics() {
        return productCharacteristics;
    }

    public void setProductCharacteristics(Map<String, Object> productCharacteristics) {
        this.productCharacteristics = productCharacteristics;
    }

    public PkgDimensions getPkgDimensions() {
        return pkgDimensions;
    }

    public void setPkgDimensions(PkgDimensions pkgDimensions) {
        this.pkgDimensions = pkgDimensions;
    }

    class PkgDimensions{

        public PkgDimensions(){

        }

    }


}


  /*
  "products": [{
          "gtin": "05449000000439",
          "tpnb": "050077691",
          "tpnc": "254857150",
          "description": "Coca Cola",
          "brand": "COCA COLA",
          "qtyContents": {
          "quantity": 1.5,
          "totalQuantity": 1.5,
          "quantityUom": "L",
          "unitQty": "100ML",
          "netContents": "1.5l ℮",
          "avgMeasure": "Average Measure (e)"
          },
          "productCharacteristics": {
          "isFood": false,
          "isDrink": true,
          "healthScore": 66,
          "isHazardous": false,
          "storageType": "Ambient",
          "isAnalgesic": false,
          "containsLoperamide": false
          },
          "pkgDimensions": [{
          "no": 1,
          "height": 32.6,
          "width": 9.3,
          "depth": 9.3,
          "dimensionUom": "cm",
          "weight": 1616.0,
          "weightUom": "g",
          "volume": 2819.574,
          "volumeUom": "cc"
          }]
          }]

   */