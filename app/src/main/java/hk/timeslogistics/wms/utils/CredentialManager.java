package hk.timeslogistics.wms.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonArray;

import java.util.Locale;
import java.util.Set;

import hk.timeslogistics.wms.Constants;

public class CredentialManager {

    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_ACCESS_TOKEN = "access_token";
    public static final String PROPERTY_PUSH_TOKEN = "push_token";
    private static final String PROPERTY_PERMISSIONS = "permission";
    private static final String PROPERTY_API_BASE = "api_base";
    private static final String PROPERTY_FRAGMENT_INDEX = "fragment_index";
    private static final String PROPERTY_LOCALE_LANGUAGE = "locale_language";
    private static final String PROPERTY_CLIENTS = "clients";
    private static final String PROPERTY_ZONES = "zones";
    private static final String PROPERTY_LOGISTICS_PROVIDERS = "logistics_Providers";
    private static final String PROPERTY_REQUEST_UPDATE_APP_DATE = "request_update_app_date";
    protected Context context;

    public CredentialManager(Context context) {
        this.context = context;
    }

    public String getPrefName() {
        return "credential";
    }

    public SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(getPrefName(), Context.MODE_PRIVATE);
    }

    /*
     * Login Related
     */
    public String getUsername() {
        return getSharedPreferences().getString(PROPERTY_USERNAME, "");
    }

    public String getAccessToken() {
        return getSharedPreferences().getString(PROPERTY_ACCESS_TOKEN, "");
    }

    public String getPushToken() {
        return getSharedPreferences().getString(PROPERTY_PUSH_TOKEN, "");
    }

    public Set<String> getPermissions() {
        return getSharedPreferences().getStringSet(PROPERTY_PERMISSIONS, null);
    }

    public String getApiBase() {
        return getSharedPreferences().getString(PROPERTY_API_BASE, Constants.API_URL_HK);
    }

    public void setUsername(String username) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_USERNAME, username);
        editor.apply();
    }

    public String getLocaleLanguage(){
        return  Locale.getDefault().getLanguage();
    }
    public void setLocaleLanguage() {
        String language = Locale.getDefault().getLanguage();
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_LOCALE_LANGUAGE, language);
        editor.apply();
    }

    public void setAccessToken(String token) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_ACCESS_TOKEN, token);
        editor.apply();
    }

    public void setApiBase(String apiBase) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_API_BASE, apiBase);
        editor.apply();
    }

    public void setPushToken(String pushToken) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_PUSH_TOKEN, pushToken);
        editor.apply();
    }

    public void setPermissions(Set<String> permissions) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putStringSet(CredentialManager.PROPERTY_PERMISSIONS, permissions);
        editor.apply();
    }

    public Boolean isLoggedIn() {
        String token = getSharedPreferences().getString(PROPERTY_ACCESS_TOKEN, "");
        return !token.isEmpty();
    }

    public Boolean isPushRegistered() {
        String token = getSharedPreferences().getString(PROPERTY_PUSH_TOKEN, "");
        return !token.isEmpty();
    }

    public void logout() {
        SharedPreferences.Editor editor = getSharedPreferences().edit().clear();
        editor.apply();
    }

    public String getFragmentIndex() {
        return getSharedPreferences().getString(PROPERTY_FRAGMENT_INDEX, "");
    }
    public void setFragmentIndex(String fragmentIndex){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_FRAGMENT_INDEX, fragmentIndex);
        editor.apply();
    }

    public void setClients(JsonArray clients){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_CLIENTS,clients.toString());
        editor.apply();
    }

    public void setZones(JsonArray zones){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_ZONES,zones.toString());
        editor.apply();
    }

    public String getClients(){
        return  getSharedPreferences().getString(CredentialManager.PROPERTY_CLIENTS,"");
    }

    public String getZones(){
        return  getSharedPreferences().getString(CredentialManager.PROPERTY_ZONES,"");
    }

    public void setLogisticsProviders(JsonArray logisticsProviders){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_LOGISTICS_PROVIDERS,logisticsProviders.toString());
        editor.apply();
    }

    public String getLogisticsProviders(){
        return  getSharedPreferences().getString(CredentialManager.PROPERTY_LOGISTICS_PROVIDERS,"");
    }

    public void setRequestUpdateAppDate(String  times){
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(CredentialManager.PROPERTY_REQUEST_UPDATE_APP_DATE,times.toString());
        editor.apply();
    }
    public String getRequestUpdateAppDate(){
        return  getSharedPreferences().getString(CredentialManager.PROPERTY_REQUEST_UPDATE_APP_DATE,"0");
    }
}