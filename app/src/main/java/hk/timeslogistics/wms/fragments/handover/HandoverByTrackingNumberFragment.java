package hk.timeslogistics.wms.fragments.handover;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.ShipmentModel;
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

public class HandoverByTrackingNumberFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private int scanFrom;
    private static final int SCAN_FROM_NUMBER = 1;

    protected Button buttonSubmit;
    protected Button buttonCancel;

    private CredentialManager credentialManager;
    private ProgressDialog mProgressDialog;

    private EditText editTextTrackingNumber;
    private TextView textViewScannedQty;
    private Spinner logisticsProviderAgent;

    private String scanTrackingNumbers;

    private SubmitTask submitTask;
    protected ArrayList<String> arrayList;
    private JsonArray scanData;
    private int scan_qty = 0;
    ArrayList<ShipmentModel> shipmentModels;
    ListView listView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        shipmentModels = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_handover_by_tracking_number, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.handover_by_tracking_number);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);

        editTextTrackingNumber = (EditText) root.findViewById(R.id.editTextTrackingNumber);
        textViewScannedQty = (TextView) root.findViewById(R.id.scanned_qty);
        editTextTrackingNumber.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextTrackingNumber);
        logisticsProviderAgent = (Spinner) root.findViewById(R.id.logistics_provider_agent);
        ImageButton buttonScanTrackingNumber = (ImageButton) root.findViewById(R.id.buttonScanTrackingNumber);
        buttonSubmit = (Button) root.findViewById(R.id.buttonSubmit);
        buttonCancel = (Button) root.findViewById(R.id.buttonCancel);
        listView = (ListView) root.findViewById(R.id.shipments);
        buttonCancel.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.handover_by_tracking_number)
                    .setCancelable(true)
                    .setMessage("Confirm Rescan?")
                    .setNeutralButton(R.string.button_cancel, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .setPositiveButton(R.string.button_confirm, (dialogInterface, i) -> {
                        resetFields();
                    })
                    .create();
            builder.show();
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(scanData.size() == 0){
                    TomAlertDialog.showAlertDialogMessage(getContext(),"Not scanned data!");
                    editTextTrackingNumber.requestFocus();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle(R.string.handover_by_tracking_number)
                            .setCancelable(true)
                            .setNeutralButton(R.string.button_cancel, (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.button_confirm, (dialogInterface, i) -> {
                                submit();;
                            })
                            .create();
                    builder.show();

                }
            }
        });

        editTextTrackingNumber.setOnKeyListener(new View.OnKeyListener(){
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {

                    String tracking_number = editTextTrackingNumber.getText().toString().trim();
                    if(tracking_number.isEmpty()){
                        editTextTrackingNumber.setText("");
                        editTextTrackingNumber.requestFocus();
                        return true;
                    }
                    checkShipment(tracking_number);
                    return true;
                }
                return false;
            }
        });
        buttonScanTrackingNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_NUMBER;
                IntentIntegrator.forSupportFragment(HandoverByTrackingNumberFragment.this).initiateScan();
            }
        });

        scanData = new JsonArray();
        editTextTrackingNumber.requestFocus();
        getAgent();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == HandoverByTrackingNumberFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_NUMBER){
                        editTextTrackingNumber.setText(result.getContents());
                        String tracking_number = editTextTrackingNumber.getText().toString().trim();
                        checkShipment(tracking_number);
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == HandoverByTrackingNumberFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void checkShipment(String tracking_number){
        if(tracking_number.isEmpty()){
            editTextTrackingNumber.setText("");
            editTextTrackingNumber.requestFocus();
            return;
        }
        if(scanData.size() == 0){
            setAgent();
        }else {
            //check tracking number
            boolean is_repeat = false;
            for (int i=0;i< scanData.size();i++){
                JsonObject item = scanData.get(i).getAsJsonObject();
                String itemTrackingNumber = item.get("tracking_number").getAsString();
                if(tracking_number.equals(itemTrackingNumber)){
                    is_repeat = true;
                    TomAlertDialog.showAlertDialogMessage(getContext(),"Repeat Scan/重复扫描");
                    break;
                }
            }
            if(!is_repeat){
                checkTrackingNumber();
            }else{
                editTextTrackingNumber.setText("");
                editTextTrackingNumber.requestFocus();
            }
        }
    }

    private void resetFields() {
        textViewScannedQty.setText("0");
        editTextTrackingNumber.setText("");
        logisticsProviderAgent.setSelection(0);
        scan_qty = 0;
        scanData = new JsonArray();
        shipmentModels.clear();
        ShipmentAdapter shipmentAdapter = new ShipmentAdapter();
        listView.setAdapter(shipmentAdapter);
    }

    private void getAgent() {
        String clients = credentialManager.getLogisticsProviders();
        JsonArray logisticsAgents = new JsonParser().parse(clients).getAsJsonArray();
        arrayList = new ArrayList<>();
        for (int i=0;i<logisticsAgents.size();i++){
            String str = logisticsAgents.get(i).toString();
            if(!str.equals("null")){
                arrayList.add(logisticsAgents.get(i).getAsString());
            }
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(),R.layout.logistics_provider_agent,arrayList);
        logisticsProviderAgent.setAdapter(arrayAdapter);
        /*FormBody.Builder builder = new FormBody.Builder();
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask("get-agent",formBody);
        submitTask.execute();*/
    }

    private void setAgent() {
        String tracking_number = editTextTrackingNumber.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("tracking_number", tracking_number);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("set-agent"),"set-agent");
        submitTask.execute();
    }

    private void submit() {
        String tracking_number = editTextTrackingNumber.getText().toString();
        String agent = logisticsProviderAgent.getSelectedItem().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("tracking_number", tracking_number);
        builder.add("agent", agent);
        RequestBody formBody = builder.build();

        JsonObject requestData = new JsonObject();
        requestData.addProperty("agent",agent);
        requestData.add("data",scanData);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),requestData.toString());
        submitTask = new SubmitTask(getContext(),requestBody,urlMapping("submit"),"submit");
        submitTask.execute();
    }

    private void checkTrackingNumber() {
        String tracking_number = editTextTrackingNumber.getText().toString();
        String agent = logisticsProviderAgent.getSelectedItem().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("tracking_number", tracking_number);
        builder.add("agent", agent);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("check-tracking-number"),"check-tracking-number");
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
            case "get-agent":
                processGetAgent(result);
                break;
            case "set-agent":
                processSetAgent(result);
                break;
            case "check-tracking-number":
                processCheckTrackingNumber(result);
                break;
            default:
                break;
        }
        /*if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }*/
    }

    @SuppressLint("SetTextI18n")
    private void setScanData(String trackingNumber){
        JsonObject scanJsonObject = new JsonObject();
        scanJsonObject.addProperty("tracking_number",trackingNumber);
        Date currentTime = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeText = dateFormat.format(currentTime);
        scanJsonObject.addProperty("scan_at",timeText);
        scanData.add(scanJsonObject);
        scan_qty += 1;
        textViewScannedQty.setText(Integer.toString(scan_qty));

        shipmentModels.clear();
        for (int i = (scanData.size() - 1);i>=0;i--){
            JsonObject item = scanData.get(i).getAsJsonObject();
            ShipmentModel shipmentModel = new ShipmentModel(item.get("tracking_number").getAsString(),item.get("scan_at").getAsString());
            shipmentModels.add(shipmentModel);
        }

        ShipmentAdapter shipmentAdapter = new ShipmentAdapter();
        listView.setAdapter(shipmentAdapter);
    }

    private void processGetAgent(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray logisticsAgents = result.getItems();
            arrayList = new ArrayList();
            for (int i=0;i<logisticsAgents.size();i++){
                String str = logisticsAgents.get(i).toString();
                if(!str.equals("null")){
                    arrayList.add(logisticsAgents.get(i).getAsString());
                }
            }
            ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(),R.layout.logistics_provider_agent,arrayList);
            logisticsProviderAgent.setAdapter(arrayAdapter);
        }
    }

    private void processSetAgent(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            String agent = result.getData().get("agent").getAsString();
            for (int i=0;i<arrayList.size();i++){
                String str = arrayList.get(i).toString();
                if(str.equals(agent)){
                    logisticsProviderAgent.setSelection(i);
                }
            }
            scan_qty = 0;
            setScanData(result.getData().get("tracking_number").getAsString());
        }else{
            resetFields();
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
        editTextTrackingNumber.setText("");
        editTextTrackingNumber.requestFocus();
    }
    @SuppressLint("SetTextI18n")
    private void processCheckTrackingNumber(RemoteResult result){
        editTextTrackingNumber.setText("");
        editTextTrackingNumber.requestFocus();
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            setScanData(result.getData().get("tracking_number").getAsString());
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
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

    private void cancel() {
        resetFields();
    }

    private String urlMapping(String operation){
        switch (operation){
            case "submit":
                return credentialManager.getApiBase() + "handover/by-tracking-number";
            case "check-tracking-number":
                return credentialManager.getApiBase() + "handover/check-tracking-number";
            case "get-agent":
                return credentialManager.getApiBase() + "handover/get-agent";
            case "set-agent":
                return credentialManager.getApiBase() + "handover/set-agent";
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

    public class ShipmentAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return shipmentModels.size();
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
            ShipmentAdapter.ViewHolder holder = null;
            if(convertView == null){
                convertView = View.inflate(getContext(),R.layout.shipment_list_view,null);
                holder = new ShipmentAdapter.ViewHolder();
                holder.scan_at = (TextView) convertView.findViewById(R.id.textViewShipmentScanAt);
                holder.tracking_number = (TextView) convertView.findViewById(R.id.textViewShipmentTrackingNumber);
                convertView.setTag(holder);
            }else{
                holder = (ShipmentAdapter.ViewHolder) convertView.getTag();
            }
            String tracking_number = shipmentModels.get(position).mTrackingNumber;
            String scan_at = shipmentModels.get(position).mScanAt;

            holder.tracking_number.setText(tracking_number);
            holder.scan_at.setText(scan_at);
            convertView.setBackgroundResource(R.color.colorSuccess);
            /*if(position == 0){

            }else {
                if(scan_qty.equals(estimated_qty)){
                    convertView.setBackgroundResource(R.color.colorSuccess);
                }else{
                    convertView.setBackgroundResource(R.color.colorAccent);
                }
            }*/
            return convertView;
        }

        class ViewHolder{
            TextView tracking_number;
            TextView scan_at;
        }


    }
}
