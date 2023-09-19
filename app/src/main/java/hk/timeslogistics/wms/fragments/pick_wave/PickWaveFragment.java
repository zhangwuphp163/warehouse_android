package hk.timeslogistics.wms.fragments.pick_wave;

import static android.widget.AdapterView.*;

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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.fragments.HomeFragment;
import hk.timeslogistics.wms.models.PickWaveModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.InitSetting;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PickWaveFragment extends Fragment{
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextClientName;
    private EditText editTextPickWaveNumber;
    private EditText editTextZoneNumber;
    private ProgressDialog mProgressDialog;
    //private Button pickWaveSearch;
    private Spinner clientName;
    private String selectedPickWaveNumber = "";
    private ImageButton buttonScanPickWaveNumber;

    private SubmitTask submitTask;
    private ListView pickWaveListView;
    private Button pickingButton;
    private Button pickingByHuButton;
    private Button secondSortButton;

    ListView listView;
    ArrayList<PickWaveModel> pickWaveModels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        pickWaveModels = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_pick_wave, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.pick_wave);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextPickWaveNumber = (EditText) root.findViewById(R.id.editTextPickWaveNumber);
        editTextPickWaveNumber.setShowSoftInputOnFocus(false);
        clientName = (Spinner) root.findViewById(R.id.client_name);
        editTextZoneNumber = (EditText) root.findViewById(R.id.editTextZoneNumber);
        editTextZoneNumber.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextZoneNumber);
        ClearEditTextContent.setupClearEditText(getContext(),editTextPickWaveNumber);
        //pickWaveSearch = (Button) root.findViewById(R.id.pickWaveSearch);
        pickWaveListView = (ListView) root.findViewById(R.id.pick_wave_list_view);
        buttonScanPickWaveNumber = (ImageButton) root.findViewById(R.id.buttonScanPickWaveNumber);

        String clients = credentialManager.getClients();
        InitSetting.setClientList(clients,getContext(),clientName);

        buttonScanPickWaveNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.forSupportFragment(PickWaveFragment.this).initiateScan();
            }
        });
        clientName.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getPickWaveList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        editTextZoneNumber.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getPickWaveList();
                    return true;
                }
                return false;
            }
        });

        editTextPickWaveNumber.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getPickWaveList();
                    return true;
                }
                return false;
            }
        });
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
        editTextPickWaveNumber.requestFocus();
        return root;
    }
    private void getPickWaveList() {
        System.out.println(urlMapping("pick-wave"));
        String pick_wave_number = editTextPickWaveNumber.getText().toString();
        String client_name = clientName.getSelectedItem().toString();
        String zone = editTextZoneNumber.getText().toString();
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("pick_wave_number", pick_wave_number);
        builder.add("client_name", client_name);
        builder.add("zone", zone);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,urlMapping("pick-wave"),"pick-wave");
        submitTask.execute();
    }

    private void processPickWaveList(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            pickWaveModels.clear();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                PickWaveModel pickWaveModel = new PickWaveModel(item.get("pick_wave_number").getAsString(),item.get("client_name").getAsString(),item.get("type").getAsString(), item.get("total_qty").getAsString());
                pickWaveModels.add(pickWaveModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.pick_wave_list_view);
            PickWaveAdapter pickWaveAdapter = new PickWaveAdapter();
            listView.setAdapter(pickWaveAdapter);
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == PickWaveFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    editTextPickWaveNumber.setText(result.getContents());
                    getPickWaveList();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == PickWaveFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void cancel() {

    }

    public class PickWaveAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return pickWaveModels.size();
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
                convertView = View.inflate(getActivity().getApplicationContext(),R.layout.pick_wave_list_view,null);
                holder = new ViewHolder();
                holder.pick_wave_number = (TextView) convertView.findViewById(R.id.pick_wave_number);
                holder.client_name = (TextView) convertView.findViewById(R.id.pick_wave_client_name);
                holder.type = (TextView) convertView.findViewById(R.id.pick_wave_type);
                holder.total_qty = (TextView) convertView.findViewById(R.id.pick_wave_total_qty);

                holder.button_picking = (Button) convertView.findViewById(R.id.picking);
                holder.button_picking_by_handling_unit = (Button) convertView.findViewById(R.id.pick_wave_picking_by_handing_unit);
                holder.button_second_sort = (Button) convertView.findViewById(R.id.second_sort);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.pick_wave_number.setText(pickWaveModels.get(position).mPickWaveNumber);
            holder.client_name.setText(pickWaveModels.get(position).mClientName);
            holder.total_qty.setText(pickWaveModels.get(position).mTotalQty);
            holder.type.setText(pickWaveModels.get(position).mType);
            if(!pickWaveModels.get(position).mType.equals("M")){
                holder.button_second_sort.setVisibility(View.GONE);
            }else{
                holder.button_second_sort.setVisibility(View.VISIBLE);
            }

            ViewHolder finalHolder = holder;
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String pick_wave_number = pickWaveModels.get(position).mPickWaveNumber;
                    if(view == finalHolder.button_picking){
                        Fragment fragment = new PickWaveDetailFragment();

                        Bundle args = new Bundle();
                        args.putString("pick_wave_number", pick_wave_number);
                        args.putString("is_scan_box_id", "0");
                        args.putString("zone_name", editTextZoneNumber.getText().toString());
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        if(MainActivity.currentFragment != null){
                            transaction.hide(MainActivity.currentFragment);
                        }
                        transaction.replace(R.id.flContent, fragment).commit();
                        MainActivity.currentFragment = fragment;
                    }else if(view == finalHolder.button_picking_by_handling_unit){
                        Fragment fragment = new PickWaveDetailFragment();

                        Bundle args = new Bundle();
                        args.putString("pick_wave_number", pick_wave_number);
                        args.putString("is_scan_box_id", "1");
                        args.putString("zone_name", editTextZoneNumber.getText().toString());
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        if(MainActivity.currentFragment != null){
                            transaction.hide(MainActivity.currentFragment);
                        }
                        transaction.replace(R.id.flContent, fragment).commit();
                        MainActivity.currentFragment = fragment;
                    }else if(view == finalHolder.button_second_sort){
                        Fragment fragment = new SecondSortFragment();
                        Bundle args = new Bundle();
                        args.putString("pick_wave_number", pick_wave_number);
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        if(MainActivity.currentFragment != null){
                            transaction.hide(MainActivity.currentFragment);
                        }
                        transaction.replace(R.id.flContent, fragment).commit();
                        MainActivity.currentFragment = fragment;
                    }
                }

            };

            holder.button_picking.setOnClickListener(listener);
            holder.button_picking_by_handling_unit.setOnClickListener(listener);
            holder.button_second_sort.setOnClickListener(listener);
            return convertView;
        }
    }

    class ViewHolder{
        TextView type;
        TextView client_name;
        TextView pick_wave_number;
        TextView total_qty;
        Button button_picking;
        Button button_picking_by_handling_unit;
        Button button_second_sort;
    }

    private void processResponse(RemoteResult result,String operation){
        TomProgress.showProgress(getContext(),false);
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "pick-wave":
                processPickWaveList(result);
                break;
            /*case "clients":
                processClients(result);
                break;*/
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

    private String urlMapping(String operation){
        switch (operation){
            case "pick-wave":
                return credentialManager.getApiBase() + "pick-wave";
            case "clients":
                return credentialManager.getApiBase() + "base/clients";
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

}

