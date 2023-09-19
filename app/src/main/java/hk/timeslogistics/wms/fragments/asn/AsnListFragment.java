package hk.timeslogistics.wms.fragments.asn;

import static android.widget.AdapterView.OnItemSelectedListener;
import static android.widget.AdapterView.OnKeyListener;
import static hk.timeslogistics.wms.MainActivity.*;
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
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.Spinner;
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
import hk.timeslogistics.wms.fragments.HomeFragment;
import hk.timeslogistics.wms.fragments.inbound.InboundBySNFragment;
import hk.timeslogistics.wms.fragments.inbound.InboundFragment;
import hk.timeslogistics.wms.models.AsnModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.InitSetting;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomAlertDialog;
import hk.timeslogistics.wms.utils.TomProgress;
import hk.timeslogistics.wms.utils.UrlMapping;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class AsnListFragment extends Fragment{
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextSkuBarcode;
    private ProgressDialog mProgressDialog;
    private Button searchButton;
    private Spinner clientName;
    private ImageButton buttonScanSkuBarcode;

    private AsyncTask<Void, Void, RemoteResult> submitTask;
    private ListView asnListView;

    ListView listView;
    ArrayList<AsnModel> asnModels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        asnModels = new ArrayList<>();
    }
    public void onDestroy() {
        super.onDestroy();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_asn_list, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.inbound_order_list);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextSkuBarcode = (EditText) root.findViewById(R.id.editTextSkuBarcode);
        editTextSkuBarcode.setShowSoftInputOnFocus(false);
        buttonScanSkuBarcode = (ImageButton) root.findViewById(R.id.buttonScanSkuBarcode);
        clientName = (Spinner) root.findViewById(R.id.client_name);
        //searchButton = (Button) root.findViewById(R.id.search_button);
        asnListView = (ListView) root.findViewById(R.id.asn_list_view);
        String clients = credentialManager.getClients();
        InitSetting.setClientList(clients,getContext(),clientName);

        buttonScanSkuBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.forSupportFragment(AsnListFragment.this).initiateScan();
            }
        });
        clientName.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        editTextSkuBarcode.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getList();
                    return true;
                }
                return false;
            }
        });

        ClearEditTextContent.setupClearEditText(getContext(),editTextSkuBarcode);
        /*editTextSkuBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 在输入框内容改变之前的操作
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 在输入框内容改变时的操作
                if (s.length() > 0) {
                    editTextSkuBarcode.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.clear_edit_text), null);
                } else {
                    editTextSkuBarcode.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 在输入框内容改变之后的操作
            }
        });*/

        /*asnListView.setOnItemClickListener(new OnItemClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView asnNumberId = (TextView) view.findViewById(R.id.asn_number);
                String asn_number = asnNumberId.getText().toString();
                Fragment fragment = new InboundFragment();
                Bundle args = new Bundle();
                args.putString("asn_number", asn_number);
                fragment.setArguments(args);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if(!inboundFragment.isAdded()){
                    transaction.remove(inboundFragment);
                }

                transaction.replace(R.id.flContent, fragment).commit();

                currentFragment = fragment;
                inboundFragment = fragment;
            }
        });*/
        editTextSkuBarcode.requestFocus();

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
                MainActivity.currentFragment = fragment;
                Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(R.string.home);
            }
        });
        return root;
    }
    private void getList() {
        //TomProgress.showProgress(getContext(),true);
        String sku_barcode = editTextSkuBarcode.getText().toString();
        String client_name = clientName.getSelectedItem().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("sku_barcode", sku_barcode);
        builder.add("client_name", client_name);
        RequestBody formBody = builder.build();
        submitTask = new AsnListFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"asn-list"),"asn-list");
        submitTask.execute();
    }

    private void processAsnList(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            asnModels.clear();
            JsonArray items = result.getItems();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                AsnModel asnModel = new AsnModel(item.get("asn_number").getAsString(),item.get("client_name").getAsString(),item.get("asn_date").getAsString());
                asnModels.add(asnModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.asn_list_view);
            AsnListAdapter asnListAdapter = new AsnListAdapter();
            listView.setAdapter(asnListAdapter);
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == AsnListFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    editTextSkuBarcode.setText(result.getContents());
                    getList();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == AsnListFragment.RESULT_OK) {

            }
        }
    }

    public class AsnListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return asnModels.size();
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
                convertView = View.inflate(getActivity().getApplicationContext(),R.layout.asn_list_view,null);
                holder = new ViewHolder();
                holder.asn_number = (TextView) convertView.findViewById(R.id.asn_number);
                holder.asn_date = (TextView) convertView.findViewById(R.id.asn_date);
                holder.client_name = (TextView) convertView.findViewById(R.id.client_name);
                holder.buttonToInbound = (Button) convertView.findViewById(R.id.to_inbound);
                holder.buttonInboundBySn = (Button) convertView.findViewById(R.id.inbound_by_sn);
                holder.buttonViewDetails = (Button) convertView.findViewById(R.id.view_details);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.asn_number.setText(asnModels.get(position).mAsnNumber);
            holder.asn_date.setText(asnModels.get(position).mAsnDate);
            holder.client_name.setText(asnModels.get(position).mClientName);

            ViewHolder finalHolder = holder;
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view == finalHolder.buttonToInbound){
                        String asn_number = asnModels.get(position).mAsnNumber;
                        Fragment fragment = new InboundFragment();
                        Bundle args = new Bundle();
                        args.putString("asn_number", asn_number);
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        if(!inboundFragment.isAdded()){
                            transaction.remove(inboundFragment);
                        }

                        transaction.replace(R.id.flContent, fragment).commit();
                        currentFragment = fragment;
                        inboundFragment = fragment;
                    }else if(view == finalHolder.buttonViewDetails){
                        String asn_number = asnModels.get(position).mAsnNumber;
                        Fragment fragment = new AsnItemFragment();
                        Bundle args = new Bundle();
                        args.putString("asn_number", asn_number);
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        if(!asnItemFragment.isAdded()){
                            transaction.remove(asnItemFragment);
                        }

                        transaction.replace(R.id.flContent, fragment).commit();
                        currentFragment = fragment;
                        asnItemFragment = fragment;
                    } else if (view == finalHolder.buttonInboundBySn) {
                        String asn_number = asnModels.get(position).mAsnNumber;
                        Fragment fragment = new InboundBySNFragment();
                        Bundle args = new Bundle();
                        args.putString("asn_number", asn_number);
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        if(!inboundBySnFragment.isAdded()){
                            transaction.remove(inboundBySnFragment);
                        }

                        transaction.replace(R.id.flContent, fragment).commit();
                        currentFragment = fragment;
                        inboundBySnFragment = fragment;
                    }
                }

            };
            holder.buttonToInbound.setOnClickListener(listener);
            holder.buttonViewDetails.setOnClickListener(listener);
            holder.buttonInboundBySn.setOnClickListener(listener);
            return convertView;
        }

    }

    static class ViewHolder{
        TextView asn_number;
        TextView client_name;
        TextView asn_date;
        Button buttonToInbound;
        Button buttonInboundBySn;
        Button buttonViewDetails;
    }

    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "asn-list":
                processAsnList(result);
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
        public SubmitTask(Context context,RequestBody formBody, String url, String operation) {
            super(context,formBody, url);
            mOperation = operation;

        }
        protected void onPostExecute(final RemoteResult result) {
            submitTask = null;
            processResponse(result,mOperation);
        }
    }
}

