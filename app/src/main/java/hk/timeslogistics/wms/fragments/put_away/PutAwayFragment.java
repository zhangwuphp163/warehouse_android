package hk.timeslogistics.wms.fragments.put_away;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.PutAwayModel;
import hk.timeslogistics.wms.models.PutAwayTaskModel;
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

public class PutAwayFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;

    private static final int RESULT_OK = -1;

    private static final int SCAN_FROM_SKU_BARCODE = 1;
    private static final int SCAN_FROM_LOCATION_CONTAINER = 2;
    private static final int SCAN_FROM_TO_LOCATION = 3;
    private int scanFrom;

    protected Button buttonSubmit;
    protected Button buttonClear;

    private CredentialManager credentialManager;
    private ImageButton buttonScanSkuBarcode;
    private ImageButton buttonScanLocationContainer;
    private ImageButton buttonScanToLocation;
    private EditText editTextSkuBarcode;
    private EditText editTextLocationContainer;
    private EditText editTextToLocation;
    private EditText editTextPutAwayQty;
    private Spinner spinnerCondition;
    private String isScanSkuBarcode = "1";
    private String locationType = "bin";
    private String enablePutAwayQty = "0";
    private CheckBox checkboxIsScanSkuBarcode;
    private LinearLayout linearLayoutPutAwayQty;
    private LinearLayout linearLayoutSkuBarcode;
    private SubmitTask submitTask;
    ArrayList<PutAwayModel> putAwayModels;
    ListView listView;
    int client_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        putAwayModels = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_put_away, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.put_away);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        buttonScanSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanSkuBarcode);
        buttonScanToLocation = (ImageButton) root.findViewById(R.id.buttonScanToLocation);
        buttonScanLocationContainer = (ImageButton) root.findViewById(R.id.buttonScanLocationContainer);
        linearLayoutPutAwayQty = (LinearLayout) root.findViewById(R.id.linearLayoutPutAwayQty);
        linearLayoutSkuBarcode = (LinearLayout) root.findViewById(R.id.linearLayoutSkuBarcode);
        checkboxIsScanSkuBarcode = (CheckBox) root.findViewById(R.id.checkboxIsScanSkuBarcode);

        editTextPutAwayQty = (EditText) root.findViewById(R.id.editTextPutAwayQty);
        editTextSkuBarcode = (EditText) root.findViewById(R.id.editTextSkuBarcode);
        editTextToLocation = (EditText) root.findViewById(R.id.editTextToLocation);
        editTextLocationContainer = (EditText) root.findViewById(R.id.editTextLocationContainer);

        editTextSkuBarcode.setShowSoftInputOnFocus(false);
        editTextToLocation.setShowSoftInputOnFocus(false);
        editTextLocationContainer.setShowSoftInputOnFocus(false);
        editTextPutAwayQty.setShowSoftInputOnFocus(false);

        ClearEditTextContent.setupClearEditText(getContext(),editTextSkuBarcode);
        ClearEditTextContent.setupClearEditText(getContext(),editTextToLocation);
        ClearEditTextContent.setupClearEditText(getContext(),editTextLocationContainer);

        spinnerCondition = (Spinner) root.findViewById(R.id.spinnerCondition);

        buttonSubmit = (Button) root.findViewById(R.id.buttonSubmit);
        buttonClear = (Button) root.findViewById(R.id.buttonClear);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                Boolean is_submit = setFieldRequestFocus();
                if(enablePutAwayQty.equals("0")){
                    TomAlertDialog.showAlertDialogMessage(getContext(),"No available quantity\n无可上架数量");
                    return;
                }
                if(is_submit){
                    String message = getString(R.string.qty).toString();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.put_away)
                            .setCancelable(true)
                            .setMessage(message +": "+(isScanSkuBarcode.equals("1")?editTextPutAwayQty.getText().toString():enablePutAwayQty))
                            .setNeutralButton(R.string.button_cancel, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.button_confirm, (dialogInterface, i) -> {
                                submit();
                            })
                            .create();
                    builder.show();
                }
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });

        buttonScanSkuBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_SKU_BARCODE;
                IntentIntegrator.forSupportFragment(PutAwayFragment.this).initiateScan();
            }
        });

        buttonScanLocationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_LOCATION_CONTAINER;
                IntentIntegrator.forSupportFragment(PutAwayFragment.this).initiateScan();
            }
        });

        buttonScanToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_TO_LOCATION;
                IntentIntegrator.forSupportFragment(PutAwayFragment.this).initiateScan();
            }
        });

        editTextSkuBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getList();
                    //suggestLocation();
                    //setFieldRequestFocus();
                    return true;
                }
                return false;
            }
        });
        editTextLocationContainer.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getList();
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

        checkboxIsScanSkuBarcode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    isScanSkuBarcode = "1";
                    linearLayoutPutAwayQty.setVisibility(View.VISIBLE);
                    linearLayoutSkuBarcode.setVisibility(View.VISIBLE);
                }else{
                    isScanSkuBarcode = "0";
                    linearLayoutPutAwayQty.setVisibility(View.GONE);
                    linearLayoutSkuBarcode.setVisibility(View.GONE);
                }
                resetFields();
            }
        });
        editTextLocationContainer.requestFocus();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == PutAwayFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_SKU_BARCODE){
                        editTextSkuBarcode.setText(result.getContents());
                        getList();
                        return;
                    }else if(scanFrom == SCAN_FROM_LOCATION_CONTAINER){
                        editTextLocationContainer.setText(result.getContents());
                        getList();
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
            if (resultCode == PutAwayFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void resetFields() {
        editTextSkuBarcode.setText("");
        editTextPutAwayQty.setText("");
        editTextToLocation.setText("");
        editTextLocationContainer.setText("");
        editTextLocationContainer.requestFocus();
        putAwayModels.clear();
        listView = (ListView) getActivity().findViewById(R.id.put_away_list_view);
        ItemAdapter itemAdapter = new ItemAdapter();
        listView.setAdapter(itemAdapter);
    }

    private void getList() {
        TomProgress.showProgress(getContext(),true);
        String container_number = editTextLocationContainer.getText().toString();
        String sku_barcode = editTextSkuBarcode.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("container_number", container_number);
        builder.add("sku_barcode", sku_barcode);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("container-items"),"container-items");
        submitTask.execute();
    }

    private void checkSku() {
        String sku_barcode = editTextSkuBarcode.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("sku_barcode", sku_barcode);
        builder.add("sku_barcode", sku_barcode);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("check-sku"),"check-sku");
        submitTask.execute();
    }

    private void submit() {
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String container_number = editTextLocationContainer.getText().toString();
        if(container_number.isEmpty()){
            return;
        }
        String to_location = editTextToLocation.getText().toString();
        if(to_location.isEmpty()){
            return;
        }
        String condition = spinnerCondition.getSelectedItem().toString();
        String qty = editTextPutAwayQty.getText().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("container_number", container_number);
        builder.add("sku_barcode", sku_barcode);
        builder.add("to_location", to_location);
        builder.add("is_scan_sku_barcode", isScanSkuBarcode);
        builder.add("condition", condition);
        builder.add("location_type", locationType);
        builder.add("client_id", Integer.toString(client_id));
        builder.add("qty", qty);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("submit"),"submit");
        submitTask.execute();
    }

    private void suggestLocation(){
        String barcode = editTextSkuBarcode.getText().toString();
        String bin_code = editTextLocationContainer.getText().toString();
        if(barcode.length() == 0){
            Toast.makeText(getContext(), R.string.sku_barcode_is_required, Toast.LENGTH_LONG).show();
            return;
        }
        editTextSkuBarcode.setText(barcode);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("sku_barcode", barcode);
        builder.add("bin_code", bin_code);
        builder.add("location_type", locationType);
        RequestBody formBody = builder.build();
        submitTask = new PutAwayFragment.SubmitTask(getContext(),formBody,urlMapping("suggest-location"),"suggest-location");
        submitTask.execute();
    }

    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "container-items":
                TomProgress.showProgress(getContext(),false);
                processContainerItems(result);
                break;
            case "submit":
                TomProgress.showProgress(getContext(),false);
                processSubmit(result);
                break;
            case "check-sku":
                processCheckSku(result);
                break;
            /*case "suggest-location":
                if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                    JsonArray items = result.getItems();

                    int itemsLength = items.size();
//                    if(itemsLength > 3){
//                        itemsLength = 3;
//                    }
                    int fromLocationIndex = 0;
                    int toLocationIndex = 0;
                    for (int i = 0;i<itemsLength;i++){
                        JsonObject item = (JsonObject) items.get(i);
                        String bin_type = item.get("bin_type").getAsString();
                        if(bin_type.equals("Temporary")){
                            if(fromLocationIndex == 0){
                                buttonSuggestFromLocation0.setText(item.get("location").getAsString());
                                buttonSuggestFromLocation0.setVisibility(View.VISIBLE);
                            }
                            if(fromLocationIndex == 1){
                                buttonSuggestFromLocation1.setText(item.get("location").getAsString());
                                buttonSuggestFromLocation1.setVisibility(View.VISIBLE);
                            }
                            if(fromLocationIndex == 2){
                                buttonSuggestFromLocation2.setText(item.get("location").getAsString());
                                buttonSuggestFromLocation2.setVisibility(View.VISIBLE);
                            }
                            fromLocationIndex +=1;
                        }
                        if(bin_type.equals("Standard")){
                            if(toLocationIndex == 0){
                                buttonSuggestLocation0.setText(item.get("location").getAsString());
                                buttonSuggestLocation0.setVisibility(View.VISIBLE);
                            }
                            if(toLocationIndex == 1){
                                buttonSuggestLocation1.setText(item.get("location").getAsString());
                                buttonSuggestLocation1.setVisibility(View.VISIBLE);
                            }
                            if(toLocationIndex == 2){
                                buttonSuggestLocation2.setText(item.get("location").getAsString());
                                buttonSuggestLocation2.setVisibility(View.VISIBLE);
                            }
                            toLocationIndex += 1;
                        }

                    }
                    //setFieldRequestFocus();
//                AlertManager.okay(getContext());
//                Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                } else {
                    //resetFields();
                    AlertManager.error(getContext());
                    Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
                }

                break;*/
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
            //JsonObject jsonObject = result.getData();
            //String qty = jsonObject.get("enabled_relocation_qty").getAsString();

            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
            editTextToLocation.requestFocus();
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    private void processSubmit(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
            resetFields();
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void processContainerItems(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            putAwayModels.clear();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                client_id = item.get("client_id").getAsInt();
                PutAwayModel putAwayModel = new PutAwayModel(item);
                putAwayModels.add(putAwayModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.put_away_list_view);
            ItemAdapter itemAdapter = new ItemAdapter();
            listView.setAdapter(itemAdapter);
            locationType = result.getData().get("location_type").getAsString();
            enablePutAwayQty = result.getData().get("total_qty").getAsString();
            setFieldRequestFocus();
        } else {

            if(editTextSkuBarcode.getText().toString().isEmpty()){
                editTextLocationContainer.setText("");
                editTextLocationContainer.requestFocus();
            }else{
                editTextSkuBarcode.setText("");
                editTextSkuBarcode.requestFocus();
            }

            locationType = result.getData().get("location_type").getAsString();
            enablePutAwayQty = result.getData().get("total_qty").getAsString();
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private boolean setFieldRequestFocus(){
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String location_container = editTextLocationContainer.getText().toString();
        String qty = editTextPutAwayQty.getText().toString();
        String to_location = editTextToLocation.getText().toString();
        if(location_container.isEmpty()){
            editTextLocationContainer.requestFocus();
            return false;
        }
        if(isScanSkuBarcode.equals("1")){
            if(sku_barcode.isEmpty()){
                editTextSkuBarcode.requestFocus();
                return false;
            }
            if(to_location.isEmpty()){
                editTextToLocation.requestFocus();
                return false;
            }
            if(qty.isEmpty()){
                editTextPutAwayQty.requestFocus();
                return false;
            }
        }else {
            if(to_location.isEmpty()){
                editTextToLocation.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void cancel() {
        resetFields();
    }

    private String urlMapping(String operation){
        switch (operation){
            case "container-items":
                return credentialManager.getApiBase() + "put-away/container-items";
            case "submit":
                return credentialManager.getApiBase() + "put-away";
            case "check-sku":
                return credentialManager.getApiBase() + "put-away/check-sku";
            case "suggest-location":
                return credentialManager.getApiBase() + "put-away/suggest-location";
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

    public class ItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return putAwayModels.size();
        }
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            @SuppressLint("ViewHolder") View v = View.inflate(getActivity().getApplicationContext(),R.layout.put_away_list_view,null);
            String sku_barcode = putAwayModels.get(position).sku_barcode;
            String asn_number = putAwayModels.get(position).asn_number;
            String qty = putAwayModels.get(position).qty;
            TextView TextViewSkuBarcode = (TextView) v.findViewById(R.id.textViewSkuBarcode);
            TextView textViewAsnNumber = (TextView) v.findViewById(R.id.textViewAsnNumber);
            TextView textViewQty = (TextView) v.findViewById(R.id.textViewQty);
            TextViewSkuBarcode.setText(sku_barcode);
            textViewAsnNumber.setText(asn_number);
            textViewQty.setText(qty);
            return v;
        }
    }

}
