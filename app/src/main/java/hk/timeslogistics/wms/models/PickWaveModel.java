package hk.timeslogistics.wms.models;

public class PickWaveModel {
    public String mPickWaveNumber;
    public String mClientName;
    public String mType;
    public String mTotalQty;

    public PickWaveModel(String pickWaveNumber,String clientName, String type, String totalQty){
        this.mClientName = clientName;
        this.mPickWaveNumber = pickWaveNumber;
        this.mType = type;
        this.mTotalQty = totalQty;
    }
}

