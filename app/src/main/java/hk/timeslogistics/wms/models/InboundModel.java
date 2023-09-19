package hk.timeslogistics.wms.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class InboundModel {
    public String mBarcode;
    public String mScannedQty;
    public String mEstimatedQty;
    public String mActualQty;

    public InboundModel(String barcode,String estimated_qty,String scanned_qty,String actual_qty){
        this.mBarcode = barcode;
        this.mScannedQty = scanned_qty;
        this.mEstimatedQty = estimated_qty;
        this.mActualQty = actual_qty;
    }

    public static Boolean checkSkuBarcodeInAsn(JsonObject item, String sku_barcode){
        boolean is_exists = false;
        JsonArray barcodes = item.get("barcodes").getAsJsonArray();
        for (int k = 0;k<barcodes.size();k++){
            String itemSkuBarcode = barcodes.get(k).getAsString();
            System.out.println(itemSkuBarcode);
            if(itemSkuBarcode.equals(sku_barcode)){
                is_exists = true;
                break;
            }
        }
        return is_exists;
    }
}

