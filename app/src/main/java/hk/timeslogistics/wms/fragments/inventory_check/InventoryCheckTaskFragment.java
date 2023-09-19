package hk.timeslogistics.wms.fragments.inventory_check;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import hk.timeslogistics.wms.models.InventoryCheckTaskModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomProgress;
import hk.timeslogistics.wms.utils.UrlMapping;
import okhttp3.FormBody;
import okhttp3.RequestBody;

public class InventoryCheckTaskFragment extends Fragment{
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private String cycle_count_code = "";

    private SubmitTask submitTask;
    private ListView inventoryCheckTaskView;

    ListView listView;
    ArrayList<InventoryCheckTaskModel> inventoryCheckTaskModels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("inventory_check_task");
        inventoryCheckTaskModels = new ArrayList<>();
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_inventory_check_task, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.inventory_check);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);

        inventoryCheckTaskView = (ListView) root.findViewById(R.id.inventory_check_task_list_view);

        if (getArguments() != null) {
            cycle_count_code = getArguments().getString("cycle_count_code");
            getCycleCountTaskList();
        }
        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = MainActivity.inventoryCheckListFragment;
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
                    fragment = new InventoryCheckListFragment();
                    transaction.add(R.id.flContent, fragment);
                    transaction.hide(MainActivity.currentFragment).show(fragment).commit();
                }
                MainActivity.currentFragment = fragment;
                MainActivity.inventoryCheckListFragment = fragment;
            }
        });
        return root;
    }

    private void getCycleCountTaskList() {
        TomProgress.showProgress(getContext(),true);
        String language = Locale.getDefault().getLanguage();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("cycle_count_code", cycle_count_code);
        builder.add("language", language);
        RequestBody formBody = builder.build();
        submitTask = new InventoryCheckTaskFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"inventory-check-task-list"),"inventory-check-task-list");
        submitTask.execute();
    }

    private void completed(Integer id) {
        TomProgress.showProgress(getContext(),true);
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("id", id.toString());
        RequestBody formBody = builder.build();
        submitTask = new InventoryCheckTaskFragment.SubmitTask(getContext(),formBody,UrlMapping.getUrl(credentialManager,"inventory-check-task-completed"),"inventory-check-task-completed");
        submitTask.execute();
    }

    private void processInventoryCheckTaskList(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            JsonArray items = result.getItems();
            inventoryCheckTaskModels.clear();
            for (int i = 0;i<items.size();i++){
                JsonObject item = (JsonObject) items.get(i);
                InventoryCheckTaskModel inventoryCheckTaskModel = new InventoryCheckTaskModel(item);
                inventoryCheckTaskModels.add(inventoryCheckTaskModel);
            }
            listView = (ListView) getActivity().findViewById(R.id.inventory_check_task_list_view);
            InventoryCheckTaskAdapter inventoryCheckTaskAdapter = new InventoryCheckTaskAdapter();
            listView.setAdapter(inventoryCheckTaskAdapter);
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    private void processInventoryCheckTaskCompleted(RemoteResult result){
        if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
            AlertManager.okay(getContext());
            Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();
            getCycleCountTaskList();
        } else {
            AlertManager.error(getContext());
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == InventoryCheckTaskFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    getCycleCountTaskList();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == InventoryCheckTaskFragment.RESULT_OK) {

            }
        }
    }

    public class InventoryCheckTaskAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return inventoryCheckTaskModels.size();
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
                convertView = View.inflate(getActivity().getApplicationContext(),R.layout.inventory_check_task_list_view,null);
                holder = new ViewHolder();
                holder.task = (TextView) convertView.findViewById(R.id.task);
                holder.status = (TextView) convertView.findViewById(R.id.status);
                holder.count_qty = (TextView) convertView.findViewById(R.id.count_qty);
                holder.inventory_qty = (TextView) convertView.findViewById(R.id.inventory_qty);
                holder.scanned_qty = (TextView) convertView.findViewById(R.id.scanned_qty);
                holder.checker = (TextView) convertView.findViewById(R.id.checker);
                holder.progress = (TextView) convertView.findViewById(R.id.progress);
                holder.progress_bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
                holder.buttonSingle = (Button) convertView.findViewById(R.id.single);
                holder.buttonBatch = (Button) convertView.findViewById(R.id.batch);
                holder.buttonCompleted = (Button) convertView.findViewById(R.id.completed);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            holder.cycle_count_code = inventoryCheckTaskModels.get(position).mCycleCountCode;
            holder.task_id = inventoryCheckTaskModels.get(position).mTaskId;
            holder.task.setText(inventoryCheckTaskModels.get(position).mTask);
            holder.status.setText(inventoryCheckTaskModels.get(position).mStatus);
            holder.scanned_qty.setText(inventoryCheckTaskModels.get(position).mScannedQty);
            holder.count_qty.setText(inventoryCheckTaskModels.get(position).mCountQty);
            holder.inventory_qty.setText(inventoryCheckTaskModels.get(position).mInventoryQty);
            holder.checker.setText(inventoryCheckTaskModels.get(position).mChecker);
            holder.progress.setText(inventoryCheckTaskModels.get(position).mProgress);
            holder.progress_bar.setProgress(inventoryCheckTaskModels.get(position).mProgressBar);
            if(inventoryCheckTaskModels.get(position).mStatus.equals("completed")||inventoryCheckTaskModels.get(position).mStatus.equals("盘点完成")){
                convertView.findViewById(R.id.inventory_check_button_line).setVisibility(View.GONE);
            }else {
                convertView.findViewById(R.id.inventory_check_button_line).setVisibility(View.VISIBLE);
            }

            ViewHolder finalHolder = holder;
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view == finalHolder.buttonSingle){
                        Fragment fragment = MainActivity.inventoryCheckTaskItemFragment;
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        if(!fragment.isAdded()){
                            Bundle args = new Bundle();
                            args.putString("id", finalHolder.task_id.toString());
                            args.putString("cycle_count_code", finalHolder.cycle_count_code);
                            args.putString("scan_by", "single");
                            fragment.setArguments(args);
                            if(MainActivity.currentFragment != null){
                                transaction.hide(MainActivity.currentFragment);
                            }
                            transaction.add(R.id.flContent, fragment).commit();
                        }else{
                            transaction.remove(fragment);
                            fragment = new InventoryCheckTaskItemFragment();
                            Bundle args = new Bundle();
                            args.putString("id", finalHolder.task_id.toString());
                            args.putString("cycle_count_code", finalHolder.cycle_count_code);
                            args.putString("scan_by", "single");
                            fragment.setArguments(args);
                            transaction.add(R.id.flContent, fragment);
                            transaction.hide(MainActivity.currentFragment).show(fragment).commit();
                        }
                        MainActivity.currentFragment = fragment;
                        MainActivity.inventoryCheckTaskItemFragment = fragment;
                    }
                    if(view == finalHolder.buttonBatch){
                        Fragment fragment = MainActivity.inventoryCheckTaskItemFragment;
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        if(!fragment.isAdded()){
                            Bundle args = new Bundle();
                            args.putString("id", finalHolder.task_id.toString());
                            args.putString("cycle_count_code", finalHolder.cycle_count_code);
                            args.putString("scan_by", "batch");
                            fragment.setArguments(args);
                            if(MainActivity.currentFragment != null){
                                transaction.hide(MainActivity.currentFragment);
                            }
                            transaction.add(R.id.flContent, fragment).commit();
                        }else{
                            transaction.remove(fragment);
                            fragment = new InventoryCheckTaskItemFragment();
                            Bundle args = new Bundle();
                            args.putString("id", finalHolder.task_id.toString());
                            args.putString("cycle_count_code", finalHolder.cycle_count_code);
                            args.putString("scan_by", "batch");
                            fragment.setArguments(args);
                            transaction.add(R.id.flContent, fragment);
                            transaction.hide(MainActivity.currentFragment).show(fragment).commit();
                        }
                        MainActivity.currentFragment = fragment;
                        MainActivity.inventoryCheckTaskItemFragment = fragment;
                    }
                    if(view == finalHolder.buttonCompleted){
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                .setTitle(R.string.confirm_complete)
                                .setMessage(R.string.complete_task_check_tips)//内容
                                .setCancelable(true)
                                .setNeutralButton(getString(R.string.confirm_complete), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        completed(finalHolder.task_id);
                                    }
                                }).setPositiveButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        dialogInterface.dismiss();
                                }}).create();
                        alertDialog.show();
                    }

                }
            };
            holder.buttonSingle.setOnClickListener(listener);
            holder.buttonBatch.setOnClickListener(listener);
            holder.buttonCompleted.setOnClickListener(listener);

            return convertView;
        }

    }
    static class ViewHolder{
        TextView task;
        Integer task_id;
        String cycle_count_code;
        TextView status;
        TextView count_qty;
        TextView scanned_qty;
        TextView inventory_qty;
        TextView checker;
        TextView progress;
        ProgressBar progress_bar;
        Button buttonSingle;
        Button buttonBatch;
        Button buttonCompleted;
    }

    private void processResponse(RemoteResult result,String operation){
        if (result.shouldLogout()) {
            credentialManager.logout();
            getActivity().finish();
            Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
        }
        switch (operation){
            case "inventory-check-task-list":
                TomProgress.showProgress(getContext(),false);
                processInventoryCheckTaskList(result);
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

