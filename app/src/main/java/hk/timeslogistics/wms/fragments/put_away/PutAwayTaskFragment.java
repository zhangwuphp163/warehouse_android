package hk.timeslogistics.wms.fragments.put_away;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Locale;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.InventoryModel;
import hk.timeslogistics.wms.models.PutAwayTaskModel;
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

public class PutAwayTaskFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextContainerNumber;

    private ImageButton buttonScanContainerNumber;

    protected Button buttonSubmit;
    protected Button buttonCancel;

    private PutAwayTaskFragment.SubmitTask submitTask;

    ListView listView;
    ArrayList<PutAwayTaskModel> putAwayTaskModels;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        putAwayTaskModels = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_put_away_task, null);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.put_away_task);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextContainerNumber = (EditText) root.findViewById(R.id.editTextContainerNumber);
        buttonScanContainerNumber = (ImageButton) root.findViewById(R.id.buttonScanContainerNumber);

        editTextContainerNumber.setFocusableInTouchMode(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextContainerNumber);
        buttonScanContainerNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.forSupportFragment(PutAwayTaskFragment.this).initiateScan();
            }
        });

        editTextContainerNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    getList();
                    return true;
                }
                return false;
            }
        });
        getList();
        editTextContainerNumber.requestFocus();

        return root;
    }
    private void getList() {
        String number = editTextContainerNumber.getText().toString();
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("container_number", number);
        RequestBody formBody = builder.build();
        submitTask = new SubmitTask(getContext(),formBody,credentialManager.getApiBase() + "put-away/tasks");
        submitTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == PutAwayTaskFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    editTextContainerNumber.setText(result.getContents());
                    getList();
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == PutAwayTaskFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void cancel() {

    }

    public class InventoryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return putAwayTaskModels.size();
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
            @SuppressLint("ViewHolder") View v = View.inflate(getActivity().getApplicationContext(),R.layout.put_away_task_list_view,null);
            //String status = putAwayTaskModels.get(position).status;
            String status_text = putAwayTaskModels.get(position).status_text;
            String created_at = putAwayTaskModels.get(position).created_at;
            String container_number = putAwayTaskModels.get(position).container_number;
            TextView textViewStatus = (TextView) v.findViewById(R.id.textViewStatus);
            TextView textViewContainerNumber = (TextView) v.findViewById(R.id.textViewContainerNumber);
            TextView textViewCreatedAt = (TextView) v.findViewById(R.id.textViewCreatedAt);
            textViewStatus.setText(status_text);
            textViewContainerNumber.setText(container_number);
            textViewCreatedAt.setText(created_at);
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
            if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                JsonArray items = result.getItems();
                putAwayTaskModels.clear();
                for (int i = 0;i<items.size();i++){
                    JsonObject item = (JsonObject) items.get(i);
                    PutAwayTaskModel putAwayTaskModel = new PutAwayTaskModel(item);
                    putAwayTaskModels.add(putAwayTaskModel);
                }
                listView = (ListView) getActivity().findViewById(R.id.put_away_task_list_view);
                InventoryAdapter inventoryAdapter = new InventoryAdapter();
                listView.setAdapter(inventoryAdapter);
            } else {
                TomAlertDialog.showAlertDialogMessage(getContext(),result.getPayload());
            }
        }
    }
}

