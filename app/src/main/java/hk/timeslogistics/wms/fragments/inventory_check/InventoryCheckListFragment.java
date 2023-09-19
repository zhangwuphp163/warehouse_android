package hk.timeslogistics.wms.fragments.inventory_check;

import static android.widget.AdapterView.OnClickListener;
import static android.widget.AdapterView.OnItemClickListener;
import static android.widget.AdapterView.OnKeyListener;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.fragments.HomeFragment;
import hk.timeslogistics.wms.models.InventoryCheckModel;
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

public class InventoryCheckListFragment extends Fragment{
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextCycleCountCode;
    private ProgressDialog mProgressDialog;
    private Button searchButton;
    private String selectedPickWaveNumber = "";
    private ImageButton buttonScanCycleCountCode;

    private SubmitTask submitTask;
    private ListView inventoryCheckListView;

    ListView listView;
    ArrayList<InventoryCheckModel> inventoryCheckModels;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        inventoryCheckModels = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_inventory_check_list, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.inventory_check);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextCycleCountCode = (EditText) root.findViewById(R.id.editTextCycleCountCode);
        editTextCycleCountCode.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextCycleCountCode);
        buttonScanCycleCountCode = (ImageButton) root.findViewById(R.id.buttonScanCycleCountCode);
        inventoryCheckListView = (ListView) root.findViewById(R.id.inventory_check_list_view);
        buttonScanCycleCountCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.forSupportFragment(InventoryCheckListFragment.this).initiateScan();
            }
        });

        editTextCycleCountCode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getCycleCountList();
                    return true;
                }
                return false;
            }
        });

        inventoryCheckListView.setOnItemClickListener(new OnItemClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textViewCycleCountCode = (TextView) view.findViewById(R.id.cycle_count_code);
                String cycleCountCode = textViewCycleCountCode.getText().toString();
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
        editTextCycleCountCode.requestFocus();
        getCycleCountList();
        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(MainActivity.homeFragment);
                Fragment fragment = new HomeFragment();
                transaction.add(R.id.flContent,fragment);
                transaction.hide(MainActivity.currentFragment).show(fragment).commit();

                /*transaction.hide(MainActivity.currentFragment).show(MainActivity.homeFragment).commit();
                if(!MainActivity.homeFragment.isAdded()){
                    if(MainActivity.currentFragment != null){
                        transaction.hide(MainActivity.currentFragment);
                    }

                }else{

                }*/
                MainActivity.currentFragment = fragment;
                Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(R.string.home);
            }
        });
        return root;
    }
    private void getCycleCountList() {
        String cycle_count_code = editTextCycleCountCode.getText().toString();
        String language = Locale.getDefault().getLanguage();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("cycle_count_code", cycle_count_code);
        builder.add("language", language);
        RequestBody formBody = builder.build();
        submitTask = new InventoryCheckListFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"inventory-check-list"),"inventory-check-list");
        submitTask.execute();
    }

    private void processInventoryCheckList(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            inventoryCheckModels.clear();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                InventoryCheckModel inventoryCheckModel = new InventoryCheckModel(item.get("cycle_count_code").getAsString(),item.get("status").getAsString(),item.get("target_qty").getAsInt(),item.get("scanned_qty").getAsInt(),item.get("inventory_qty").getAsInt());
                inventoryCheckModels.add(inventoryCheckModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.inventory_check_list_view);
            InventoryCheckListAdapter inventoryCheckListAdapter = new InventoryCheckListAdapter();
            listView.setAdapter(inventoryCheckListAdapter);
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == InventoryCheckListFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    editTextCycleCountCode.setText(result.getContents());
                    getCycleCountList();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == InventoryCheckListFragment.RESULT_OK) {

            }
        }
    }

    public class InventoryCheckListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return inventoryCheckModels.size();
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
            @SuppressLint("ViewHolder") View v = View.inflate(getActivity().getApplicationContext(),R.layout.inventory_check_list_view,null);
            String cycle_count_code = inventoryCheckModels.get(position).mCycleCountCode;
            String status = inventoryCheckModels.get(position).mStatus;
            Integer target_qty = inventoryCheckModels.get(position).mTargetQty;
            Integer scanned_qty = inventoryCheckModels.get(position).mScannedQty;
            Integer inventory_qty = inventoryCheckModels.get(position).mInventoryQty;

            TextView textViewCycleCountCode = (TextView) v.findViewById(R.id.cycle_count_code);
            TextView textViewStatus = (TextView) v.findViewById(R.id.status);
            TextView textViewTargetQty = (TextView) v.findViewById(R.id.target_qty);
            TextView textViewScannedQty = (TextView) v.findViewById(R.id.scanned_qty);
            TextView textViewInventoryQty = (TextView) v.findViewById(R.id.inventory_qty);
            textViewCycleCountCode.setText(cycle_count_code);
            textViewStatus.setText(status);
            textViewTargetQty.setText(target_qty.toString());
            textViewScannedQty.setText(scanned_qty.toString());
            textViewInventoryQty.setText(inventory_qty.toString());
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
            case "inventory-check-list":
                TomProgress.showProgress(getContext(),false);
                processInventoryCheckList(result);
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

