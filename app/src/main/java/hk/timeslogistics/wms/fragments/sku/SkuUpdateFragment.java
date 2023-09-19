package hk.timeslogistics.wms.fragments.sku;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.InventoryModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomAlertDialog;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SkuUpdateFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextSkuBarcode;
    private EditText editTextLength;
    private EditText editTextWidth;
    private EditText editTextHeight;
    private EditText editTextWeight;

    protected Button buttonSubmit;
    protected Button buttonCancel;

    private String client_id = null;

    private SkuUpdateFragment.SubmitTask submitTask;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_sku_update, null);

        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(R.string.sku_update);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextSkuBarcode = (EditText) root.findViewById(R.id.editTextSkuBarcode);
        editTextLength = (EditText) root.findViewById(R.id.editTextLength);
        editTextWidth = (EditText) root.findViewById(R.id.editTextWidth);
        editTextHeight = (EditText) root.findViewById(R.id.editTextHeight);
        editTextWeight = (EditText) root.findViewById(R.id.editTextWeight);
        ImageButton buttonScanSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanSkuBarcode);

        buttonSubmit = (Button) root.findViewById(R.id.buttonSubmit);
        buttonCancel = (Button) root.findViewById(R.id.buttonCancel);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    submit();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        buttonScanSkuBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.forSupportFragment(SkuUpdateFragment.this).initiateScan();
            }
        });

        editTextSkuBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getSkuInfo();
                    return true;
                }
                return false;
            }
        });
        editTextSkuBarcode.requestFocus();

        return root;
    }

    private void getSkuInfo(){
        String sku_barcode = editTextSkuBarcode.getText().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("sku_barcode", sku_barcode);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,credentialManager.getApiBase() + "sku/info","info");
        submitTask.execute();
    }

    private void reset(){
        client_id = "";
        editTextHeight.setText("");
        editTextLength.setText("");
        editTextWeight.setText("");
        editTextWidth.setText("");
        editTextLength.setEnabled(false);
        editTextHeight.setEnabled(false);
        editTextWidth.setEnabled(false);
        editTextWeight.setEnabled(false);
        editTextSkuBarcode.setText("");
        editTextSkuBarcode.requestFocus();
    }
    private void submit() throws JSONException {
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String length = editTextLength.getText().toString();
        String width = editTextWidth.getText().toString();
        String height = editTextHeight.getText().toString();
        String weight = editTextWeight.getText().toString();
        TomProgress.showProgress(getContext(),true);
        JSONObject requestData = new JSONObject();
        requestData.put("sku_barcode",sku_barcode);
        requestData.put("length",length);
        requestData.put("width",width);
        requestData.put("height",height);
        requestData.put("weight",weight);
        requestData.put("client_id",client_id);

        Log.d("debug",requestData.toString());

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),requestData.toString());

        submitTask = new SubmitTask(getContext(),requestBody,credentialManager.getApiBase() + "sku/update","update");
        submitTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == SkuUpdateFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    editTextSkuBarcode.setText(result.getContents());
                    editTextLength.requestFocus();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == SkuUpdateFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void cancel() {

    }

    private void processInfo(RemoteResult result){
        if(result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)){
            JsonArray skus = result.getData().get("skus").getAsJsonArray();
            if(skus.size() > 1){
                ArrayList<String> clientList = new ArrayList<>();
                Log.d("debug",result.getData().get("skus").toString());

                for (int i=0;i<skus.size();i++){
                    JsonObject sku = skus.get(i).getAsJsonObject();
                    String client = sku.get("client").getAsString();
                    clientList.add(client);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("选择SKU客户");
                String[] clientArray = clientList.toArray(new String[0]);

                builder.setItems(clientArray, (dialogInterface, i) -> {
                    JsonObject sku = skus.get(i).getAsJsonObject();
                    setInputText(sku);
                });

                builder.show();
            }else if(skus.size() == 1) {
                JsonObject sku = skus.get(0).getAsJsonObject();
                setInputText(sku);
            }
            editTextLength.requestFocus();
        }else {
            reset();
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void setInputText(JsonObject sku){
        editTextLength.setEnabled(true);
        editTextHeight.setEnabled(true);
        editTextWidth.setEnabled(true);
        editTextWeight.setEnabled(true);
        client_id = sku.get("client_id").getAsString();
        String weight = sku.get("weight").getAsString();
        String height = sku.get("height").getAsString();
        String width = sku.get("width").getAsString();
        String length = sku.get("length").getAsString();
        editTextWeight.setText(weight);
        editTextHeight.setText(height);
        editTextLength.setText(length);
        editTextWidth.setText(width);
    }

    private void processUpdate(RemoteResult result){
        if(result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)){
            reset();
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
        }else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void processResponse(RemoteResult result,String operation){
        TomProgress.showProgress(getContext(),false);
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "info":
                processInfo(result);
                break;
            case "update":
                processUpdate(result);
                break;
            default:
                if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                    AlertManager.okay(getContext());
                    Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SubmitTask extends HttpSubmitTask {
        private final String mOperation;
        public SubmitTask(Context context, RequestBody formBody, String url,String operation) {
            super(context, formBody, url);
            mOperation = operation;
        }
        @Override
        protected void onPostExecute(final RemoteResult result) {
            submitTask = null;
            processResponse(result,mOperation);
        }
    }
}

