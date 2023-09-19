package hk.timeslogistics.wms.models;

import com.google.gson.JsonObject;

public class PickWaveDetailModel {
    public String location;
    public String qty;
    public String take_down_qty;
    public String waiting_take_down_qty;
    public String skip_qty;
    public String sku_barcode;
    public String bin_code;
    public boolean is_take_down;

    public PickWaveDetailModel(JsonObject jsonObject){
        this.location = jsonObject.get("location").getAsString();
        this.qty = jsonObject.get("qty").getAsString();
        this.take_down_qty = jsonObject.get("take_down_qty").getAsString();
        this.waiting_take_down_qty = jsonObject.get("waiting_take_down_qty").getAsString();
        this.skip_qty = jsonObject.get("skip_qty").getAsString();
        this.sku_barcode = jsonObject.get("sku_barcode").getAsString();
        this.is_take_down = jsonObject.get("is_take_down").getAsBoolean();
        this.bin_code = jsonObject.get("bin_code").getAsString();
    }
}

