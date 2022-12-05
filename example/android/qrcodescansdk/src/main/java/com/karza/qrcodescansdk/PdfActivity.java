package com.karza.qrcodescansdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.gemalto.jp2.JP2Decoder;
//import com.github.barteksc.pdfviewer.PDFView;
//import com.github.barteksc.pdfviewer.listener.OnErrorListener;
//import com.google.android.material.textview.MaterialTextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.qrcode.QRCodeReader;
//import com.shockwave.pdfium.PdfPasswordException;
//import com.tom_roush.pdfbox.pdmodel.PDDocument;
//import com.tom_roush.pdfbox.rendering.PDFRenderer;
//import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.karza.qrcodescansdk.Utils.printE;
//import static org.spongycastle.asn1.x500.style.RFC4519Style.o;


public class PdfActivity extends AppCompatActivity  {

    private static int PDF_SELECTION_CODE = 99;
    private final int RC_CAMERA = 111;
//    PDFView pdfView;
    //    ImageView ivBitmap;
    ProgressBar progressBar;


    public static PdfData.PdfInterfaceListener listener;

//
//    public OcrObject() {
//        // set null or default listener or accept as argument to constructor
//        this.listener = null;
//    }

    // Assign the listener implementing events interface that will receive the events
    public static void setCustomObjectListener(PdfData.PdfInterfaceListener listener1) {
        listener = listener1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
//        mScanner.setAutoFocusInterval(1000);
//        mScanner.setAutoFocusMode(AutoFocusMode.SAFE);
//        mScanner.setScanMode(ScanMode.CONTINUOUS);

//        mScanner.set
        Stetho.initializeWithDefaults(this);
//        PDFBoxResourceLoader.init(getApplicationContext());


//        btScanPdf = findViewById(R.id.bt_scan_pdf);
//        pdfView = findViewById(R.id.pdf_view);
        progressBar = findViewById(R.id.progress_bar);
//        ivBitmap = findViewById(R.id.iv_bitmap);
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                // Do some work
                showPdfFromUri(Uri.parse(getIntent().getExtras().getString("image_uri")),getIntent().getExtras().getString("password"));

            }
        };
        Executor mExecutor = Executors.newSingleThreadExecutor(); mExecutor.execute(mRunnable);
    }


    @Override
    protected void onResume() {
        super.onResume();
        methodRequiresPermission();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @AfterPermissionGranted(RC_CAMERA)
    private void methodRequiresPermission() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
//            mScanner.startPreview();

//            readQrfromFile();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this,"This app needs access to your CAMERA",
                    RC_CAMERA, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
//width_height: 2479 3508
    void readQrfromFile(Bitmap bitmapUnique) {
//        File image = new File(Environment.getExternalStorageDirectory() + "/Download/aadhaar1.png");
//        Bitmap bMap = BitmapFactory.decodeFile(image.getAbsolutePath());
        try {
            Bitmap bMap = bitmapUnique;
//            Bitmap bMap = Bitmap.createScaledBitmap(bitmapUnique, 3000, 3000, false);
            String contents = null;
            int width = bMap.getWidth(), height = bMap.getHeight();
//            int width = 2479, height =3508;

            int[] intArray = new int[width*height];
//copy pixel data from the Bitmap into the 'intArray' array
            bMap.getPixels(intArray, 0, width, 0, 0,width, height);

//
//            bMap.recycle();
//            bMap = null;


            LuminanceSource source = new RGBLuminanceSource(width, height, intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));


            Reader reader = new MultiFormatReader();
            Reader reader1 = new QRCodeReader();
            Reader reader3 = new DataMatrixReader();

            try {

//                ;
                Result result ;
                try {
                    result = reader.decode(bitmap);
                }
                catch (Exception ex){
                    try {
                        Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
                        decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                        decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
                        result = reader1.decode(bitmap, decodeHints);
                    }
                    catch (Exception ex1){
//                        Map<DecodeHintType, Object> hints = new HashMap<>();
//                        hints.put(DecodeHintType.PURE_BARCODE, true);
//                        result = reader3.decode(bitmap);
                        Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
                        decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                        decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);

                        result = reader.decode(bitmap,decodeHints);
                    }
                }
//                bMap.get
                contents = result.getText();

                byte[] bytes = convertToByteArray(contents);
//                                    Bundle bundle =  result.toString().contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") ? readAadharXmlData(text):readByteArray(bytes) ;
                Bundle bundle =  readByteArray(bytes) ;

//                progressBar.setVisibility(View.GONE);
//                tvProgressMessage.setVisibility(View.GONE);

//                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
//                intent.putExtras(bundle);
//                startActivity(intent);



                if (listener != null) {
                    Log.e("status_done","Cool Inside Listener");
                    listener.onPdfDataSuccess("success",bundle);
                }
//        OcrObject.onOcrSuccess(type,jsonObject);
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("RESULT",jsonObject.toString());
//        returnIntent.putExtra("TYPE",type);
//        setResult(Activity.RESULT_OK,returnIntent);
                finish();




//                callback.onDecoded(bundle);


                Log.e(PdfActivity.class.getSimpleName(), "Contents-> " + contents);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    void copyToCache(File file, Uri uri) throws IOException {

//        if (!file.exists()) {
        //Get input stream object to read the pdf
        try {
            InputStream input = getContentResolver().openInputStream(uri);
            FileOutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int size;
            // Copy the entire contents of the file
            while ((size = input.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            //Close the buffer
            input.close();
            output.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
//        }
    }




    private void showPdfFromUri(final Uri  uri, String password) {

//        pdfView.fromUri(uri)
//                .defaultPage(0)
//                .password(password)
//                .spacing(10)
//                .onError(new OnErrorListener() {
//                    @Override
//                    public void onError(Throwable t) {
//                        if (t instanceof PdfPasswordException){
////                            showPasswordAlert(uri);
//
//
//
//                            if (listener != null) {
//                                Log.e("status_done","Cool Inside Listener");
//                                listener.onPdfDataFailure("failure","Please enter correct password");
//                            }
////        OcrObject.onOcrSuccess(type,jsonObject);
////        Intent returnIntent = new Intent();
////        returnIntent.putExtra("RESULT",jsonObject.toString());
////        returnIntent.putExtra("TYPE",type);
////        setResult(Activity.RESULT_OK,returnIntent);
//                            finish();
//
//                        }
//                    }
//                })
//                .load()
//        ;



//        pdfView.remove



//        pdfView.go(to: CGRect(x: 0, y: 0, width: 300, height: 300), on: page)
//        ArrayList<Bitmap> bitmaps =  pdfToBitmap(uri);


        File fileCopy = new File(getCacheDir(), "karza.pdf");

        try {
            copyToCache(fileCopy, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        PDDocument pd = null;
        try {
              Bitmap bitmap =   convertPdfToBitmap(uri);
              if(bitmap!=null)
              readQrfromFile(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }



    public byte[] convertToByteArray(String s) {
        BigInteger bi = new BigInteger(s, 10);
        byte[] data = bi.toByteArray();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            GZIPInputStream gis = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
            gis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return os.toByteArray();
    }

    public Bundle readByteArray(byte[] bytes) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isError", false);
        bundle.putBoolean("is_old", false);
        String s = new String(bytes, StandardCharsets.ISO_8859_1);
//        Log.e(Decoder.class.getSimpleName(), s);
        String delimiter = "ÿ";
        char isMobileEmailPresent = s.charAt(0);
        int index = 1;
        bundle.putString("referenceId", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("name", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("dob", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("gender", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("careOf", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("district", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("landmark", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("house", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("location", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("pincode", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("postOffice", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("state", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("street", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("subDistrict", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("vtc", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        String address = bundle.getString("careOf")+" "+bundle.getString("house")+" "+bundle.getString("location")+" "+bundle.getString("landmark")
                +" "+bundle.getString("street")+", "+bundle.getString("subDistrict")+" "+bundle.getString("district")+" "+bundle.getString("postOffice")
                +", "+bundle.getString("vtc")+" "+bundle.getString("state")+" "+bundle.getString("pincode");
        bundle.putString("address", address);
//        bundle.putString("signatureHash", s.substring(s.length() - 256));
        int endIndex = s.length() - 256;
        if (isMobileEmailPresent == '3') {
            bundle.putString("mobileHash", toHexString(s.substring(s.length() - 256 - 32, s.length() - 256).getBytes(StandardCharsets.ISO_8859_1)));
            bundle.putString("emailHash", toHexString(s.substring(s.length() - 256 - 64, s.length() - 256 - 32).getBytes(StandardCharsets.ISO_8859_1)));
            endIndex -= 64;
        } else if (isMobileEmailPresent == '1') {
            bundle.putString("mobileHash", toHexString(s.substring(s.length() - 256 - 32, s.length() - 256).getBytes(StandardCharsets.ISO_8859_1)));
            endIndex -= 32;
        } else if (isMobileEmailPresent == '2') {
            bundle.putString("emailHash", toHexString(s.substring(s.length() - 256 - 32, s.length() - 256).getBytes(StandardCharsets.ISO_8859_1)));
            endIndex -= 32;
        }

//        String jp2 = s.substring(index + 1, endIndex);
//        final Bitmap bmp = new JP2Decoder(jp2.getBytes(StandardCharsets.ISO_8859_1)).decode();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] b = baos.toByteArray();
//        String image = Base64.encodeToString(b, Base64.DEFAULT);
//        bundle.putString("imageBase64", image);
////        printE(Decoder.class.getSimpleName(), bundle.toString());
//
//        try {
////            Signature signature = Signature.getInstance("SHA256withRSA");
////            signature.initVerify(getCertificateFromFile(AADHAAR_CERTIFICATE).getPublicKey());
////            signature.update(s.substring(0,s.length()-256).getBytes(StandardCharsets.ISO_8859_1));
////            if (signature.verify(s.substring(s.length() - 256).getBytes(StandardCharsets.ISO_8859_1))) {
//            bundle.putString("isSignatureVerified", "Yes");
////            } else {
////                bundle.putString("isSignatureVerified", "No");
////            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        try {
            String jp2 = s.substring(index + 1, endIndex);
            final Bitmap bmp = new JP2Decoder(jp2.getBytes(StandardCharsets.ISO_8859_1)).decode();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] b = baos.toByteArray();
            String image = Base64.encodeToString(b, Base64.DEFAULT);
            bundle.putString("imageBase64", image);
            printE(Decoder.class.getSimpleName(), bundle.toString());


            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(getCertificateFromFile(Utils.AADHAAR_CERTIFICATE).getPublicKey());
            signature.update(s.substring(0,s.length()-256).getBytes(StandardCharsets.ISO_8859_1));
            if (signature.verify(s.substring(s.length() - 256).getBytes(StandardCharsets.ISO_8859_1))) {
                bundle.putString("isSignatureVerified", "Yes");
            } else {
                bundle.putString("isSignatureVerified", "No");
            }
        }catch (Exception e){
//            bundle.putString("imageBase64", "");
            e.printStackTrace();
        }


        return bundle;
    }


    public String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);
        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));
        // Pad with leading zeros
        while (hexString.length() < 32) {
            hexString.insert(0, '0');
        }
        return hexString.toString();
    }
    public X509Certificate getCertificateFromFile(String certificateString) throws GeneralSecurityException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.ISO_8859_1));
        return (X509Certificate) certFactory.generateCertificate(inputStream);
    }

    private static void SaveImage(Bitmap finalBitmap, byte[] fileBytes) {
//
        String root = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+Environment.DIRECTORY_DOWNLOADS;
        File myDir = new File(root );
        myDir.mkdirs();

        String fname = "pdf"+String.valueOf(System.currentTimeMillis())+".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
//            Bitmap bitmap = BitmapFactory.decodeByteArray(finalBitmap, 0, finalBitmap.length);
//            BufferedOutputStream bos = new BufferedOutputStream(out);
//            bos.write(fileBytes);
//            bos.flush();
//            bos.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private  Bitmap convertPdfToBitmap(Uri fileUri) {
//        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        Bitmap bitmap= null;

        File pdfFile = new File(fileUri.getPath());

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));

            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < 1; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

//                bitmaps.add(bitmap);

                // close the page
                page.close();

            }

            // close the renderer
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;

    }


}