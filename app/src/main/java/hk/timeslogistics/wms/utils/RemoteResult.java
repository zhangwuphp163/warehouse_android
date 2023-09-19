package hk.timeslogistics.wms.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RemoteResult {

    protected Integer status;
    protected String payload;
    protected JsonObject data;
    protected JsonArray items;
    JsonArray over_receive_data;

    public RemoteResult(Integer newStatus) {
        status = newStatus;
    }

    public RemoteResult(Integer newStatus, Exception e) {
        status = newStatus;
        payload = e.getMessage();
    }

    public Integer getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public Boolean shouldLogout() {
        return status < 0;
    }

    public void setData(JsonObject newData){
        data = newData;
    }
    public void setItems(JsonArray newItems){
        items = newItems;
    }

    public JsonObject getData() {
        return data;
    }

    public JsonArray getItems(){
        return items;
    }
    public JsonArray getOverReceiveData(){
        return  over_receive_data;
    }

    @Override
    public String toString() {
        return "RemoteResult{" +
                "status=" + status +
                ", payload='" + payload + '\'' +
                '}';
    }
}
