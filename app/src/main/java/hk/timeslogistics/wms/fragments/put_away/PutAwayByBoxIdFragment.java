package hk.timeslogistics.wms.fragments.put_away;

import static hk.timeslogistics.wms.MainActivity.asnItemFragment;
import static hk.timeslogistics.wms.MainActivity.currentFragment;
import static hk.timeslogistics.wms.MainActivity.inboundFragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.fragments.asn.AsnItemFragment;
import hk.timeslogistics.wms.fragments.asn.AsnListFragment;
import hk.timeslogistics.wms.fragments.inbound.InboundFragment;
import hk.timeslogistics.wms.fragments.inventory.InventoryFragment;
import hk.timeslogistics.wms.models.InventoryModel;
import hk.timeslogistics.wms.models.PutAwayModel;
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

public class PutAwayByBoxIdFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;

    private static final int RESULT_OK = -1;

    private static final int SCAN_FROM_TO_LOCATION = 3;
    private int scanFrom;

    protected Button buttonSubmit;
    protected Button buttonCancel;

    private CredentialManager credentialManager;
    private ProgressDialog mProgressDialog;
    private ImageButton buttonScanBoxId;
    private ImageButton buttonScanToLocation;
    private EditText editTextBoxId;
    private EditText editTextToLocation;
    private Spinner spinnerCondition;
    ArrayList<PutAwayModel> putAwayModels;
    ListView listView;
    private SubmitTask submitTask;

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
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_put_away_by_box_id, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.put_away);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        buttonScanBoxId = (ImageButton) root.findViewById(R.id.buttonScanBoxId);
        buttonScanToLocation = (ImageButton) root.findViewById(R.id.buttonScanToLocation);
        editTextToLocation = (EditText) root.findViewById(R.id.editTextToLocation);
        editTextBoxId = (EditText) root.findViewById(R.id.editTextBoxId);
        editTextBoxId.setShowSoftInputOnFocus(false);
        editTextToLocation.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextBoxId);
        ClearEditTextContent.setupClearEditText(getContext(),editTextToLocation);


        spinnerCondition = (Spinner) root.findViewById(R.id.spinnerCondition);

        spinnerCondition = (Spinner) root.findViewById(R.id.spinnerCondition);

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
                submit();
            }
        });


        buttonScanToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_TO_LOCATION;
                IntentIntegrator.forSupportFragment(PutAwayByBoxIdFragment.this).initiateScan();
            }
        });

        editTextBoxId.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getDetail();
                    return true;
                }
                return false;
            }
        });
        editTextBoxId.requestFocus();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == PutAwayByBoxIdFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_TO_LOCATION){
                        editTextToLocation.setText(result.getContents());
                    }else {
                        editTextBoxId.setText(result.getContents());
                        getDetail();
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == PutAwayByBoxIdFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void resetFields() {
        editTextToLocation.setText("");
        editTextBoxId.setText("");
        editTextBoxId.requestFocus();
    }

    private void getDetail() {
        String boxid = editTextBoxId.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("boxid", boxid);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("boxid"),"boxid");
        submitTask.execute();
    }

    private void submit() {
        String to_location = editTextToLocation.getText().toString();
        String boxid = editTextBoxId.getText().toString();
        String condition = spinnerCondition.getSelectedItem().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("to_location", to_location);
        builder.add("condition", condition);
        builder.add("boxid", boxid);
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
            case "boxid":
                processDetails(result);
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

    private void processDetails(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            putAwayModels.clear();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                PutAwayModel putAwayModel = new PutAwayModel(item);
                putAwayModels.add(putAwayModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.put_away_by_box_id_list_view);
            DetailAdapter detailAdapter = new DetailAdapter();
            listView.setAdapter(detailAdapter);
            editTextToLocation.requestFocus();
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }
    private void processSubmit(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            resetFields();
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
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
                return credentialManager.getApiBase() + "put-away/by-boxid";
            case "boxid":
                return credentialManager.getApiBase() + "put-away/details";
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

    public class DetailAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return putAwayModels.size();
        }
        @Override
        public Object getItem(int position) {
            return putAwayModels.get(position);
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
                convertView = View.inflate(getActivity().getApplicationContext(),R.layout.put_away_by_boxid_list_view,null);
                holder = new ViewHolder();
                holder.sku_barcode = (TextView) convertView.findViewById(R.id.sku_barcode);
                holder.sku_name = (TextView) convertView.findViewById(R.id.sku_name);
                holder.qty = (TextView) convertView.findViewById(R.id.qty);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.sku_name.setText(putAwayModels.get(position).sku_name);
            holder.sku_barcode.setText(putAwayModels.get(position).sku_barcode);
            holder.qty.setText(putAwayModels.get(position).qty);
            return convertView;
        }

    }

    static class ViewHolder{
        TextView sku_name;
        TextView sku_barcode;
        TextView qty;
    }
}
