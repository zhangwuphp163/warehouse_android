package hk.timeslogistics.wms.utils;

public class UrlMapping {
    public static String getUrl(CredentialManager credentialManager,String operation){
        switch (operation){
            case "asn-list":
                return credentialManager.getApiBase() + "inbound/list";
            case "asn-items":
                return credentialManager.getApiBase() + "inbound/asn-items";
            case "get-clients":
                return credentialManager.getApiBase() + "base/clients";
            case "inventory-check-list":
                return credentialManager.getApiBase() + "inventory-check/list";
            case "inventory-check-task-list":
                return credentialManager.getApiBase() + "inventory-check/task-list";
            case "inventory-check-task-completed":
                return credentialManager.getApiBase() + "inventory-check/task-completed";
            case "inventory-check-task-items":
                return credentialManager.getApiBase() + "inventory-check/task-items";
            case "inventory-check-task-scan":
                return credentialManager.getApiBase() + "inventory-check/task-scan";
            case "inventory-check-task-recounting":
                return credentialManager.getApiBase() + "inventory-check/task-recounting";
            case "handling-unit-list":
                return credentialManager.getApiBase() + "pick-wave/handling-unit-list";
            default:
                return "";
        }
    }
    public static String getToken(CredentialManager credentialManager){
        return  credentialManager.getAccessToken();
    }
}
