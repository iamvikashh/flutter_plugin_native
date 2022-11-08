package com.sdk.karzalivness.models;

import androidx.annotation.Keep;

/**
 * Class for returning result values of Face Detection
 */
@Keep
public class KLiveResult {

    private double livenessScore;
    private String imageB64String;
    private String requestId;

    public KLiveResult(double livenessScore, String imageB64String, String requestId) {
        this.livenessScore = livenessScore;
        this.imageB64String = imageB64String;
        this.requestId = requestId;
    }


    //*********************************************************//
    //***************** Setter Functions **********************//

    public void setLivenessScore(double livenessScore) {
        this.livenessScore = livenessScore;
    }

    public void setImageB64String(String imageB64String) {
        this.imageB64String = imageB64String;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    //*********************************************************//
    //****************** Getter Functions *********************//

    /**
     * Get Liveness detected score in {@link Double}.
     * It's value is always between 0 - 1.
     * If value > 0.98 its fake, else it's live.
     *
     * @return {@link Double}.
     */
    public double getLivenessScore() {
        return livenessScore;
    }

    /**
     * Get Base64 string of the image captured.
     *
     * @return {@link String}.
     */
    public String getImageB64String() {
        return imageB64String;
    }

    /**
     * Get RequestID of the transaction/process done.
     *
     * @return {@link String}.
     */
    public String getRequestId() {
        return requestId;
    }

}