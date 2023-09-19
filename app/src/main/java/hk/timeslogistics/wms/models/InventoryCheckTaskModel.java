package hk.timeslogistics.wms.models;

import com.google.gson.JsonObject;

public class InventoryCheckTaskModel {
    public Integer mTaskId;
    public String mTask;
    public String mCycleCountCode;
    public String mStatus;
    public String mCountQty;
    public String mScannedQty;
    public String mInventoryQty;
    public String mChecker;
    public String mProgress;
    public Integer mProgressBar;

    public InventoryCheckTaskModel(JsonObject jsonObject){
        this.mTask = jsonObject.get("task").getAsString();
        this.mCycleCountCode = jsonObject.get("cycle_count_code").getAsString();
        this.mTaskId = jsonObject.get("task_id").getAsInt();
        this.mStatus = jsonObject.get("status").getAsString();
        this.mCountQty = jsonObject.get("count_qty").getAsString();
        this.mInventoryQty = jsonObject.get("inventory_qty").getAsString();
        this.mScannedQty = jsonObject.get("scanned_qty").getAsString();
        this.mChecker = jsonObject.get("checker").getAsString();
        this.mProgress = jsonObject.get("progress").getAsString();
        this.mProgressBar = jsonObject.get("progress_bar").getAsInt();
    }

}

