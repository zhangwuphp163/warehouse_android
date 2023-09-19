package hk.timeslogistics.wms.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import hk.timeslogistics.wms.R;

public class TomProgress {
    private static ProgressDialog mProgressDialog;
    protected static HttpSubmitTask httpSubmitTask;

    public static void setHttpSubmitTask(HttpSubmitTask mHttpSubmitTask){
        httpSubmitTask = mHttpSubmitTask;
    }
    public static void showProgress(Context context, Boolean value) {
        if (value) {
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(context, null, context.getString(R.string.message_please_wait), true,false);
            }
            mProgressDialog.show();
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.hide();
            }
        }
        assert mProgressDialog != null;
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(httpSubmitTask != null){
                    httpSubmitTask.cancel(true);
                }
            }
        });
    }
}
