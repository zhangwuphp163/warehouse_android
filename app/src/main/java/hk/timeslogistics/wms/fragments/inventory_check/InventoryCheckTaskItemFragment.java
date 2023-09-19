package hk.timeslogistics.wms.fragments.inventory_check;

import static android.widget.AdapterView.OnClickListener;
import static android.widget.AdapterView.OnKeyListener;

import android.annotation.SuppressLint;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Locale;

import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.InventoryCheckTaskItemModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomProgress;
import hk.timeslogistics.wms.utils.UrlMapping;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class InventoryCheckTaskItemFragment extends Fragment{
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextCycleCountBinCode;
    private EditText editTextCycleCountSkuBarcode;
    private EditText editTextCycleCountScanQty;
    private ProgressDialog mProgressDialog;
    private Button buttonSubmit;
    private String selectedPickWaveNumber = "";
    private ImageButton buttonScanCycleCountSkuBarcode;
    private ImageButton buttonScanCycleCountBinCode;

    private SubmitTask submitTask;
    private ListView inventoryCheckTaskItemListView;
    private String id;
    private String cycleCountCode;
    private String scanBy = "single";
    private int scanFrom;
    private static final int SCAN_FROM_BIN_CODE = 1;
    private static final int SCAN_FROM_SKU_BARCODE = 2;

    ListView listView;
    ArrayList<InventoryCheckTaskItemModel> inventoryCheckTaskItemModels;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("inventory_check_task_item");
        inventoryCheckTaskItemModels = new ArrayList<>();

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_inventory_check_task_item, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.inventory_check_scan);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextCycleCountBinCode = (EditText) root.findViewById(R.id.editTextCycleCountBinCode);
        editTextCycleCountSkuBarcode = (EditText) root.findViewById(R.id.editTextCycleCountSkuBarcode);
        editTextCycleCountBinCode.setShowSoftInputOnFocus(false);
        editTextCycleCountSkuBarcode.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextCycleCountBinCode);
        ClearEditTextContent.setupClearEditText(getContext(),editTextCycleCountSkuBarcode);
        editTextCycleCountScanQty = (EditText) root.findViewById(R.id.editTextCycleCountScanQty);
        buttonScanCycleCountSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanCycleCountSkuBarcode);
        buttonScanCycleCountBinCode = (ImageButton) root.findViewById(R.id.buttonScanCycleCountBinCode);
        inventoryCheckTaskItemListView = (ListView) root.findViewById(R.id.inventory_check_task_item_list_view);
        buttonSubmit = (Button) root.findViewById(R.id.buttonSubmit);

        if(getArguments() != null){
            id = getArguments().getString("id");
            cycleCountCode = getArguments().getString("cycle_count_code");
            scanBy = getArguments().getString("scan_by");
            getList(id);
        }

        buttonScanCycleCountSkuBarcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_SKU_BARCODE;
                IntentIntegrator.forSupportFragment(InventoryCheckTaskItemFragment.this).initiateScan();
            }
        });
        buttonScanCycleCountBinCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFrom = SCAN_FROM_BIN_CODE;
                IntentIntegrator.forSupportFragment(InventoryCheckTaskItemFragment.this).initiateScan();
            }
        });

        editTextCycleCountBinCode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    editTextCycleCountSkuBarcode.setText("");
                    if(scanBy.equals("batch")){
                        editTextCycleCountScanQty.setText("");
                    }
                    getList(id);
                    editTextCycleCountSkuBarcode.requestFocus();
                    return true;
                }
                return false;
            }
        });
        editTextCycleCountSkuBarcode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String bin_code = editTextCycleCountBinCode.getText().toString();
                    if(bin_code.isEmpty()){
                        editTextCycleCountSkuBarcode.setText("");
                        editTextCycleCountBinCode.requestFocus();
                        return true;
                    }
                    String qty = editTextCycleCountScanQty.getText().toString();
                    if(qty.isEmpty()){
                        editTextCycleCountScanQty.requestFocus();
                        return true;
                    }
                    //submit scan
                    scanSubmit(id);
                    return true;
                }
                return false;
            }
        });
        editTextCycleCountScanQty.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String bin_code = editTextCycleCountBinCode.getText().toString();
                    if(bin_code.isEmpty()){
                        editTextCycleCountSkuBarcode.setText("");
                        editTextCycleCountBinCode.requestFocus();
                        return true;
                    }
                    String sku_barcode = editTextCycleCountSkuBarcode.getText().toString();
                    if(sku_barcode.isEmpty()){
                        editTextCycleCountSkuBarcode.setText("");
                        editTextCycleCountSkuBarcode.requestFocus();
                        return true;
                    }
                    scanSubmit(id);
                    return true;
                }
                return false;
            }
        });
        buttonSubmit.setOnClickListener(view -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.confirm_complete)
                    .setMessage(R.string.complete_task_check_tips)//内容
                    .setCancelable(true)
                    .setNeutralButton(getString(R.string.confirm_complete), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            completed(id);
                        }
                    }).setPositiveButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                        }}).create();
            alertDialog.show();
        });
        editTextCycleCountBinCode.requestFocus();
        if(scanBy.equals("single")){
            editTextCycleCountScanQty.setText("1");
            editTextCycleCountScanQty.setFocusableInTouchMode(false);
        }
        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = MainActivity.inventoryCheckTaskFragment;
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if(!fragment.isAdded()){
                    Bundle args = new Bundle();
                    args.putString("cycle_count_code", cycleCountCode);
                    fragment.setArguments(args);
                    if(MainActivity.currentFragment != null){
                        transaction.hide(MainActivity.currentFragment);
                    }
                    transaction.add(R.id.flContent, fragment).commit();
                }else{
                    transaction.remove(fragment);
                    fragment = new InventoryCheckTaskFragment();
                    Bundle args = new Bundle();
                    args.putString("cycle_count_code", cycleCountCode);
                    fragment.setArguments(args);
                    transaction.add(R.id.flContent, fragment);
                    transaction.hide(MainActivity.currentFragment).show(fragment).commit();
                }
                MainActivity.currentFragment = fragment;
                MainActivity.inventoryCheckTaskFragment = fragment;
            }
        });

        return root;
    }

    private void completed(String id) {
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("id", id);
        RequestBody formBody = builder.build();
        submitTask = new InventoryCheckTaskItemFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"inventory-check-task-completed"),"inventory-check-task-completed");
        submitTask.execute();
    }
    private void getList(String id) {
        String bin_code = editTextCycleCountBinCode.getText().toString();
        String language = Locale.getDefault().getLanguage();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("id", id);
        builder.add("bin_code", bin_code);
        builder.add("language", language);
        RequestBody formBody = builder.build();
        submitTask = new InventoryCheckTaskItemFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"inventory-check-task-items"),"inventory-check-task-items");
        submitTask.execute();
    }
    private void scanSubmit(String id) {
        String bin_code = editTextCycleCountBinCode.getText().toString();
        String sku_barcode = editTextCycleCountSkuBarcode.getText().toString();
        String qty = editTextCycleCountScanQty.getText().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("id", id);
        builder.add("bin_code", bin_code);
        builder.add("barcode", sku_barcode);
        builder.add("qty", qty);
        RequestBody formBody = builder.build();
        submitTask = new InventoryCheckTaskItemFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"inventory-check-task-scan"),"inventory-check-task-scan");
        submitTask.execute();
    }

    private void recounting(String id,String sku_id,String bin_id) {
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("id", id);
        builder.add("bin_id", bin_id);
        builder.add("sku_id", sku_id);
        RequestBody formBody = builder.build();
        submitTask = new InventoryCheckTaskItemFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"inventory-check-task-recounting"),"inventory-check-task-recounting");
        submitTask.execute();
    }

    private void processInventoryCheckTaskItems(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            inventoryCheckTaskItemModels.clear();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                InventoryCheckTaskItemModel inventoryCheckTaskItemModel = new InventoryCheckTaskItemModel(item);
                inventoryCheckTaskItemModels.add(inventoryCheckTaskItemModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.inventory_check_task_item_list_view);
            InventoryCheckTaskItemAdapter inventoryCheckTaskItemAdapter = new InventoryCheckTaskItemAdapter();
            listView.setAdapter(inventoryCheckTaskItemAdapter);
        } else {
            editTextCycleCountBinCode.setText("");
            editTextCycleCountBinCode.requestFocus();
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    private void processInventoryCheckTaskScan(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
            resetFields();
            getList(id);
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    private void resetFields(){
        editTextCycleCountSkuBarcode.setText("");
        editTextCycleCountSkuBarcode.requestFocus();
        if(scanBy.equals("batch")){
            editTextCycleCountScanQty.setText("");
        }
    }

    private void processInventoryCheckTaskRecounting(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
            editTextCycleCountBinCode.setText("");
            editTextCycleCountSkuBarcode.setText("");
            if(scanBy.equals("batch")){
                editTextCycleCountScanQty.setText("");
            }
            editTextCycleCountBinCode.requestFocus();
            getList(id);
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    private void processInventoryCheckTaskCompleted(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            String next_id = result.getData().get("next_id").getAsString();
            if(next_id.equals("0")){
                AlertManager.error(getContext());
                Toast.makeText(getContext(), "All cycle count task is completed!", Toast.LENGTH_LONG).show();
                return;
            }else {
                AlertManager.okay(getContext());
                Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
                resetFields();
                id = next_id;
                getList(id);
            }

        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == InventoryCheckTaskItemFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_BIN_CODE){
                        editTextCycleCountBinCode.setText(result.getContents());
                        editTextCycleCountSkuBarcode.setText("");
                        if(scanBy.equals("batch")){
                            editTextCycleCountScanQty.setText("");
                        }
                        getList(id);
                        editTextCycleCountSkuBarcode.requestFocus();
                    }else if(scanFrom == SCAN_FROM_SKU_BARCODE){
                        editTextCycleCountSkuBarcode.setText(result.getContents());
                        String bin_code = editTextCycleCountBinCode.getText().toString();
                        if(bin_code.isEmpty()){
                            editTextCycleCountSkuBarcode.setText("");
                            editTextCycleCountBinCode.requestFocus();
                            return;
                        }
                        String qty = editTextCycleCountScanQty.getText().toString();
                        if(qty.isEmpty()){
                            editTextCycleCountScanQty.requestFocus();
                            return;
                        }
                        //submit scan
                        scanSubmit(id);
                        return;
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == InventoryCheckTaskItemFragment.RESULT_OK) {

            }
        }
    }

    public class InventoryCheckTaskItemAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return inventoryCheckTaskItemModels.size();
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
            @SuppressLint("ViewHolder") View v = View.inflate(getActivity().getApplicationContext(),R.layout.inventory_check_task_item_list_view,null);
            String sku_barcode = inventoryCheckTaskItemModels.get(position).skuBarcode;
            String zone_area_bin_code = inventoryCheckTaskItemModels.get(position).zone_area_bin_code;
            String scanned_qty = inventoryCheckTaskItemModels.get(position).scanned_qty;
            String inventory_qty = inventoryCheckTaskItemModels.get(position).inventory_qty;
            String count_qty = inventoryCheckTaskItemModels.get(position).count_qty;
            LinearLayout taskItemDetail = (LinearLayout) v.findViewById(R.id.task_item_detail);
            TextView textViewSkuBarcode = (TextView) v.findViewById(R.id.sku_barcode);
            TextView textViewBinCode = (TextView) v.findViewById(R.id.bin_code);
            TextView textViewScannedQty = (TextView) v.findViewById(R.id.scanned_qty);
            TextView textViewCountQty = (TextView) v.findViewById(R.id.count_qty);
            TextView textViewInventoryQty = (TextView) v.findViewById(R.id.inventory_qty);
            textViewSkuBarcode.setText(sku_barcode);
            textViewBinCode.setText(zone_area_bin_code);
            textViewScannedQty.setText(scanned_qty);
            textViewCountQty.setText(count_qty);
            textViewInventoryQty.setText(inventory_qty);
//#FF8C00
            int scanned_qty_int = Integer.parseInt(scanned_qty);
            int count_qty_int = Integer.parseInt(count_qty);
            if(scanned_qty_int == count_qty_int){
                taskItemDetail.setBackgroundResource(R.color.colorSuccess);
            }else if(scanned_qty_int > count_qty_int){
                taskItemDetail.setBackgroundResource(R.color.colorBlue);
            }else{
                taskItemDetail.setBackgroundResource(R.color.colorPartialSuccess);
            }
            Button button = (Button) v.findViewById(R.id.recounting);
            View.OnClickListener listener = new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.recounting)
                            .setCancelable(true)
                            .setNeutralButton(getString(R.string.button_submit), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    recounting(id,inventoryCheckTaskItemModels.get(position).sku_id,inventoryCheckTaskItemModels.get(position).bin_id);
                                }
                            }).setPositiveButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    dialogInterface.dismiss();
                                }}).create();

                    alertDialog.show();
                }
            };
            button.setOnClickListener(listener);
            return v;
        }

    }

    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "inventory-check-task-items":
                processInventoryCheckTaskItems(result);
                break;
            case "inventory-check-task-scan":
                TomProgress.showProgress(getContext(),false);
                processInventoryCheckTaskScan(result);
                break;
            case "inventory-check-task-recounting":
                TomProgress.showProgress(getContext(),false);
                processInventoryCheckTaskRecounting(result);
                break;
            case "inventory-check-task-completed":
                TomProgress.showProgress(getContext(),false);
                processInventoryCheckTaskCompleted(result);
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

