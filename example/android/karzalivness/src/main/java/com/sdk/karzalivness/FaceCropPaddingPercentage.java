
package com.sdk.karzalivness;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

class FaceCropPaddingPercentage implements Parcelable {
    private static final int DEFAULT_FACE_CROP_PADDING_PERCENTAGE = 10;

    private int left = DEFAULT_FACE_CROP_PADDING_PERCENTAGE;
    private int right = DEFAULT_FACE_CROP_PADDING_PERCENTAGE;
    private int bottom = DEFAULT_FACE_CROP_PADDING_PERCENTAGE;
    private int top = DEFAULT_FACE_CROP_PADDING_PERCENTAGE;

    FaceCropPaddingPercentage(int left, int right, int bottom, int top) {
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
    }

    public FaceCropPaddingPercentage() {
    }


    //************************************************************//
    //******************* Other Methods **************************//

    @Override
    public String toString() {
        return "FaceCropPaddingPercentage{" +
                "left=" + left +
                ", right=" + right +
                ", bottom=" + bottom +
                ", top=" + top +
                '}';
    }

    public static FaceCropPaddingPercentage create(final JSONObject jsonObject) {
        FaceCropPaddingPercentage faceCrop;

        faceCrop = new FaceCropPaddingPercentage(
                jsonObject.optInt("left", DEFAULT_FACE_CROP_PADDING_PERCENTAGE),
                jsonObject.optInt("right", DEFAULT_FACE_CROP_PADDING_PERCENTAGE),
                jsonObject.optInt("bottom", DEFAULT_FACE_CROP_PADDING_PERCENTAGE),
                jsonObject.optInt("top", DEFAULT_FACE_CROP_PADDING_PERCENTAGE)
        );

        return faceCrop;
    }


    //************************************************************//
    //******************* Parcelable Methods *********************//

    protected FaceCropPaddingPercentage(Parcel in) {
        this.left = ((int) in.readValue((int.class.getClassLoader())));
        this.right = ((int) in.readValue((int.class.getClassLoader())));
        this.bottom = ((int) in.readValue((int.class.getClassLoader())));
        this.top = ((int) in.readValue((int.class.getClassLoader())));
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(left);
        dest.writeValue(right);
        dest.writeValue(bottom);
        dest.writeValue(top);
    }


    //************************************************************//
    //****************** Overridden Methods **********************//

    public int describeContents() {
        return 0;
    }

    public final static Creator<FaceCropPaddingPercentage> CREATOR = new Creator<FaceCropPaddingPercentage>() {

        public FaceCropPaddingPercentage createFromParcel(android.os.Parcel in) {
            return new FaceCropPaddingPercentage(in);
        }

        public FaceCropPaddingPercentage[] newArray(int size) {
            return (new FaceCropPaddingPercentage[size]);
        }
    };


    //************************************************************//
    //***************** Setter and Getter Methods ****************//

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    public int getTop() {
        return top;
    }

}
