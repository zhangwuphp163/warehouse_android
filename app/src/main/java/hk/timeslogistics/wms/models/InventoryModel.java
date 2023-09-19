package hk.timeslogistics.wms.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class InventoryModel {
    public String location;
    public String sku_name;
    public String sku_condition;
    public String status;
    public int qty;
    public int usable_qty;
    public String sku_barcode;
    public InventoryModel(JsonObject jsonObject){
        this.location = jsonObject.get("location").getAsString();
        this.sku_name = jsonObject.get("sku_name").getAsString();
        this.sku_condition = jsonObject.get("sku_condition").getAsString();
        this.status = jsonObject.get("status").getAsString();
        this.qty = jsonObject.get("qty").getAsInt();
        this.usable_qty = jsonObject.get("usable_qty").getAsInt();
        this.sku_barcode = jsonObject.get("sku_barcode").getAsString();
    }

}

