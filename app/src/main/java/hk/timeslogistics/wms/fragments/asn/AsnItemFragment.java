package hk.timeslogistics.wms.fragments.asn;

import static android.widget.AbsListView.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.AsnItemModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomAlertDialog;
import hk.timeslogistics.wms.utils.TomProgress;
import hk.timeslogistics.wms.utils.UrlMapping;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class AsnItemFragment extends Fragment{
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;

    private AsyncTask<Void, Void, RemoteResult> submitTask;
    private ListView asnItemListView;
    private TextView textViewNotMoreData;
    private String asn_number = "";
    private int page = 1;
    private boolean is_more_data = true;

    ListView listView;
    ArrayList<AsnItemModel> asnItemModels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        asnItemModels = new ArrayList<>();
    }
    public void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_asn_items, null);
        if (getArguments() != null) {
            asn_number = getArguments().getString("asn_number");
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("ASN [ "+asn_number+" ]");
        }

        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        asnItemListView = (ListView) root.findViewById(R.id.asn_items_list_view);
        textViewNotMoreData = (TextView) root.findViewById(R.id.not_more_data);
        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = MainActivity.asnItemFragment;
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
                    fragment = MainActivity.asnListFragment;
                    transaction.add(R.id.flContent, fragment);
                    transaction.hide(MainActivity.currentFragment).show(fragment).commit();
                }
                MainActivity.currentFragment = fragment;
                MainActivity.asnListFragment = fragment;
            }
        });
        getAsnItemList();
        asnItemListView.setOnScrollListener(new OnScrollListener(){

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if(is_more_data && OnScrollListener.SCROLL_STATE_IDLE == i && absListView.getLastVisiblePosition() == absListView.getCount()-1){
                    getAsnItemList();
                }
            }
            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        return root;
    }

    public class AsnItemAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return asnItemModels.size();
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
            @SuppressLint("ViewHolder") View v = View.inflate(getActivity().getApplicationContext(),R.layout.asn_items,null);
            String barcode = asnItemModels.get(position).mBarcode;
            String skuName = asnItemModels.get(position).mSkuName;
            String poNumber = asnItemModels.get(position).mPoNumber;
            String code = asnItemModels.get(position).mCode;
            String estimatedQty = asnItemModels.get(position).mEstimatedQty;
            String actualQty = asnItemModels.get(position).mActualQty;
            TextView textViewSkuBarcode = (TextView) v.findViewById(R.id.sku_barcode);
            TextView textViewPoNumber = (TextView) v.findViewById(R.id.po_number);
            TextView textViewSkuName = (TextView) v.findViewById(R.id.sku_name);
            TextView textViewEstimatedQty = (TextView) v.findViewById(R.id.estimated_qty);
            TextView textViewActualQty = (TextView) v.findViewById(R.id.actual_qty);
            textViewSkuBarcode.setText(barcode);
            textViewPoNumber.setText(poNumber);
            textViewSkuName.setText(skuName);
            textViewEstimatedQty.setText(estimatedQty);
            textViewActualQty.setText(actualQty);

            int estimated_qty = Integer.parseInt(estimatedQty);
            int actual_qty = Integer.parseInt(actualQty);
            if(estimated_qty == actual_qty){
                v.setBackgroundResource(R.color.colorGreen);
            }else if(estimated_qty >= actual_qty){
                v.setBackgroundResource(R.color.colorPartialSuccess);
            }else{
                v.setBackgroundResource(R.color.colorBlue);
            }
            return v;
        }

    }

    private void getAsnItemList() {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("asn_number", asn_number);
        builder.add("page", Integer.toString(page));
        builder.add("limit", "20");
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"asn-items"),"asn-items");
        TomProgress.showProgress(getContext(),true);
        submitTask.execute();
    }
    private void processAsnItems(RemoteResult result){
        //asnItemModels.clear();
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                AsnItemModel asnItemModel = new AsnItemModel(item);
                asnItemModels.add(asnItemModel);
            }
            page ++;
            if(items.size() == 0){
                is_more_data = false;
            }
        } else {
            TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
        }
        listView = (ListView) getActivity().findViewById(R.id.asn_items_list_view);
        AsnItemAdapter asnItemAdapter = new AsnItemAdapter();
        listView.setAdapter(asnItemAdapter);
        if(!is_more_data){
            textViewNotMoreData.setVisibility(View.VISIBLE);
        }
    }
    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "asn-items":
                TomProgress.showProgress(getContext(),false);
                processAsnItems(result);
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

