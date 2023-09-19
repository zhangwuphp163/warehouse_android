package hk.timeslogistics.wms.models;

import com.google.gson.JsonObject;

public class OrderItemModel {
    public String sku_barcode;
    public String request_qty;
    public String actual_qty;
    public OrderItemModel(JsonObject jsonObject){
        this.request_qty = jsonObject.get("requested_qty").getAsString();
        this.actual_qty = jsonObject.get("actual_qty").getAsString();
        this.sku_barcode = jsonObject.get("sku_barcode").getAsString();
    }

}

