package hk.timeslogistics.wms.models;

public class AsnModel {
    public String mAsnNumber;
    public String mClientName;
    public String mAsnDate;

    public AsnModel(String asnNumber, String clientName, String asnDate){
        this.mClientName = clientName;
        this.mAsnNumber = asnNumber;
        this.mAsnDate = asnDate;
    }
}

