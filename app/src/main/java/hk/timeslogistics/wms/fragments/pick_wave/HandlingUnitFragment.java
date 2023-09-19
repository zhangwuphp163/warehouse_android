package hk.timeslogistics.wms.fragments.pick_wave;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.util.ArrayList;
import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.HandlingUnitModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.BarcodeCreate;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomAlertDialog;
import hk.timeslogistics.wms.utils.TomProgress;
import hk.timeslogistics.wms.utils.UrlMapping;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class HandlingUnitFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;

    private static final int RESULT_OK = -1;

    private static final int SCAN_FROM_HANDLING_UNIT_NUMBER = 1;
    private int scanFrom;

    private CredentialManager credentialManager;
    private ProgressDialog mProgressDialog;
    private EditText editTextHandlingUnitNumber;
    private TextView textViewTotalQty;
    private TextView textViewPickWaveNumber;
    private TextView textViewTakeDownQty;
    private TextView textViewSkipQty;
    private TextView textViewHandlingUnitNumbers;
    private Button buttonSkipDetail;
    private JsonArray skipItems;
    private ListView listView;
    private ImageButton imageButtonShowBarcode;
    ArrayList<HandlingUnitModel> handlingUnitModels;

    private HttpSubmitTask submitTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        handlingUnitModels = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_handling_unit, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.handling_unit);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        ImageButton buttonScanHandlingUnitNumber = (ImageButton) root.findViewById(R.id.buttonScanHandlingUnitNumber);
        imageButtonShowBarcode = (ImageButton) root.findViewById(R.id.show_barcode);

        editTextHandlingUnitNumber = (EditText) root.findViewById(R.id.editTextHandlingUnitNumber);
        editTextHandlingUnitNumber.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextHandlingUnitNumber);
        textViewTotalQty = (TextView) root.findViewById(R.id.total_qty);
        textViewSkipQty = (TextView) root.findViewById(R.id.skip_qty);
        textViewTakeDownQty = (TextView) root.findViewById(R.id.take_down_qty);
        textViewPickWaveNumber = (TextView) root.findViewById(R.id.pick_wave_number);
        textViewHandlingUnitNumbers = (TextView) root.findViewById(R.id.handling_unit_numbers);
        LinearLayout linearLayoutPickingDetail = (LinearLayout) root.findViewById(R.id.linear_layout_picking_detail);
        buttonSkipDetail = (Button) root.findViewById(R.id.button_skip_detail);
        editTextHandlingUnitNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER){
                    getList();
                }
                return false;
            }
        });
        buttonScanHandlingUnitNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_HANDLING_UNIT_NUMBER;
                IntentIntegrator.forSupportFragment(HandlingUnitFragment.this).initiateScan();
            }
        });
        buttonSkipDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlingUnitModels.clear();
                for (int i = 0;i<skipItems.size();i++){
                    JsonObject item = (JsonObject) skipItems.get(i);
                    if(item.get("type").getAsString().equals("skip")){
                        HandlingUnitModel handlingUnitModel = new HandlingUnitModel(item);
                        handlingUnitModels.add(handlingUnitModel);
                    }
                }
                listView = (ListView) getActivity().findViewById(R.id.handling_unit_list_view);
                HandlingUnitListAdapter handlingUnitListAdapter = new HandlingUnitListAdapter();
                listView.setAdapter(handlingUnitListAdapter);
            }
        });

        editTextHandlingUnitNumber.requestFocus();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == HandlingUnitFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_HANDLING_UNIT_NUMBER){
                        editTextHandlingUnitNumber.setText(result.getContents());
                        getList();
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == HandlingUnitFragment.RESULT_OK) {
                cancel();
            }
        }
    }
    private void cancel() {

    }

    private void getList() {
        reset();
        String handling_unit_number = editTextHandlingUnitNumber.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("handling_unit_number", handling_unit_number);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody, UrlMapping.getUrl(credentialManager,"handling-unit-list"),"handling-unit-list");
        TomProgress.showProgress(getContext(),true);
        submitTask.execute();
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
    private void processList(RemoteResult result){
        TomProgress.showProgress(getContext(),false);
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonObject data = result.getData();
            textViewSkipQty.setText(data.get("skip_qty").getAsString());
            textViewTakeDownQty.setText(data.get("take_down_qty").getAsString());
            textViewPickWaveNumber.setText(data.get("pick_wave_number").getAsString());
            textViewTotalQty.setText(data.get("qty").getAsString());
            textViewHandlingUnitNumbers.setText(data.get("handling_unit_numbers").getAsString());
            if(data.get("skip_qty").getAsString().equals("0")){
                buttonSkipDetail.setVisibility(View.GONE);
            }else{
                buttonSkipDetail.setVisibility(View.VISIBLE);
            }
            editTextHandlingUnitNumber.setText("");
            editTextHandlingUnitNumber.requestFocus();
            skipItems = result.getItems();

            Bitmap BarCodeBitmap = null;
            BarCodeBitmap = BarcodeCreate.createBarcode(data.get("pick_wave_number").getAsString(),400,80, BarcodeFormat.CODE_128);
            imageButtonShowBarcode.setImageBitmap(BarCodeBitmap);
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void reset(){
        textViewTotalQty.setText("");
        textViewTakeDownQty.setText("");
        textViewSkipQty.setText("");
        textViewPickWaveNumber.setText("");
        buttonSkipDetail.setVisibility(View.GONE);
        handlingUnitModels.clear();
        listView = (ListView) getActivity().findViewById(R.id.handling_unit_list_view);
        HandlingUnitListAdapter handlingUnitListAdapter = new HandlingUnitListAdapter();
        listView.setAdapter(handlingUnitListAdapter);
        skipItems = new JsonArray();
    }
    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "handling-unit-list":
                processList(result);
                break;
            default:
                if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                    AlertManager.okay(getContext());
                    Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                } else {
                    TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
                }
                break;
        }
    }
    public class HandlingUnitListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return handlingUnitModels.size();
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
                convertView = View.inflate(getActivity().getApplicationContext(),R.layout.handling_unit_list_view,null);
                holder = new ViewHolder();
                holder.location = (TextView) convertView.findViewById(R.id.location);
                holder.sku_barcode = (TextView) convertView.findViewById(R.id.sku_barcode);
                holder.operated_at = (TextView) convertView.findViewById(R.id.operated_at);
                holder.handling_unit_number = (TextView) convertView.findViewById(R.id.list_view_handling_unit_number);
                holder.qty = (TextView) convertView.findViewById(R.id.qty);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            String operated_at = handlingUnitModels.get(position).mOperatedAt;
            String operator = handlingUnitModels.get(position).mOperator;

            holder.location.setText(handlingUnitModels.get(position).mLocation);
            holder.sku_barcode.setText(handlingUnitModels.get(position).mSkuBarcode);
            holder.operated_at.setText(operated_at+" ("+operator+")");
            holder.qty.setText(handlingUnitModels.get(position).mQty);
            holder.handling_unit_number.setText(handlingUnitModels.get(position).mQty);
            return convertView;
        }

    }

    static class ViewHolder{
        TextView operator;
        TextView handling_unit_number;
        TextView qty;
        TextView sku_barcode;
        TextView location;
        TextView operated_at;
    }
}
