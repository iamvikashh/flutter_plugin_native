package com.sdk.karzalivness;

class KFaceDetector {

    private long nativeObject;
    private static volatile KFaceDetector kFaceDetector;

    static {
        System.loadLibrary("KarzaFaceDetector2");
        System.loadLibrary("KarzaFaceLandmarker2");
        System.loadLibrary("karza-native-lib");
    }

    private KFaceDetector(long nativeObject) {
        this.nativeObject = nativeObject;
    }

    public static KFaceDetector getInstance(String modelPath, String landMarkPath, KConfig config) {
        if (kFaceDetector == null) {
            synchronized (KFaceDetector.class) {
                if (kFaceDetector == null) {
                    if (config.useLandmark()) {
                        kFaceDetector = new KFaceDetector(initDetector(modelPath, landMarkPath));
                    } else {
                        kFaceDetector = new KFaceDetector(init2(modelPath));
                    }
                }
            }
        }

        return kFaceDetector;
    }

    static void releaseInstance() {
        kFaceDetector = null;
    }

    //**************************************************************************//
    //******** Methods that will interact with Android Java Code. After ********//
    //******** triggering these functions, they will then call the native ******//
    //******** methods, which will then return the relevant model output. ******//

    /**
     * Call to check if we have initialized an object natively and have got it's reference here.
     */
    boolean isInitialised() {
        return nativeObject != 0;
    }

    /**
     * Call to detect face and get a face frame with score.
     *
     * @param pixels  Bitmap pixel representation.
     * @param width   Input bitmap width.
     * @param height  Input bitmap height.
     * @param channel Number of channel in bitmap.
     * @return Float array result of model.
     */
    public float[] getFaceResult(int[] pixels, int width
            , int height, int channel) {
        if (isInitialised()) {
            return findFaces(nativeObject, pixels, width, height, channel);
        }
        return new float[]{0};
    }

    /**
     * Call to find 5 landmarks on given face and get the same as a result.
     *
     * @param pixels     Bitmap pixel representation.
     * @param width      Input bitmap width.
     * @param height     Input bitmap height.
     * @param channel    Number of channel in bitmap.
     * @param x          x-coordinate of the face frame/box.
     * @param y          y-coordinate of the face frame/box.
     * @param rectWidth  Width of the face frame/box.
     * @param rectHeight Height of the face frame/box.
     * @return Float array result of model.
     */
    public float[] landmarks(int[] pixels, int width
            , int height, int channel, int x, int y, int rectWidth, int rectHeight) {
        if (isInitialised()) {
            return getLandMarks(nativeObject, pixels, width, height
                    , channel, x, y, rectWidth, rectHeight);
        }
        return new float[]{0};
    }

    /**
     * Call to find the Brightness Index value of the image/pixels[].
     *
     * @param pixels
     * @return {@link Integer} type Brightness Index value.
     */
    int getBrightnessIndex(int[] pixels) {
        return checkBrightness(pixels);
    }

    /**
     * Call to stop model from detecting any further.
     */
    //TODO: Inclusion of releaseInstance function here...
    void stopDetection() {
        if (!isInitialised()) return;
        releaseModel(nativeObject);
        nativeObject = 0;
    }


    //**************************************************************************//
    //************************ Native Methods **********************************//

    public static native long initDetector(String location, String landMarkPath);

    public static native long init2(String faceModel);

    public static native float[] findFaces(long object, int[] pixels, int width
            , int height, int channel);

    public static native int checkBrightness(int[] pixels);

    public static native float[] getLandMarks(long object, int[] pixels, int width
            , int height, int channel, int x, int y, int rectWidth, int rectHeight);

    public static native void releaseModel(long object);
}
