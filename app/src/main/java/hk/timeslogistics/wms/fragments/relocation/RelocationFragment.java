package hk.timeslogistics.wms.fragments.relocation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomAlertDialog;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RelocationFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;

    private static final int RESULT_OK = -1;

    private static final int SCAN_FROM_SKU_BARCODE = 1;
    private static final int SCAN_FROM_FROM_LOCATION = 2;
    private static final int SCAN_FROM_TO_LOCATION = 3;
    private int scanFrom;

    protected Button buttonSubmit;
    protected Button buttonCancel;

    private CredentialManager credentialManager;
    private ProgressDialog mProgressDialog;
    private EditText editTextSkuBarcode;
    private EditText editTextFromLocation;
    private EditText editTextToLocation;
    private EditText editTextRelocationQty;
    private TextView enableRelocationQtyView;

    private SubmitTask submitTask;

    int client_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_relocation, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_relocation);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        ImageButton buttonScanSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanSkuBarcode);
        ImageButton buttonScanFromLocation = (ImageButton) root.findViewById(R.id.buttonScanFromLocation);
        ImageButton buttonScanToLocation = (ImageButton) root.findViewById(R.id.buttonScanToLocation);

        editTextSkuBarcode = (EditText) root.findViewById(R.id.editTextSkuBarcode);
        editTextFromLocation = (EditText) root.findViewById(R.id.editTextFromLocation);
        editTextToLocation = (EditText) root.findViewById(R.id.editTextToLocation);
        editTextRelocationQty = (EditText) root.findViewById(R.id.editTextRelocationQty);
        editTextSkuBarcode.setShowSoftInputOnFocus(false);
        editTextFromLocation.setShowSoftInputOnFocus(false);
        editTextToLocation.setShowSoftInputOnFocus(false);
        editTextRelocationQty.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextFromLocation);
        ClearEditTextContent.setupClearEditText(getContext(),editTextSkuBarcode);
        ClearEditTextContent.setupClearEditText(getContext(),editTextToLocation);

        enableRelocationQtyView = (TextView) root.findViewById(R.id.enableRelocationQtyView);

        buttonSubmit = (Button) root.findViewById(R.id.buttonSubmit);
        buttonCancel = (Button) root.findViewById(R.id.buttonCancel);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = setFieldRequestFocus();
                if(flag){
                    submit();
                }
            }
        });

        buttonScanSkuBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_SKU_BARCODE;
                IntentIntegrator.forSupportFragment(RelocationFragment.this).initiateScan();
            }
        });

        buttonScanFromLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_FROM_LOCATION;
                IntentIntegrator.forSupportFragment(RelocationFragment.this).initiateScan();
            }
        });

        buttonScanToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_TO_LOCATION;
                IntentIntegrator.forSupportFragment(RelocationFragment.this).initiateScan();
            }
        });

        editTextSkuBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    setFieldRequestFocus();
                    return true;
                }
                return false;
            }
        });
        editTextFromLocation.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    checkSku();
                    setFieldRequestFocus();
                    return true;
                }
                return false;
            }
        });
        editTextToLocation.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    setFieldRequestFocus();
                    return true;
                }
                return false;
            }
        });
        editTextRelocationQty.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    setFieldRequestFocus();
                    return true;
                }
                return false;
            }
        });

        resetFields();
        //getActivity().setTitle(getString(R.string.title_activity_main));
        editTextSkuBarcode.requestFocus();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == RelocationFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_SKU_BARCODE){
                        editTextSkuBarcode.setText(result.getContents());
                    }else if(scanFrom == SCAN_FROM_FROM_LOCATION){
                        editTextFromLocation.setText(result.getContents());
                        checkSku();
                        return;
                    }else {
                        editTextToLocation.setText(result.getContents());
                    }
                    setFieldRequestFocus();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == RelocationFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void resetFields() {
        editTextSkuBarcode.setText("");
        editTextSkuBarcode.requestFocus();
        editTextFromLocation.setText("");
        editTextToLocation.setText("");
        editTextRelocationQty.setText("");
        enableRelocationQtyView.setText("");
    }

    private void checkSku() {
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String from_location = editTextFromLocation.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("sku_barcode", sku_barcode);
        builder.add("from_location", from_location);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("check-sku"),"check-sku");
        submitTask.execute();
    }

    private void submit() {
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String from_location = editTextFromLocation.getText().toString();
        String to_location = editTextToLocation.getText().toString();
        String relocation_qty = editTextRelocationQty.getText().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("sku_barcode", sku_barcode);
        builder.add("from_location", from_location);
        builder.add("to_location", to_location);
        builder.add("relocation_qty", relocation_qty);
        builder.add("client_id", Integer.toString(client_id));
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("submit"),"submit");
        submitTask.execute();
    }

    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "submit":
                TomProgress.showProgress(getContext(),false);
                processSubmit(result);
                break;
            case "check-sku":
                processCheckSku(result);
                break;
            default:
                if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                    AlertManager.okay(getContext());
                    Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                } else {
                    AlertManager.error(getContext());
                    Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void processCheckSku(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonObject jsonObject = result.getData();
            String qty = jsonObject.get("enabled_relocation_qty").getAsString();
            enableRelocationQtyView.setText(qty);
            client_id = jsonObject.get("client_id").getAsInt();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
            //Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    private void processSubmit(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
            resetFields();
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean setFieldRequestFocus(){
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String from_location = editTextFromLocation.getText().toString();
        String to_location = editTextToLocation.getText().toString();
        String relocation_qty = editTextRelocationQty.getText().toString();
        if(sku_barcode.isEmpty()){
            editTextSkuBarcode.requestFocus();
            return false;
        }
        if(from_location.isEmpty()){
            editTextFromLocation.requestFocus();
            return false;
        }
        if(to_location.isEmpty()){
            editTextToLocation.requestFocus();
            return false;
        }
        if(relocation_qty.isEmpty()){
            editTextRelocationQty.requestFocus();
            return false;
        }
        return true;
        //submit();
    }

    private void cancel() {
        resetFields();
    }

    private String urlMapping(String operation){
        switch (operation){
            case "submit":
                return credentialManager.getApiBase() + "relocation";
            case "check-sku":
                return credentialManager.getApiBase() + "relocation/check-sku";
            default:
                return "";
        }
    }

    private class SubmitTask extends HttpSubmitTask {
        private final String mOperation;
        public SubmitTask(Context context, RequestBody formBody, String url, String operation) {
            super(context, formBody, url);
            mOperation = operation;

        }
        protected void onPostExecute(final RemoteResult result) {
            submitTask = null;
            processResponse(result,mOperation);
        }
    }
}
