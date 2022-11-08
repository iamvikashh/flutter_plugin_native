package com.sdk.karzalivness;

interface KConstants {

    // Error codes
    int ERROR_CAMERA_PREVIEW = 3001;
    int IO_EXCEPTION = 3002;
    int SOCKET_EXCEPTION = 3003;

    String SILENT_ANTI_HACK_URL_OLD = "v2/liveness-check";
    String LOGS_URL = "videokyc/api/v2/videokyc-logs";
    String SILENT_ANTI_HACK_URL = "videokyc/api/v2/liveness";
    String LIVENESS_CALL_REFERER = "videokyc/kyc-session";
    String CONFIG_URL = "videokyc/api/v2/liveness-config";
    String CONFIG_CALL_REFERER = "videokyc/login";

}
