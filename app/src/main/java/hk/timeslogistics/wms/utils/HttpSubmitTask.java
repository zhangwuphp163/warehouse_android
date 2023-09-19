package hk.timeslogistics.wms.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpSubmitTask extends AsyncTask<Void,Void,RemoteResult> {
    private HttpSubmitTask submitTask;
    private final RequestBody mFormBody;
    private final String mToken;
    private final String mLocaleLanguage;
    private final String mUrl;

    public HttpSubmitTask(Context context,RequestBody formBody, String url) {
        mFormBody = formBody;
        mToken = context.getSharedPreferences("credential",0).getString("access_token","");
        mLocaleLanguage = Locale.getDefault().getLanguage();
        mUrl = url;
    }

    @Override
    protected RemoteResult doInBackground(Void... params) {
        try {
            final OkHttpClient client = new OkHttpClient().newBuilder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(120,TimeUnit.SECONDS)
                    .writeTimeout(120,TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(mUrl)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + mToken)
                    .addHeader("localeLanguage", mLocaleLanguage)
                    .post(mFormBody)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                if (response.code() == 401) {
                    throw new SecurityException();
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }
            assert response.body() != null;
            String responseString = response.body().string();
            RemoteResult result = new Gson().fromJson(responseString, SubmitResult.class);
            System.out.println(responseString);
            if (result == null) {
                throw new IOException("Server empty response");
            }
            return result;
        } catch (SecurityException e) {
            return new RemoteResult(-1, e);
        }catch (Exception e) {
            return new RemoteResult(65535, e);
        }
    }

    @Override
    protected void onPostExecute(final RemoteResult result) {
        submitTask = null;
    }

    @Override
    protected void onCancelled() {
        submitTask = null;
    }

}
