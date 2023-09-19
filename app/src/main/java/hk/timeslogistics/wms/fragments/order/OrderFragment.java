package hk.timeslogistics.wms.fragments.order;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import hk.timeslogistics.wms.R;
import hk.timeslogistics.wms.models.InventoryModel;
import hk.timeslogistics.wms.models.OrderItemModel;
import hk.timeslogistics.wms.utils.AlertManager;
import hk.timeslogistics.wms.utils.ClearEditTextContent;
import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.HttpSubmitTask;
import hk.timeslogistics.wms.utils.RemoteResult;
import hk.timeslogistics.wms.utils.TomProgress;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class OrderFragment extends Fragment {
    private static final int REQUEST_SUBMIT = 1;
    private static final int RESULT_OK = -1;
    private CredentialManager credentialManager;
    private EditText editTextNumber;
    private TextView clientName;
    private TextView orderNumber;
    private TextView trackingNumber;
    private TextView shipFrom;
    private TextView shipFromAddress;
    private TextView shipTo;
    private TextView shipToAddress;
    private TextView createdAt;
    private TextView allocateAt;
    private TextView secondSortAt;
    private TextView packAt;
    private TextView handoverAt;
    private Spinner numberType;
    private OrderFragment.SubmitTask submitTask;
    ArrayList<OrderItemModel> orderItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        credentialManager = new CredentialManager(getActivity());
        credentialManager.setFragmentIndex("");
        orderItems = new ArrayList<>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_order, null);

        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle(R.string.order);
        getActivity().findViewById(R.id.iconBack).setBackgroundResource(R.drawable.back);
        editTextNumber = (EditText) root.findViewById(R.id.editTextNumber);
        editTextNumber.setShowSoftInputOnFocus(false);
        ClearEditTextContent.setupClearEditText(getContext(),editTextNumber);
        numberType = (Spinner) root.findViewById(R.id.numberType);
        ImageButton buttonScanNumber = (ImageButton) root.findViewById(R.id.buttonScanNumber);

        clientName = (TextView) root.findViewById(R.id.client_name);
        orderNumber = (TextView) root.findViewById(R.id.order_number);
        trackingNumber = (TextView) root.findViewById(R.id.tracking_numbers);
        shipFrom = (TextView) root.findViewById(R.id.ship_from);
        shipFromAddress = (TextView) root.findViewById(R.id.ship_from_address);
        shipTo = (TextView) root.findViewById(R.id.ship_to);
        shipToAddress = (TextView) root.findViewById(R.id.ship_to_address);
        createdAt = (TextView) root.findViewById(R.id.created_at);
        allocateAt = (TextView) root.findViewById(R.id.allocate_at);
        secondSortAt = (TextView) root.findViewById(R.id.second_sort_at);
        packAt = (TextView) root.findViewById(R.id.pack_at);
        handoverAt = (TextView) root.findViewById(R.id.handover_at);

        buttonScanNumber.setOnClickListener(v -> IntentIntegrator.forSupportFragment(OrderFragment.this).initiateScan());

        editTextNumber.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                try {
                    submit();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                editTextNumber.setText("");
                return true;
            }
            return false;
        });
        editTextNumber.requestFocus();

        return root;
    }

    private void resetText(){
        clientName.setText("");
        orderNumber.setText("");
        trackingNumber.setText("");
        shipFrom.setText("");
        shipFromAddress.setText("");
        shipTo.setText("");
        shipToAddress.setText("");
        createdAt.setText("");
        packAt.setText("");
        handoverAt.setText("");
        allocateAt.setText("");
        secondSortAt.setText("");
        orderItems.clear();
    }
    private void submit() throws JSONException {
        String number = editTextNumber.getText().toString();
        String number_type = numberType.getSelectedItem().toString();
        String language = Locale.getDefault().getLanguage();
        TomProgress.showProgress(getContext(),true);
        JSONObject requestData = new JSONObject();
        requestData.put("number", number);
        requestData.put("number_type", number_type);
        requestData.put("language", language);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),requestData.toString());
        submitTask = new SubmitTask(getContext(),requestBody,credentialManager.getApiBase() + "order/query");
        submitTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Zxing
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (resultCode == OrderFragment.RESULT_OK) {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    editTextNumber.setText(result.getContents());
                    try {
                        submit();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
            }
        } else if (requestCode == REQUEST_SUBMIT) {
            if (resultCode == OrderFragment.RESULT_OK) {
                cancel();
            }
        }
    }

    private void cancel() {

    }

    public class OrderAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return orderItems.size();
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
            @SuppressLint("ViewHolder") View v = View.inflate(getActivity().getApplicationContext(),R.layout.order_item_list_view,null);
            String sku_barcode = orderItems.get(position).sku_barcode;
            String request_qty = orderItems.get(position).request_qty;
            String actual_qty = orderItems.get(position).actual_qty;

            TextView textViewRequestQty = (TextView) v.findViewById(R.id.textViewRequestQty);
            TextView textViewActualQty = (TextView) v.findViewById(R.id.textViewActualQty);
            TextView textViewSkuBarcode = (TextView) v.findViewById(R.id.textViewSkuBarcode);
            textViewSkuBarcode.setText(sku_barcode);
            textViewRequestQty.setText(request_qty);
            textViewActualQty.setText(actual_qty);
            return v;
        }
    }

    public void setInfo(JsonObject orderDetail){
        String client_name = orderDetail.get("client_name").getAsString();
        String order_number = orderDetail.get("order_number").getAsString();
        String tracking_numbers = orderDetail.get("tracking_numbers").getAsString();
        String ship_from = orderDetail.get("ship_from").getAsString();
        String ship_from_address = orderDetail.get("ship_from_address").getAsString();
        String ship_to = orderDetail.get("ship_to").getAsString();
        String ship_to_address = orderDetail.get("ship_to_address").getAsString();
        String created_at = orderDetail.get("created_at").getAsString();
        String pack_at = orderDetail.get("pack_at").getAsString();
        String handover_at = orderDetail.get("handover_at").getAsString();
        String allocate_at = orderDetail.get("allocate_at").getAsString();
        String second_sort_at = orderDetail.get("second_sort_at").getAsString();

        clientName.setText(Html.fromHtml(client_name));
        orderNumber.setText(Html.fromHtml(order_number));
        trackingNumber.setText(Html.fromHtml(tracking_numbers));
        shipFrom.setText(Html.fromHtml(ship_from));
        shipFromAddress.setText(Html.fromHtml(ship_from_address));
        shipTo.setText(Html.fromHtml(ship_to));
        shipToAddress.setText(Html.fromHtml(ship_to_address));
        createdAt.setText(Html.fromHtml(created_at));
        packAt.setText(Html.fromHtml(pack_at));
        handoverAt.setText(Html.fromHtml(handover_at));
        allocateAt.setText(Html.fromHtml(allocate_at));
        secondSortAt.setText(Html.fromHtml(second_sort_at));
        orderItems.clear();
        JsonArray items = orderDetail.get("items").getAsJsonArray();
        for (int i = 0;i<items.size();i++){
            JsonObject item = (JsonObject) items.get(i).getAsJsonObject();
            OrderItemModel orderItemModel = new OrderItemModel(item);
            orderItems.add(orderItemModel);
        }
        ListView listView = (ListView) getActivity().findViewById(R.id.orderItemListView);
        OrderAdapter orderAdapter = new OrderAdapter();
        listView.setAdapter(orderAdapter);

        /*AlertManager.okay(getContext());
        Toast.makeText(getContext(), getString(R.string.success), Toast.LENGTH_LONG).show();*/
    }

    @SuppressLint("StaticFieldLeak")
    private class SubmitTask extends HttpSubmitTask {
        public SubmitTask(Context context, RequestBody formBody, String url) {
            super(context, formBody, url);
        }
        @Override
        protected void onPostExecute(final RemoteResult result) {
            submitTask = null;
            TomProgress.showProgress(getContext(),false);
            editTextNumber.setText("");
            if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                if(result.getItems().size() > 1){
                    JsonArray items = result.getItems().getAsJsonArray();
                    String[] clients = new String[items.size()];
                    for(int i=0;i<items.size();i++){
                        JsonObject item = items.get(i).getAsJsonObject();
                        clients[i] = item.get("client_name").toString();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("多个相同订单,请选择订单客户：");
                    builder.setItems(clients, (dialog, which) -> setInfo(result.getItems().get(which).getAsJsonObject()));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else if(result.getItems().size() == 1){
                    setInfo(result.getItems().get(0).getAsJsonObject());
                }
            } else {
                resetText();
                AlertManager.error(getContext());
                Toast.makeText(getContext(), result.getPayload(), Toast.LENGTH_LONG).show();
            }
        }
    }
}

