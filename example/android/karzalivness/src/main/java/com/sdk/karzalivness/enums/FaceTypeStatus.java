package com.sdk.karzalivness.enums;

import androidx.annotation.Keep;

/**
 * Invalid face detection types status enum.
 */
@Keep
public enum FaceTypeStatus {
    /**
     * Low Brightness conditions
     */
    LOW_BRIGHTNESS,
    /**
     * High Brightness conditions
     */
    HIGH_BRIGHTNESS,
    /**
     * Face too far
     */
    TOO_FAR,
    /**
     * Face too close
     */
    TOO_CLOSE,
    /**
     * Face lies outside
     */
    LIES_OUTSIDE,
    /**
     * Multiple Faces
     */
    MULTIPLE_FACES
}
