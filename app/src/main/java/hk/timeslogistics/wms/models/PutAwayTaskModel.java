package hk.timeslogistics.wms.models;
import com.google.gson.JsonObject;
public class PutAwayTaskModel {
    public String container_number;
    public String created_at;
    public String status;
    public String status_text;

    public PutAwayTaskModel(JsonObject jsonObject){
        this.status = jsonObject.get("status").getAsString();
        this.status_text = jsonObject.get("status_text").getAsString();
        this.container_number = jsonObject.get("container_number").getAsString();
        this.created_at = jsonObject.get("created_at").getAsString();
    }

}

