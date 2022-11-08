package com.sdk.karzalivness;

import com.sdk.karzalivness.R;

import java.util.HashMap;

class KNetResult {

    private boolean mNetworkResultStatus = false;
    private String mNetworkResultsJsonString = "";
    private double score = 0.0;
    private int responseCode = 0;
    private String requestId = "";


    //************************************************************************//
    //************************************************************************//

    protected HashMap<String, Integer> mHashMap = new HashMap<>();

    protected void addSpecialMsg() {
    }

    public KNetResult() {
        mHashMap.put("INVALID_ARGUMENT", R.string.string_network_error_invalid_argument);
        mHashMap.put("DOWNLOAD_ERROR", R.string.string_network_error_download_error);
        mHashMap.put("UNAUTHORIZED", R.string.string_network_error_unauthorized);
        mHashMap.put("RATE_LIMIT_EXCEEDED", R.string.string_network_error_rate_limit_exceeded);
        mHashMap.put("NOT_FOUND", R.string.string_network_error_not_found);
        mHashMap.put("INTERNAL_ERROR", R.string.string_network_error_internal_error);
        mHashMap.put("RPC_TIMEOUT", R.string.string_network_error_rpc_timeout);
        mHashMap.put("CONNECTION_TIMEOUT", R.string.string_network_error_timeout);
        mHashMap.put("CONNECTION_ERROR", R.string.string_network_error_connect_host_error);
        mHashMap.put("SERVICE_UNAVAILABLE", R.string.string_network_error_service_unavailable);
        mHashMap.put("PAYMENT_REQUIRED", R.string.string_network_error_payment_required);
        addSpecialMsg();
    }



    //************************************************************************//
    //********************* Getters & Setters ********************************//

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setmNetworkResultsJsonString(String mNetworkResultsJsonString) {
        this.mNetworkResultsJsonString = mNetworkResultsJsonString;
    }

    public void setmNetworkResultStatus(boolean mNetworkResultStatus) {
        this.mNetworkResultStatus = mNetworkResultStatus;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    //-------------------------------//

    public int getResponseCode() {
        return this.responseCode;
    }

    public double getScore() {
        return this.score;
    }

    public String getmNetworkResultsJsonString() {
        return this.mNetworkResultsJsonString;
    }

    public boolean getmNetworkResultStatus() {
        return this.mNetworkResultStatus;
    }

    private int getErrorMsgIDByStatus(String status) {
        if (mHashMap.containsKey(status)) {
            return mHashMap.get(status);
        }

        return -1;
    }

    public String getRequestId() {
        return requestId;
    }

}