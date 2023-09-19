package hk.timeslogistics.wms.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import hk.timeslogistics.wms.R;

public class InitSetting {
    public static void setClientList(String clients, Context context, Spinner spinner){
        JsonArray clientJsonArray = new JsonParser().parse(clients).getAsJsonArray();
        ArrayList arrayList = new ArrayList();
        for (int i=0;i<clientJsonArray.size();i++){
            arrayList.add(clientJsonArray.get(i).getAsString());
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(context, R.layout.logistics_provider_agent,arrayList);
        spinner.setAdapter(arrayAdapter);
    }
}
