package com.sdk.karzalivness;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

class HttpCall {

    private static final String TAG = "HttpCall";
    private static final boolean LOGS = BuildConfig.DEBUG;
    private static final int BUFFER_SIZE = 4096;

    private String urlString;
    private HttpMethod httpMethod;
    private String postData;
    private Map<String, String> headers;
    private String cookies;
    private String downloadPath;

    private HttpCall(String urlString, HttpMethod httpMethod, String postData
            , Map<String, String> headers, String cookies) {
        this.urlString = urlString;
        this.httpMethod = httpMethod;
        this.postData = postData;
        this.headers = headers;
        this.cookies = cookies;
    }

    /**
     * Method for doing any type of network request. Builder method already initializes
     * the params required for a network call. Here, we get a response of type {@link NetworkResponse}
     * which we the return.
     * @return NetworkResponse
     */
    public NetworkResponse executeRequest(){
        NetworkResponse response = new NetworkResponse();
        URL url = null;
        HttpURLConnection urlConnection = null;
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        try {
            //----------------- Request generation steps/checks -----------------//
            if (TextUtils.isEmpty(urlString)) {
                if (LOGS)Log.e("HttpCall", "BAD HttpRequest URL is null");
                throw new Exception();
            }

            url = new URL(urlString);
            urlConnection = null;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(httpMethod == null ? HttpMethod.POST.toString() : httpMethod.name());

            if (headers != null) {
                for (HashMap.Entry<String, String> pair : headers.entrySet()) {
                    urlConnection.setRequestProperty(pair.getKey(), pair.getValue());
                }
            }

            if (cookies != null && !cookies.isEmpty()){
                urlConnection.setRequestProperty("Cookie", cookies);
            }
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);

            if (postData != null && !postData.isEmpty()) {
                urlConnection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream( urlConnection.getOutputStream());
                wr.write(postData.getBytes(StandardCharsets.UTF_8));
                wr.close();
            }

            urlConnection.connect();

            //------------- Response received here ----------------//
            int responseCode = urlConnection.getResponseCode();
            String responseString;
            if(responseCode == HttpsURLConnection.HTTP_OK){
                responseString = readStream(urlConnection.getInputStream());
            }else{
                responseString = readStream(urlConnection.getErrorStream());
            }
            HashMap<String, String> cookies = new HashMap<>();
//            for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
//                if (cookie.getDomain().equals("api.totalkyc.com")){
//                    cookies.put(cookie.getName(), cookie.getValue());
//                }
//            }
            //Finally, we initialize our NetworkResponse type variable using these 3 values, i.e. -
            //responseCode, responseString & cookies.
            response = new NetworkResponse(responseCode, responseString, cookies);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null)
                urlConnection.disconnect();
        }
        return response;
    }

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



    //***********************************************************//
    //*************** Builder Method for HttpCall ***************//

    public static class Builder{
        private String urlString;
        private String postData;
        private HttpMethod method;
        private Map<String, String> headers;
        private String cookies;

        public Builder(String urlString) {
            this.urlString = urlString;
        }

        public Builder setMethod(HttpMethod method){
            this.method = method;
            return this;
        }

        public Builder setPostData(String data){
            this.postData = data;
            return this;
        }

        public Builder setHeader(HashMap<String, String> headers){
            this.headers = headers;
            return this;
        }

        public Builder setCookies(String cookies){
            this.cookies = cookies;
            return this;
        }

        public HttpCall build(){
            return new HttpCall(urlString, method, postData, headers, cookies);
        }
    }
}
