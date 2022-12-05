package com.karza.qrcodescansdk;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.karza.qrcodescansdk.Utils.printE;

import android.content.Context;
import android.net.ConnectivityManager;

public class ApiService {

    public static final String TAG = ApiService.class.getSimpleName();
    static String transactionId;
    private static ApiService mInstance = null;
    ApiCallback apiCallback;
    private String karzaToken;
    private KEnvironment kEnvironment = KEnvironment.TEST;
    private static Context mContext;

    public static synchronized void initialize(KEnvironment kEnv, String token, Context mContex) {
          if(mInstance==null){
              mInstance = new ApiService(kEnv, token);
              transactionId=createTransactionID();
             mContext=mContex;
          }

    }

    public static synchronized ApiService getInstance() {
        return mInstance;
    }

    private ApiService(){}

    private ApiService(KEnvironment kEnv, String token) {
        kEnvironment = kEnv;
        karzaToken = token;
    }

    public void setApiCallback(ApiCallback callback) {
        apiCallback = callback;
    }

    public void apiPost(final String activity) {
        //ConnectivityManager connectivityManager =(ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().addNetworkInterceptor(new StethoInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        JSONObject jsonObject = createJson(activity);
        printE(TAG, "Request Body-> " + jsonObject.toString());
        AndroidNetworking
                .post(BuildConfig.BASE_URL.replace("{env}", kEnvironment.env))
                .addHeaders("Content-Type", "application/json")
                .addHeaders("karzaToken", karzaToken)
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .setOkHttpClient(okHttpClient)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        printE(TAG, "Response Body-> " + response.toString());
                        apiCallback.onSuccess(activity, response);
                    }

                    @Override
                    public void onError(ANError error) {
                        if (error.getErrorCode() != 0) {
                            printE(TAG, "Response Body-> " + error.getErrorBody());
                            apiCallback.onError(activity, error.getErrorBody(), error.getErrorCode());

                        } else {
                            String errMsg=error.getErrorDetail().equals("connectionError")?"No network available, please check your WiFi or Data connection":"something went wrong please try again";
                            printE(TAG, "Response Body-> " + error.getErrorDetail());
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            apiCallback.onError(activity, errMsg, error.getErrorCode());
                        }

                    }
                });
    }

    public void apiPostWithAadhaarData(final String activity, String message) {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .addNetworkInterceptor(new StethoInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        JSONObject jsonObject = createJsonWithAadharData(activity, message);
        printE(TAG, "Data Request Body-> " + jsonObject.toString());
        AndroidNetworking
                .post(BuildConfig.BASE_URL.replace("{env}", kEnvironment.env))
                .addHeaders("Content-Type", "application/json")
                .addHeaders("karzaToken", karzaToken)
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .setOkHttpClient(okHttpClient)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        printE(TAG, "Data Response Body-> " + response.toString());
                        apiCallback.onSuccess(activity, response);
                    }

                    @Override
                    public void onError(ANError error) {
                        if (error.getErrorCode() != 0) {
                            printE(TAG, "Response Body-> " + error.getErrorBody());
                            apiCallback.onError(activity, error.getErrorBody(), error.getErrorCode());

                        } else {
                            printE(TAG, "Response Body-> " + error.getErrorDetail());
                            // error.getErrorDetail() : connectionError, parseError, requestCancelledError
                            apiCallback.onError(activity, error.getErrorDetail(), error.getErrorCode());
                        }
                    }
                });
    }


    public JSONObject createJson(String activity) {
        JSONObject jsonObjectMain = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("activity", activity);
            jsonObject.put("timeStamp", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("platform", "android");
            jsonObjectMain.put("data", jsonObject);
            jsonObjectMain.put("transactionId", transactionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjectMain;
    }

    public JSONObject createJsonWithAadharData(String activity, String message) {
        JSONObject jsonObjectMain = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        JSONObject aadhaarData = new JSONObject();
        try {
            jsonObject.put("activity", activity);
            jsonObject.put("timeStamp", String.valueOf(System.currentTimeMillis()));
            jsonObject.put("platform", "android");
            aadhaarData.put("isSuccess", true);
            aadhaarData.put("responseCode", 200);
            aadhaarData.put("message", message);
            jsonObject.put("aadhaarResponse", aadhaarData);
            jsonObjectMain.put("data", jsonObject);
            jsonObjectMain.put("transactionId", transactionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjectMain;
    }

    public static String createTransactionID() {

        try {
            return UUID.randomUUID().toString();
        } catch (Exception e) {
            printE(TAG, e.toString());
        }
        return "12345678-abcd-efgh-ijkl-123456789012";
    }
    public static void reassignTransId(){
        transactionId=createTransactionID();
    }

    public interface ApiCallback {
        void onSuccess(String activity, JSONObject jsonObject);
        void onError(String activity, String error,int errorCode);
    }



}
