package hk.timeslogistics.wms;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import hk.timeslogistics.wms.utils.CredentialManager;
import hk.timeslogistics.wms.utils.ErrorHandler;
import hk.timeslogistics.wms.utils.RemoteResult;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A login screen that offers login
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private Spinner mServerSelector;
    private ImageView mImageView;
    private View mProgressView;
    private View mLoginFormView;
    private CredentialManager credentialManager;

    private int logoClicked = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mServerSelector = (Spinner) findViewById(R.id.serverSelector);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        if (mSignInButton != null) {
            mSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        credentialManager = new CredentialManager(this);

        // mServerSelector.set
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (++logoClicked >= 7) {
                    Toast.makeText(LoginActivity.this, "Server selector enabled", Toast.LENGTH_SHORT).show();
                    mServerSelector.setVisibility(View.VISIBLE);
                }
            }
        });

        mServerSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        credentialManager.setApiBase(Constants.API_URL_HK);
                        break;
                    case 1:
                        credentialManager.setApiBase(Constants.API_URL_TH);
                        break;
                    case 2:
                        credentialManager.setApiBase(Constants.API_URL_SG);
                        break;
                    case 3:
                        credentialManager.setApiBase(Constants.API_URL_CN);
                        break;
                    case 4:
                        credentialManager.setApiBase(Constants.API_URL_STAGING);
                        break;
                    case 5:
                        //credentialManager.setApiBase(Constants.API_URL_DEVELOPMENT);
                        credentialManager.setApiBase("http://192.168.4.10:8003/android/");
                        break;
                    case 6:
                        credentialManager.setApiBase(Constants.API_URL_HK_EP);
                        break;
                    case 7:
                        credentialManager.setApiBase(Constants.API_URL_MY);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (credentialManager.isLoggedIn()) {
            onLogin();
        }
    }

    private void onLogin() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);

            // Dismiss keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mUsernameView.getWindowToken(), 0);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, RemoteResult> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected RemoteResult doInBackground(Void... params) {
            try {
                String url = credentialManager.getApiBase() + "auth/login";
                final OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .build();

                RequestBody formBody = new FormBody.Builder()
                        .add("username", mUsername)
                        .add("password", mPassword)
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Gson gson = new Gson();
                return gson.fromJson(response.body().string(), LoginResult.class);
            } catch (Exception e) {
                Log.e("API", e.toString(), e);
                return new RemoteResult(65535);
            }
        }

        @Override
        protected void onPostExecute(final RemoteResult result) {
            mAuthTask = null;
            showProgress(false);

            if (result.getStatus().equals(ErrorHandler.STATUS_SUCCESS)) {
                credentialManager.setUsername(mUsername);
                credentialManager.setAccessToken(((LoginResult) result).getToken());
                credentialManager.setPermissions(((LoginResult) result).getPermissions());
                credentialManager.setClients(((LoginResult) result).getClients());
                credentialManager.setLogisticsProviders(((LoginResult) result).getLogisticsProviders());
                credentialManager.setZones(((LoginResult) result).getZones());
                onLogin();
            } else {
                //mUsernameView.setText("");
                //mPasswordView.setText("");
                if(!result.getStatus().toString().equals("1003")){
                    mUsernameView.setError(getString(R.string.error_login_incorrect));
                    mUsernameView.requestFocus();
                }
                Toast.makeText(LoginActivity.this, ErrorHandler.getErrorMessage(LoginActivity.this, result), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class LoginResult extends RemoteResult {
        String token;
        String is_select_temporary_bin;
        JsonArray clients;
        JsonArray zones;
        JsonArray temporary_bins;
        JsonArray logistics_providers;
        Set<String> permissions;

        public LoginResult(Integer newStatus) {
            super(newStatus);
        }

        public String getToken() {
            return token;
        }
        public String getIsSelectTemporaryBin() {
            return is_select_temporary_bin;
        }
        public Set<String> getPermissions () {
            return permissions;
        }
        public JsonArray getClients(){
            return clients;
        }
        public JsonArray getTemporaryBins(){
            return temporary_bins;
        }
        public JsonArray getLogisticsProviders(){
            return logistics_providers;
        }
        public JsonArray getZones(){
            return zones;
        }
    }
}