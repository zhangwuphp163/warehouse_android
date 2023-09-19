package hk.timeslogistics.wms.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AsnItemModel {
    public String mBarcode;
    public String mCode;
    public String mEstimatedQty;
    public String mActualQty;
    public String mSkuName;
    public String mPoNumber;


    public AsnItemModel(JsonObject jsonObject){
        this.mCode = jsonObject.get("code").getAsString();
        this.mBarcode = jsonObject.get("barcode").getAsString();
        this.mEstimatedQty = jsonObject.get("estimated_qty").getAsString();
        this.mActualQty = jsonObject.get("actual_qty").getAsString();
        this.mSkuName = jsonObject.get("sku_name").getAsString();
        this.mPoNumber = jsonObject.get("po_number").getAsString();
    }
}

