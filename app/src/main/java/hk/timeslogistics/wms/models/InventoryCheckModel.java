package hk.timeslogistics.wms.models;

public class InventoryCheckModel {
    public String mCycleCountCode;
    public String mStatus;
    public Integer mTargetQty;
    public Integer mScannedQty;
    public Integer mInventoryQty;

    public InventoryCheckModel(String cycleCountCode, String status, Integer targetQty,Integer scannedQty,Integer inventoryQty){
        this.mCycleCountCode = cycleCountCode;
        this.mStatus = status;
        this.mTargetQty = targetQty;
        this.mScannedQty = scannedQty;
        this.mInventoryQty = inventoryQty;
    }

}

