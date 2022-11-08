package com.sdk.karzalivness.enums;

import androidx.annotation.Keep;

/**
 * Camera face detection status enum.
 */
@Keep
public enum FaceStatus {
    /**
     * Valid face detected
     */
    VALID_FACE,
    /**
     * No face detected
     */
    NO_FACE,
    /**
     * Invalid  face detected
     */
    INVALID_FACE
}
