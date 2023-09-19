package hk.timeslogistics.wms.fragments.pick_wave;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.R;;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class SecondSortFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private static final int SCAN_FROM_SKU_BARCODE = 1;
    private static final int SCAN_FROM_PICK_WAVE_NUMBER = 2;
    private static final int SCAN_FROM_NUMBER_PLATE = 3;
    private static final int SCAN_FROM_ORDER_NUMBER = 4;
    private int scanFrom;
    private CredentialManager credentialManager;
    private EditText editTextSkuBarcode;
    private EditText editTextPickWaveNumber;
    private EditText editTextNumberPlate;
    private EditText editTextOrderNumber;
    private LinearLayout toteCodeLinear;
    private TextView textViewTote;
    private ProgressDialog mProgressDialog;
    private ImageButton buttonScanPickWaveNumber;
    private ImageButton buttonScanSkuBarcode;
    private ImageButton buttonScanNumberPlate;
    private ImageButton buttonScanOrderNumber;
    protected Button buttonCompleteSorting;
    protected Button buttonClear;
    private HttpSubmitTask submitTask;
    private RemoteResult result = null;
    private Thread requestThread = null;
    private String pick_wave_number = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("pick_wave_detail");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_second_sort, null);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.second_sort);

        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextSkuBarcode = (EditText) root.findViewById(R.id.editTextSkuBarcode);
        editTextPickWaveNumber = (EditText) root.findViewById(R.id.editTextPickWaveNumber);
        editTextNumberPlate = (EditText) root.findViewById(R.id.editTextNumberPlate);
        editTextOrderNumber = (EditText) root.findViewById(R.id.editTextOrderNumber);
        toteCodeLinear = (LinearLayout) root.findViewById(R.id.toteCodeLinear);
        textViewTote = (TextView) root.findViewById(R.id.textViewTote);
        editTextPickWaveNumber.setShowSoftInputOnFocus(false);
        editTextNumberPlate.setShowSoftInputOnFocus(false);
        editTextOrderNumber.setShowSoftInputOnFocus(false);
        editTextSkuBarcode.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextPickWaveNumber);
        ClearEditTextContent.setupClearEditText(getContext(),editTextNumberPlate);
        ClearEditTextContent.setupClearEditText(getContext(),editTextOrderNumber);
        ClearEditTextContent.setupClearEditText(getContext(),editTextSkuBarcode);
        buttonCompleteSorting = (Button) root.findViewById(R.id.buttonCompleteSorting);
        buttonClear = (Button) root.findViewById(R.id.buttonClear);
        buttonScanPickWaveNumber = (ImageButton) root.findViewById(R.id.buttonScanPickWaveNumber);
        buttonScanSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanSkuBarcode);
        buttonScanNumberPlate = (ImageButton) root.findViewById(R.id.buttonScanNumberPlate);
        buttonScanOrderNumber = (ImageButton) root.findViewById(R.id.buttonScanOrderNumber);

        buttonScanSkuBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_SKU_BARCODE;
                IntentIntegrator.forSupportFragment(SecondSortFragment.this).initiateScan();
            }
        });
        buttonScanPickWaveNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_PICK_WAVE_NUMBER;
                IntentIntegrator.forSupportFragment(SecondSortFragment.this).initiateScan();
            }
        });
        buttonScanNumberPlate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_NUMBER_PLATE;
                IntentIntegrator.forSupportFragment(SecondSortFragment.this).initiateScan();
            }
        });

        buttonScanOrderNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_ORDER_NUMBER;
                IntentIntegrator.forSupportFragment(SecondSortFragment.this).initiateScan();
            }
        });

        editTextPickWaveNumber.requestFocus();

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });

        buttonCompleteSorting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pick_wave_number = editTextPickWaveNumber.getText().toString();
                if(pick_wave_number.isEmpty()){
                    editTextPickWaveNumber.setFocusableInTouchMode(true);
                    editTextPickWaveNumber.setText("");
                    editTextPickWaveNumber.requestFocus();
                    return;
                }
                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle(R.string.button_complete_sorting)
                        .setMessage(R.string.complete_sorting_tips)//内容
                        .setCancelable(true)
                        .setNeutralButton(getString(R.string.button_complete_sorting), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                completeSorting();
                            }
                        }).setPositiveButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                            }
                }).create();
                alertDialog.show();
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

        editTextPickWaveNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    setFieldRequestFocus();
                    return true;
                }
                return false;
            }
        });
        editTextNumberPlate.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    setFieldRequestFocus();
                    return true;
                }
                return false;
            }
        });
        editTextOrderNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    assignOrderLabel();
                    return true;
                }
                return false;
            }
        });

        if (getArguments() != null) {
            pick_wave_number = getArguments().getString("pick_wave_number");
            editTextPickWaveNumber.setText(pick_wave_number);
            editTextPickWaveNumber.setFocusableInTouchMode(false);
            editTextNumberPlate.requestFocus();
        }else{
            editTextPickWaveNumber.requestFocus();
        }

        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = MainActivity.pickWaveFragment;
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
                    fragment = new PickWaveFragment();
                    transaction.add(R.id.flContent, fragment);
                    transaction.hide(MainActivity.currentFragment).show(fragment).commit();
                }
                MainActivity.currentFragment = fragment;
                MainActivity.pickWaveFragment = fragment;
            }
        });

        return root;
    }

    private void setFieldRequestFocus(){
        String pick_wave_number = editTextPickWaveNumber.getText().toString();
        String number_plate = editTextNumberPlate.getText().toString();
        String sku_barcode = editTextSkuBarcode.getText().toString();
        if(pick_wave_number.isEmpty()){
            editTextPickWaveNumber.requestFocus();
            return;
        }
        editTextPickWaveNumber.setFocusable(false);
        if(number_plate.isEmpty()){
            editTextNumberPlate.requestFocus();
            return;
        }
        //editTextNumberPlate.setFocusable(false);
        if(sku_barcode.isEmpty()){
            editTextSkuBarcode.requestFocus();
            return;
        }
        submit();
    }

    private void resetFields() {
        textViewTote.setText("");
        editTextNumberPlate.setText("");
        //editTextNumberPlate.setFocusableInTouchMode(true);
        editTextPickWaveNumber.setText("");
        editTextPickWaveNumber.setFocusableInTouchMode(true);
        editTextSkuBarcode.setText("");
        editTextOrderNumber.setText("");
        editTextPickWaveNumber.requestFocus();
    }
    private void submit() {
        textViewTote.setText("");
        toteCodeLinear.setBackgroundResource(R.color.colorNormal);
        FormBody.Builder builder = new FormBody.Builder();
        String pick_wave_number = editTextPickWaveNumber.getText().toString();
        String number_plate = editTextNumberPlate.getText().toString();
        String sku_barcode = editTextSkuBarcode.getText().toString();
        builder.add("pick_wave_number", pick_wave_number);
        builder.add("number_plate", number_plate);
        builder.add("sku_barcode", sku_barcode);
        RequestBody formBody = builder.build();
        TomProgress.showProgress(getContext(),true);
        String url = urlMapping("scan");
        String operation = "scan";
        submitTask = new SubmitTask(getContext(),formBody,url,operation);
        submitTask.execute();
        /*requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                submitTask = new HttpSubmitTask(getContext(),formBody,credentialManager.getAccessToken(),url,mProgressDialog);
                submitTask.execute();
                try {
                    IS_SUBMIT = 1;
                    result = submitTask.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        requestThread.start();
        try{
            requestThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            editTextSkuBarcode.setText("");
            editTextSkuBarcode.requestFocus();
            System.out.println(result.getData());
        }*/
    }

    private void completeSorting(){
        textViewTote.setText("");
        toteCodeLinear.setBackgroundResource(R.color.colorNormal);
        FormBody.Builder builder = new FormBody.Builder();
        String pick_wave_number = editTextPickWaveNumber.getText().toString();
        builder.add("pick_wave_number", pick_wave_number);
        RequestBody formBody = builder.build();
        TomProgress.showProgress(getContext(),true);
        String url = urlMapping("complete");
        String operation = "complete";
        submitTask = new SubmitTask(getContext(),formBody,url,operation);
        submitTask.execute();
    }

    private void assignOrderLabel(){
        textViewTote.setText("");
        toteCodeLinear.setBackgroundResource(R.color.colorNormal);
        FormBody.Builder builder = new FormBody.Builder();
        String order_number = editTextOrderNumber.getText().toString();
        String pick_wave_number = editTextPickWaveNumber.getText().toString();
        builder.add("pick_wave_number", pick_wave_number);
        builder.add("order_number", order_number);
        RequestBody formBody = builder.build();
        TomProgress.showProgress(getContext(),true);
        String operation = "assign_order_label";
        String url = urlMapping(operation);
        submitTask = new SubmitTask(getContext(),formBody,url,operation);
        submitTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == SecondSortFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_SKU_BARCODE){
                        editTextSkuBarcode.setText(result.getContents());
                        setFieldRequestFocus();
                    }else if(scanFrom == SCAN_FROM_PICK_WAVE_NUMBER){
                        editTextPickWaveNumber.setText(result.getContents());
                        setFieldRequestFocus();
                    }else if(scanFrom == SCAN_FROM_NUMBER_PLATE){
                        editTextNumberPlate.setText(result.getContents());
                        setFieldRequestFocus();
                    }else if(scanFrom == SCAN_FROM_ORDER_NUMBER){
                        editTextOrderNumber.setText(result.getContents());
                    }
                    return;
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == SecondSortFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void cancel() {}

    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        TomProgress.showProgress(getContext(),false);
        switch (operation){
            case "scan":
                editTextSkuBarcode.setText("");
                editTextSkuBarcode.requestFocus();
                textViewTote.setText(result.getData().get("tote").getAsString());
                break;
            case "assign_order_label":
                toteCodeLinear.setBackgroundResource(R.color.colorFailed);
                editTextOrderNumber.setText("");
                editTextOrderNumber.requestFocus();
                textViewTote.setTextSize(1,50);
                textViewTote.setText(result.getData().get("tote").getAsString());
                break;
            case "complete":
                resetFields();
                break;
            default:
                break;
        }
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.success(getContext());
            toteCodeLinear.setBackgroundResource(R.color.colorSuccess);
        } else {
            AlertManager.error(getContext());
            toteCodeLinear.setBackgroundResource(R.color.colorFailed);
            textViewTote.setText(result.getPayload());
            textViewTote.setTextSize(1,30);
        }
    }
    private class SubmitTask extends HttpSubmitTask {
        private final String mOperation;
        public SubmitTask(Context context, RequestBody formBody, String url,String operation) {
            super(context, formBody, url);
            mOperation = operation;

        }
        protected void onPostExecute(final RemoteResult result) {
            submitTask = null;
            processResponse(result,mOperation);
        }
    }

    private String urlMapping(String operation){
        switch (operation){
            case "scan":
                return credentialManager.getApiBase() + "sorting/scan";
            case "assign_order_label":
                return credentialManager.getApiBase() + "sorting/assign-order-label";
            case "complete":
                return credentialManager.getApiBase() + "sorting/complete";
            default:
                return "";
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }
}

