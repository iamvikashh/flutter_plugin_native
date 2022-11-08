package com.sdk.karzalivness.interfaces;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sdk.karzalivness.KLivenessView;
import com.sdk.karzalivness.enums.FaceStatus;
import com.sdk.karzalivness.enums.FaceTypeStatus;
import com.sdk.karzalivness.enums.KLiveStatus;
import com.sdk.karzalivness.models.KLiveResult;

/**
 * Callback interface to communicate with {@link KLivenessView}.
 * This interface must need to be implemented by host {@link android.app.Activity}
 * or {@link androidx.fragment.app.Fragment} to work with SDK.
 */
@Keep
public interface KLivenessCallbacks {


    /**
     * This method is called to get permission required by {@link KLivenessView}
     * only permission mandatory is android.permission.CAMERA
     *
     * @param permissions array of permissions
     */
    void needPermissions(@NonNull final String... permissions);


    /**
     * This method is called when SDK is performing long running operation.
     * It's a good practice to show loader to user, so that user will know something is happening.
     * Otherwise user may press back button.
     */
    void showLoader();

    /**
     * This method is called to hide loader, if showing.
     */
    void hideLoader();

    /**
     * Get result of image scan in this callback method.
     *
     * @param status {@link KLiveStatus} Status which tell about result type.
     * @param result {@link KLiveResult} Which can be null in case {@link KLiveStatus} is not successful.
     *               If {@link KLiveStatus} is SUCCESS then result would contain Base64 string of image scanned
     *               and score of result in {@link Double}.
     */
    void onReceiveKLiveResult(KLiveStatus status, @Nullable KLiveResult result);

    /**
     * This method is called while user is scanning their face.
     * This will tell about current status of face {@link FaceStatus}, and problems with it, if so.
     *
     * @param faceStatus     {@link FaceStatus}
     * @param faceTypeStatus {@link FaceTypeStatus}
     */
    void faceStatus(FaceStatus faceStatus, @Nullable FaceTypeStatus faceTypeStatus);

    /**
     * Called in case any internal error happened.
     *
     * @param message Error message.
     */
    void onError(String message);

}
