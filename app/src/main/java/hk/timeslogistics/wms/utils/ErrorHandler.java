package hk.timeslogistics.wms.utils;


import android.content.Context;

import hk.timeslogistics.wms.R;

public class ErrorHandler {

    public static final int STATUS_SUCCESS = 0;

    public static String getErrorMessage(Context content, RemoteResult result) {

        switch (result.getStatus()) {
            case -1:
                return content.getString(R.string.error_invalid_token);

            case 1:
                return content.getString(R.string.error_missing_parameter);

            case 1001:
                return content.getString(R.string.error_username_not_exist);
            case 1002:
                return content.getString(R.string.error_password_incorrect);
            case 1003:
                return content.getString(R.string.account_not_bound_centre);

            case 3001:
                return String.format(content.getString(R.string.error_unique_number_not_exist), result.getPayload());
            case 3002:
                return String.format(content.getString(R.string.error_unique_number_already_consolidated), result.getPayload());

            case 3101:
                return String.format(content.getString(R.string.error_tracking_number_not_exist), result.getPayload());
            case 3102:
                return String.format(content.getString(R.string.error_tracking_number_already_consolidated), result.getPayload());
            case 3103:
                return String.format(content.getString(R.string.error_tracking_number_not_consolidated), result.getPayload());
            case 3104:
                return String.format(content.getString(R.string.error_tracking_number_already_outbound), result.getPayload());

            case 3201:
                return String.format(content.getString(R.string.error_tracking_number_no_belongs_to_bin), result.getPayload());

            case 4001:
                return String.format(content.getString(R.string.error_bin_number_not_exist), result.getPayload());
            case 4002:
                return String.format(content.getString(R.string.error_bin_number_not_empty), result.getPayload());
            case 4003:
                return String.format(content.getString(R.string.error_bin_number_occupied), result.getPayload());

            case 50000:
                return String.format(content.getString(R.string.message_internal_server_error));
            case 50001:
                return String.format(content.getString(R.string.error_user_no_centre_id));

            case 65535:
                return content.getString(R.string.msg_connection_error);
            default:
                return content.getString(R.string.msg_unknown_error) + ": " + result.getStatus();
        }


    }

}