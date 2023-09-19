package hk.timeslogistics.wms.models;

import com.google.gson.JsonObject;

public class InventoryCheckTaskItemModel {
    public String sku_code;
    public String skuBarcode;
    public String sku_name;
    public String bin_code;
    public String zone_area_bin_code;
    public String scanned_qty;
    public String inventory_qty;
    public String count_qty;
    public String sku_id;
    public String bin_id;
    public InventoryCheckTaskItemModel(JsonObject jsonObject){
        this.sku_code = jsonObject.get("code").getAsString();
        this.bin_code = jsonObject.get("bin_code").getAsString();
        this.skuBarcode = jsonObject.get("barcode").getAsString();
        this.sku_name = jsonObject.get("name").getAsString();
        this.zone_area_bin_code = jsonObject.get("zone_area_bin_code").getAsString();
        this.scanned_qty = jsonObject.get("scanned_qty").getAsString();
        this.inventory_qty = jsonObject.get("inventory_qty").getAsString();
        this.count_qty = jsonObject.get("count_qty").getAsString();
        this.sku_id = jsonObject.get("sku_id").getAsString();
        this.bin_id = jsonObject.get("bin_id").getAsString();
    }

}

