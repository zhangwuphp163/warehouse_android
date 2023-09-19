package hk.timeslogistics.wms.fragments.pick_wave;

import android.annotation.SuppressLint;
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
import java.util.Objects;
import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.PickWaveDetailModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomAlertDialog;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class PickWaveDetailFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextLocation;
    private EditText editTextSkuBarcode;
    private EditText editTextPickWaveNumber;
    private EditText editTextZoneNumber;
    private EditText editTextBoxId;

    private String is_scan_box_id = "0";

    private static final int SCAN_FROM_PICK_WAVE_NUMBER = 1;
    private static final int SCAN_FROM_LOCATION = 2;
    private static final int SCAN_FROM_SKU_BARCODE = 3;
    private static final int SCAN_FROM_ZONE_NUMBER = 4;
    private static final int SCAN_FROM_BOX_ID = 5;
    private int scanFrom;

    protected Button buttonClearIcon;
    protected Button buttonClearLocationIcon;

    private View editTextSkipQtyView;
    private View editTextTakeDownQtyView;

    private SubmitTask submitTask;

    ListView listView;
    ArrayList<PickWaveDetailModel> pickWaveDetails;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        pickWaveDetails = new ArrayList<>();
        credentialManager.setFragmentIndex("pick_wave_detail");
    }

    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams") ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_pick_wave_detail, null);

        editTextSkipQtyView = (View) inflater.inflate(R.layout.edittext_skip_qty,null);
        editTextTakeDownQtyView = (View) inflater.inflate(R.layout.edittext_take_down_qty,null);

        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(R.string.picking);

        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextLocation = (EditText) root.findViewById(R.id.editTextLocation);
        editTextSkuBarcode = (EditText) root.findViewById(R.id.editTextSkuBarcode);
        editTextPickWaveNumber = (EditText) root.findViewById(R.id.editTextPickWaveNumber);
        editTextZoneNumber = (EditText) root.findViewById(R.id.editTextZoneNumber);
        editTextBoxId = (EditText) root.findViewById(R.id.editTextBoxId);



        editTextBoxId.setShowSoftInputOnFocus(false);
        editTextLocation.setShowSoftInputOnFocus(false);
        editTextSkuBarcode.setShowSoftInputOnFocus(false);
        editTextPickWaveNumber.setShowSoftInputOnFocus(false);
        editTextZoneNumber.setShowSoftInputOnFocus(false);

        ClearEditTextContent.setupClearEditText(getContext(),editTextBoxId);
        ClearEditTextContent.setupClearEditText(getContext(),editTextLocation);
        ClearEditTextContent.setupClearEditText(getContext(),editTextSkuBarcode);
        ClearEditTextContent.setupClearEditText(getContext(),editTextPickWaveNumber);
        ClearEditTextContent.setupClearEditText(getContext(),editTextZoneNumber);

        ImageButton buttonScanPickWaveNumber = (ImageButton) root.findViewById(R.id.buttonScanPickWaveNumber);
        ImageButton buttonScanZoneNumber = (ImageButton) root.findViewById(R.id.buttonScanZoneNumber);
        ImageButton buttonScanLocation = (ImageButton) root.findViewById(R.id.buttonScanLocation);
        ImageButton buttonScanBoxId = (ImageButton) root.findViewById(R.id.buttonScanBoxId);
        ImageButton buttonScanSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanSkuBarcode);

        buttonClearIcon = (Button) root.findViewById(R.id.buttonClearIcon);
        buttonClearLocationIcon = (Button) root.findViewById(R.id.buttonClearLocationIcon);

        //ListView pickWaveDetailListView = (ListView) root.findViewById(R.id.pick_wave_detail_list);

        if (getArguments() != null) {
            String pick_wave_number = getArguments().getString("pick_wave_number");

            editTextPickWaveNumber.setText(pick_wave_number);
            editTextPickWaveNumber.setFocusableInTouchMode(false);
            is_scan_box_id = getArguments().getString("is_scan_box_id");
            String zone_name = getArguments().getString("zone_name");
            editTextZoneNumber.setText(zone_name);
            editTextLocation.requestFocus();
            getPickWaveList();
        }
        if(is_scan_box_id.equals("1")){
            root.findViewById(R.id.linearLayoutBoxid).setVisibility(View.VISIBLE);
            editTextBoxId.requestFocus();
        }else{
            root.findViewById(R.id.linearLayoutBoxid).setVisibility(View.GONE);
            editTextPickWaveNumber.requestFocus();
        }
        buttonClearIcon.setOnClickListener(view -> {
            editTextBoxId.setText("");
            editTextBoxId.requestFocus();
        });
        buttonClearLocationIcon.setOnClickListener(view -> {
            editTextLocation.setText("");
            editTextLocation.requestFocus();
        });

        buttonScanZoneNumber.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_ZONE_NUMBER;
            IntentIntegrator.forSupportFragment(PickWaveDetailFragment.this).initiateScan();
        });
        buttonScanPickWaveNumber.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_PICK_WAVE_NUMBER;
            IntentIntegrator.forSupportFragment(PickWaveDetailFragment.this).initiateScan();
        });
        buttonScanLocation.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_LOCATION;
            IntentIntegrator.forSupportFragment(PickWaveDetailFragment.this).initiateScan();
        });
        buttonScanSkuBarcode.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_SKU_BARCODE;
            IntentIntegrator.forSupportFragment(PickWaveDetailFragment.this).initiateScan();
        });
        buttonScanBoxId.setOnClickListener(v -> {
            scanFrom = SCAN_FROM_BOX_ID;
            IntentIntegrator.forSupportFragment(PickWaveDetailFragment.this).initiateScan();
        });
        editTextBoxId.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                setFieldRequestFocus();
                return true;
            }
            return false;
        });

        editTextLocation.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                setFieldRequestFocus();
                return true;
            }
            return false;
        });
        editTextSkuBarcode.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                setFieldRequestFocus();
                return true;
            }
            return false;
        });

        editTextPickWaveNumber.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                getPickWaveList();
                return true;
            }
            return false;
        });
        editTextZoneNumber.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                getPickWaveList();
                return true;
            }
            return false;
        });

        editTextPickWaveNumber.setSelectAllOnFocus(true);
        editTextLocation.setSelectAllOnFocus(true);
        editTextSkuBarcode.setSelectAllOnFocus(true);

        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(v -> {
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
        });

        return root;
    }

    private void setFieldRequestFocus(){
        String pick_wave_number = editTextPickWaveNumber.getText().toString();
        String location = editTextLocation.getText().toString();
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String boxid = editTextBoxId.getText().toString();
        if(pick_wave_number.isEmpty()){
            editTextPickWaveNumber.requestFocus();
            return;
        }
        if(is_scan_box_id.equals("1")){
            if(boxid.isEmpty()){
                editTextBoxId.requestFocus();
                return;
            }
        }
        if(location.isEmpty()){
            editTextLocation.requestFocus();
            return;
        }
        if(sku_barcode.isEmpty()){
            editTextSkuBarcode.requestFocus();
            return;
        }
        byScanTakeDown(pick_wave_number,location,sku_barcode,boxid);
    }

    /*private void resetFields() {
        editTextPickWaveNumber.setText("");
        editTextPickWaveNumber.setFocusableInTouchMode(true);
        editTextSkuBarcode.setText("");
        editTextSkuBarcode.setText("");
        editTextBoxId.setText("");
        editTextPickWaveNumber.requestFocus();

        pickWaveDetails.clear();
        listView = (ListView) getActivity().findViewById(R.id.pick_wave_detail_list);
        PickWaveDetailAdapter pickWaveDetailAdapter = new PickWaveDetailAdapter();
        listView.setAdapter(pickWaveDetailAdapter);
    }*/

    private void getPickWaveList() {
        String pick_wave_number = editTextPickWaveNumber.getText().toString();
        String zone_name = editTextZoneNumber.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pick_wave_number", pick_wave_number);
        builder.add("zone", zone_name);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("get-pick-wave-list"),"get-pick-wave-list");
        submitTask.execute();
    }

    private void byScanTakeDown(String pick_wave_number,String location,String sku_barcode,String boxid) {
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pick_wave_number", pick_wave_number);
        builder.add("location", location);
        builder.add("sku_barcode", sku_barcode);
        builder.add("boxid", boxid);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("by-scan-take-down"),"by-scan-take-down");
        submitTask.execute();
    }

    private void takeDown(String pick_wave_number,String location,String sku_barcode,String qty,String boxid) {
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pick_wave_number", pick_wave_number);
        builder.add("location", location);
        builder.add("sku_barcode", sku_barcode);
        builder.add("qty", qty);
        builder.add("boxid", boxid);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("take-down"),"take-down");
        submitTask.execute();
    }

    private void skipQty(String pick_wave_number,String location,String sku_barcode,String qty,String boxid) {
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pick_wave_number", pick_wave_number);
        builder.add("location", location);
        builder.add("sku_barcode", sku_barcode);
        builder.add("qty", qty);
        builder.add("boxid", boxid);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("skip-qty"),"skip-qty");
        submitTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == PickWaveDetailFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if(scanFrom == SCAN_FROM_PICK_WAVE_NUMBER){
                        editTextPickWaveNumber.setText(result.getContents());
                        getPickWaveList();
                        return;
                    }else if(scanFrom == SCAN_FROM_LOCATION){
                        editTextLocation.setText(result.getContents());
                    }else if(scanFrom == SCAN_FROM_SKU_BARCODE){
                        editTextSkuBarcode.setText(result.getContents());
                    }else if(scanFrom == SCAN_FROM_ZONE_NUMBER){
                        editTextZoneNumber.setText(result.getContents());
                        getPickWaveList();
                        return;
                    }else if(scanFrom == SCAN_FROM_BOX_ID){
                        editTextBoxId.setText(result.getContents());
                    }
                    setFieldRequestFocus();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == PickWaveDetailFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void cancel() {

    }

    private class PickWaveDetailAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return pickWaveDetails.size();
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
            @SuppressLint("ViewHolder") View v = View.inflate(getActivity().getApplicationContext(), R.layout.pick_wave_detail_list_view, null);
            String location = pickWaveDetails.get(position).location;
            String sku_barcode = pickWaveDetails.get(position).sku_barcode;
            String qty = pickWaveDetails.get(position).qty;
            String take_down_qty = pickWaveDetails.get(position).take_down_qty;
            String waiting_take_down_qty = pickWaveDetails.get(position).waiting_take_down_qty;
            String skip_qty = pickWaveDetails.get(position).skip_qty;
            String bin_code = pickWaveDetails.get(position).bin_code;
            boolean isTakeDown = pickWaveDetails.get(position).is_take_down;
            TextView inventoryLocation = (TextView) v.findViewById(R.id.location);
            TextView inventorySkuBarcode = (TextView) v.findViewById(R.id.sku_barcode);
            TextView textViewQty = (TextView) v.findViewById(R.id.qty);
            TextView textViewIsTakeDown = (TextView) v.findViewById(R.id.is_take_down);
            TextView textViewTakeDownQty = (TextView) v.findViewById(R.id.take_down_qty);
            TextView textViewSkiQty = (TextView) v.findViewById(R.id.skip_qty);
            TextView textViewWaitingTakeDownQty = (TextView) v.findViewById(R.id.waiting_take_down_qty);
            inventoryLocation.setText(location);
            inventorySkuBarcode.setText(sku_barcode);
            textViewQty.setText(qty);
            textViewIsTakeDown.setText(Boolean.toString(isTakeDown));
            textViewWaitingTakeDownQty.setText(waiting_take_down_qty);
            textViewTakeDownQty.setText(take_down_qty);
            textViewSkiQty.setText(skip_qty);
            Button buttonTakeDown = (Button) v.findViewById(R.id.button_take_down);
            Button buttonSkip = (Button) v.findViewById(R.id.button_skip);

            if(isTakeDown){
                buttonTakeDown.setVisibility(View.GONE);
                buttonSkip.setVisibility(View.GONE);
            }
            LinearLayout linearLayout = (LinearLayout) v.findViewById(R.id.pick_wave_detail_line);

            if(!skip_qty.equals("0")){
                linearLayout.setBackgroundResource(R.color.colorDanger);
            } else if(!waiting_take_down_qty.equals("0")){
                linearLayout.setBackgroundResource(R.color.colorPartialSuccess);
            }else{
                linearLayout.setBackgroundResource(R.color.colorSuccess);
            }
            View.OnClickListener listener = view -> {
                String boxid = editTextBoxId.getText().toString();
                if(is_scan_box_id.equals("1")){
                    if(boxid.isEmpty()){
                        editTextBoxId.requestFocus();
                        TomAlertDialog.showAlertDialogMessage(getContext(),"Please input Box ID\n请输入箱号");
                        return;
                    }
                }
                if(view == buttonTakeDown){
                    alertTakeDownInfo(v,bin_code);
                }else if(view == buttonSkip){
                    alertSkipDialog(v,bin_code);
                }

            };

            buttonTakeDown.setOnClickListener(listener);
            buttonSkip.setOnClickListener(listener);
            v.setOnClickListener(listener);

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
            case "take-down":
                TomProgress.showProgress(getContext(),false);
                processTakeDown(result);
                break;
            case "get-pick-wave-list":
                processPickWaveList(result);
                break;
            case "by-scan-take-down":
                TomProgress.showProgress(getContext(),false);
                processByScanTakeDown(result);
                break;
            case "skip-qty":
                TomProgress.showProgress(getContext(),false);
                processSkipQty(result);
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

    private void processPickWaveList(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            pickWaveDetails.clear();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                PickWaveDetailModel pickWaveDetailModel = new PickWaveDetailModel(item);
                pickWaveDetails.add(pickWaveDetailModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.pick_wave_detail_list);
            PickWaveDetailAdapter inventoryAdapter = new PickWaveDetailAdapter();
            listView.setAdapter(inventoryAdapter);
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void processByScanTakeDown(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            editTextSkuBarcode.setText("");
            editTextSkuBarcode.requestFocus();
            getPickWaveList();
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private void processTakeDown(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            editTextLocation.setText("");
            editTextSkuBarcode.setText("");
            editTextLocation.requestFocus();
            getPickWaveList();
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }
    private void processSkipQty(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            editTextLocation.setText("");
            editTextSkuBarcode.setText("");
            editTextLocation.requestFocus();
            getPickWaveList();
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
    }

    private String urlMapping(String operation){
        switch (operation){
            case "take-down":
                return credentialManager.getApiBase() + "pick-wave/take-down";
            case "get-pick-wave-list":
                return credentialManager.getApiBase() + "pick-wave/get-list";
            case "by-scan-take-down":
                return credentialManager.getApiBase() + "pick-wave/by-scan-take-down";
            case "skip-qty":
                return credentialManager.getApiBase() + "pick-wave/skip-qty";
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

        @Override
        protected void onCancelled() {
            submitTask = null;
        }
    }

    public void alertTakeDownInfo(View view,String bin_code) {
        String boxid = editTextBoxId.getText().toString();
        TextView textViewSkuBarcode = (TextView) view.findViewById(R.id.sku_barcode);
        TextView textViewLocation = (TextView) view.findViewById(R.id.location);
        TextView textViewWaitingTakeDownQty = (TextView) view.findViewById(R.id.waiting_take_down_qty);
        String sku_barcode = textViewSkuBarcode.getText().toString();
        String location = textViewLocation.getText().toString();
        String qty = textViewWaitingTakeDownQty.getText().toString();
        String content = getString(R.string.location) + ": " + location + "\n\n" +
                getString(R.string.sku_barcode) + ": " + sku_barcode + "\n\n" +
                getString(R.string.qty) + ": " + qty;
        EditText editTextTakeDownQty = (EditText) editTextTakeDownQtyView.findViewById(R.id.take_down_qty);
        editTextTakeDownQty.setText(qty);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.picking)
                .setMessage(content)//内容
                .setView(editTextTakeDownQtyView)
                .setCancelable(true)
                .setOnCancelListener(dialogInterface -> {
                    ViewGroup parent = (ViewGroup) editTextTakeDownQtyView.getParent();
                    if(parent != null){
                        parent.removeAllViews();
                    }
                })
                .setPositiveButton(getString(R.string.confirm_picking), (dialogInterface, i) -> {
                    ViewGroup parent = (ViewGroup) editTextTakeDownQtyView.getParent();
                    if(parent != null){
                        parent.removeAllViews();
                    }
                    String pick_wave_number = editTextPickWaveNumber.getText().toString();
                    String take_down_qty = editTextTakeDownQty.getText().toString();
                    if(Integer.parseInt(take_down_qty) > Integer.parseInt(qty)){
                        return;
                    }
                    takeDown(pick_wave_number,bin_code,sku_barcode,take_down_qty,boxid);
                }).create();
        alertDialog.show();
        editTextTakeDownQty.setSelection(editTextTakeDownQty.length());
    }

    public void alertSkipDialog(View view,String bin_code) {
        String boxid = editTextBoxId.getText().toString();
        TextView textViewSkuBarcode = (TextView) view.findViewById(R.id.sku_barcode);
        TextView textViewLocation = (TextView) view.findViewById(R.id.location);
        TextView textViewWaitingTakeDownQty = (TextView) view.findViewById(R.id.waiting_take_down_qty);
        String sku_barcode = textViewSkuBarcode.getText().toString();
        String location = textViewLocation.getText().toString();
        String qty = textViewWaitingTakeDownQty.getText().toString();
        String content = getString(R.string.location) + ": " + location + "\n\n" +
                getString(R.string.sku_barcode) + ": " + sku_barcode + "\n\n" +
                getString(R.string.qty) + ": " + qty;
        EditText editTextSkipQty = (EditText) editTextSkipQtyView.findViewById(R.id.skip_qty);
        editTextSkipQty.setText("");
        editTextSkipQty.requestFocus();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(R.string.skip)
                .setMessage(content)//内容
                .setView(editTextSkipQtyView)
                .setCancelable(true)
                .setOnCancelListener(dialogInterface -> {
                    ViewGroup parent = (ViewGroup) editTextSkipQtyView.getParent();
                    if(parent != null){
                        parent.removeAllViews();
                    }
                })
                .setPositiveButton(getString(R.string.confirm_skip), (dialogInterface, i) -> {
                    ViewGroup parent = (ViewGroup) editTextSkipQtyView.getParent();
                    if(parent != null){
                        parent.removeAllViews();
                    }
                    String pick_wave_number = editTextPickWaveNumber.getText().toString();
                    String skip_qty = editTextSkipQty.getText().toString();
                    if(skip_qty.length() == 0) return;
                    if(Integer.parseInt(skip_qty) > Integer.parseInt(qty)){
                        Toast.makeText(getContext(),"Missing QTY "+skip_qty+" is greater than the QTY "+qty+" to be removed from the shelf",Toast.LENGTH_LONG).show();
                        return;
                    }
                    skipQty(pick_wave_number,bin_code,sku_barcode,skip_qty,boxid);
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void onDestroy() {
        super.onDestroy();
    }

}
