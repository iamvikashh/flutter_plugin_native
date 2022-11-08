package com.sdk.karzalivness;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.sdk.karzalivness.enums.CameraFacing;
import com.sdk.karzalivness.enums.FaceStatus;
import com.sdk.karzalivness.enums.FaceTypeStatus;
import com.sdk.karzalivness.enums.KEnvironment;
import com.sdk.karzalivness.enums.KLiveStatus;
import com.sdk.karzalivness.interfaces.KLivenessCallbacks;
import com.sdk.karzalivness.models.KLiveResult;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

public class KLivenessView extends FrameLayout implements KCameraFragment.Contract,
        KNetworkUtil.NetworkResultContract {

    private static final int DEFAULT_SECOND = 15;
    private static final int DEFAULT_STROKE_WIDTH = 5;

    private final String TAG = "KLivenessView";
    private final String NETWORK_RESPONSE = "NETWORK_RESPONSE";
    private int colorFace, colorNoFace, colorInvalidFace;
    private int backgroundColor;
    private int timeOutSecond;
    private int strokeWidth;
    private KLivenessCallbacks callbacks;
    private KEnvironment environment;
    private String token;
    private JSONObject txtDetails;
    private KCameraFragment fragment;
    private KOverlayViewHAL overlayView;
    private FragmentManager fragmentManager;
    private KConfig kConfig;
    private KNetworkUtil netContract;
    private boolean isResultObtained;


    public KLivenessView(@NonNull Context context) {
//        super(context);
//        init(context, null);
        this(context, null, 0);
    }

    public KLivenessView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLivenessView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    //**************************************************************************//
    //***************** Init & Initialize Methods ******************************//

    private void init(final Context context, final AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray array = context.getTheme().obtainStyledAttributes(attributeSet
                    , R.styleable.KLivenessView, 0, 0);
            colorFace = array.getColor(R.styleable.KLivenessView_klive_color_has_face, Color.BLUE);
            colorNoFace = array.getColor(R.styleable.KLivenessView_klive_color_no_face, Color.YELLOW);
            colorInvalidFace = array.getColor(R.styleable.KLivenessView_klive_color_invalid_face
                    , Color.RED);
            backgroundColor = array.getColor(R.styleable.KLivenessView_klive_background
                    , Color.WHITE);
            timeOutSecond = array.getInt(R.styleable.KLivenessView_klive_time_out_second, DEFAULT_SECOND);
            strokeWidth = array.getDimensionPixelSize(R.styleable.KLivenessView_klive_circle_width, DEFAULT_STROKE_WIDTH);
            array.recycle();
        } else {
            colorFace = Color.BLUE;
            colorNoFace = Color.YELLOW;
            colorInvalidFace = Color.RED;
            backgroundColor = Color.WHITE;
            timeOutSecond = DEFAULT_SECOND;
            strokeWidth = DEFAULT_STROKE_WIDTH;
        }
        // init view inflation
        inflate(context, R.layout.layout_k_liveness_view, this);
    }

    public void initialize(final FragmentManager fragmentManager, final KLivenessCallbacks callbacks
            , final String token, final KEnvironment environment, @Nullable final JSONObject details, CameraFacing cameraFacing) {

        if (fragmentManager == null || callbacks == null) {
            throw new IllegalStateException("FragmentManager or KLivenessCallbacks can not be null");
        }
        if (TextUtils.isEmpty(token)) {
            throw new IllegalStateException("Token can't be empty");
        }

        this.environment = environment;
        this.token = token;
        this.txtDetails = details;
        this.callbacks = callbacks;
        this.overlayView = findViewById(R.id.overLay);
        this.netContract = new KNetworkUtil(this, environment.env);

        if (!verifyRequiredPermission()) {
            this.fragmentManager = fragmentManager;
            return;
        }


        //This API Call helps us get all the configurations we want to do for the Face Liveness Checks.
        callConfigAPI(data -> {
            //Setting the kConfig type variable.
            KLivenessView.this.kConfig = data;
            if (BuildConfig.DEBUG) Log.i(TAG, this.kConfig.toString());

            //Setting up the Overlay View and isResultObtained bool.
            isResultObtained = false;
            overlayView.setOverLayColor(backgroundColor);
            overlayView.setBoundaryColor(colorNoFace, strokeWidth);
            this.overlayView.setVisibility(VISIBLE);

            //TODO: 3.
            //Passing all required values to KCameraFragment before attaching it.
            fragment = KCameraFragment.newInstance(colorFace, colorNoFace, colorInvalidFace
                    , timeOutSecond, cameraFacing == CameraFacing.FRONT);
            fragment.setkLivenessCallbacks(callbacks);
            fragment.setContract(KLivenessView.this);

            //FragmentManager and starting KCameraFragment.
            KLivenessView.this.fragmentManager = fragmentManager;
            fragmentManager.beginTransaction().replace(R.id.main_frame, fragment).commit();
            fragmentManager.executePendingTransactions();
        });

    }


    //**************************************************************************//
    //************* Setter Methods for View properties so that user can ********//
    //************* set it both through layout as well as programmatically *****//

    public void setColorFace(int colorFace) {
        this.colorFace = colorFace;
    }

    public void setColorNoFace(int colorNoFace) {
        this.colorNoFace = colorNoFace;
    }

    public void setColorInvalidFace(int colorInvalidFace) {
        this.colorInvalidFace = colorInvalidFace;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setTimeOutSecond(int timeOutSecond) {
        this.timeOutSecond = timeOutSecond;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }


    //*****************************************************************//
    //*************** Stopper for Liveness Detection ******************//

    /**
     * Call to stop Liveness Detection.
     */
    private void stopDetect() {
        //TODO: 1.
        overlayView.setVisibility(GONE);
        if (fragment != null && fragment.isAdded()) {
            fragment.releaseNative();
            fragmentManager.beginTransaction().remove(fragment).commit();
            fragmentManager.executePendingTransactions();
        }
        KFaceDetector.releaseInstance();
    }


    //*****************************************************************//
    //*********** KCameraFragment Contract Interface Methods ***********//

    @Override
    public void onFaceDetected(final FaceStatus faceStatus, final FaceTypeStatus faceTypeStatus) {
        if (callbacks != null) {
            runOnMainThread(() -> callbacks.faceStatus(faceStatus, faceTypeStatus));
        }
        switch (faceStatus) {
            case NO_FACE:
                overlayView.setBoundaryColor(colorNoFace, strokeWidth);
                break;
            case VALID_FACE:
                overlayView.setBoundaryColor(colorFace, strokeWidth);
                break;
            case INVALID_FACE:
                overlayView.setBoundaryColor(colorInvalidFace, strokeWidth);
                break;
        }
    }

    @Override
    public void timeOut() {
        if (isResultObtained) return;
        isResultObtained = true;
        if (callbacks == null) return;
        //Here, we already come from a Main UI Thread.
        //So we don't put stopDetect() in the runOnMainThread() method.
        //TODO: 2.
        stopDetect();
        runOnMainThread(() -> callbacks.onReceiveKLiveResult(KLiveStatus.CAMERA_TIMEOUT, null));
    }

    @Override
    public void onFaceResult(Bitmap bitmap, float score, Rect faceRect, float[] landmark) {
        if (isResultObtained) return;
        isResultObtained = true;
        //As stopDetect() works on UI Thread.
        //TODO: 2.
        runOnMainThread(this::stopDetect);
        showLoader();

        AppExecutors.getInstance().getExeNetworkIO().execute(() -> {

            KNetResult kNetResult;

            //Making Image to send.
            Bitmap imageToSend = KUtility.getImage(bitmap, kConfig, faceRect);
            String imageBase64 = KUtility.bitmapToB64JPEG(imageToSend, kConfig);

            //Making MetaData object.
            try {
                JSONObject metadata = new JSONObject();

                JSONObject originalImageData = new JSONObject();
                originalImageData.put("width", bitmap.getWidth());
                originalImageData.put("height", bitmap.getHeight());

                JSONObject modifiedImageData = new JSONObject();
                modifiedImageData.put("width", imageToSend.getWidth());
                modifiedImageData.put("height", imageToSend.getHeight());

                JSONObject faceRectData = new JSONObject();
                faceRectData.put("x", faceRect.left);
                faceRectData.put("y", faceRect.top);
                faceRectData.put("width", faceRect.width());
                faceRectData.put("height", faceRect.height());

                JSONObject faceCropPaddingData = new JSONObject();
                faceCropPaddingData.put("left", kConfig.getFaceCropPaddingPercentage().getLeft());
                faceCropPaddingData.put("top", kConfig.getFaceCropPaddingPercentage().getTop());
                faceCropPaddingData.put("right", kConfig.getFaceCropPaddingPercentage().getRight());
                faceCropPaddingData.put("bottom", kConfig.getFaceCropPaddingPercentage().getBottom());

                metadata.put("original_size", originalImageData);
                metadata.put("modified_size", modifiedImageData);
                metadata.put("bounding_box", faceRectData);
                metadata.put("landmarks", Arrays.toString(landmark));
                metadata.put("is_face_cropped", kConfig.isShouldSendCroppedImage());
                metadata.put("is_jpeg", kConfig.getIsJPEG());
                metadata.put("compression_quality", kConfig.getQuality());
                metadata.put("use_max_score", kConfig.useMaxScore());
                metadata.put("use_max_rect", kConfig.useMaxRect());
                metadata.put("max_area_percentage_front", kConfig.getMaxAreaPercentageFront());
                metadata.put("min_area_percentage_front", kConfig.getMinAreaPercentageFront());
                metadata.put("max_area_percentage_back", kConfig.getMaxAreaPercentageBack());
                metadata.put("min_area_percentage_back", kConfig.getMinAreaPercentageBack());
                metadata.put("frame_count", kConfig.getFrameCount());
                metadata.put("min_frame_count", kConfig.getMinFrameCount());
                metadata.put("max_input_resolution", kConfig.getMaxInputResolution());
                metadata.put("face_crop_padding_percentage", faceCropPaddingData);

                //API Call to get Liveness Score. We get KNetResult from here.
                kNetResult = netContract.doAntiHack(token, imageBase64, String.valueOf(metadata), txtDetails, kConfig);
            } catch (Exception e) {
                callbacks.onError(e.toString());
                if (BuildConfig.DEBUG) Log.e(TAG, "MetaData Error");
                e.printStackTrace();

                //API Call to get Liveness Score. We get KNetResult from here.
                kNetResult = netContract.doAntiHack(token, imageBase64, "{\"error\":\"MetadataError\"}", txtDetails, kConfig);
            }

            //Creating KLiveResult object after finally getting KNetResult.
            KNetResult finalKNetResult = kNetResult;
            final KLiveResult result = new KLiveResult(finalKNetResult.getScore(), KUtility.bitmapToB64JPEG(bitmap),
                    finalKNetResult.getRequestId());

            //Main thread call
            runOnMainThread(() -> {
                KLiveStatus status = KLiveStatus.get(finalKNetResult.getResponseCode());
                if (status == KLiveStatus.SUCCESS) {
                    DecimalFormat decimalFormat = new DecimalFormat("#.######");
                    double formatted;
                    try {
                        formatted = Double.parseDouble(decimalFormat.format(result.getLivenessScore()));
                    } catch (NumberFormatException e) {
                        callbacks.onError(e.toString());
                        if (BuildConfig.DEBUG) Log.e(TAG, "Score Format Error");
                        e.printStackTrace();

                        formatted = result.getLivenessScore();
                    }

                    result.setLivenessScore(formatted);
                }

                if (callbacks != null) {
                    callbacks.onReceiveKLiveResult(status, result);
                }

            });

            hideLoader();
        });

    }

    //Enable this for testing purpose. For seeing the rects drawn.
    @Override
    public void drawDebug(Rect rect, Rect parentRect, float[] landmarks) {
//        overlayView.setDebugRect(BuildConfig.DEBUG ? rect : null
//                , BuildConfig.DEBUG ? parentRect : null
//                , BuildConfig.DEBUG ? landmarks : null);
    }

    @Override
    public KConfig getConfig() {
        return kConfig;
    }


    //*****************************************************************//
    //************ NetworkResultContract Interface Methods ************//

    @Override
    public void onNetworkResult(int statusCode, String throwable) {
        if (callbacks == null) return;
        callbacks.onError(statusCode + "--" + throwable);
        runOnMainThread(() -> callbacks.onReceiveKLiveResult(KLiveStatus.APP_ERROR, null));
    }


    //*****************************************************************//
    //***************** Extra/Other/Helper Methods ********************//

    private void runOnMainThread(Runnable runnable) {
        AppExecutors.getInstance().getExeMainThread().execute(runnable);
    }

    private void hideLoader() {
        if (callbacks == null) return;
        runOnMainThread(() -> callbacks.hideLoader());
    }

    private void showLoader() {
        if (callbacks == null) return;
        runOnMainThread(() -> callbacks.showLoader());
    }

    private void callConfigAPI(KDataListener<KConfig> dataCallbacks) {
        //Here, we ourselves create a KDataListener<KConfig> object and pass it as a param to
        //callConfigAPI() method, so that we can get a KConfig type object and pass it in the "data"
        //variable of KDataListener<KConfig> using onData() method. We give this "data" back to the
        //place where callConfigAPI() was called.
        showLoader();

        AppExecutors.getInstance().getExeNetworkIO().execute(() -> {
            HashMap<String, String> headerJSON = new HashMap<>();
            headerJSON.put("Referer", BuildConfig.BASE_URL.replace("env", environment.env) + KConstants.CONFIG_CALL_REFERER);
            headerJSON.put("Origin", "https://app.karza.in");
            headerJSON.put("Content-Type", "application/json");
            headerJSON.put("karzatoken", token);
            KConfig[] config = new KConfig[1];

            try {
                long starTime = System.currentTimeMillis();
                NetworkResponse response = new HttpCall
                        .Builder(BuildConfig.BASE_URL.replace("env", environment.env) + KConstants.CONFIG_URL)
                        .setMethod(HttpMethod.GET)
                        .setHeader(headerJSON)
                        .build().executeRequest();

                if (BuildConfig.DEBUG) {
                    Log.e("Config Time ", "Diff " + (System.currentTimeMillis() - starTime));
                }
                if (BuildConfig.DEBUG) {
                    Log.i(NETWORK_RESPONSE, "Response => " + "https://app.karza.in/env/videokyc/api/v2/liveness-config" + " ------------ " + response);
                }

                JSONObject object = new JSONObject(response.getDataString());

                if (response.getStatus() == 200 && object.has("statusCode")) {
                    int apiStatus = object.getInt("statusCode");
                    if (apiStatus == 101) {
                        JSONObject dataObject = object.getJSONObject("result")
                                .getJSONObject("data");
                        config[0] = KConfig.create(dataObject);
                    } else {
                        if (BuildConfig.DEBUG) Log.e(TAG, "Config API Error");
                        callbacks.onError("API Error: " +
                                KLiveStatus.get(apiStatus).statusCode + "--" +
                                KLiveStatus.get(apiStatus).status);
                    }
                } else {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Config API Error");
                    callbacks.onError("API Error: " +
                            KLiveStatus.get(response.getStatus()).statusCode + "--" +
                            KLiveStatus.get(response.getStatus()).status);
                }
            } catch (Exception e) {
                hideLoader();
                callbacks.onError(e.toString());
                e.printStackTrace();
            }

            //If our ConfigAPI fails, then we can create a default JSONObject(KConfig.create())
            //thus always get default/fallback values in it.
            hideLoader();
            if (config[0] == null) {
                config[0] = new KConfig();
            }

            //Passing config data using KDataCallback, back to where function was called.
            AppExecutors.getInstance().getExeMainThread().execute(()
                    -> dataCallbacks.onData(config[0]));
        });
    }


    //****************************************************************//
    //***************** Permission Helper Methods ********************//

    /**
     * Verify required permission and callback to ask permission in case of any missing
     *
     * @return true if permission allowed false otherwise
     */
    private boolean verifyRequiredPermission() {
        final boolean cameraPer = checkPermission(Manifest.permission.CAMERA);
        if (cameraPer) return true;
        AppExecutors.getInstance().getExeMainThread().execute(() -> {
            if (callbacks != null) {
                if (!cameraPer) {
                    callbacks.needPermissions(Manifest.permission.CAMERA);
                }
            }
        });
        return false;
    }

    /**
     * Check permission requires
     *
     * @param perms list of permissions
     * @return result of collective permission state
     */
    private boolean checkPermission(@NonNull final String... perms) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //Log.w(TAG, "hasPermissions: API version < M, returning true by default");
            // DANGER ZONE!!! Changing this will break the library.
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = ContextCompat.checkSelfPermission(getContext(), perm)
                    == PackageManager.PERMISSION_GRANTED;
            if (!hasPerm) {
                return false;
            }
        }
        return true;
    }


}
