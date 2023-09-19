package hk.timeslogistics.wms.models;

public class ShipmentModel {
    public String mTrackingNumber;
    public String mScanAt;

    public ShipmentModel(String trackingNumber, String scanAt){
        this.mTrackingNumber = trackingNumber;
        this.mScanAt = scanAt;
    }
}

