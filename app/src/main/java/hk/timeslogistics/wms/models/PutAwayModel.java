package hk.timeslogistics.wms.models;

import com.google.gson.JsonObject;

public class PutAwayModel {
    public String sku_name;
    public String  qty;
    public String sku_barcode;
    public String asn_number;
    public int client_id;
    public PutAwayModel(JsonObject jsonObject){
        this.sku_name = jsonObject.get("sku_name").getAsString();
        this.qty = jsonObject.get("qty").getAsString();
        this.sku_barcode = jsonObject.get("sku_barcode").getAsString();
        this.asn_number = jsonObject.get("asn_number").getAsString();
        this.client_id = jsonObject.get("client_id").getAsInt();
    }

}

