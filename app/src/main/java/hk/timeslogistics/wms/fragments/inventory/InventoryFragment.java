package hk.timeslogistics.wms.fragments.inventory;

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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.InventoryModel;
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

public class InventoryFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextInventoryNumber;
    private Spinner numberType;
    private ProgressDialog mProgressDialog;
    private ImageButton buttonScanNumber;

    protected Button buttonSubmit;
    protected Button buttonCancel;

    private InventoryFragment.SubmitTask submitTask;

    ListView listView;
    ArrayList<InventoryModel> inventories;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        inventories = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_inventory, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.inventory);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextInventoryNumber = (EditText) root.findViewById(R.id.editTextInventoryNumber);
        editTextInventoryNumber.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextInventoryNumber);
        numberType = (Spinner) root.findViewById(R.id.numberType);
        buttonScanNumber = (ImageButton) root.findViewById(R.id.buttonScanNumber);
        buttonSubmit = (Button) root.findViewById(R.id.buttonSubmit);
        buttonCancel = (Button) root.findViewById(R.id.buttonCancel);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextInventoryNumber.setText("");
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        buttonScanNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.forSupportFragment(InventoryFragment.this).initiateScan();
            }
        });

        editTextInventoryNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    submit();
                    editTextInventoryNumber.setText("");
                    return true;
                }
                return false;
            }
        });
        editTextInventoryNumber.requestFocus();

        return root;
    }
    private void submit() {
        String number = editTextInventoryNumber.getText().toString();
        String number_type = numberType.getSelectedItem().toString();
        String language = Locale.getDefault().getLanguage();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("number", number);
        builder.add("number_type", number_type);
        builder.add("language", language);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,credentialManager.getApiBase() + "inventory");
        submitTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == InventoryFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    editTextInventoryNumber.setText(result.getContents());
                    submit();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == InventoryFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void cancel() {

    }

    public class InventoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return inventories.size();
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
            View v = View.inflate(getActivity().getApplicationContext(),R.layout.inventory_list_view,null);
            String location = inventories.get(position).location;
            String sku_barcode = inventories.get(position).sku_barcode;
            String sku_name = inventories.get(position).sku_name;
            //String sku_condition = inventories.get(position).sku_condition;
            String status = inventories.get(position).status;
            int qty = inventories.get(position).qty;
            int usable_qty = inventories.get(position).usable_qty;
            TextView inventoryLocation = (TextView) v.findViewById(R.id.inventory_location);
            TextView inventorySkuBarcode = (TextView) v.findViewById(R.id.inventory_sku_barcode);
            TextView inventoryQty = (TextView) v.findViewById(R.id.inventory_qty);
            TextView inventoryUsableQty = (TextView) v.findViewById(R.id.inventory_usable_qty);
            TextView inventoryStatus = (TextView) v.findViewById(R.id.inventory_status);
            TextView inventorySkuName = (TextView) v.findViewById(R.id.inventory_sku_name);
            inventoryLocation.setText(location);
            inventorySkuBarcode.setText(sku_barcode);
            inventoryUsableQty.setText(Integer.toString(usable_qty));
            inventoryStatus.setText(status);
            inventorySkuName.setText(sku_name);
            inventoryQty.setText(Integer.toString(qty));
            return v;
        }
    }

    private class SubmitTask extends HttpSubmitTask {
        public SubmitTask(Context context, RequestBody formBody, String url) {
            super(context, formBody, url);
        }
        @Override
        protected void onPostExecute(final RemoteResult result) {
            submitTask = null;
            TomProgress.showProgress(getContext(),false);
            if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                JsonArray items = result.getItems();
                inventories.clear();
                for (int i = 0;i<items.size();i++){
                    JsonObject item = (JsonObject) items.get(i);
                    InventoryModel inventoryModel = new InventoryModel(item);
                    inventories.add(inventoryModel);
                }
                AlertManager.okay(getContext());
                Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();

                listView = (ListView) getActivity().findViewById(R.id.inventory_list_view);
                InventoryAdapter inventoryAdapter = new InventoryAdapter();
                listView.setAdapter(inventoryAdapter);
            } else {
                AlertManager.error(getContext());
                Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
            }
        }
    }
}

