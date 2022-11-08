package com.sdk.karzalivness;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.util.concurrent.Executors;

class KUtility {

    private static final String TAG = "KUtility";

    //**************************************************************************//
    //******************* Image/Bitmap Manipulation Methods ********************//

    @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
    public static Bitmap getBitmap(final ImageProxy imageProxy) {
        if (imageProxy.getImage().getFormat() == ImageFormat.YUV_420_888) {
            return yuvToBitmap(imageProxy.getImage());
        } else return imageToBitmap(imageProxy.getImage());
    }

    public static Bitmap yuvToBitmap(Image image) {

////      ******************************************
////      *********** Old Implementation ***********
////      ******************************************

//        Image.Plane[] planes = image.getPlanes();
//        ByteBuffer yBuffer = planes[0].getBuffer();
//        ByteBuffer uBuffer = planes[1].getBuffer();
//        ByteBuffer vBuffer = planes[2].getBuffer();
//
//        int ySize = yBuffer.remaining();
//        int uSize = uBuffer.remaining();
//        int vSize = vBuffer.remaining();
//
//        byte[] nv21 = new byte[ySize + uSize + vSize];
//        //U and V are swapped
//        yBuffer.get(nv21, 0, ySize);
//        vBuffer.get(nv21, ySize, vSize);
//        uBuffer.get(nv21, ySize + vSize, uSize);
//
//        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
//
//        byte[] imageBytes = out.toByteArray();
//        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        //--------- Y Plane ---------//

        // PixelStride should always be 1.
        int rowStride = image.getPlanes()[0].getRowStride();
        assert (image.getPlanes()[0].getPixelStride() == 1) : ("Stride error");

        int pos = 0;

        if (rowStride == width) {
            // Most likely case and the best case. No padding, copy direct.
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        } else {
            // Padding at plane Y. Copy Row by Row.
            int yBufferPos = -rowStride; // not an actual position
            for (; pos < ySize; pos += width) {
                yBufferPos += rowStride;
                yBuffer.position(yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        //--------- UV Plane ---------//

        // Planes U and V should have the same rowStride and pixelStride (overlapped or not).
        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();
        assert (rowStride == image.getPlanes()[1].getRowStride()) : ("Stride error");
        assert (pixelStride == image.getPlanes()[1].getPixelStride()) : ("Stride error");

        // Check UV channels in order to know if they are overlapped (Best Case).
        // If they are overlapped, U and V first bytes are consecutive and pixelStride = 2, which means vBuffer[1] is alias of uBuffer[0].

        // BEST: Valid NV21 representation (UV overlapped, no padding). Copy direct.
        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte) ~savePixel);
                if (uBuffer.get(0) == (byte) ~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21ByteArrayToBitmap(nv21, image); // shortcut

                }
            } catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        //WORST: Not overlapped UV. Copy byte by byte.
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int vuPos = col * pixelStride + row * rowStride;
                //V before U, when making NV21 array. V is interleaved b/w Y and U.
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21ByteArrayToBitmap(nv21, image);
    }

    public static Bitmap nv21ByteArrayToBitmap(byte[] nv21, Image image){
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public static Bitmap imageToBitmap(final Image imageProxy) {
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        buffer.rewind();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        byte[] clonedBytes = bytes.clone();
        return BitmapFactory.decodeByteArray(clonedBytes, 0, bytes.length);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, false);
    }

    public static Bitmap getScaledBitmap(final Bitmap inputBitmap, int maxResolution) {
        int newWidth = 0;
        int newHeight = 0;
        float ratioBitmap = ((float) inputBitmap.getWidth() / (float) inputBitmap.getHeight());
        if (inputBitmap.getWidth() >= inputBitmap.getHeight()) {
            newWidth = maxResolution;
            newHeight = (int) (newWidth / ratioBitmap);
        } else {
            newHeight = maxResolution;
            newWidth = (int) (ratioBitmap * newHeight);
        }
        return Bitmap.createScaledBitmap(inputBitmap, newWidth, newHeight, false);
    }


    //**************************************************************************//
    //******************** Image Save Method ***********************************//

    public static void testSaveImage(Bitmap bitmap, String folderName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            File createFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), folderName);
            if (!createFolder.exists())
                createFolder.mkdir();
            File f = new File(createFolder, "fr" + "_" + String.valueOf(System.currentTimeMillis())
                    .substring(4) + ".jpeg");
            try (FileOutputStream out = new FileOutputStream(f)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (IOException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "Bitmap related error");
                }
                e.printStackTrace();
            } finally {
                if (BuildConfig.DEBUG) {
                    Log.e("Temp", " Conf : " + folderName);
                }
            }
        });
//
    }


    //**************************************************************************//
    //******************* Copy model asset to storage Method *******************//

    static void copyAssetToStorage(Context context, String assetName, File file) {
        try {
            AssetManager manager = context.getAssets();
            InputStream inputStream = manager.open(assetName);
//            File output = new File(context.getFilesDir(), fileName);
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Asset copy error");
            }
            e.printStackTrace();
        }
    }


    //**************************************************************************//
    //*********************** GetStackTrace Method *****************************//

    public static String getStackTraceString(Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String crashLog = result.toString();
        printWriter.close();
        return crashLog;
    }


    //**************************************************************************//
    //********************* IOU + Cluster related Methods **********************//

    private static int intersectionArea(Rect rect1, Rect rect2) {
        int xOverlapLength = Math.max(0, Math.min(rect1.right, rect2.right) - Math.max(rect1.left, rect2.left));
        int yOverlapLength = Math.max(0, Math.min(rect1.bottom, rect2.bottom) - Math.max(rect1.top, rect2.top));
        return xOverlapLength * yOverlapLength;
    }

    public static double IOU(Rect rect1, Rect rect2) {
        int intersectionArea = intersectionArea(rect1, rect2);
        int area1 = rect1.width() * rect1.height();
        int area2 = rect2.width() * rect2.height();
        return (intersectionArea) / (double) ((area1 + area2) - intersectionArea);
    }

    public static void clusterSetter(FrameCluster cluster, Bitmap frame, float score,
                                     Rect rect, float[] landmark) {
        cluster.setBitmap(frame);
        cluster.setScore(score);
        cluster.setRect(rect);
        cluster.setLandmarks(landmark);
    }

    public static void frameScoreAreaCompare(FrameCluster frameCluster, KConfig kConfig,
                                             Bitmap currentFrame, float score, Rect currentRect,
                                             float[] landmark) {
        // check if current score is bigger than last one
        final boolean isCurrentScoreBigger = score > frameCluster.getScore();
        boolean isCurrentRectBigger;
        if (frameCluster.getRect() == null) {
            isCurrentRectBigger = true;
        } else {
            isCurrentRectBigger = (frameCluster.getRect().width() * frameCluster.getRect().height()) <
                    (currentRect.width() * currentRect.height());
        }

        // Check if only score is enabled and rect is disabled in KConfig && score is higher this time.
        if (kConfig.useMaxScore() && !kConfig.useMaxRect() && isCurrentScoreBigger) {
            clusterSetter(frameCluster, currentFrame, score, currentRect, landmark);
        }
        // Check if only rect is enabled and score is disabled in KConfig && rect is bigger this time.
        else if (kConfig.useMaxRect() && !kConfig.useMaxScore() && isCurrentRectBigger) {
            clusterSetter(frameCluster, currentFrame, score, currentRect, landmark);
        }
        // If both score and rect is enabled, then check both score and rect must be bigger
        else if (kConfig.useMaxScore() && kConfig.useMaxRect()
                && isCurrentScoreBigger && isCurrentRectBigger) {
            clusterSetter(frameCluster, currentFrame, score, currentRect, landmark);
        }
    }


    //**************************************************************************//
    //******************* Extra/Other Image Manipulation Methods ***************//

    public static Bitmap getImage(final Bitmap bitmap, final KConfig config, final Rect rect) {
        Bitmap finalBitmap = bitmap;
//        Bitmap cropBitmap = null;

        if (bitmap != null) {
            if (config != null) {

                if (config.isShouldSendEntireRawImage()) {
                    if (config.isShouldDownSample()) {
                        finalBitmap = KUtility.getScaledBitmap(bitmap, config.getMaxRes());
                    } else {
                        finalBitmap = bitmap;
                    }
                } else if (config.isShouldSendCroppedImage()) {
//                cropBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width()
//                        , rect.height());
                    //TODO: "Coords shouldn't go outside image coords" Logic for better handling
                    int orgRectWidth = rect.width();
                    int orgRectHeight = rect.height();
                    rect.left = rect.left - (int) (orgRectWidth * ((float) config.getFaceCropPaddingPercentage().getLeft() / 100));
                    rect.right = rect.right + (int) (orgRectWidth * ((float) config.getFaceCropPaddingPercentage().getRight() / 100));
                    rect.bottom = rect.bottom + (int) (orgRectHeight * ((float) config.getFaceCropPaddingPercentage().getBottom() / 100));
                    rect.top = rect.top - (int) (orgRectHeight * ((float) config.getFaceCropPaddingPercentage().getTop() / 100));
                    try {
                        finalBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width()
                                , rect.height());
                    } catch (Exception e) {
                        finalBitmap = bitmap;
                        if (BuildConfig.DEBUG) {
                            Log.e(TAG, "Crop Image Creation Error =>  Modified Rect dimensions" +
                                    " went out of bounds of actual bitmap OR createBitmap error");
                        }
                        e.printStackTrace();
                    }
                }
//            if (finalBitmap != null && cropBitmap != null) {
//                testSaveImage(finalBitmap, "finalBitmap");
//                testSaveImage(cropBitmap, "cropBitmap");
//            }
            }
        }

        return finalBitmap;
    }

    public static String bitmapToB64JPEG(Bitmap bmp) {
        if (bmp == null) return "";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    public static String bitmapToB64JPEG(Bitmap bmp, KConfig config) {
        if (bmp == null) return "";
        if (config == null) return bitmapToB64JPEG(bmp);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap.CompressFormat format = config.getIsJPEG() ? Bitmap.CompressFormat.JPEG
                : Bitmap.CompressFormat.PNG;
        bmp.compress(format, config.getQuality(), byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    public static Bitmap base64toBitmap(String rawBase64) {
        byte[] decodedString = Base64.decode(rawBase64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }


}

