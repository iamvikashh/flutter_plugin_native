package com.sdk.karzalivness;

import android.util.Log;

import androidx.annotation.Nullable;

import com.sdk.karzalivness.enums.KLiveStatus;

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
import java.util.Arrays;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Copyright (c) 2020 Karza Technologies. All rights reserved.
 **/
class KNetworkUtil {
    private final static String TAG = KNetworkUtil.class.getSimpleName();
    private final static boolean LOGS = BuildConfig.DEBUG;
    private static final String NETWORK_RESPONSE = "NETWORK_RESPONSE";
    private static final String X_KARZA_KEY_HEADER = "karzatoken";
    // The hack score threshold value is 0.98 which is get by training from big industry data,
    // he/she is real person if score smaller than the threshold.
    private static final double ANTI_HACK_THRESHOLD = 0.98f;
    private static final String crlf = "\r\n";
    private static final String twoHyphens = "--";
    private static final String boundary = "*****";
    private static final String charset = "UTF-8";
    private final NetworkResultContract contract;
    private final String environment;
    public static String token;

    public KNetworkUtil(NetworkResultContract contract, String environment) {
        this.contract = contract;
        this.environment = environment;
    }


    //************************************************************************//
    //******************** API Call Functions ********************************//

    public KNetResult doAntiHack(String token, String base64, String metaData, @Nullable JSONObject txtDetails, KConfig config) {

        HashMap<String, String> param = new HashMap<>();
//        if (config.useLandmark()) {
//            String StrLandmarks = Arrays.toString(landmark);
//            if (LOGS) Log.i(TAG, "String Type Landmarks -> " + StrLandmarks);
//            param.put("landmarks", StrLandmarks);
//        }
        param.put("imageB64", base64);
        param.put("metadata", metaData);
        param.put("imageType", config.getIsJPEG() ? "jpeg" : "png");
        if (txtDetails != null) {
            if (txtDetails.length() != 0) {
                param.put("freeTextDetails", txtDetails.toString());
            }
        }

        return doInternal(token, BuildConfig.BASE_URL.replace("env"
                , environment) + KConstants.SILENT_ANTI_HACK_URL, param);

    }


    private KNetResult doInternal(String token, String url, HashMap<String, String> hashParams) {
        if (LOGS) Log.d("URL", url);
        return doPostSilent(token, url, hashParams);
    }


    public KNetResult doPostSilent(String token, String url, HashMap<String, String> params) {
        KNetResult nResult = new KNetResult();
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
            if (httpConn == null) {
                Log.e(TAG, "create http failed");
            }
            int status = getHttpURLConnection(token, url, httpConn);
            if (status != 0) {
                return nResult;
            }
            OutputStream output = httpConn.getOutputStream();
            DataOutputStream request = new DataOutputStream(output);

            if (params != null) {
                for (String key : params.keySet()) {
                    if (LOGS) Log.d(TAG, "called http");
                    KHttpManager.addFormField(request, key, params.get(key));
                }
            }
//            if (files != null) {
//                for (String key : files.keySet()) {
//                    Log.d(TAG,"called image http");
//                    KHttpManager.addFormImageField(request, key, files.get(key), key + ".jpg");
//                }
//            }
            request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
            request.flush();
            request.close();


            //******************* Here we get the response and we digest it. **********************//

            if (LOGS)
                Log.e(TAG, "code: " + httpConn.getResponseCode() + " reason: " + httpConn.getResponseMessage());

            int responseCode = httpConn.getResponseCode();
            String responseString;
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                responseString = readStream(httpConn.getInputStream());
            } else {
                responseString = readStream(httpConn.getErrorStream());
            }
            JSONObject responseObject = new JSONObject(responseString);

            if (LOGS) {
                Log.i(NETWORK_RESPONSE, "Response => " + url + " ------------ " + responseString);
            }
            httpConn.disconnect();


            if (responseCode == 200) {
                nResult.setmNetworkResultsJsonString(responseString);
                nResult.setRequestId(responseObject.optString("requestId", ""));

                String apiStatusCode = responseObject.optString("statusCode", "0");
                if (apiStatusCode.equals("101")) {
                    nResult.setResponseCode(KLiveStatus.SUCCESS.statusCode);
                    double score = responseObject.getJSONObject("result").getJSONObject("data")
                            .getJSONObject("liveness").getJSONObject("result").getDouble("score");
                    nResult.setScore(score);
                } else {
                    nResult.setResponseCode(KLiveStatus.get(Integer.parseInt(apiStatusCode)).statusCode);
                }
            } else {
                nResult.setResponseCode(KLiveStatus.get(responseCode).statusCode);
                nResult.setRequestId(responseObject.optString("requestId", ""));
            }


        } catch (IOException | JSONException e) {
            if (contract != null) {
                contract.onNetworkResult(KConstants.IO_EXCEPTION
                        , KUtility.getStackTraceString(e));
            }
        }
        return nResult;
    }


    //************************************************************************//
    //********************** Other Helper Functions **************************//

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    private int getHttpURLConnection(String token, String url
            , HttpURLConnection httpConn) {
        try {
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setConnectTimeout(10000);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Cache-Control", "no-cache");
            httpConn.setRequestProperty(X_KARZA_KEY_HEADER, token);
            httpConn.setRequestProperty("Referer", BuildConfig.BASE_URL.replace("env", environment) + KConstants.LIVENESS_CALL_REFERER);
            httpConn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            httpConn.connect();
        } catch (SocketTimeoutException e) {
            if (contract != null) {
                contract.onNetworkResult(KConstants.SOCKET_EXCEPTION
                        , KUtility.getStackTraceString(e));
            }
            return -1;
        } catch (IOException e) {
            if (contract != null) {
                contract.onNetworkResult(KConstants.IO_EXCEPTION
                        , KUtility.getStackTraceString(e));
            }
            return -3;
        }
        return 0;
    }


    //************************************************************************//
    //****************** Network Result Contract / Interface *****************//

    public interface NetworkResultContract {
        void onNetworkResult(int statusCode, String throwable);
    }

//     public static class KNetResultRecognize extends KNetResult {
//         @Override
//         protected void addSpecialMsg() {
//             mHashMap.put("DETECTION_FAILED", R.string.string_network_error_recognization_detection_failed);
//         }
//     }

//    public static class KNetResultLiveness extends KNetResult {
//        @Override
//        protected void addSpecialMsg() {
//            mHashMap.put("DETECTION_FAILED", R.string.string_network_error_detection_failed);
//            mHashMap.put("WRONG_LIVENESS_DATA", R.string.string_network_error_wrong_liveness_data);
//            mHashMap.put("NO_FACE_DETECTED", R.string.string_network_error_detection_failed);
//        }
//    }

}
