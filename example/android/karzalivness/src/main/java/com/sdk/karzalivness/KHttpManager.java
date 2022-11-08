package com.sdk.karzalivness;

import android.graphics.Bitmap;
import android.util.Log;

import com.sdk.karzalivness.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;

/**
 * Copyright (c) 2020 Karza Technologies. All rights reserved.
 **/
class KHttpManager {
    private static final String X_KARZA_KEY_HEADER = "karzatoken";
    public static String X_KARZA_KEY;
    private static final String TAG = "KHttpManager";
    private static final boolean LOGS = BuildConfig.DEBUG;
    private static final String crlf = "\r\n";
    private static final String twoHyphens = "--";
    private static final String boundary = "*****";
    private static final String charset = "UTF-8";


//    //*******************************************************************************************//
//    //*** Functions that were used earlier(not deleted as they might have some use in future) ***//
//
//    public static String doPost(String url, HashMap<String, String> hashMapParam) {
//        try {
//            HttpURLConnection httpConn = getTextHttpURLConnection(url);
//            if (httpConn == null) return null;
//
//            OutputStream output = httpConn.getOutputStream();
//            DataOutputStream request = new DataOutputStream(output);
//
//            if (hashMapParam != null) {
//                StringBuilder params = new StringBuilder();
//                for (String key : hashMapParam.keySet()) {
//                    params.append(key)
//                            .append("=")
//                            .append(hashMapParam.get(key))
//                            .append("&");
//
//                }
//
//                addFormField(request, params.toString().substring(0, params.toString().length() - 1));
//            }
//            request.flush();
//            request.close();
//
//            String result = "";
//            InputStream inputStream = null;
//            int responseCode = httpConn.getResponseCode();
//            if (responseCode == 200) {
//                inputStream = httpConn.getInputStream();
//            } else if (responseCode > 200) {
//                inputStream = httpConn.getErrorStream();
//            }
//            String line;
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            while ((line = reader.readLine()) != null) {
//                result += "\n" + line;
//            }
//            reader.close();
//
//
//            httpConn.disconnect();
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static String doPost2(String url, HashMap<String, String> params, HashMap<String, byte[]> files) {
//        JSONObject payload = new JSONObject();
//        try {
//
//            payload.put("karzaToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcnlfdGltZSI6IjE4LTAzLTIwMjBUMTY6MTQ6MDciLCJlbnYiOiJ0ZXN0IiwidXNlcl9pZCI6NzQwMjIzLCJjbGllbnRfaWQiOiJLYXJ6YV9UZWNoX3RWakl1WiIsInJlcXVlc3RfaWQiOiI1ZmY0NmE0OS1jZWMyLTQ2MTUtODk0NC0xMjNjZDgyMTQ3OGQiLCJzY29wZSI6WyJhYWRoYWFyX3htbCIsImxpdmVuZXNzIl19.hL4yFnb1ynyZcAOKfzvrh2D1k5UIT-UW0VEwWYmVTAg");
//            payload.put("livenessImage", "sdfsdfsdfsdfdsfds");
//        } catch (JSONException je) {
//            if (LOGS) Log.d(TAG, "JSON payload creation failed");
//            return null;
//        }
//        try {
//
//            HttpURLConnection httpConn = getTextHttpURLConnectionJson("https://app.karza.in/dev/videokyc/api/validate-token");
//            if (httpConn == null) return null;
//
//            OutputStream output = httpConn.getOutputStream();
//            OutputStream request = new DataOutputStream(output);
//            request.write(payload.toString().getBytes("UTF-8"));
////            request.flush();
//            request.close();
//            String result = "";
//            if (LOGS)
//                Log.e(TAG, "code: " + httpConn.getResponseCode() + " reason: " + httpConn.getResponseMessage());
//            InputStream inputStream = null;
//            int responseCode = httpConn.getResponseCode();
//            if (responseCode == 200) {
//                inputStream = httpConn.getInputStream();
//            } else if (responseCode == 402) {
//                httpConn.disconnect();
//                return "PAYMENT_REQUIRED";
//            } else if (responseCode == 503) {
//                httpConn.disconnect();
//                return "SERVICE_UNAVAILABLE";
//            } else if (responseCode > 200) {
//                inputStream = httpConn.getErrorStream();
//            }
//            String line;
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            while ((line = reader.readLine()) != null) {
//                result += "\n" + line;
//            }
//            reader.close();
//
//            httpConn.disconnect();
//            if (LOGS) Log.e("Karza-Response", result);
//            return result;
//        } catch (IOException ioe) {
//            Log.d(TAG, "Failed to create Http Object");
//        }
//
//        return null;
//    }
//
//    public static String doPost(String url, HashMap<String, String> params, HashMap<String, byte[]> files) {
//        try {
//            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
//            if (httpConn == null) {
//                Log.e(TAG, "create http failed");
//                KNetworkUtil.KNetResult.responseCode = -1;
//                return makeResultString(-2);
//            }
//            int status = getHttpURLConnection(url, httpConn);
//            if (status != 0) {
//                KNetworkUtil.KNetResult.responseCode = -1;
//                return makeResultString(status);
//            }
//
//            OutputStream output = httpConn.getOutputStream();
//            DataOutputStream request = new DataOutputStream(output);
//
//            if (params != null) {
//                for (String key : params.keySet()) {
//                    Log.d(TAG,"called http");
//                    addFormField(request, key, params.get(key));
//                }
//            }
//
//            if (files != null) {
//                for (String key : files.keySet()) {
//                    Log.d(TAG,"called image http");
//                    addFormImageField(request, key, files.get(key), key + ".jpg");
//                }
//            }
//
//            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
//            request.flush();
//            request.close();
//
//            String result = "";
//            Log.e(TAG, "code: " + httpConn.getResponseCode() + " reason: " + httpConn.getResponseMessage());
//            InputStream inputStream = null;
//            int responseCode = httpConn.getResponseCode();
//            KNetworkUtil.KNetResult.responseCode = responseCode;
//            if (responseCode == 200) {
//                inputStream = httpConn.getInputStream();
//            } else if (responseCode == 402) {
//                httpConn.disconnect();
//                return "PAYMENT_REQUIRED";
//            } else if (responseCode == 503) {
//                httpConn.disconnect();
//                return "SERVICE_UNAVAILABLE";
//            } else if (responseCode > 200) {
//                inputStream = httpConn.getErrorStream();
//            }
//            String line;
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            while ((line = reader.readLine()) != null) {
//                result += "\n" + line;
//            }
//            reader.close();
//
//            httpConn.disconnect();
//            Log.e("Karza-Response", result);
//            return result;
//        } catch (IOException e) {
//            Log.d(TAG, e.toString());
//        }
//        return null;
//    }
//
//    public static KNetResult doPostSilent(String token, String url, HashMap<String, String> params, HashMap<String, byte[]> files) {
//        KNetResult kNetResult = new KNetResult();
//        try {
//            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
//            if (httpConn == null) {
//                Log.e(TAG, "create http failed");
//            }
//            int status = getHttpURLConnection(token, url, httpConn);
////            if (status != 0) {
////                KNetworkUtil.KNetResult.responseCode = -1;
////            }
//
//            OutputStream output = httpConn.getOutputStream();
//            DataOutputStream request = new DataOutputStream(output);
//
//            if (params != null) {
//                for (String key : params.keySet()) {
//                    if (LOGS) Log.d(TAG, "called http");
//                    addFormField(request, key, params.get(key));
//                }
//            }
//
//            if (files != null) {
//                for (String key : files.keySet()) {
//                    if (LOGS) Log.d(TAG, "called image http");
//                    addFormImageField(request, key, files.get(key), key + ".jpg");
//                }
//            }
//
//            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
//            request.flush();
//            request.close();
//
//            String result = "";
//            if (LOGS)
//                Log.e(TAG, "code: " + httpConn.getResponseCode() + " reason: " + httpConn.getResponseMessage());
//            InputStream inputStream = null;
//            int responseCode = httpConn.getResponseCode();
//            kNetResult.setResponseCode(responseCode);
//            if (responseCode == 200) {
//                inputStream = httpConn.getInputStream();
//                kNetResult.setmNetworkResultStatus(true);
//                String line;
//
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                while ((line = reader.readLine()) != null) {
//                    result += "\n" + line;
//                }
//                reader.close();
//
//                httpConn.disconnect();
//                if (LOGS) Log.e(TAG, "Api request : " + result);
//                kNetResult.setmNetworkResultsJsonString(result);
//                JSONObject responseObject = new JSONObject(result);
//                double score = responseObject.getJSONObject("result").getJSONObject("data").getJSONObject("liveness").getJSONObject("result").getDouble("score");
//                kNetResult.setScore(score);
//            }
//        } catch (IOException e) {
//            Log.d(TAG, e.toString());
//        } catch (JSONException je) {
//            Log.d(TAG, je.toString());
//        }
//        return kNetResult;
//    }
//
//    private static String makeResultString(int status) {
//        JSONObject jsonObject = new JSONObject();
//        String strStatus = null;
//        if (status == -1) {
//            strStatus = "CONNECTION_TIMEOUT";
//        } else {
//            strStatus = "CONNECTION_ERROR";
//        }
//        try {
//            jsonObject.put("mNetworkResultStatus", strStatus);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObject.toString();
//    }


    //************************************************************************//
    //***************** getHttpURLConnection Methods *************************//
//
//    private static int getHttpURLConnection(String token, String url
//            , HttpURLConnection httpConn) {
//        try {
//            httpConn.setUseCaches(false);
//            httpConn.setDoOutput(true);
//            httpConn.setConnectTimeout(10000);
//            httpConn.setRequestMethod("POST");
//            httpConn.setRequestProperty("Connection", "Keep-Alive");
//            httpConn.setRequestProperty("Cache-Control", "no-cache");
//            httpConn.setRequestProperty(X_KARZA_KEY_HEADER, token);
//            httpConn.setRequestProperty("Referer", "https://app.karza.in/dev/videokyc/kyc-session");
//            httpConn.setRequestProperty(
//                    "Content-Type", "multipart/form-data;boundary=" + boundary);
//            httpConn.connect();
//        } catch (SocketTimeoutException e) {
//            return -1;
//        } catch (IOException e) {
//            return -3;
//        }
//        return 0;
//    }
//
//    private static HttpURLConnection getTextHttpURLConnection(String url) throws IOException {
//        HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
//        if (httpConn == null) {
//            Log.e(TAG, "create http failed");
//            return null;
//        }
//        httpConn.setReadTimeout(3000);
//        httpConn.setConnectTimeout(3000);
//        httpConn.setRequestMethod("POST");
//        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//        httpConn.setRequestProperty("Connection", "Keep-Alive");
//        httpConn.setRequestProperty("Charset", "UTF-8");
//        httpConn.setRequestProperty("Cache-Control", "no-cache");
//        httpConn.setDoInput(true);
//        httpConn.setDoOutput(true);
//        httpConn.connect();
//        return httpConn;
//    }
//
//    private static HttpURLConnection getTextHttpURLConnectionJson(String url) throws IOException {
//        HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
//        if (httpConn == null) {
//            Log.e(TAG, "create http failed");
//            return null;
//        }
//        httpConn.setRequestMethod("POST");
//        httpConn.setRequestProperty("Content-Type", "application/json");
//        httpConn.setRequestProperty("Accept", "application/json, text/plain, */*");
//        httpConn.setRequestProperty("Referer", "https://app.karza.in/dev/videokyc/kyc-session");
//        httpConn.setRequestMethod("POST");
////        httpConn.setRequestProperty("Charset", "UTF-8");
////        httpConn.setRequestProperty("Cache-Control", "no-cache");
////        httpConn.setDoInput(true);
////        httpConn.setDoOutput(true);
//        httpConn.connect();
//        return httpConn;
//    }


    //************************************************************************//
    //********************* Add Form Field Methods ***************************//

    private static void addFormField(DataOutputStream request, String param) throws IOException {
        request.writeBytes(param);
    }

    /**
     * Adds a form field to the request
     *
     * @param request the request output stream
     * @param name    field name
     * @param value   field value
     * @param value   field value
     * @throws IOException
     */
    public static void addFormField(DataOutputStream request, String name, String value) throws IOException {
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                name + "\"" + crlf);
        request.writeBytes("Content-Type: text/plain; charset=" + charset + crlf);
        request.writeBytes(crlf);
        request.writeBytes(value + crlf);
    }

    /**
     * Adds a form field to the request
     *
     * @param request  the request output stream
     * @param name     field name
     * @param image    the bitmap that will be compress to JPEG with quality 90.
     * @param filename the filename of this image
     * @throws IOException
     */
    public static void addFormImageField(DataOutputStream request, String name, Object image
            , String filename) throws IOException {
        request.writeBytes(twoHyphens + boundary + crlf);
        request.writeBytes("Content-Disposition: form-data; name=\"" +
                name + "\"; filename=\"" +
                filename + "\"" + crlf);
        request.writeBytes("Content-Type: image/jpeg" + crlf);
        request.writeBytes(crlf);
        if (image instanceof Bitmap) {
            ((Bitmap) image).compress(Bitmap.CompressFormat.JPEG, 90, request);
        } else {
            request.write((byte[]) image);
        }
        request.writeBytes(crlf);
    }
}
