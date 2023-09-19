package hk.timeslogistics.wms.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Timer;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.BuildConfig;
import hk.timeslogistics.wms.LoginActivity;
import hk.timeslogistics.wms.MainActivity;
import hk.timeslogistics.wms.fragments.inbound.InboundFragment;
import hk.timeslogistics.wms.fragments.inventory_check.InventoryCheckTaskFragment;
import hk.timeslogistics.wms.fragments.inventory_check.InventoryCheckTaskItemFragment;
import hk.timeslogistics.wms.fragments.pick_wave.SecondSortFragment;
import hk.timeslogistics.wms.fragments.relocation.RelocationFragment;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment{
    private AlertDialog updateDialog;
    private CredentialManager credentialManager;
    private TextView textViewNavInbound;
    private TextView textViewNavRelocation;
    private TextView textViewNavLogout;
    private TextView textViewAppUpdate;
    private AsyncTask<Void, Void, RemoteResult> mCheckTask;
    private ProgressDialog mProgressDialog;
    private TextView textViewAsnList;
    private TextView textViewPutAway;
    private TextView textViewPutAwayByBoxId;
    private TextView textViewInventory;
    private TextView textViewPickWave;
    private TextView textViewHandoverByTrackingNumber;
    private TextView textViewConfirmHandover;
    private TextView textViewSecondSort;
    private TextView textViewInventoryCheck;
    private TextView textViewHandlingUnit;
    private TextView textViewRelocationByBoxId;
    private TextView getTextViewPutAwayTask;
    private TextView getTextViewOrder;
    private TextView textViewSkuUpdate;
    private Boolean is_click_update_btn = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("home");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_home, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.home);
        getActivity().setTitle(getString(R.string.title_activity_main));
        textViewNavLogout = (TextView) root.findViewById(R.id.textViewNavLogout);
        textViewNavInbound = (TextView) root.findViewById(R.id.textViewNavInbound);
        textViewAsnList = (TextView) root.findViewById(R.id.textViewAsnList);
        textViewNavRelocation = (TextView) root.findViewById(R.id.textViewRelocation);
        textViewAppUpdate = (TextView) root.findViewById(R.id.textViewAppUpdate);
        textViewPutAway = (TextView) root.findViewById(R.id.textViewPutAway);
        textViewPutAwayByBoxId = (TextView) root.findViewById(R.id.textViewPutAwayByBoxId);
        textViewInventory = (TextView) root.findViewById(R.id.textViewInventory);
        textViewPickWave = (TextView) root.findViewById(R.id.textViewPickWave);
        textViewSecondSort = (TextView) root.findViewById(R.id.textViewSecondSort);
        textViewHandoverByTrackingNumber = (TextView) root.findViewById(R.id.textViewHandoverByTrackingNumber);
        textViewConfirmHandover = (TextView) root.findViewById(R.id.textViewConfirmHandover);
        textViewInventoryCheck = (TextView) root.findViewById(R.id.textViewInventoryCheck);
        textViewHandlingUnit = (TextView) root.findViewById(R.id.textViewHandlingUnit);
        textViewRelocationByBoxId = (TextView) root.findViewById(R.id.textViewRelocationByBoxId);
        textViewSkuUpdate = (TextView) root.findViewById(R.id.textViewSkuUpdate);
        getTextViewPutAwayTask = (TextView) root.findViewById(R.id.textViewPutAwayTask);
        getTextViewOrder = (TextView) root.findViewById(R.id.textViewOrder);

        textViewNavInbound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new InboundFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if(!MainActivity.inboundFragment.isAdded()){
                    transaction.remove(MainActivity.inboundFragment);
                }
                if(MainActivity.currentFragment != null){
                    transaction.hide(MainActivity.currentFragment);
                }
                transaction.add(R.id.flContent, fragment).show(fragment).commit();
                MainActivity.currentFragment = fragment;
                MainActivity.inboundFragment = fragment;
                //switchFragment(MainActivity.inboundFragment);
            }
        });
        textViewAsnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.asnListFragment,getString(R.string.inbound_order_list));
            }
        });
        textViewNavRelocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new RelocationFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if(!MainActivity.relocationFragment.isAdded()){
                    transaction.remove(MainActivity.relocationFragment);
                }
                if(MainActivity.currentFragment != null){
                    transaction.hide(MainActivity.currentFragment);
                }
                transaction.add(R.id.flContent, fragment).show(fragment).commit();
                MainActivity.currentFragment = fragment;
                MainActivity.relocationFragment = fragment;
                //switchFragment(MainActivity.relocationFragment,getString(R.string.nav_relocation));
            }
        });
        textViewRelocationByBoxId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(new RelocationFragment(),getString(R.string.nav_relocation));
            }
        });
        textViewPutAwayByBoxId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.putAwayByBoxIdFragment,getString(R.string.put_away_by_box_id));
            }
        });
        textViewPutAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.putAwayFragment,getString(R.string.put_away));
            }
        });
        textViewInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.inventoryFragment,getString(R.string.inventory));
            }
        });

        textViewSkuUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.skuUpdateFragment,getString(R.string.sku_update));
            }
        });
        textViewPickWave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.pickWaveFragment,getString(R.string.pick_wave));
            }
        });
        textViewHandoverByTrackingNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.handoverByTrackingNumberFragment,getString(R.string.handover_by_tracking_number));
            }
        });
        textViewConfirmHandover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.confirmHandoverFragment,getString(R.string.confirm_handover));
            }
        });
        textViewInventoryCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.inventoryCheckListFragment,getString(R.string.inventory_check));
            }
        });
        textViewSecondSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SecondSortFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if(!MainActivity.secondSortFragment.isAdded()){
                    transaction.remove(MainActivity.secondSortFragment);
                }
                if(MainActivity.currentFragment != null){
                    transaction.hide(MainActivity.currentFragment);
                }
                transaction.add(R.id.flContent, fragment).show(fragment).commit();
                MainActivity.currentFragment = fragment;
                MainActivity.secondSortFragment = fragment;
                //switchFragment(MainActivity.secondSortFragment);
            }
        });
        textViewNavLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                credentialManager.logout();
                getActivity().finish();
                Intent intent  = new Intent(getContext(), LoginActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
        });
        textViewAppUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_click_update_btn = true;
                /*updateDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.nav_system_update)
                        .setPositiveButton(R.string.button_confirm, null)
                        .setNeutralButton(" ", null)
                        .setMessage("目前版本: " + BuildConfig.VERSION_NAME + "\n" + "最新版本: 載入中...")
                        .create();
                updateDialog.show();*/
                mCheckTask  = new HomeFragment.UpdateTask();
                mCheckTask.execute();
            }
        });
        textViewHandlingUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.handlingUnitFragment,getString(R.string.handling_unit));
            }
        });
        getTextViewPutAwayTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.putAwayTaskFragment,getString(R.string.put_away_task));
            }
        });

        getTextViewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFragment(MainActivity.orderFragment,getString(R.string.order));
            }
        });

        ImageView iconBack = (ImageView) getActivity().findViewById(R.id.iconBack);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = MainActivity.homeFragment;
                FragmentManager fragmentManager = MainActivity.currentFragment.getActivity().getSupportFragmentManager();
                fragmentManager.popBackStackImmediate();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                if(!fragment.isAdded()){
                    if(MainActivity.currentFragment != null){
                        transaction.hide(MainActivity.currentFragment);
                    }
                    transaction.add(R.id.flContent, fragment).commit();
                }else{;
                    transaction.hide(MainActivity.currentFragment).show(fragment).commit();
                }
                MainActivity.currentFragment = fragment;
                MainActivity.homeFragment = fragment;
            }
        });

        int currentTimestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        String timestamp = credentialManager.getRequestUpdateAppDate();
        int originalTimestamp = Integer.parseInt(timestamp);
        //System.out.println(currentTimestamp - originalTimestamp);
        if(currentTimestamp - originalTimestamp> 1800){
            mCheckTask  = new HomeFragment.UpdateTask();
            mCheckTask.execute();
        }
        return root;
    }

    public class UpdateTask extends AsyncTask<Void, Void, RemoteResult> {
        @Override
        protected RemoteResult doInBackground(Void... params) {

            try {
                String url = credentialManager.getApiBase() + "system/update";
                final OkHttpClient client = new OkHttpClient();

                FormBody.Builder builder = new FormBody.Builder();
                builder.add("current_version", BuildConfig.VERSION_NAME);
                RequestBody formBody = builder.build();

                Request request = new Request.Builder()
                        .url(url)
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + credentialManager.getAccessToken())
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        throw new SecurityException();
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                }

                RemoteResult result = new Gson().fromJson(response.body().string(), HomeFragment.UpdateResult.class);

                if (result == null) {
                    throw new IOException("Server empty response");
                }

                return result;
            } catch (SecurityException e) {
                return new RemoteResult(-1);
            } catch (Exception e) {
                return new RemoteResult(65535);
            }
        }

        @Override
        protected void onPostExecute(final RemoteResult result) {
            mCheckTask = null;

            if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                String newVersion = ((HomeFragment.UpdateResult) result).getLatestVersion();
                if(!newVersion.equals(BuildConfig.VERSION_NAME) || is_click_update_btn){
                    is_click_update_btn = false;
                    updateDialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.nav_system_update)
                            .setPositiveButton(R.string.button_confirm, null)
                            //.setNeutralButton(" ", null)
                            .setNeutralButton("下載最新版本", null)
                            //.setMessage("目前版本: " + BuildConfig.VERSION_NAME + "\n" + "最新版本: 載入中...")
                            .setMessage("目前版本: " + BuildConfig.VERSION_NAME + "\n" + "最新版本: " + newVersion)
                            .create();
                    updateDialog.show();
                    if (updateDialog != null) {
                        //Boolean isDev = !BuildConfig.BUILD_TYPE.equals("release");
                    /*if (isDev) {
                        Toast.makeText(getActivity(), "請先解除安裝開發版\n然後再安裝正式版本\n長按【下載最新版本】下載 APK 檔案", Toast.LENGTH_SHORT).show();
                    }*/

                        /*updateDialog.setMessage(
                                "目前版本: " + BuildConfig.VERSION_NAME + "\n" +
                                        "最新版本: " + ((HomeFragment.UpdateResult) result).getLatestVersion()
                        );*/

                        //updateDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText("下載最新版本");

                        updateDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse( ((HomeFragment.UpdateResult) result).getUrl()));
                                startActivity(intent);
                                return true;
                            }
                        });

                        updateDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                downloadUpdate((HomeFragment.UpdateResult) result);
                            }
                        });
                    }
                }else{
                    //Toast.makeText(getActivity(),"已经是最新版本", Toast.LENGTH_LONG).show();
                }
                long timestamp = System.currentTimeMillis() / 1000;
                credentialManager.setRequestUpdateAppDate(String.valueOf(timestamp));
            } else {
                if (result.shouldLogout()) {
                    credentialManager.logout();
                    getActivity().finish();

                    Toast.makeText(getActivity(), ErrorHandler.getErrorMessage(getActivity(), result), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), ErrorHandler.getErrorMessage(getActivity(), result), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mCheckTask = null;
        }
    }

    private void downloadUpdate(HomeFragment.UpdateResult result) {
        showProgress(true);

        final Uri downloadUri = Uri.parse(result.getUrl());
        final Uri destinationUri = Uri.parse(getActivity().getExternalCacheDir() + "/app.apk");

        if (!getActivity().getExternalCacheDir().exists()) {
            getActivity().getExternalCacheDir().mkdirs();
        }

        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                        showProgress(false);

                        File toInstall = new File(downloadRequest.getDestinationURI().getPath());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri apkUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", toInstall);
                            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            intent.setData(apkUri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        } else {
                            Uri apkUri = Uri.fromFile(toInstall);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        Toast.makeText(getActivity(), "下載失敗", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                        updateProgress(totalBytes, downloadedBytes, progress);
                    }
                });

        ThinDownloadManager downloadManager = new ThinDownloadManager();
        downloadManager.add(downloadRequest);
    }

    protected void showProgress(Boolean value) {
        if (value) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setTitle(getString(R.string.message_please_wait));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            }
            mProgressDialog.show();
        } else {
            if (mProgressDialog != null) {
                mProgressDialog.hide();
            }
            mProgressDialog = null;
        }
    }

    private class UpdateResult extends RemoteResult {
        private String latest_version;
        private String url;

        public UpdateResult(Integer newStatus) {
            super(newStatus);
        }

        public String getLatestVersion() {
            return latest_version;
        }

        public String getUrl() {
            return url;
        }
    }

    public void switchFragment(Fragment fragment,String title) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(!fragment.isAdded()){
            if(MainActivity.currentFragment != null){
                transaction.hide(MainActivity.currentFragment);
            }
            transaction.add(R.id.flContent, fragment).commit();
        }else{
            transaction.hide(MainActivity.currentFragment).show(fragment).commit();
        }

        MainActivity.currentFragment = fragment;
        if(fragment.equals(new InventoryCheckTaskFragment())){
            MainActivity.inventoryCheckTaskFragment = fragment;
        } else if (fragment.equals(new InventoryCheckTaskItemFragment())) {
            MainActivity.inventoryCheckTaskItemFragment = fragment;
        }
        ((AppCompatActivity) getActivity()).getDelegate().getSupportActionBar().setTitle(title);
        /*FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();*/
    }
    protected void updateProgress(long totalBytes, long downloadedBytes, int progress) {
        if (mProgressDialog != null) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(new BigDecimal(totalBytes).intValueExact());
            mProgressDialog.setProgress(new BigDecimal(downloadedBytes).intValueExact());
        }
    }
}
