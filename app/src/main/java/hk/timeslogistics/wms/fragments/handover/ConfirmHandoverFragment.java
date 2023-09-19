package hk.timeslogistics.wms.fragments.handover;

import android.annotation.SuppressLint;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConfirmHandoverFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private int scanFrom;
    private static final int SCAN_FROM_HANDOVER_NUMBER = 1;

    protected Button buttonSubmit;
    protected Button buttonCancel;

    private CredentialManager credentialManager;
    private ProgressDialog mProgressDialog;

    private EditText editTextHandoverNumber;
    private TextView textViewHandoverNumber;
    private TextView textViewLogisticsProviderAgent;
    private TextView textViewHandoverTotalPallet;
    private TextView textViewHandoverTotalShipmentQty;
    private TextView textViewHandoverTotalWeight;
    private CheckBox checkboxIsConfirmHandover;
    private TextView textViewConfirmHandoverRemark;
    private ImageButton buttonScanHandoverNumber;

    private SubmitTask submitTask;
    private int enable_confirm_handover = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_confirm_handover, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.confirm_handover);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);

        editTextHandoverNumber = (EditText) root.findViewById(R.id.editTextHandoverNumber);
        textViewHandoverNumber = (TextView) root.findViewById(R.id.textViewHandoverNumber);
        textViewHandoverTotalPallet = (TextView) root.findViewById(R.id.textViewHandoverTotalPallet);
        textViewLogisticsProviderAgent = (TextView) root.findViewById(R.id.textViewLogisticsProviderAgent);
        textViewHandoverTotalPallet = (TextView) root.findViewById(R.id.textViewHandoverTotalPallet);
        textViewHandoverTotalShipmentQty = (TextView) root.findViewById(R.id.textViewHandoverTotalShipmentQty);
        textViewHandoverTotalWeight = (TextView) root.findViewById(R.id.textViewHandoverTotalWeight);
        textViewConfirmHandoverRemark = (TextView) root.findViewById(R.id.textViewConfirmHandoverRemark);

        checkboxIsConfirmHandover = (CheckBox) root.findViewById(R.id.checkboxIsConfirmHandover);
        buttonScanHandoverNumber = (ImageButton) root.findViewById(R.id.buttonScanHandoverNumber);

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

        editTextHandoverNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getInfo();
                    return true;
                }
                return false;
            }
        });
        buttonScanHandoverNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanFrom = SCAN_FROM_HANDOVER_NUMBER;
                IntentIntegrator.forSupportFragment(ConfirmHandoverFragment.this).initiateScan();
            }
        });
        checkboxIsConfirmHandover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }

        });
        editTextHandoverNumber.requestFocus();
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == ConfirmHandoverFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_HANDOVER_NUMBER){
                        editTextHandoverNumber.setText(result.getContents());
                        getInfo();
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == ConfirmHandoverFragment.RESULT_OK) {
                cancel();
            }
        }
    }
    private void resetFields() {
        editTextHandoverNumber.setText("");
        textViewHandoverTotalWeight.setText("");
        textViewHandoverTotalShipmentQty.setText("");
        textViewHandoverTotalPallet.setText("");
        textViewHandoverNumber.setText("");
        textViewLogisticsProviderAgent.setText("");
        textViewConfirmHandoverRemark.setText("");
        checkboxIsConfirmHandover.setChecked(false);
        textViewConfirmHandoverRemark.setVisibility(View.GONE);
        checkboxIsConfirmHandover.setVisibility(View.GONE);
    }
    private void getInfo(){
        setFieldRequestFocus();
        enable_confirm_handover = 0;
        String handover_number = editTextHandoverNumber.getText().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("handover_number", handover_number);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("get-info"),"get-info");
        submitTask.execute();
    }
    private void submit() {
        String handover_number = textViewHandoverNumber.getText().toString();
        if(handover_number.isEmpty()){
            Toast.makeText(getContext(), getString(R.string.handover_number_is_required), Toast.LENGTH_LONG).show();
            return;
        }
        if(enable_confirm_handover == 0){
            return;
        }

        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("handover_number", handover_number);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("submit"),"submit");
        submitTask.execute();
    }

    private void processResponse(RemoteResult result,String operation){
        TomProgress.showProgress(getContext(),false);
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "submit":
                processSubmit(result);
                break;
            case "get-info":
                processGetInfo(result);
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

    @SuppressLint("SetTextI18n")
    private void processGetInfo(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            textViewHandoverNumber.setText(result.getData().get("handover_number").getAsString());
            textViewLogisticsProviderAgent.setText(result.getData().get("logistics_provider").getAsString());
            textViewHandoverTotalPallet.setText(result.getData().get("total_pallets").getAsString());
            textViewHandoverTotalShipmentQty.setText(result.getData().get("total_nums").getAsString());
            textViewHandoverTotalWeight.setText(result.getData().get("total_weight").getAsString() + "(KG)");
            if(result.getData().get("enable_confirm_handover").getAsString().equals("1")) {
                checkboxIsConfirmHandover.setVisibility(View.VISIBLE);
                textViewConfirmHandoverRemark.setVisibility(View.GONE);
                enable_confirm_handover = result.getData().get("enable_confirm_handover").getAsInt();
            } else{
                checkboxIsConfirmHandover.setVisibility(View.GONE);
                textViewConfirmHandoverRemark.setVisibility(View.VISIBLE);
                textViewConfirmHandoverRemark.setText(result.getData().get("confirm_handover_remark").getAsString());
            }
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_SHORT).show();
        }
        editTextHandoverNumber.setText("");
        editTextHandoverNumber.requestFocus();
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

    private void setFieldRequestFocus(){
        String handover_number = editTextHandoverNumber.getText().toString();
        if(handover_number.isEmpty()){
            editTextHandoverNumber.requestFocus();
            return;
        }
    }

    private void cancel() {
        resetFields();
    }

    private String urlMapping(String operation){
        switch (operation){
            case "submit":
                return credentialManager.getApiBase() + "handover/confirm";
            case "get-info":
                return credentialManager.getApiBase() + "handover/get-info";
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

}
