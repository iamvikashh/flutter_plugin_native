package com.sdk.karzalivness;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

class KConfig implements Parcelable {

    private static final int DEFAULT_MIN_BRIGHTNESS = 2;
    private static final int DEFAULT_MAX_BRIGHTNESS = 8;
    private static final float DEFAULT_MAX_PERCENTAGE_AREA_FOR_RECT_FRONT_CAM = 0.85f;
    private static final float DEFAULT_MIN_PERCENTAGE_AREA_FOR_RECT_FRONT_CAM = 0.2f;
    private static final float DEFAULT_MAX_PERCENTAGE_AREA_FOR_RECT_BACK_CAM = 0.8f;
    private static final float DEFAULT_MIN_PERCENTAGE_AREA_FOR_RECT_BACK_CAM = 0.15f;
    private static final int DEFAULT_FRAME_THRESHOLD = 15;
    private static final int DEFAULT_MIN_FRAME_THRESHOLD = 8;
    private static final float DEFAULT_IOU_THRESHOLD = 0.75f;
    private static final int DEFAULT_MIN_CLUSTER_THRESHOLD = 3;
    private static final int DEFAULT_MIN_NO_FACE_THRESHOLD = 1;
    private static final int DEFAULT_MAX_INPUT_RESOLUTION = 320;
    private static final int DEFAULT_MAX_PHOTO_RESOLUTION = 1080;
    private static final int DEFAULT_QUALITY = 100;
    private static final FaceCropPaddingPercentage
            DEFAULT_FACE_CROP_PADDING_PERCENTAGE = new FaceCropPaddingPercentage();

    private boolean shouldDownSample = false;
    private boolean shouldSendCroppedImage = false;
    private boolean shouldSendEntireRawImage = true;
    private boolean isJPEG = true;
    private boolean useMaxScore = true;
    private boolean useMaxRect = false;
    private boolean useLandmark = false;
    private int minBrightness = DEFAULT_MIN_BRIGHTNESS;
    private int maxBrightness = DEFAULT_MAX_BRIGHTNESS;
    private double maxAreaPercentageFront = DEFAULT_MAX_PERCENTAGE_AREA_FOR_RECT_FRONT_CAM;
    private double minAreaPercentageFront = DEFAULT_MIN_PERCENTAGE_AREA_FOR_RECT_FRONT_CAM;
    private double maxAreaPercentageBack = DEFAULT_MAX_PERCENTAGE_AREA_FOR_RECT_BACK_CAM;
    private double minAreaPercentageBack = DEFAULT_MIN_PERCENTAGE_AREA_FOR_RECT_BACK_CAM;
    private int frameCount = DEFAULT_FRAME_THRESHOLD;
    private int minFrameCount = DEFAULT_MIN_FRAME_THRESHOLD;
    private double iouThreshold = DEFAULT_IOU_THRESHOLD;
    private int minClusterThreshold = DEFAULT_MIN_CLUSTER_THRESHOLD;
    private int minNoFaceThreshold = DEFAULT_MIN_NO_FACE_THRESHOLD;
    private int maxInputResolution = DEFAULT_MAX_INPUT_RESOLUTION;
    private int maxRes = DEFAULT_MAX_PHOTO_RESOLUTION;
    private int quality = DEFAULT_QUALITY;
    private FaceCropPaddingPercentage faceCropPaddingPercentage = DEFAULT_FACE_CROP_PADDING_PERCENTAGE;


    //************************************************************//
    //******************** Constructor Methods *******************//

    KConfig(boolean shouldDownSample, boolean shouldSendCroppedImage
            , boolean shouldSendEntireRawImage, int maxRes, boolean isJPEG
            , int quality, boolean useMaxRect, boolean useMaxScore
            , int minBrightness, int maxBrightness
            , double maxAreaPercentageFront, double minAreaPercentageFront
            , double maxAreaPercentageBack, double minAreaPercentageBack
            , int frameCount, int minFrameCount, double iouThreshold
            , int minClusterThreshold, int minNoFaceThreshold, boolean useLandmark
            , int maxInputResolution, JSONObject faceCropPaddingPercentage) {
        this.shouldDownSample = shouldDownSample;
        this.shouldSendCroppedImage = shouldSendCroppedImage;
        this.shouldSendEntireRawImage = shouldSendEntireRawImage;
        this.maxRes = maxRes;
        this.isJPEG = isJPEG;
        this.quality = quality;
        this.useMaxRect = useMaxRect;
        this.useMaxScore = useMaxScore;
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;
        this.maxAreaPercentageFront = maxAreaPercentageFront;
        this.minAreaPercentageFront = minAreaPercentageFront;
        this.maxAreaPercentageBack = maxAreaPercentageBack;
        this.minAreaPercentageBack = minAreaPercentageBack;
        this.frameCount = frameCount;
        this.minFrameCount = minFrameCount;
        this.iouThreshold = iouThreshold;
        this.minClusterThreshold = minClusterThreshold;
        this.minNoFaceThreshold = minNoFaceThreshold;
        this.useLandmark = useLandmark;
        this.maxInputResolution = maxInputResolution;

        FaceCropPaddingPercentage[] faceCrop = new FaceCropPaddingPercentage[1];
        if (faceCropPaddingPercentage != null) {
            faceCrop[0] = FaceCropPaddingPercentage.create(faceCropPaddingPercentage);
        } else {
            faceCrop[0] = new FaceCropPaddingPercentage();
        }

        this.faceCropPaddingPercentage = faceCrop[0];

    }

    public KConfig() {
    }


    //************************************************************//
    //******************* Parcelable Methods *********************//

    protected KConfig(Parcel in) {
        shouldDownSample = in.readByte() != 0;
        shouldSendCroppedImage = in.readByte() != 0;
        shouldSendEntireRawImage = in.readByte() != 0;
        maxRes = in.readInt();
        isJPEG = in.readByte() != 0;
        quality = in.readInt();
        useMaxScore = in.readByte() != 0;
        useMaxRect = in.readByte() != 0;
        useLandmark = in.readByte() != 0;
        minBrightness = in.readInt();
        maxBrightness = in.readInt();
        maxAreaPercentageFront = in.readDouble();
        minAreaPercentageFront = in.readDouble();
        maxAreaPercentageBack = in.readDouble();
        minAreaPercentageBack = in.readDouble();
        iouThreshold = in.readDouble();
        minClusterThreshold = in.readInt();
        minNoFaceThreshold = in.readInt();
        frameCount = in.readInt();
        minFrameCount = in.readInt();
        maxInputResolution = in.readInt();
        faceCropPaddingPercentage = ((FaceCropPaddingPercentage)
                in.readValue((FaceCropPaddingPercentage.class.getClassLoader())));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (shouldDownSample ? 1 : 0));
        dest.writeByte((byte) (shouldSendCroppedImage ? 1 : 0));
        dest.writeByte((byte) (shouldSendEntireRawImage ? 1 : 0));
        dest.writeInt(maxRes);
        dest.writeByte((byte) (isJPEG ? 1 : 0));
        dest.writeInt(quality);
        dest.writeByte((byte) (useMaxScore ? 1 : 0));
        dest.writeByte((byte) (useMaxRect ? 1 : 0));
        dest.writeByte((byte) (useLandmark ? 1 : 0));
        dest.writeInt(minBrightness);
        dest.writeInt(maxBrightness);
        dest.writeDouble(maxAreaPercentageFront);
        dest.writeDouble(minAreaPercentageFront);
        dest.writeDouble(maxAreaPercentageBack);
        dest.writeDouble(minAreaPercentageBack);
        dest.writeDouble(iouThreshold);
        dest.writeInt(minClusterThreshold);
        dest.writeInt(minNoFaceThreshold);
        dest.writeInt(frameCount);
        dest.writeInt(minFrameCount);
        dest.writeInt(maxInputResolution);
        dest.writeValue(faceCropPaddingPercentage);
    }


    //************************************************************//
    //****************** Overridden Methods **********************//

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<KConfig> CREATOR = new Creator<KConfig>() {
        @Override
        public KConfig createFromParcel(Parcel in) {
            return new KConfig(in);
        }

        @Override
        public KConfig[] newArray(int size) {
            return new KConfig[size];
        }
    };


    //************************************************************//
    //***************** Checker and Getter Methods ***************//

    public boolean isShouldDownSample() {
        return shouldDownSample;
    }

    public boolean isShouldSendCroppedImage() {
        return shouldSendCroppedImage;
    }

    public boolean isShouldSendEntireRawImage() {
        return shouldSendEntireRawImage;
    }

    public int getMaxRes() {
        return maxRes;
    }

    public boolean getIsJPEG() {
        return isJPEG;
    }

    public int getQuality() {
        return quality;
    }

    public boolean useMaxScore() {
        return useMaxScore;
    }

    public boolean useMaxRect() {
        return useMaxRect;
    }

    public boolean useLandmark() {
        return useLandmark;
    }

    public int getMinBrightness() {
        return minBrightness;
    }

    public int getMaxBrightness() {
        return maxBrightness;
    }

    public double getMaxAreaPercentageFront() {
        return maxAreaPercentageFront;
    }

    public double getMinAreaPercentageFront() {
        return minAreaPercentageFront;
    }

    public double getMaxAreaPercentageBack() {
        return maxAreaPercentageBack;
    }

    public double getMinAreaPercentageBack() {
        return minAreaPercentageBack;
    }

    public double getMaxArea(boolean isFront) {
        if (isFront) {
            return getMaxAreaPercentageFront();
        } else return getMaxAreaPercentageBack();
    }

    public double getMinArea(boolean isFront) {
        if (isFront) {
            return getMinAreaPercentageFront();
        } else return getMinAreaPercentageBack();
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getMinFrameCount() {
        return minFrameCount;
    }

    public double getIouThreshold() {
        return iouThreshold;
    }

    public int getMinClusterThreshold() {
        return minClusterThreshold;
    }

    public int getMinNoFaceThreshold() {
        return minNoFaceThreshold;
    }

    public int getMaxInputResolution() {
        return maxInputResolution;
    }

    public FaceCropPaddingPercentage getFaceCropPaddingPercentage() {
        return faceCropPaddingPercentage;
    }


    //************************************************************//
    //******************* Other Methods **************************//

    @Override
    public String toString() {
        return "KConfig{" +
                "shouldDownSample=" + shouldDownSample +
                ", shouldSendCroppedImage=" + shouldSendCroppedImage +
                ", shouldSendEntireRawImage=" + shouldSendEntireRawImage +
                ", maxRes=" + maxRes +
                ", isJPEG=" + isJPEG +
                ", quality=" + quality +
                ", useMaxScore=" + useMaxScore +
                ", useMaxRect=" + useMaxRect +
                ", useLandmark=" + useLandmark +
                ", minBrightness=" + minBrightness +
                ", maxBrightness=" + maxBrightness +
                ", maxAreaPercentageFront=" + maxAreaPercentageFront +
                ", minAreaPercentageFront=" + minAreaPercentageFront +
                ", maxAreaPercentageBack=" + maxAreaPercentageBack +
                ", minAreaPercentageBack=" + minAreaPercentageBack +
                ", iouThreshold=" + iouThreshold +
                ", minClusterThreshold=" + minClusterThreshold +
                ", minNoFaceThreshold=" + minNoFaceThreshold +
                ", frameCount=" + frameCount +
                ", minFrameCount=" + minFrameCount +
                ", maxInputResolution=" + maxInputResolution +
                ", faceCropPaddingPercentage" + faceCropPaddingPercentage +
                '}';
    }

    public static KConfig create(final JSONObject object) {

        return new KConfig(object.optBoolean("shouldDownSample", false)
                , object.optBoolean("shouldSendCroppedImage", false)
                , object.optBoolean("shouldSendEntireRawImage", true)
                , object.optInt("maxRes", DEFAULT_MAX_PHOTO_RESOLUTION)
                , object.optBoolean("isJPEG", true)
                , object.optInt("quality", DEFAULT_QUALITY)
                , object.optBoolean("useMaxRect", false)
                , object.optBoolean("useMaxScore", true)
                , object.optInt("minBrightness", DEFAULT_MIN_BRIGHTNESS)
                , object.optInt("maxBrightness", DEFAULT_MAX_BRIGHTNESS)
                , object.optDouble("maxAreaPercentageFront", DEFAULT_MAX_PERCENTAGE_AREA_FOR_RECT_FRONT_CAM)
                , object.optDouble("minAreaPercentageFront", DEFAULT_MIN_PERCENTAGE_AREA_FOR_RECT_FRONT_CAM)
                , object.optDouble("maxAreaPercentageBack", DEFAULT_MAX_PERCENTAGE_AREA_FOR_RECT_BACK_CAM)
                , object.optDouble("minAreaPercentageBack", DEFAULT_MIN_PERCENTAGE_AREA_FOR_RECT_BACK_CAM)
                , object.optInt("frameCount", DEFAULT_FRAME_THRESHOLD)
                , object.optInt("minFrameCount", DEFAULT_MIN_FRAME_THRESHOLD)
                , object.optDouble("iouThreshold", DEFAULT_IOU_THRESHOLD)
                , object.optInt("minClusterThreshold", DEFAULT_MIN_CLUSTER_THRESHOLD)
                , object.optInt("minNoFaceThreshold", DEFAULT_MIN_NO_FACE_THRESHOLD)
                , object.optBoolean("useLandmark", false)
                , object.optInt("maxInputResolution", DEFAULT_MAX_INPUT_RESOLUTION)
                , object.optJSONObject("faceCropPaddingPercentage")
        );

    }

}
