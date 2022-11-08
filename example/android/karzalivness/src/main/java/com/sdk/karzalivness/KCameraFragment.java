package com.sdk.karzalivness;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.sdk.karzalivness.databinding.FragmentKCameraBinding;
import com.sdk.karzalivness.enums.FaceStatus;
import com.sdk.karzalivness.enums.FaceTypeStatus;
import com.sdk.karzalivness.interfaces.KLivenessCallbacks;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class KCameraFragment extends KBaseFragment {

    private static final String TAG = "KCameraFragment";
    private static final boolean DEBUG = true;
    private static final String PARAM_1 = "param_1";
    private static final String PARAM_2 = "param_2";
    private static final String PARAM_3 = "param_3";
    private static final String PARAM_4 = "param_4";
    private static final String PARAM_5 = "param_5";

    private static final String FILE_NAME = "sample_data.dat";
    private static final String FILE_LANDMARK = "landmark.dat";
    private static final int MAX_OUTPUT = 5;
    private final String[] PER = new String[]{Manifest.permission.CAMERA};

    private FragmentKCameraBinding binding;
    private ProcessCameraProvider cameraProvider;
    protected KLivenessCallbacks kLivenessCallbacks;
    private Camera camera;
    private boolean started;
    private boolean isPopupPermShown;
    private KFaceDetector faceDetector;
    private boolean isFront;
    protected int hasFaceColor, noFaceColor, invalidFaceColor;
    private long maxTime;
    private Handler handler;
    private Contract contract;
    private boolean isStateExisting = false;

    private int viewWidth, viewHeight;
    private int frameProcessed;
    private Bitmap currentFrame;
    private double maxAreaThreshold;
    private double minAreaThreshold;
    private int viewMargin = 10;
    private Rect parentRectForFace;
    private Rect maxOuterPermissibleRect;
    private FrameCluster overallBestFrameC;
    private FrameCluster localClusterBestC;
    private FrameCluster prevFrameC;
    private FaceStatus prevFaceStatus;
    private FaceTypeStatus prevFaceTypeStatus;
    private int clusterSize;
    private KConfig kConfig;
    private int noFaceCount;
    private long timeIn;
    private long timeOut;
    private long timeDiff;
    private int frameAmount;


    //****************************************************************//
    //****************************************************************//

    //ActivityResultLauncher takes a launcher object.
    //registerForActivityResult takes a contract and a callback and finally returns the launcher object for that.

    //Contract is the Input on which we work. Callback takes the result of that contract, using which we define
    //our methods/calls to do further.
    private final ActivityResultLauncher<String> permissionResult =
            registerForActivityResult(new ActivityResultContracts.RequestPermission()
                    , result -> {
                        if (result) {
                            startCamera();
                        } else {
                            isPopupPermShown = false;
                            askPermission();
                        }
                    });

    private void askPermission() {
        if (!isPopupPermShown) {
            permissionResult.launch(PER[0]);
            isPopupPermShown = true;
        }
    }

    public KCameraFragment() {
        // Required empty public constructor
    }


    //****************************************************************//
    //******************* Instance Method ****************************//

    /**
     * newInstance Method, which accepts the basic values required for showing the Drawn Camera Fragment.
     * It takes these values and puts them in the fragment bundle, which are then later used in onCreate().
     */
    public static KCameraFragment newInstance(int hasFaceColor
            , int noFaceColor, int invalidFaceColor, int maxTime, boolean isFront) {
        KCameraFragment fragment = new KCameraFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(PARAM_1, isFront);
        bundle.putInt(PARAM_2, maxTime);
        bundle.putInt(PARAM_3, hasFaceColor);
        bundle.putInt(PARAM_4, noFaceColor);
        bundle.putInt(PARAM_5, invalidFaceColor);
        fragment.setArguments(bundle);
        return fragment;
    }


    //****************************************************************//
    //****************** Overridden Methods **************************//

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        try {
//            File output = new File(context.getFilesDir(), FILE_NAME);
//            File output2 = new File(context.getFilesDir(), FILE_LANDMARK);
//            if (!output.exists()) {
//                KUtility.copyAssetToStorage(getContext(), "sample_data.dat", output);
//            }
//            if (!output2.exists()) {
//                KUtility.copyAssetToStorage(getContext(), "landmark.dat", output2);
//            }
//            if (contract != null) {
//                kConfig = contract.getConfig();
//            }
//
//            if (BuildConfig.DEBUG) {
//                Log.i(TAG, "File Path 1 -> " + output.getAbsolutePath());
//                Log.i(TAG, "File Path 2 -> " + output2.getAbsolutePath());
//            }
//            faceDetector = KFaceDetector.getInstance(output.getAbsolutePath()
//                    , output2.getAbsolutePath(), kConfig);
//        } catch (Exception e) {
//            if (BuildConfig.DEBUG) Log.e(TAG, "Error occurred: " + e.toString());
//            kLivenessCallbacks.onError(e.toString());
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {

            initModelsAndKFace();
            handler = new Handler(Looper.getMainLooper());
            if (getArguments() != null) {
                isFront = getArguments().getBoolean(PARAM_1, true);
                maxTime = getArguments().getInt(PARAM_2) * 1000L;
                hasFaceColor = getArguments().getInt(PARAM_3);
                noFaceColor = getArguments().getInt(PARAM_4);
                invalidFaceColor = getArguments().getInt(PARAM_5);
            }
            //Here, we go to KBaseFragment's hasPermissions() method to check if the Activity has been provided
            //the permissions(PER String Array).
            // If not, then we use Android 11's way of asking for permissions.
            //If yes, we move to startCamera() method.
            if (!hasPermissions(getContext(), PER)) {
                askPermission();
            } else {
                startCamera();
            }

            viewMargin = getResources().getDimensionPixelSize(R.dimen.view_margin);

        } else {
            isStateExisting = true;
            getParentFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentKCameraBinding.inflate(inflater, container, false);
//        binding = FragmentCameraBinding.inflate(inflater);
//        View view = binding.getRoot();
//        View view =  inflater.inflate(R.layout.fragment_camera, container, false);
//        cameraView = view.findViewById(R.id.cameraView);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Dimen :" + view.getHeight() + " " + view.getWidth());
                }
                if (view.getHeight() > 0 && view.getWidth() > 0) {
                    viewWidth = view.getWidth();
                    viewHeight = view.getHeight();

                    //Finding edge length through circle for drawing Parent Rect.
                    //int radius = (viewWidth / 2) - viewMargin;
                    double radius = (viewWidth / 2.0);
                    double centerX = viewWidth / 2.0;
                    double centerY = viewHeight / 2.0;
                    double edgeLength = radius / Math.sqrt(2);

                    //final int padding = (int)(radius - edgeLength) - 10;
                    final int padding = (int) (radius - edgeLength);
                    int topX = (int) (centerX - edgeLength);
                    int topY = (int) ((centerY - edgeLength) - padding);
                    int bottomX = (int) (centerX + edgeLength);
                    int bottomY = (int) ((centerY + edgeLength) + padding);
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Coords -> " + topX + "-" + topY + "-" + bottomX + "-" + bottomY);
                    }

                    parentRectForFace = new Rect(topX, topY, bottomX, bottomY);
                    maxOuterPermissibleRect = new Rect((int) (topX - (parentRectForFace.width() * 0.1)),
                            (int) (topY - (parentRectForFace.height() * 0.05)),
                            (int) (bottomX + (parentRectForFace.width() * 0.1)),
                            (int) (bottomY + (parentRectForFace.height() * 0.05)));

                    //Finding Parent Rect's Area for Face Rect Zoom In/Out calculations.
                    int parentArea = parentRectForFace.width() * parentRectForFace.height();
                    maxAreaThreshold = parentArea * kConfig.getMaxArea(isFront);
                    minAreaThreshold = parentArea * kConfig.getMinArea(isFront);
                    if (BuildConfig.DEBUG) {
                        Log.v(TAG, "Threshold :" + maxAreaThreshold + " " + minAreaThreshold);
                    }
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (!started) {
//                        cameraView.post(new Runnable() {
//                            @Override
//                            public void run() {
                        startCamera();
//                            }
//                        });

                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!isStateExisting) {
            this.releaseNative();
            KFaceDetector.releaseInstance();
        }
    }

    //****************************************************************//
    //**************** Other Important Methods ***********************//

    void setkLivenessCallbacks(KLivenessCallbacks kLivenessCallbacks) {
        this.kLivenessCallbacks = kLivenessCallbacks;
    }

    void setContract(Contract contract) {
        this.contract = contract;
    }

    private void initModelsAndKFace() {
        try {
            File output = new File(requireContext().getFilesDir(), FILE_NAME);
            File output2 = new File(requireContext().getFilesDir(), FILE_LANDMARK);
            if (!output.exists()) {
                KUtility.copyAssetToStorage(requireContext(), "sample_data.dat", output);
            }
            if (!output2.exists()) {
                KUtility.copyAssetToStorage(requireContext(), "landmark.dat", output2);
            }
            if (contract != null) {
                kConfig = contract.getConfig();
            }

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "File Path 1 -> " + output.getAbsolutePath());
                Log.i(TAG, "File Path 2 -> " + output2.getAbsolutePath());
            }
            faceDetector = KFaceDetector.getInstance(output.getAbsolutePath()
                    , output2.getAbsolutePath(), kConfig);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error occurred: " + e.toString());
            kLivenessCallbacks.onError(e.toString());
            e.printStackTrace();
        }
    }

    private void detectionTimeout() {
        if (contract != null) {
            //TODO: 1.
            handler.post(() -> cameraProvider.unbindAll());
            handler.removeCallbacksAndMessages(null);

            //Logic for when we have DEFAULT_MIN_FRAME_THRESHOLD <= framesProcessed < DEFAULT_FRAME_THRESHOLD,
            //then we still give onFaceResult. Else we finally give timeout.
            if (frameProcessed >= kConfig.getMinFrameCount() && overallBestFrameC != null) {
                contract.onFaceResult(overallBestFrameC.getBitmap()
                        , overallBestFrameC.getScore(), overallBestFrameC.getRect(),
                        overallBestFrameC.getLandmarks());
            } else {
                contract.timeOut();
            }
        }
    }

    private void setFaceStatsAndCallContract(FaceStatus faceStatus, FaceTypeStatus faceTypeStatus) {
        if (prevFaceStatus != faceStatus ||
                prevFaceTypeStatus != faceTypeStatus) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "onFaceDetected Callback Called");
            }
            prevFaceStatus = faceStatus;
            prevFaceTypeStatus = faceTypeStatus;
            contract.onFaceDetected(faceStatus, faceTypeStatus);
        }
    }

    //TODO: 1. and 2.
    void releaseNative() {
        faceDetector.stopDetection();
    }


    //****************************************************************//
    //********************* START CAMERA METHOD **********************//

    //TODO: 3.
    private void startCamera() {
        if (!hasPermissions(getContext(), PER)) {
//            askPermission();
            return;
        }
        if (viewHeight == 0 || viewWidth == 0) {
            return;
        }
        this.started = true;

        handler.postDelayed(this::detectionTimeout, maxTime);

        try {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this.getContext());
            cameraProviderFuture.addListener(() -> {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    bindCameraPreview();
                } catch (Exception e) {
                    if (BuildConfig.DEBUG) Log.e(TAG, "Error occurred: " + e.toString());
                    kLivenessCallbacks.onError(e.toString());
                    e.printStackTrace();
                }
            }, ContextCompat.getMainExecutor(this.getContext()));
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error occurred: " + e.toString());
            kLivenessCallbacks.onError(e.toString());
            e.printStackTrace();
        }
    }


    //****************************************************************//
    //***************** BIND CAMERA PREVIEW METHOD *******************//

    /**
     * Binds the Camera Preview according to the view displayed.
     * Also takes in the image and does some bitmap manipulations to finally pass it to the runInference Method.
     */
    private void bindCameraPreview() {

        try {
            //Getting camera preview stream into the PreviewView of layout file.
            binding.cameraView.getPreviewStreamState().observeForever(streamState -> {
                if (BuildConfig.DEBUG) Log.e(TAG, "bindCameraPreview: " + streamState);
            });

            //------ Setting multiple Use Cases to give to CameraProvider ------//
            //------- to bind these to the Camera and thus perform these -------//
            //---------- operations on every frame the camera intakes ----------//

            //Preview UseCase
            int rotation = binding.cameraView.getDisplay().getRotation();
            Preview preview = new Preview.Builder()
                    .setTargetRotation(rotation)
                    .build();

            //Camera Selector UseCase
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(isFront ? CameraSelector.LENS_FACING_FRONT
                            : CameraSelector.LENS_FACING_BACK)
                    .build();

            //ImageAnalysis UseCase
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setTargetRotation(rotation)
                    .build();

//        //ImageCapture UseCase
//        ImageCapture imageCapture = new ImageCapture.Builder()
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                // We request aspect ratio but no resolution to match preview config, but letting
//                // CameraX optimize for whatever specific resolution best fits our use cases
//                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                // Set initial target rotation, we will have to call this again if rotation changes
//                // during the lifecycle of this use case
//                .setTargetRotation(rotation)
//                .build();

            preview.setSurfaceProvider(binding.cameraView.getSurfaceProvider());

            //-----------------------------------------------------------------//

            //Runs an executor/thread to analyze the images separately. Receives an executor and an image as input.
            imageAnalysis.setAnalyzer(AppExecutors.getInstance().getExeDiskIO(), image -> {

                //--------------//
                //If model is not initialized till here, then don't do any
                //analysis on image and just return (for every image).
                if (faceDetector == null || !faceDetector.isInitialised()) {
                    image.close();
                    return;
                }
                //--------------//

                //--------------//
                //Keep on adding the frames for finally finding the FPS.
                //Also take the timeIn for that frame.
                frameAmount++;
                timeIn = System.currentTimeMillis();

                Bitmap bitmap = KUtility.getBitmap(image);
                if (image.getImageInfo().getRotationDegrees() > 0) {
                    bitmap = KUtility.rotateImage(bitmap, image.getImageInfo().getRotationDegrees());
                }
                currentFrame = bitmap;
                bitmap = KUtility.getScaledBitmap(bitmap, kConfig.getMaxInputResolution());
                runInference(bitmap);

                //TimeOut Logic for each frame.
                //We keep on counting the time diff taken for each frame and keep adding them.
                timeOut = System.currentTimeMillis();
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "TimeOut - TimeIn = " + (timeOut - timeIn));
                }
                timeDiff += timeOut - timeIn;

                image.close();
                //--------------//

            });

            //Here, we take the min value out of the view/phone width or height (width is min always in portrait)
            //and then we create a ViewPort to adjust our CameraPreview accordingly.

            //TODO: Doubt? Aspect Ratio for ViewPort is dimen x dimen?
//        int dimen = Math.min(viewHeight, viewWidth);
//        ViewPort viewPort = new ViewPort.Builder(
//                new Rational(dimen, dimen),
//                getContext().getResources()
//                        .getConfiguration().orientation).build();
//
//        UseCaseGroup useCaseGroup = new UseCaseGroup.Builder()
//                .addUseCase(preview)
//                .addUseCase(imageAnalysis)
//                .addUseCase(imageCapture)
//                .setViewPort(viewPort)
//                .build();


//        cameraView.setImplementationMode(PreviewView.ImplementationMode.PERFORMANCE);
//        camera = cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, useCaseGroup);
//        preview.setSurfaceProvider(cameraView.getSurfaceProvider());
//        cameraView.setBackgroundColor(R.color.tra);
//        observeCameraState(camera.getCameraInfo());

            //Unbind any previous use cases joined with cameraProvider and bind the new ones again.

            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
//        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e(TAG, "Error occurred: " + e.toString());
            kLivenessCallbacks.onError(e.toString());
            e.printStackTrace();
        }
    }

//    private void observeCameraState(CameraInfo cameraInfo){
//
//            cameraInfo.getCameraState().observeForever(cameraState -> {
//                Log.e(TAG, "observeCameraState: "+ cameraState.getType() );
//                if(cameraState.getError()!=null)
//                Log.e(TAG, "observeCameraState: "+ cameraState.getError().getCode() );
//            });
//    }


    //****************************************************************//
    //***************** RUN INFERENCE METHOD *************************//

    /**
     * Runs the Model Inference on the image passed and uses the result to run the required callback methods.
     * Passes respective result to the {@link KLivenessView} which in turn changes view of the {@link KOverlayView}.
     */
    private void runInference(Bitmap scaledBitmap) {

//        if (!shouldProcessFrames) return;
//        KUtility.testSaveImage(scaledBitmap, "input");

        //--------- Getting bitmap pixels in an array ---------//
        int[] pixels = new int[scaledBitmap.getWidth() * scaledBitmap.getHeight()];
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.getWidth()
                , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());

        //----------------- Brightness Check ------------------//
        int brightness = faceDetector.getBrightnessIndex(pixels);
        //Min. Brightness Check.
        if (brightness < kConfig.getMinBrightness()) {
            if (contract != null) {
                setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.LOW_BRIGHTNESS);
                frameProcessed = 0;
                clusterSize = 0;
            }
            return;
        }
        //Max. Brightness Check.
        if (brightness > kConfig.getMaxBrightness()) {
            if (contract != null) {
                setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.HIGH_BRIGHTNESS);
                frameProcessed = 0;
                clusterSize = 0;
            }
            return;
        }

        //---------------------------------------------------------//
        //----------------- Face Detection Check ------------------//
        float[] output = faceDetector.getFaceResult(pixels, scaledBitmap.getWidth()
                , scaledBitmap.getHeight(), 3);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Output -> " + Arrays.toString(output));
        }

        if (output.length > 0) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Face Detect Output Length = " + output.length);
            }
            if (output.length < MAX_OUTPUT) return;
            if (output.length == MAX_OUTPUT) {
                float score = output[0];
                float scaleX = currentFrame.getWidth() / (float) scaledBitmap.getWidth();
                float scaleY = currentFrame.getHeight() / (float) scaledBitmap.getHeight();

                float x1 = output[1];
                float x2 = output[2];
                float x3 = output[3];
                float x4 = output[4];
                output[1] *= scaleX;
                output[2] *= scaleY;
                output[3] *= scaleX;
                output[4] *= scaleY;
                float scaleViewX = viewWidth / (float) currentFrame.getWidth();
                float scaleViewY = viewHeight / (float) currentFrame.getHeight();
//                Log.e(TAG, "Array " + Arrays.toString(output));
//                Log.v(TAG, "Bitmap Actual - " + currentFrame.getWidth() + " H - " + currentFrame.getHeight());
//                Log.v(TAG, "Bitmap W - " + scaledBitmap.getWidth() + " H - " + scaledBitmap.getHeight());
//                Log.v(TAG, "Scale X - " + scaleX + " Y - " + scaleY);
                Rect currentRect = new Rect((int) (output[1]), (int) (output[2])
                        , (int) ((output[1] + output[3])), (int) (output[2] + output[4]));

                Rect currentRectDebug = new Rect((int) (output[1] * scaleViewX), (int) (output[2] * scaleViewY)
                        , (int) ((output[1] + output[3]) * scaleViewX), (int) ((output[2] + output[4]) * scaleViewY));
                int rectArea = currentRectDebug.width() * currentRectDebug.height();
//                Log.v(TAG, "Rect : " + currentRectDebug.toString() + " Area : " + rectArea);
//                Log.i(TAG, "Width : " + currentRectDebug.width() + " Height : " + currentRectDebug.height());

                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Bound : " + (rectArea < maxAreaThreshold) + " - " + (rectArea > minAreaThreshold));
                }
                if (contract != null) {
                    contract.drawDebug(currentRectDebug, maxOuterPermissibleRect, null);
                }

//                //----------------- Brightness Check ------------------//
//
//                Bitmap cropBitmap = Bitmap.createBitmap(scaledBitmap, (int) x1, (int) x2, (int) x3
//                        , (int) x4);
//                //KUtility.testSaveImage(cropBitmap, "output");
//
//                //Get pixels for the cropped part
//                int[] cropPixels = new int[cropBitmap.getWidth() * cropBitmap.getHeight()];
//                cropBitmap.getPixels(cropPixels, 0, cropBitmap.getWidth()
//                        , 0, 0, cropBitmap.getWidth(), cropBitmap.getHeight());
//
//                int brightness = faceDetector.getBrightnessIndex(cropPixels);
//
//                //Min. Brightness Check.
//                if (brightness < kConfig.getMinBrightness()){
//                    if (contract != null){
//                        setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.LOW_BRIGHTNESS);
//                        frameProcessed = 0;
//                        clusterSize = 0;
//                    }
//                    return;
//                }
//                //Max. Brightness Check.
//                if (brightness > kConfig.getMaxBrightness()){
//                    if (contract != null){
//                        setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.HIGH_BRIGHTNESS);
//                        frameProcessed = 0;
//                        clusterSize = 0;
//                    }
//                    return;
//                }

                //--------- Parent and Child Rect related Checks ---------//

                // If Face/Child Rect lie outside of Parent/Max allowed Rect in Circle, do nothing(return).
                if (!maxOuterPermissibleRect.contains(currentRectDebug)) {
                    if (contract != null) {
                        setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.LIES_OUTSIDE);
                        frameProcessed = 0;
                        clusterSize = 0;
                    }
                    return;
                }

                // If Face/Child Rect Area is too big(zoomed in image) do nothing(return).
                if (rectArea > maxAreaThreshold) {
                    if (contract != null) {
                        setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.TOO_CLOSE);
                    }
                    return;
                }

                //If Face/Child Rect Area is too small(zoomed out image), do noting(return).
                if (rectArea < minAreaThreshold) {
                    if (contract != null) {
                        setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.TOO_FAR);
                    }
                    return;
                }

                //---------------------------------------------------------//
                //-------------------- Landmarks Check --------------------//

                // If all previous checks were true && landmarks has to be
                // used only then we perform landmark detection.
                float[] landmark = null;
                float[] landmarksForDebug;

                if (kConfig.useLandmark()) {
//                    landmark = faceDetector.landmarks(pixels, scaledBitmap.getWidth()
//                            , scaledBitmap.getHeight(), 3, (int) output[1]
//                            , (int) output[2], (int) output[3], (int) output[4]);
                    landmark = faceDetector.landmarks(pixels, scaledBitmap.getWidth()
                            , scaledBitmap.getHeight(), 3, (int) x1
                            , (int) x2, (int) x3, (int) x4);


                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "landmark -> " + Arrays.toString(landmark));
                    }

                    if (contract != null) {
                        //Check for very last second case, where landmark array becomes 1 in size.
                        if (landmark != null && landmark.length > 1) {
                            landmarksForDebug = new float[landmark.length];
                            for (int i = 0; i < landmark.length; ) {
                                float x = (landmark[i] * scaleX);
                                float y = (landmark[i + 1] * scaleY);
                                float a = x * scaleViewX;
                                float b = y * scaleViewY;

                                landmark[i] = x;
                                landmark[i + 1] = y;
                                landmarksForDebug[i] = a;
                                landmarksForDebug[i + 1] = b;
                                i += 2;
                            }
                        } else {
                            return;
                        }
                        //Finally, we draw the Rects and the Landmarks on the Camera Preview.
                        contract.drawDebug(currentRectDebug, maxOuterPermissibleRect, landmarksForDebug);
                    }
                }

                // Inform about Valid Face Detection.
                if (contract != null) {
                    setFaceStatsAndCallContract(FaceStatus.VALID_FACE, null);
                }

                //---------------------------------------------------------//
                //------ Frame Cluster Compare for getting Best Frame -----//

                //1st Frame has nothing to compare to. So, it automatically becomes a FrameCluster.
                if (frameProcessed == 0) {
                    prevFrameC = new FrameCluster();
                    localClusterBestC = new FrameCluster();
                    KUtility.clusterSetter(prevFrameC, currentFrame, score, currentRect, landmark);
                    KUtility.clusterSetter(localClusterBestC, currentFrame, score, currentRect, landmark);

                    frameProcessed++;
                    clusterSize++;
                    return;
                } else if (frameProcessed > 0) {
                    //Firstly, check for IOU value, if it is close/similar to the previous frame.
                    //Previous frame by this logic, will always be the latest value of FrameCluster frame.
                    double IOUValue = KUtility.IOU(currentRect, prevFrameC.getRect());
                    //If IOU value clears the threshold, we will do Score-RectArea checks and then
                    //overwrite this frame in FrameCluster if it wins.
                    if (IOUValue >= kConfig.getIouThreshold()) {
                        clusterSize++;

                        if (clusterSize < kConfig.getMinClusterThreshold()) {
                            if (BuildConfig.DEBUG) {
                                Log.i(TAG, "IOU Same Frame + Small Cluster " + IOUValue);
                            }
                            KUtility.frameScoreAreaCompare(localClusterBestC, kConfig, currentFrame, score,
                                    currentRect, landmark);
                        } else {
                            if (BuildConfig.DEBUG) {
                                Log.i(TAG, "IOU Same Frame + Big Cluster " + IOUValue);
                            }
                            KUtility.frameScoreAreaCompare(localClusterBestC, kConfig, currentFrame, score,
                                    currentRect, landmark);
                            //Check if overallBestFrameC existed before this or not. If no, instantiate/create it and assign it the local best value,
                            //otherwise just do compare logic.
                            if (overallBestFrameC == null) {
                                overallBestFrameC = new FrameCluster();
                                overallBestFrameC = localClusterBestC;
                            } else {
                                //Compare the best local value with the final best clusters value.
                                KUtility.frameScoreAreaCompare(overallBestFrameC, kConfig,
                                        localClusterBestC.getBitmap(), localClusterBestC.getScore(),
                                        localClusterBestC.getRect(), localClusterBestC.getLandmarks());
                            }
                        }

                        KUtility.clusterSetter(prevFrameC, currentFrame, score, currentRect, landmark);
                    } else {
                        //new cluster with different IOU type has to be made. Also localCluster will now
                        //be set according to the new IOU value type.
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "IOU Changed + Small Cluster Started " + IOUValue);
                        }
                        clusterSize = 1;
                        KUtility.clusterSetter(localClusterBestC, currentFrame, score, currentRect, landmark);
                        KUtility.clusterSetter(prevFrameC, currentFrame, score, currentRect, landmark);
                    }
                }

                //Increase frame count after all validations(because if we reach here, this means we had good frames only)
                //and check if FRAME_THRESHOLD is reached or not. If not, then simply do nothing as it's a void function.
                //It just returns upon last statement and we start working with the next frame/image.
                //Also, it can be that no Big Cluster is formed, but frame threshold was reached. So in that case, overallBestFrameC would
                //be null, and thus we restart the Cluster Logic for the next frame/image received.
                frameProcessed++;
                if (frameProcessed >= kConfig.getFrameCount() && contract != null) {
                    if (overallBestFrameC != null) {
                        //TODO: 1.
                        handler.post(() -> cameraProvider.unbindAll());
                        handler.removeCallbacksAndMessages(null);

                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "Total Time Diff -> " + timeDiff);
                            Log.i(TAG, "No. of total frames captured -> " + frameAmount);
                            Log.i(TAG, "Time for each frame => " + (timeDiff / frameAmount));
                            Log.i(TAG, "FPS => " + ((frameAmount / ((timeDiff) / 1000.0))));
                        }

                        contract.onFaceResult(overallBestFrameC.getBitmap()
                                , overallBestFrameC.getScore(), overallBestFrameC.getRect(),
                                overallBestFrameC.getLandmarks());
                    }
                }

            }// If there were Multiple Faces Detected, we ignore the frame and don't increase frame count.
            else {
                setFaceStatsAndCallContract(FaceStatus.INVALID_FACE, FaceTypeStatus.MULTIPLE_FACES);
                frameProcessed = 0;
                clusterSize = 0;
            }
        } // If there was No Face Detected, we ignore the frame and don't increase frame count.
        else {
            noFaceCount++;
            if (noFaceCount >= kConfig.getMinNoFaceThreshold()) {
                setFaceStatsAndCallContract(FaceStatus.NO_FACE, null);
                frameProcessed = 0;
                clusterSize = 0;
            }
        }

    }


    //**************************************************************************//
    //****************** KCameraFragment's Contract Interface *******************//

    interface Contract {
        void onFaceDetected(FaceStatus faceStatus, FaceTypeStatus faceTypeStatus);

        void timeOut();

        void onFaceResult(final Bitmap bitmap, final float score, final Rect faceRect, float[] landmark);

        void drawDebug(Rect rect, Rect parentRect, float[] landmarks);

        KConfig getConfig();
    }

}