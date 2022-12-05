/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.karza.qrcodescansdk;

import android.content.Context;
import android.hardware.Camera.Area;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class Utils {
    private static final float MIN_DISTORTION = 0.3f;
    private static final float MAX_DISTORTION = 3f;
    private static final float DISTORTION_STEP = 0.1f;
    private static final int MIN_PREVIEW_PIXELS = 589824;
    private static final int MIN_FPS = 10000;
    private static final int MAX_FPS = 30000;
    public static final String AADHAAR_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
            "MIIG7jCCBdagAwIBAgIEAv5vUzANBgkqhkiG9w0BAQsFADCB4jELMAkGA1UEBhMCSU4xLTArBgNVBAoTJENhcHJpY29ybiBJZGVudGl0eSBTZXJ2aWNlcyBQdnQgTHRkLjEdMBsGA1UECxMUQ2VydGlmeWluZyBBdXRob3JpdHkxDzANBgNVBBETBjExMDA5MjEOMAwGA1UECBMFREVMSEkxJzAlBgNVBAkTHjE4LExBWE1JIE5BR0FSIERJU1RSSUNUIENFTlRFUjEfMB0GA1UEMxMWRzUsVklLQVMgREVFUCBCVUlMRElORzEaMBgGA1UEAxMRQ2Fwcmljb3JuIENBIDIwMTQwHhcNMjAwNTI3MDUwMzA1WhcNMjMwNTI3MDUwMzA1WjCCARAxCzAJBgNVBAYTAklOMQ4wDAYDVQQKEwVVSURBSTEaMBgGA1UECxMRVGVjaG5vbG9neSBDZW50cmUxDzANBgNVBBETBjU2MDA5MjESMBAGA1UECBMJS2FybmF0YWthMRIwEAYDVQQJEwliYW5nYWxvcmUxOzA5BgNVBDMTMlVJREFJIFRlY2ggQ2VudHJlLCBBYWRoYXIgQ29tcGxleCwgTlRJIExheW91dCwgVGF0MUkwRwYDVQQFE0BiMTlhODdmYWU3YWU5ZWY1NWZmMTY2YjVjYzYyNTcwMGUyOGQ4MmRhNzZiZDUzZjA5ODM2ZWVhZWFiM2ZlMzg1MRQwEgYDVQQDEwtEUyBVSURBSSAwMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANSINogOm/Y3pyz0xqILE4C8eJ79af1dk9Kt0QWuICQyq2beNWzBFml5BVBLjeUvjbWbz2zv4yY9lotTb0kKlWEwP+yctIVVDliaWHr+/zxcwAFDoJKLULJokvIYaUeSDrLDvtSq2K3eypIvmS5Df/T6miBJKyYbxDj+8LTZxeSXh12xBUs9X6RxkWSM2cqIJkPPb2mPYFwchtTCczapaUYaGoQB6mbbwW1PQR6qXxUBVefFe373sGh3Pyty0bOOw/NBYHLES1p+3jUXSp2ovqMxsEEIq0c/oCjjhbJYUKa0190EhZDyTYojGuNsD4VCb7jJk1xN67szEKyYQ2Ld/40CAwEAAaOCAnkwggJ1MEAGA1UdJQQ5MDcGCisGAQQBgjcUAgIGCCsGAQUFBwMEBggrBgEFBQcDAgYKKwYBBAGCNwoDDAYJKoZIhvcvAQEFMBMGA1UdIwQMMAqACEOABKAHteDPMIGIBggrBgEFBQcBAQR8MHowLAYIKwYBBQUHMAGGIGh0dHA6Ly9vY3ZzLmNlcnRpZmljYXRlLmRpZ2l0YWwvMEoGCCsGAQUFBzAChj5odHRwczovL3d3dy5jZXJ0aWZpY2F0ZS5kaWdpdGFsL3JlcG9zaXRvcnkvQ2Fwcmljb3JuQ0EyMDE0LmNlcjCB+AYDVR0gBIHwMIHtMFYGBmCCZGQCAzBMMEoGCCsGAQUFBwICMD4aPENsYXNzIDMgQ2VydGlmaWNhdGUgaXNzdWVkIGJ5IENhcHJpY29ybiBDZXJ0aWZ5aW5nIEF1dGhvcml0eTBEBgZggmRkCgEwOjA4BggrBgEFBQcCAjAsGipPcmdhbml6YXRpb25hbCBEb2N1bWVudCBTaWduZXIgQ2VydGlmaWNhdGUwTQYHYIJkZAEKAjBCMEAGCCsGAQUFBwIBFjRodHRwczovL3d3dy5jZXJ0aWZpY2F0ZS5kaWdpdGFsL3JlcG9zaXRvcnkvY3BzdjEucGRmMEQGA1UdHwQ9MDswOaA3oDWGM2h0dHBzOi8vd3d3LmNlcnRpZmljYXRlLmRpZ2l0YWwvY3JsL0NhcHJpY29ybkNBLmNybDARBgNVHQ4ECgQITfksz0HaUFUwDgYDVR0PAQH/BAQDAgbAMCIGA1UdEQQbMBmBF2FudXAua3VtYXJAdWlkYWkubmV0LmluMAkGA1UdEwQCMAAwDQYJKoZIhvcNAQELBQADggEBACED9DwfU+qImzRkqc4FLN1ED4wgKXsvqwszJrvKKjwiQSxILTcapKPaTuW51HTlKOYUDmQH8MXGWLYjnyJDp/gpj6thcuwiXRFL87UarUMDd5A+dBn4UPkUSuThn+CjrhGQcStaKSz5QfzdOO/2fZeZgDB0xo7IyDtVfC2ZvW1xrxWngKNVkp8XkPNmPW/jHk7395/1obaHsjKNcAaAxNztXGG6azwsURx83Fy6irF4pHFTfZV3Y93iBZovXeetYc1bgIAvLSFd2Yvuy6yGyL8nb8vUMbWYIasZ47E4q+kMDmB49xedQg97L5CRfN0gIrk7foxnTexvSlLtEVo2M/A=\n" +
            "-----END CERTIFICATE-----";
    private static Context mContext;

    private Utils() {
    }

    public static synchronized void setContext(Context context) {
        mContext = context;
    }

    public static void printE(String strTag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(strTag, !TextUtils.isEmpty(message) ? message : "Message is null to print");
        }
    }

    public static void toastMssg(String mssg) {
        Toast.makeText(mContext, mssg, Toast.LENGTH_LONG).show();
    }

    @NonNull
    public static Point findSuitableImageSize(@NonNull final Parameters parameters,
            final int frameWidth, final int frameHeight) {
        final List<Size> sizes = parameters.getSupportedPreviewSizes();
        if (sizes != null && !sizes.isEmpty()) {
            Collections.sort(sizes, new CameraSizeComparator());
            final float frameRatio = (float) frameWidth / (float) frameHeight;
            for (float distortion = MIN_DISTORTION; distortion <= MAX_DISTORTION;
                    distortion += DISTORTION_STEP) {
                for (final Size size : sizes) {
                    final int width = size.width;
                    final int height = size.height;
                    if (width * height >= MIN_PREVIEW_PIXELS &&
                            Math.abs(frameRatio - (float) width / (float) height) <= distortion) {
                        return new Point(width, height);
                    }
                }
            }
        }
        final Size defaultSize = parameters.getPreviewSize();
        if (defaultSize == null) {
            throw new CodeScannerException("Unable to configure camera preview size");
        }
        return new Point(defaultSize.width, defaultSize.height);
    }

    public static void configureFpsRange(@NonNull final Parameters parameters) {
        final List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
        if (supportedFpsRanges == null || supportedFpsRanges.isEmpty()) {
            return;
        }
        Collections.sort(supportedFpsRanges, new FpsRangeComparator());
        for (final int[] fpsRange : supportedFpsRanges) {
            if (fpsRange[Parameters.PREVIEW_FPS_MIN_INDEX] >= MIN_FPS &&
                    fpsRange[Parameters.PREVIEW_FPS_MAX_INDEX] <= MAX_FPS) {
                parameters.setPreviewFpsRange(fpsRange[Parameters.PREVIEW_FPS_MIN_INDEX],
                        fpsRange[Parameters.PREVIEW_FPS_MAX_INDEX]);
                return;
            }
        }
    }

    public static void configureSceneMode(@NonNull final Parameters parameters) {
        if (!Parameters.SCENE_MODE_BARCODE.equals(parameters.getSceneMode())) {
            final List<String> supportedSceneModes = parameters.getSupportedSceneModes();
            if (supportedSceneModes != null &&
                    supportedSceneModes.contains(Parameters.SCENE_MODE_BARCODE)) {
                parameters.setSceneMode(Parameters.SCENE_MODE_BARCODE);
            }
        }
    }

    public static void configureVideoStabilization(@NonNull final Parameters parameters) {
        if (parameters.isVideoStabilizationSupported() && !parameters.getVideoStabilization()) {
            parameters.setVideoStabilization(true);
        }
    }

    public static void configureFocusArea(@NonNull final Parameters parameters,
                                          @NonNull final Rect area, final int width, final int height, final int orientation) {
        final List<Area> areas = new ArrayList<>(1);
        final Rect rotatedArea =
                area.rotate(-orientation, width / 2f, height / 2f).bound(0, 0, width, height);
        areas.add(new Area(new android.graphics.Rect(mapCoordinate(rotatedArea.getLeft(), width),
                mapCoordinate(rotatedArea.getTop(), height),
                mapCoordinate(rotatedArea.getRight(), width),
                mapCoordinate(rotatedArea.getBottom(), height)), 1000));
        if (parameters.getMaxNumFocusAreas() > 0) {
            parameters.setFocusAreas(areas);
        }
        if (parameters.getMaxNumMeteringAreas() > 0) {
            parameters.setMeteringAreas(areas);
        }
    }

    public static void configureDefaultFocusArea(@NonNull final Parameters parameters,
                                                 @NonNull final Rect frameRect, @NonNull final Point previewSize,
                                                 @NonNull final Point viewSize, final int width, final int height,
                                                 final int orientation) {
        final boolean portrait = isPortrait(orientation);
        final int rotatedWidth = portrait ? height : width;
        final int rotatedHeight = portrait ? width : height;
        configureFocusArea(parameters,
                getImageFrameRect(rotatedWidth, rotatedHeight, frameRect, previewSize, viewSize),
                rotatedWidth, rotatedHeight, orientation);
    }

    public static void configureDefaultFocusArea(@NonNull final Parameters parameters,
                                                 @NonNull final DecoderWrapper decoderWrapper, @NonNull final Rect frameRect) {
        final Point imageSize = decoderWrapper.getImageSize();
        Utils.configureDefaultFocusArea(parameters, frameRect, decoderWrapper.getPreviewSize(),
                decoderWrapper.getViewSize(), imageSize.getX(), imageSize.getY(),
                decoderWrapper.getDisplayOrientation());
    }

    public static void configureFocusModeForTouch(@NonNull final Parameters parameters) {
        if (Parameters.FOCUS_MODE_AUTO.equals(parameters.getFocusMode())) {
            return;
        }
        final List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes != null && focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        }
    }

    public static void disableAutoFocus(@NonNull final Parameters parameters) {
        final List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes == null || focusModes.isEmpty()) {
            return;
        }
        final String focusMode = parameters.getFocusMode();
        if (focusModes.contains(Parameters.FOCUS_MODE_FIXED)) {
            if (Parameters.FOCUS_MODE_FIXED.equals(focusMode)) {
                return;
            } else {
                parameters.setFocusMode(Parameters.FOCUS_MODE_FIXED);
                return;
            }
        }
        if (focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
            if (!Parameters.FOCUS_MODE_AUTO.equals(focusMode)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
            }
        }
    }

    public static void setAutoFocusMode(@NonNull final Parameters parameters,
            final AutoFocusMode autoFocusMode) {
        final List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes == null || focusModes.isEmpty()) {
            return;
        }
        if (autoFocusMode == AutoFocusMode.CONTINUOUS) {
            if (Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(parameters.getFocusMode())) {
                return;
            }
            if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                return;
            }
        }
        if (Parameters.FOCUS_MODE_AUTO.equals(parameters.getFocusMode())) {
            return;
        }
        if (focusModes.contains(Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
        }
    }

    public static void setFlashMode(@NonNull final Parameters parameters,
            @NonNull final String flashMode) {
        if (flashMode.equals(parameters.getFlashMode())) {
            return;
        }
        final List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(flashMode)) {
            parameters.setFlashMode(flashMode);
        }
    }

    public static void setZoom(@NonNull final Parameters parameters, final int zoom) {
        if (parameters.isZoomSupported()) {
            if (parameters.getZoom() != zoom) {
                final int maxZoom = parameters.getMaxZoom();
                if (zoom <= maxZoom) {
                    parameters.setZoom(zoom);
                } else {
                    parameters.setZoom(maxZoom);
                }
            }
        }
    }

    public static int getDisplayOrientation(@NonNull final Context context,
            @NonNull final CameraInfo cameraInfo) {
        final WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            throw new CodeScannerException("Unable to access window manager");
        }
        final int degrees;
        final int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                if (rotation % 90 == 0) {
                    degrees = (360 + rotation) % 360;
                } else {
                    throw new CodeScannerException("Invalid display rotation");
                }
        }
        return ((cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? 180 : 360) +
                cameraInfo.orientation - degrees) % 360;
    }

    public static boolean isPortrait(final int orientation) {
        return orientation == 90 || orientation == 270;
    }

    @NonNull
    public static Point getPreviewSize(final int imageWidth, final int imageHeight,
            final int frameWidth, final int frameHeight) {
        if (imageWidth == frameWidth && imageHeight == frameHeight) {
            return new Point(frameWidth, frameHeight);
        }
        final int resultWidth = imageWidth * frameHeight / imageHeight;
        if (resultWidth < frameWidth) {
            return new Point(frameWidth, imageHeight * frameWidth / imageWidth);
        } else {
            return new Point(resultWidth, frameHeight);
        }
    }

    @NonNull
    public static Rect getImageFrameRect(final int imageWidth, final int imageHeight,
                                         @NonNull final Rect viewFrameRect, @NonNull final Point previewSize,
                                         @NonNull final Point viewSize) {
        final int previewWidth = previewSize.getX();
        final int previewHeight = previewSize.getY();
        final int viewWidth = viewSize.getX();
        final int viewHeight = viewSize.getY();
        final int wD = (previewWidth - viewWidth) / 2;
        final int hD = (previewHeight - viewHeight) / 2;
        final float wR = (float) imageWidth / (float) previewWidth;
        final float hR = (float) imageHeight / (float) previewHeight;
        return new Rect(Math.max(Math.round((viewFrameRect.getLeft() + wD) * wR), 0),
                Math.max(Math.round((viewFrameRect.getTop() + hD) * hR), 0),
                Math.min(Math.round((viewFrameRect.getRight() + wD) * wR), imageWidth),
                Math.min(Math.round((viewFrameRect.getBottom() + hD) * hR), imageHeight));
    }

    @NonNull
    public static byte[] rotateYuv(@NonNull final byte[] source, final int width, final int height,
                                   final int rotation) {
        if (rotation == 0 || rotation == 360) {
            return source;
        }
        if (rotation % 90 != 0 || rotation < 0 || rotation > 270) {
            throw new IllegalArgumentException("Invalid rotation (valid: 0, 90, 180, 270)");
        }
        final byte[] output = new byte[source.length];
        final int frameSize = width * height;
        final boolean swap = rotation % 180 != 0;
        final boolean flipX = rotation % 270 != 0;
        final boolean flipY = rotation >= 180;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                final int yIn = j * width + i;
                final int uIn = frameSize + (j >> 1) * width + (i & ~1);
                final int vIn = uIn + 1;
                final int wOut = swap ? height : width;
                final int hOut = swap ? width : height;
                final int iSwapped = swap ? j : i;
                final int jSwapped = swap ? i : j;
                final int iOut = flipX ? wOut - iSwapped - 1 : iSwapped;
                final int jOut = flipY ? hOut - jSwapped - 1 : jSwapped;
                final int yOut = jOut * wOut + iOut;
                final int uOut = frameSize + (jOut >> 1) * wOut + (iOut & ~1);
                final int vOut = uOut + 1;
                output[yOut] = (byte) (0xff & source[yIn]);
                output[uOut] = (byte) (0xff & source[uIn]);
                output[vOut] = (byte) (0xff & source[vIn]);
            }
        }
        return output;
    }

    @Nullable
    public static Result decodeLuminanceSource(@NonNull final MultiFormatReader reader,
            @NonNull final LuminanceSource luminanceSource) throws ReaderException {
        try {
            return reader.decodeWithState(new BinaryBitmap(new HybridBinarizer(luminanceSource)));
        } catch (final NotFoundException e) {
            return reader.decodeWithState(
                    new BinaryBitmap(new HybridBinarizer(luminanceSource.invert())));
        } finally {
            reader.reset();
        }
    }

    private static int mapCoordinate(final int value, final int size) {
        return 2000 * value / size - 1000;
    }

    private static final class CameraSizeComparator implements Comparator<Size> {
        @Override
        public int compare(@NonNull final Size a, @NonNull final Size b) {
            return Integer.compare(b.height * b.width, a.height * a.width);
        }
    }

    private static final class FpsRangeComparator implements Comparator<int[]> {
        @Override
        public int compare(final int[] a, final int[] b) {
            int comparison = Integer.compare(b[Parameters.PREVIEW_FPS_MAX_INDEX],
                    a[Parameters.PREVIEW_FPS_MAX_INDEX]);
            if (comparison == 0) {
                comparison = Integer.compare(b[Parameters.PREVIEW_FPS_MIN_INDEX],
                        a[Parameters.PREVIEW_FPS_MIN_INDEX]);
            }
            return comparison;
        }
    }
}
