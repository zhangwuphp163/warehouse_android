package hk.timeslogistics.wms.models;

import com.google.gson.JsonObject;

public class HandlingUnitModel {
    public String mLocation;
    public String mQty;
    public String mSkuBarcode;
    public String mOperator;
    public String mOperatedAt;
    public String mHandlingUnitNumber;
    public String mType;

    public HandlingUnitModel(JsonObject jsonObject){
        this.mLocation = jsonObject.get("location").getAsString();
        this.mQty = jsonObject.get("qty").getAsString();
        this.mSkuBarcode = jsonObject.get("sku_barcode").getAsString();
        this.mOperatedAt = jsonObject.get("take_down_at").getAsString();
        this.mOperator = jsonObject.get("operator").getAsString();
        this.mHandlingUnitNumber = jsonObject.get("handling_unit_number").getAsString();
        this.mType = jsonObject.get("type").getAsString();
    }
}

