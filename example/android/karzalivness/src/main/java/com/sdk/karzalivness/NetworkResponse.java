package com.sdk.karzalivness;

import java.util.HashMap;

class NetworkResponse {

    private int status;
    private String dataString;
    private HashMap<String, String> cookies;

    public NetworkResponse() {
    }

    public NetworkResponse(int status, String dataString) {
        this.status = status;
        this.dataString = dataString;
    }

    public NetworkResponse(int status, String dataString, HashMap<String, String> cookies) {
        this.status = status;
        this.dataString = dataString;
        this.cookies = cookies;
    }


    //****************************************************************//
    //*********************** Getter Functions ***********************//

    public int getStatus() {
        return status;
    }

    public String getDataString() {
        return dataString;
    }

    public HashMap<String, String> getCookies() {
        return cookies;
    }


    //****************************************************************//
    //******************** Other Functions ***************************//

    @Override
    public String toString() {
        return "NetworkResponse{" +
                "status=" + status +
                ", dataString='" + dataString + '\'' +
                ", cookies=" + cookies +
                '}';
    }
}
