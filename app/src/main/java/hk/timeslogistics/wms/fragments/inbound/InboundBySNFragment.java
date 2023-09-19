package hk.timeslogistics.wms.fragments.inbound;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.fragments.asn.AsnListFragment;
import hk.timeslogistics.wms.models.InboundModel;
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

public class InboundBySNFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;

    private static final int RESULT_OK = -1;
    private static final int SCAN_FROM_ASN_NUMBER = 1;
    private static final int SCAN_FROM_PO_NUMBER = 2;
    private static final int SCAN_FROM_LOCATION = 3;
    private static final int SCAN_FROM_SKU_BARCODE = 4;
    private static final int SCAN_FROM_SERIAL_NUMBER = 5;
    private int scanFrom;

    protected Button buttonSubmit;

    private CredentialManager credentialManager;
    private EditText editTextAsnNumber;
    private EditText editTextPoNumber;
    private EditText editTextLocation;
    private EditText editTextSerialNumber;
    private EditText editTextUdf1;

    private EditText editTextSkuBarcode;

    private int is_fixed_udf1 = 0;

    private SubmitTask submitTask;

    ArrayList<InboundModel> inboundModels;
    ListView listView;

    private JSONArray inboundJsonArray;
    private JsonArray itemsJsonArray;

    private String locationType = "bin";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        inboundModels = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_inbound_by_sn, null);

        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(R.string.nav_inbound_by_sn);

        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);

        ImageButton buttonScanAsnNumber = (ImageButton) root.findViewById(R.id.buttonScanAsnNumber);
        ImageButton buttonScanPoNumber = (ImageButton) root.findViewById(R.id.buttonScanPoNumber);
        ImageButton buttonScanLocation = (ImageButton) root.findViewById(R.id.buttonScanLocation);
        ImageButton buttonScanSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanSkuBarcode);
        ImageButton buttonScanSerialNumber = (ImageButton) root.findViewById(R.id.buttonScanSerialNumber);

        editTextAsnNumber = (EditText) root.findViewById(R.id.editTextAsnNumber);
        editTextPoNumber = (EditText) root.findViewById(R.id.editTextPoNumber);
        editTextLocation = (EditText) root.findViewById(R.id.editTextLocation);
        editTextSkuBarcode = (EditText) root.findViewById(R.id.editTextSkuBarcode);
        editTextSerialNumber = (EditText) root.findViewById(R.id.editTextSerialNumber);
        editTextUdf1 = (EditText) root.findViewById(R.id.editTextUdf1);
        CheckBox checkBoxIsFixedUdf1 = (CheckBox) root.findViewById(R.id.checkBoxIsFixedUdf1);

        editTextAsnNumber.setShowSoftInputOnFocus(false);
        editTextPoNumber.setShowSoftInputOnFocus(false);
        editTextLocation.setShowSoftInputOnFocus(false);
        editTextSkuBarcode.setShowSoftInputOnFocus(false);
        editTextSerialNumber.setShowSoftInputOnFocus(false);
        editTextUdf1.setShowSoftInputOnFocus(false);

        ClearEditTextContent.setupClearEditText(getContext(),editTextAsnNumber);
        ClearEditTextContent.setupClearEditText(getContext(),editTextPoNumber);
        ClearEditTextContent.setupClearEditText(getContext(),editTextLocation);
        ClearEditTextContent.setupClearEditText(getContext(),editTextSkuBarcode);
        ClearEditTextContent.setupClearEditText(getContext(),editTextUdf1);
        ClearEditTextContent.setupClearEditText(getContext(),editTextSerialNumber);

        buttonSubmit = (Button) root.findViewById(R.id.buttonSubmit);
        listView = (ListView) root.findViewById(R.id.asnItemListView);

        buttonSubmit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.nav_inbound)
                    .setCancelable(true)
                    .setMessage("Confirm Submit?")
                    .setNeutralButton(R.string.button_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        checkAsn();
                    })
                    .setPositiveButton(R.string.button_confirm, (dialogInterface, i) -> {
                        setFieldRequestFocus("0");
                        /*try {
                            submit("0");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }*/
                    })
                    .create();
            builder.show();
        });

        buttonScanAsnNumber.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_ASN_NUMBER;
            IntentIntegrator.forSupportFragment(InboundBySNFragment.this).initiateScan();
        });

        buttonScanPoNumber.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_PO_NUMBER;
            IntentIntegrator.forSupportFragment(InboundBySNFragment.this).initiateScan();
        });

        buttonScanLocation.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_LOCATION;
            IntentIntegrator.forSupportFragment(InboundBySNFragment.this).initiateScan();
        });

        buttonScanSkuBarcode.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_SKU_BARCODE;
            IntentIntegrator.forSupportFragment(InboundBySNFragment.this).initiateScan();
        });
        buttonScanSerialNumber.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_SERIAL_NUMBER;
            IntentIntegrator.forSupportFragment(InboundBySNFragment.this).initiateScan();
        });

        editTextAsnNumber.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                checkAsn();
                return true;
            }
            return false;
        });
        editTextPoNumber.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                checkAsn();
                setFieldRequestFocus("0");
                return true;
            }
            return false;
        });
        editTextLocation.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                //checkLocation();
                setFieldRequestFocus("0");
                return true;
            }
            return false;
        });
        editTextSerialNumber.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                setFieldRequestFocus("0");
                return true;
            }
            return false;
        });
        editTextUdf1.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                setFieldRequestFocus("0");
                return true;
            }
            return false;
        });
        editTextSkuBarcode.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                setFieldRequestFocus("0");
                return true;
            }
            return false;
        });

        checkBoxIsFixedUdf1.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                is_fixed_udf1 = 1;
            }else{
                is_fixed_udf1 = 0;
                editTextUdf1.setText("");
            }
        });

        getActivity().setTitle(getString(R.string.title_activity_main));

        if (getArguments() != null) {
            String asn_number = getArguments().getString("asn_number");
            editTextAsnNumber.setText(asn_number);
            checkAsn();
        }else{
            editTextAsnNumber.requestFocus();
        }
        inboundJsonArray = new JSONArray();
        itemsJsonArray = new JsonArray();
        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(v -> {
            clickBack();
        });

        return root;
    }

    private void clickBack(){
        Fragment fragment = MainActivity.asnListFragment;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(!fragment.isAdded()){
            if(MainActivity.currentFragment != null){
                transaction.hide(MainActivity.currentFragment);
            }
            transaction.add(R.id.flContent, fragment).commit();
        }else{
            transaction.remove(fragment);
            fragment = new AsnListFragment();
            transaction.add(R.id.flContent, fragment);
            transaction.hide(MainActivity.currentFragment).show(fragment).commit();
        }
        MainActivity.currentFragment = fragment;
        MainActivity.asnListFragment = fragment;
    }

    public void checkLocation(){
        String asn_number = editTextAsnNumber.getText().toString();
        if(asn_number.isEmpty()){
            TomAlertDialog.showAlertDialogMessage(getContext(), String.valueOf(R.string.asn_number_is_required));
            return;
        }
        String location = editTextLocation.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("location", location);
        builder.add("asn_number", asn_number);
        builder.add("inbound_by", "serial_number");
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("check-location"),"check-location");
        submitTask.execute();
    }

    public boolean checkSku(){
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String po_number = editTextPoNumber.getText().toString();
        boolean is_exists = false;
        String itemPoNumber;
        for (int i = 0; i<itemsJsonArray.size(); i++ ){
            JsonObject item = itemsJsonArray.get(i).getAsJsonObject();
            itemPoNumber = item.get("po_number").getAsString();
            if(po_number.equals(itemPoNumber)){
                is_exists = InboundModel.checkSkuBarcodeInAsn(item,sku_barcode);
                if(is_exists){
                    break;
                }
            }
        }
        if(!is_exists){
            TomAlertDialog.showAlertDialogMessage(getContext(),"SKU Barcode["+sku_barcode+"] not found in the ASN or Po number");
            editTextSkuBarcode.setText("");
            editTextSkuBarcode.requestFocus();
        }
        return is_exists;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == InboundBySNFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_ASN_NUMBER){
                        editTextAsnNumber.setText(result.getContents());
                        checkAsn();
                        return;
                    }else if(scanFrom == SCAN_FROM_LOCATION){
                        editTextLocation.setText(result.getContents());
                        //checkLocation();
                    }else if(scanFrom == SCAN_FROM_PO_NUMBER){
                        editTextPoNumber.setText(result.getContents());
                        checkAsn();
                        return;
                    }else if(scanFrom == SCAN_FROM_SKU_BARCODE){
                        editTextSkuBarcode.setText(result.getContents());
                    }else if(scanFrom == SCAN_FROM_SERIAL_NUMBER){
                        editTextSerialNumber.setText(result.getContents());
                    }
                    setFieldRequestFocus("0");
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == InboundBySNFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    public void resetFields() {
        if(is_fixed_udf1 == 0){
            editTextUdf1.setText("");
        }
        editTextSkuBarcode.setText("");
        editTextSerialNumber.setText("");
        inboundJsonArray = new JSONArray();
        editTextSkuBarcode.requestFocus();
    }

    private void submit(String is_force_receive) throws JSONException {
        String asn_number = editTextAsnNumber.getText().toString();
        String po_number = editTextPoNumber.getText().toString();
        if(asn_number.length() == 0){
            Toast.makeText(getContext(), R.string.asn_number_is_required, Toast.LENGTH_LONG).show();
            return;
        }
        JSONObject requestData = new JSONObject();
        requestData.put("asn_number",asn_number);
        requestData.put("po_number",po_number);
        requestData.put("is_force_receive",is_force_receive);
        requestData.put("location_type",locationType);
        requestData.put("inbound_by","serial_number");
        JSONObject scanJsonObject = new JSONObject();
        try {
            String location = editTextLocation.getText().toString();
            String sku_barcode = editTextSkuBarcode.getText().toString();
            String serial_number = editTextSerialNumber.getText().toString();
            String udf_1 = editTextUdf1.getText().toString();
            inboundJsonArray = new JSONArray();
            scanJsonObject.put("po_number",po_number);
            scanJsonObject.put("location",location);
            scanJsonObject.put("sku_barcode",sku_barcode);
            scanJsonObject.put("serial_number",serial_number);
            scanJsonObject.put("udf_1",udf_1);
            inboundJsonArray.put(scanJsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if(inboundJsonArray.length() == 0){
            TomAlertDialog.showAlertDialogMessage(getContext(),"Not scanned data");
            return;
        }
        requestData.put("data",inboundJsonArray);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),requestData.toString());
        TomProgress.showProgress(getContext(),true);
        submitTask = new SubmitTask(getContext(),requestBody,urlMapping("submit"),"submit");
        submitTask.execute();
    }

    private void checkAsn() {
        String asn_number = editTextAsnNumber.getText().toString();
        String po_number = editTextPoNumber.getText().toString();
        if(asn_number.length() == 0){
            Toast.makeText(getContext(), R.string.asn_number_is_required, Toast.LENGTH_LONG).show();
            return;
        }
        resetFields();
        editTextAsnNumber.setText(asn_number);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("asn_number", asn_number);
        builder.add("po_number", po_number);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("check-asn"),"check-asn");
        submitTask.execute();
    }

    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
            return;
        }
        switch (operation){
            case "check-asn":
                processCheckAsn(result);
                break;
            case "check-location":
                processCheckLocation(result);
                break;
            case "submit":
                processSubmit(result);
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

    private void setScannedDetails(JsonArray jsonArray){
        inboundModels.clear();
        for (int i = 0;i<jsonArray.size();i++){
            JsonObject item = (JsonObject) jsonArray.get(i).getAsJsonObject();
            InboundModel inboundModel = new InboundModel(item.get("barcode").getAsString(),item.get("estimated_qty").getAsString(),item.get("scan_qty").getAsString(),item.get("actual_qty").getAsString());
            inboundModels.add(inboundModel);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AsnItemAdapter asnItemAdapter = new AsnItemAdapter();
                listView.setAdapter(asnItemAdapter);
            }
        });

    }

    private void processSubmit(RemoteResult result){
        TomProgress.showProgress(getContext(),false);
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
            itemsJsonArray = result.getItems().getAsJsonArray();
            setScannedDetails(itemsJsonArray);
            //checkAsn();
            resetFields();
        } else if(result.getStatus().equals(417)){
            AlertManager.error(getContext());
            String over_receive_data_string = result.getOverReceiveData().toString();
            JsonArray over_receive_data = new JsonParser().parse(over_receive_data_string).getAsJsonArray();
            StringBuilder message = new StringBuilder();
            for (int i=0;i<over_receive_data.size();i++){
                JsonObject over_receive_object = (JsonObject) over_receive_data.get(i);
                message.append("SKU[ ")
                        .append(over_receive_object.get("sku_barcode").getAsString())
                        .append(" ]")
                        .append(getString(R.string.overcharge))
                        .append("\n")
                        .append(getString(R.string.estimated_qty))
                        .append("[ ")
                        .append(over_receive_object.get("estimated_qty").getAsString())
                        .append(" ]\n").append(getString(R.string.actual_qty))
                        .append("[ ").append(over_receive_object.get("actual_qty").getAsString()).append(" ]\n").append(getString(R.string.currency_scaned_qty)).append("[ ").append(over_receive_object.get("qty").getAsString()).append(" ];\n");

            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.overcharge)
                    .setCancelable(true)
                    .setMessage(message.toString())
                    .setPositiveButton(R.string.button_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        checkAsn();
                    }).setNeutralButton(R.string.button_confirm_overcharge, (dialogInterface, i) -> {
                        try {
                            submit("1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    })
                    .create();
            builder.show();

        } else {
            resetFields();
            AlertManager.error(getContext());
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void processCheckAsn(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonObject jsonObject = result.getData();
            String po_number = jsonObject.get("po_number").getAsString();
            editTextPoNumber.setText(po_number);
            itemsJsonArray = result.getItems().getAsJsonArray();
            resetFields();
            setFieldRequestFocus("0");
            setScannedDetails(itemsJsonArray);
        } else {
            resetFields();
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }
    private void processCheckLocation(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            locationType = result.getData().get("location_type").getAsString();
            setFieldRequestFocus("0");
        } else {
            editTextLocation.setText("");
            editTextLocation.requestFocus();
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void setFieldRequestFocus(String is_force_receive){
        String asn_number = editTextAsnNumber.getText().toString();
        String po_number = editTextPoNumber.getText().toString();
        String location = editTextLocation.getText().toString();
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String serial_number = editTextSerialNumber.getText().toString();
        String udf_1 = editTextUdf1.getText().toString();
        if(asn_number.isEmpty()){
            editTextAsnNumber.requestFocus();
            return;
        }
        if(po_number.isEmpty()){
            editTextPoNumber.requestFocus();
            return;
        }
        if(location.isEmpty()){
            editTextLocation.requestFocus();
            return;
        }
        if(sku_barcode.isEmpty()){
            editTextSkuBarcode.requestFocus();
            return;
        }
        /*boolean is_exists = checkSku();
        if(!is_exists){
            return;
        }*/
        int is_udf_1_required = 0;
        for (int i=0;i< itemsJsonArray.size();i++){
            JsonObject item = itemsJsonArray.get(i).getAsJsonObject();
            String itemPoNumber = item.get("po_number").getAsString();
            String itemSkuBarcode = item.get("barcode").getAsString();
            if(po_number.equals(itemPoNumber) && sku_barcode.equals(itemSkuBarcode)){
                if(item.get("is_udf_1_required").getAsInt() == 1){
                    is_udf_1_required = 1;
                }
                break;
            }
        }
        if(udf_1.isEmpty() && is_udf_1_required == 1){
            editTextUdf1.requestFocus();
            return;
        }
        if(serial_number.isEmpty()){
            editTextSerialNumber.requestFocus();
            return;
        }
        try {
            submit("0");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void cancel() {
        resetFields();
    }

    private String urlMapping(String operation){
        switch (operation){
            case "check-asn":
                return credentialManager.getApiBase() + "inbound/check-asn";
            case "submit":
                return credentialManager.getApiBase() + "inbound";
            case "suggest-location":
                return credentialManager.getApiBase() + "inbound/suggest/location";
            case "check-location":
                return credentialManager.getApiBase() + "inbound/check-location";
            case "generate-task":
                return credentialManager.getApiBase() + "inbound/generate-task";
            default:
                return "";
        }
    }

    @SuppressLint("StaticFieldLeak")
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

    public class AsnItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return inboundModels.size();
        }
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null){
                convertView = View.inflate(getContext(),R.layout.asn_item_list_view,null);
                holder = new ViewHolder();
                holder.sku_barcode = (TextView) convertView.findViewById(R.id.textViewSkuBarcode);
                holder.estimated_qty = (TextView) convertView.findViewById(R.id.textViewEstimatedQty);
                holder.scan_qty = (TextView) convertView.findViewById(R.id.textViewScanQty);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            String estimated_qty = inboundModels.get(position).mEstimatedQty;
            String scan_qty = inboundModels.get(position).mActualQty;

            holder.sku_barcode.setText(inboundModels.get(position).mBarcode);
            holder.estimated_qty.setText(estimated_qty);
            holder.scan_qty.setText(scan_qty);
            if(position == 0){
                if(scan_qty.equals(estimated_qty)){
                    convertView.setBackgroundResource(R.color.colorSuccess);
                }else{
                    convertView.setBackgroundResource(R.color.colorPartialSuccess);
                }
            }else {
                if(scan_qty.equals(estimated_qty)){
                    convertView.setBackgroundResource(R.color.colorSuccess);
                }else{
                    convertView.setBackgroundResource(R.color.colorAccent);
                }
            }
            return convertView;
        }
        class ViewHolder{
            TextView sku_barcode;
            TextView estimated_qty;
            TextView scan_qty;
        }
    }
}
