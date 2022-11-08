package com.sdk.karzalivness.enums;

import androidx.annotation.Keep;

/**
 * Status of captured result
 */
@Keep
public enum KLiveStatus {

    //BASIC RESPONSE CODES
    NONE(0, "No response"),
    CAMERA_TIMEOUT(1, "Camera Timeout"),
    APP_ERROR(2, "Some internal error"),

    //HTTP STATUS CODES
    SUCCESS(200, "Success"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized Access / Auth Error"),
    INSUFFICIENT_CREDITS(402, "Insufficient Credits!!"),
    FORBIDDEN(403, "Forbidden"),
    NO_INTERNET(404, "No Internet Available"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    ERROR(503, "Something went wrong"),
    REQUEST_TIMED_OUT(504, "Gateway Time Out"),

    //INTERNAL STATUS CODES
    INVALID_ERROR(102, "Invalid ID number or combination of inputs"),
    RECORDS_ERROR(103, "No records found for the given ID or combination of inputs"),
    MAX_RETRIES_ERROR(104, "Max retries exceeded"),
    CONSENT_ERROR(105, "Missing Consent"),
    MULTI_RECORDS_ERROR(106, "Multiple Records Exist"),
    NOT_SUPPORTED_ERROR(107, "Not Supported"),
    INTERNAL_RESOURCE_ERROR(108, "Internal Resource Unavailable"),
    MANY_RECORDS_ERROR(109, "Too many records Found");


    //**************************************************************************//
    //******************** Constructor + Getter Methods ************************//

    public final int statusCode;
    public final String status;

    KLiveStatus(int i, String status) {
        this.statusCode = i;
        this.status = status;
    }

    public static KLiveStatus get(int statusCode) {
        KLiveStatus status = NONE;
        for (KLiveStatus value : values()) {
            if (value.statusCode == statusCode) {
                status = value;
                break;
            }
        }
        return status;
    }

}
