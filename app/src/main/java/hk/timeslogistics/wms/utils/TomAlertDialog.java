package hk.timeslogistics.wms.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import hk.timeslogistics.wms.R;

public class TomAlertDialog {
    public static void showAlertDialogMessage(Context context,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(" ").setIcon(R.drawable.error)
                .setCancelable(true)
                .setMessage(message)
                .create();
        builder.show();
    }
}
