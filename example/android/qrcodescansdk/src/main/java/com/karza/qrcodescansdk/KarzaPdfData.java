package com.karza.qrcodescansdk;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.stetho.Stetho;
import com.gemalto.jp2.JP2Decoder;
//import com.github.barteksc.pdfviewer.PDFView;
//import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
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
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.layout.Document;
import com.karza.qrcodescansdk.model.AadhaarCard;
import com.karza.qrcodescansdk.model.AadhaarXMLParser;
import com.karza.qrcodescansdk.util.FileUtils;
//import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
//import com.tom_roush.pdfbox.rendering.PDFRenderer;
//import com.tom_roush.pdfbox.rendering.PDFRenderer;
//import com.shockwave.pdfium.PdfPasswordException;
//import com.tom_roush.pdfbox.pdmodel.PDDocument;
//import com.tom_roush.pdfbox.rendering.PDFRenderer;
//import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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


public class KarzaPdfData   {

//    PDFView pdfView;


    private  KarzaPdfView.PdfInterfaceListener listener;

    private Context context;
    String dest1;
    private static final String FILENAME = "my_aadhar.pdf";

//
//    public OcrObject() {
//        // set null or default listener or accept as argument to constructor
//        this.listener = null;
//    }

    // Assign the listener implementing events interface that will receive the events
    void setCustomObjectListener(final Context context, KarzaPdfView.PdfInterfaceListener listener1, final String uri, final String password) {
        this.listener = listener1;
        this.context = context;
        Stetho.initializeWithDefaults(context);

        if (new File(FileUtils.getAppPath(context), FILENAME).exists()) {
            new File(FileUtils.getAppPath(context), FILENAME).delete();
        }

//        PDFBoxResourceLoader.init(context.getApplicationContext());

        methodRequiresPermission();


        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                // Do some work
                showPdfFromUri(Uri.parse(uri),password);

            }
        };
        Executor mExecutor = Executors.newSingleThreadExecutor(); mExecutor.execute(mRunnable);
    }


    private void methodRequiresPermission() {
        String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(context, perms)) {
//            mScanner.startPreview();

//            readQrfromFile();
        } else {
            listener.onPdfPermissionFailure("failure","Make sure all the permission are granted");
            // Do not have permissions, request them now
//            EasyPermissions.requestPermissions(this,"This app needs access to your CAMERA",
//                    RC_CAMERA, perms);
        }
    }

//width_height: 2479 3508
    private void readQrfromFile(Bitmap bitmapUnique) {
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

//            try {

//                ;
                Result result ;
                try {
                    // first try
                    Log.e("status_done", "First Try");

                    result = reader.decode(bitmap);
                }
                catch (Exception ex){
//                    2nd
                    try {
                        Log.e("status_done", "2nd Try");

                        Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
                        decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                        decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
                        result = reader1.decode(bitmap, decodeHints);
                    }
                    catch (Exception ex1){
//                        Map<DecodeHintType, Object> hints = new HashMap<>();
//                        hints.put(DecodeHintType.PURE_BARCODE, true);
//                        result = reader3.decode(bitmap);
                        //try
                        Log.e("status_done", "3rd Try");

                        try {
                            Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
                            decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                            decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);

                            result = reader.decode(bitmap, decodeHints);
                        }
                        catch (Exception exp){

//try
                            Log.e("status_done", "4rd Try");
                            try {
                                Map<DecodeHintType, Object> decodeHints = new EnumMap<DecodeHintType, Object>(
                                        DecodeHintType.class);
                                decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                                decodeHints.put(DecodeHintType.POSSIBLE_FORMATS,
                                        EnumSet.allOf(BarcodeFormat.class));
                                decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

                                result = reader.decode(bitmap, decodeHints);
                                Log.e("status_done", "Done Scanning QR ");
                            }
                            catch (Exception exL){

                                try {
                                    Log.e("status_done", "5th Try");

                                    Map<DecodeHintType, Object> decodeHints = new EnumMap<DecodeHintType, Object>(
                                            DecodeHintType.class);
//                                decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//                                decodeHints.put(DecodeHintType.POSSIBLE_FORMATS,
//                                        EnumSet.allOf(BarcodeFormat.class));
                                    decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

                                    result = reader.decode(bitmap, decodeHints);
                                    Log.e("status_done", "Done Scanning QR with exception");
                                }
                                catch (Exception expLL){

                                    Log.e("status_done", "7th Try");

                                    try {
                                        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                                        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                                        result = reader.decode(bitmap, (Map) hintMap);
                                    }
                                    catch (Exception expn){
                                        Log.e("status_done", "8th Try");

                                        try {
                                            MultiFormatReader reader4 = new MultiFormatReader();// use this otherwise
//                                        QRCodeReader reader5 = new QRCodeReader();// use this otherwise

//                                        Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
//
////                                        decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//                                        decodeHints.put(DecodeHintType.POSSIBLE_FORMATS,
//                                                EnumSet.allOf(BarcodeFormat.class));
                                            result = reader4.decodeWithState(bitmap);

                                        }
                                        catch (Exception exx){
                                           result = reader1.decode(bitmap);
                                        }
                                    }

                                }



                            }


//                            Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
////                            decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//                            decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
//
////                            MultiFormatReader reader4 = new MultiFormatReader();// use this otherwise
//                            result = reader.decode(bitmap,decodeHints);

                        }
                    }
                }
//                bMap.get
//                Log.e("result_value_before",result.getRawBytes());
                Log.e("result_value_before",result.getText());
                Log.e("result_value_byte",result.getRawBytes().toString());
                contents = result.getText();
//                contents = result.getText().trim().replaceAll("\"","");
                Log.e("result_value_after",contents);

                byte[] bytes = convertToByteArray(contents);
//                byte[] bytes = contents.getBytes();
//                                    Bundle bundle =  result.toString().contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") ? readAadharXmlData(text):readByteArray(bytes) ;
//                Bundle bundle =  readByteArray(bytes) ;

                 Bundle bundle =  result.getText().toString().contains("<PrintLetterBarcodeData") ? readAadharXmlData(contents):readByteArray(bytes) ;


//                progressBar.setVisibility(View.GONE);
//                tvProgressMessage.setVisibility(View.GONE);

//                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
//                intent.putExtras(bundle);
//                startActivity(intent);



                if (listener != null) {
                    Log.e("status_done","Cool Inside Listener");
                    if (new File(dest1).exists()) {
                        new File(dest1).delete();
                    }
                    if (new File(FileUtils.getAppPath(context), FILENAME).exists()) {
                        new File(FileUtils.getAppPath(context), FILENAME).delete();
                    }
                    listener.onPdfDataSuccess("success",bundle);
                }
//        OcrObject.onOcrSuccess(type,jsonObject);
//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("RESULT",jsonObject.toString());
//        returnIntent.putExtra("TYPE",type);
//        setResult(Activity.RESULT_OK,returnIntent);
//                cofinish();




//                callback.onDecoded(bundle);


                Log.e(KarzaPdfData.class.getSimpleName(), "Contents-> " + contents);
//            }
//            catch (NotFoundException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            if (listener != null) {
                if (new File(dest1).exists()) {
                    new File(dest1).delete();
                }
                if (new File(FileUtils.getAppPath(context), FILENAME).exists()) {
                    new File(FileUtils.getAppPath(context), FILENAME).delete();
                }
//                Log.e("status_done","Cool Inside Listener");
                listener.onPdfDataFailure("failure","QR not found exception");
            }
        }

    }

   private void copyToCache(File file, Uri uri) throws IOException {

//        if (!file.exists()) {
        //Get input stream object to read the pdf
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
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
        dest1 = FileUtils.getAppPath(context) + "1234.pdf";

//        final File file = new File(uri.getPath());

//        PdfReader reader = null;
        try
        {

            File file = new File(FileUtils.getAppPath(context), FILENAME);


            if (!file.exists()) {
                // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
                // the cache directory.
//                InputStream asset = getAssets().open(FILENAME);
//                File file1 = new File(uri.toString());
//                File file1 = new File(uri.toString());

                InputStream asset = context.getContentResolver().openInputStream(uri);
                FileOutputStream output = new FileOutputStream(file);
                final byte[] buffer = new byte[1024];
                int size;
                while ((size = asset.read(buffer)) != -1) {
                    output.write(buffer, 0, size);
                }
                asset.close();
                output.close();
            }


            if (new File(dest1).exists()) {
                Log.e("yes","I am here");
            }

//            ReaderProperties readerProperties = new ReaderProperties();
            final byte[] OWNERPASS =   password.getBytes();
//            PdfReader reader = new PdfReader(new FileInputStream(file), readerProperties.setPassword(OWNERPASS));
            ReaderProperties readerProperties = new ReaderProperties().setPassword(OWNERPASS);
            PdfReader pdfReader = new PdfReader(new FileInputStream(file), readerProperties);
//            PdfReader pdfReader = new PdfReader(new FileInputStream(file), readerProperties);
            pdfReader.setUnethicalReading(true);
//            PdfDocument pdfDocument = new PdfDocument(pdfReader, new PdfWriter(new FileOutputStream(dest1)));
            PdfDocument pdfDocument = new PdfDocument(pdfReader, new PdfWriter(new FileOutputStream(dest1)));
            pdfDocument.close();

//            reader.setUnethicalReading(true);

            //Try to open the PDF with the password
//            PdfDocument pdfDoc = new PdfDocument(reader, new PdfWriter(new File(dest1)));

//            PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(dest1));
//            PdfDocument pdfDocument = new PdfDocument(reader,pdfWriter);

//            PdfDocumentInfo info = pdfDocument.getDocumentInfo();

//            Document document = new Document(pdfDocument, PageSize.A4, true);
//            Document document = new Document(pdfDocument, PageSize.A4);
//            document.close();
//            pdfWriter.


//            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(new File(dest1), ParcelFileDescriptor.MODE_READ_ONLY));
//                renderer.getPageCount();
//                Log.e("file_size",""+renderer.getPageCount());
//            }

//            PdfPage page = pdfDoc.getPage(1);

//            reader.computeUserPassword();
//            Log.e("",reader.computeUserPassword().toString());
            //Do something with the document
//            pdfDocument.setDefaultPageSize(PageSize.A4);
//            pdfDocument.close();
//            reader.close();
//                if (mFileDescriptor != null) {
//                }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            if (listener != null) {
                Log.e("status_done","Cool Inside Listener");
                listener.onPdfDataFailure("failure","Please enter correct password");
            }

//                System.d.Debug.WriteLine(ex.ToString());
            //Exception thrown by PDF reader. We need to try the next password.
//            try {
//                reader.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }


        try {
            Bitmap bitmap =   convertPdfToBitmap(new File(dest1));
            if(bitmap!=null)
                readQrfromFile(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




    private byte[] convertToByteArray(String s) {
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

    private Bundle readByteArray(byte[] bytes) {
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


    public Bundle readAadharXmlData(String aadharXmlData) {
        Bundle bundle = new Bundle();

        try {
            AadhaarCard newCard = new AadhaarXMLParser().parse(aadharXmlData);

            bundle.putBoolean("isError", false);
            bundle.putBoolean("is_old", true);
//        String s = new String(bytes, StandardCharsets.ISO_8859_1);
            Log.e("aadhar_string_data", aadharXmlData);
            String delimiter = "ÿ";
            char isMobileEmailPresent = 0;
//        int index = 1;
            bundle.putString("referenceId", newCard.uid);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("name",newCard.name);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("dob",newCard.dob);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("gender",newCard.gender);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("careOf", "");
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("district", newCard.dist);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("landmark",newCard.lm);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("house",newCard.house);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("location", newCard.loc);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("pincode", newCard.pincode);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("postOffice",newCard.po);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("state", newCard.state);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("street", "");
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("subDistrict", newCard.subdist);
//        index = s.indexOf(delimiter, index + 1);
            bundle.putString("vtc",newCard.vtc);
//        index = s.indexOf(delimiter, index + 1);
            String address = bundle.getString("careOf")+" "+bundle.getString("house")+" "+bundle.getString("location")+" "+bundle.getString("landmark")
                    +" "+bundle.getString("street")+", "+bundle.getString("subDistrict")+" "+bundle.getString("district")+" "+bundle.getString("postOffice")
                    +", "+bundle.getString("vtc")+" "+bundle.getString("state")+" "+bundle.getString("pincode");
            bundle.putString("address", address);
//        bundle.putString("signatureHash", s.substring(s.length() - 256));
//        int endIndex = s.length() - 256;
//        if (isMobileEmailPresent == '3') {
//            bundle.putString("mobileHash", toHexString(s.substring(s.length() - 256 - 32, s.length() - 256).getBytes(StandardCharsets.ISO_8859_1)));
//            bundle.putString("emailHash", toHexString(s.substring(s.length() - 256 - 64, s.length() - 256 - 32).getBytes(StandardCharsets.ISO_8859_1)));
//            endIndex -= 64;
//        } else if (isMobileEmailPresent == '1') {
//            bundle.putString("mobileHash", toHexString(s.substring(s.length() - 256 - 32, s.length() - 256).getBytes(StandardCharsets.ISO_8859_1)));
//            endIndex -= 32;
//        } else if (isMobileEmailPresent == '2') {
//            bundle.putString("emailHash", toHexString(s.substring(s.length() - 256 - 32, s.length() - 256).getBytes(StandardCharsets.ISO_8859_1)));
//            endIndex -= 32;
//        }

//        String jp2 = s.substring(index + 1, endIndex);
//        final Bitmap bmp = new JP2Decoder(jp2.getBytes(StandardCharsets.ISO_8859_1)).decode();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] b = baos.toByteArray();
//        String image = Base64.encodeToString(b, Base64.DEFAULT);
//        bundle.putString("imageBase64", image);
//        printE(Decoder.class.getSimpleName(), bundle.toString());
//
//        try {
//            Signature signature = Signature.getInstance("SHA256withRSA");
//            signature.initVerify(getCertificateFromFile(Utils.AADHAAR_CERTIFICATE).getPublicKey());
//            signature.update(s.substring(0,s.length()-256).getBytes(StandardCharsets.ISO_8859_1));
//            if (signature.verify(s.substring(s.length() - 256).getBytes(StandardCharsets.ISO_8859_1))) {
//                bundle.putString("isSignatureVerified", "Yes");
//            } else {
//                bundle.putString("isSignatureVerified", "No");
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//            bundle.putString("imageBase64", "");
        }
        catch (Exception ex){
//            bundle.putString("imageBase64", "");
        }
        return bundle;
    }



    private String toHexString(byte[] hash) {
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
    private X509Certificate getCertificateFromFile(String certificateString) throws GeneralSecurityException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.ISO_8859_1));
        return (X509Certificate) certFactory.generateCertificate(inputStream);
    }

    private  void SaveImage(Bitmap finalBitmap, byte[] fileBytes) {
//
        String root = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+Environment.DIRECTORY_DOWNLOADS;
        File myDir = new File(root );
        myDir.mkdirs();

//        String fname = "pdf_"+String.valueOf(System.currentTimeMillis())+".jpg";
        String fname = "pdf_"+String.valueOf(System.currentTimeMillis())+".png";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();

//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
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
    private  Bitmap convertPdfToBitmap(File pdfFile) {
//        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        Bitmap bitmap= null;
        Bitmap bitmapNewQrOnly= null;
//        File pdfFile = new File(fileUri.getPath());

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));


            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < 1; i++) {
                PdfRenderer.Page page = renderer.openPage(i);


//                int width = Math.round(context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth());
//                int height = Math.round(context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight());

//                works for my device
//                int width = Math.round(300 / 72 * context.getResources().getDisplayMetrics().widthPixels);
//                int height = Math.round(300/ 72  * context.getResources().getDisplayMetrics().heightPixels);
//                int width = Math.round( (420/70)  * page.getWidth());
//                int height = Math.round( (420/70)   * page.getHeight());

//                int width = Math.round( (420/70)  * page.getWidth());
//                int height = Math.round( (420/70)   * page.getHeight());

//                int width = Math.round( (420/70)  * page.getWidth());
//                int height = Math.round( (420/70)   * page.getHeight());


//                int width = Math.round((300 / 72) * page.getWidth());
//                int height = Math.round((300/ 72)  * page.getHeight())+200;

//                int width = Math.round((300 / 72) * context.getResources().getDisplayMetrics().widthPixels);
//                int height = Math.round((300/ 72)  * context.getResources().getDisplayMetrics().heightPixels);


//                int width = Math.round(300 / 72 * page.getWidth());
//                int height = Math.round(300/ 72  * page.getHeight());

                Log.e("dpi_value", String.valueOf(context.getResources().getDisplayMetrics().densityDpi));
//                int width = 300 / 72 * page.getWidth();
//                int height = 300 / 72 * page.getHeight();

//                int width = 313 / 72 * page.getWidth();
//                int height = 313 / 72 * page.getHeight();
//                Rect rect = new Rect(0, 0, 1, 1);

                int width = Math.round((float)(6 * page.getWidth()));
                int height = Math.round((float)(6 * page.getHeight()));


                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                bitmap.setHasAlpha();
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
//
//                bos.close();
//                PDFRenderer pdfRenderer = new PDFRenderer(document);
////
////
////                                Bitmap bitmap = pr.renderImageWithDPI(0, 300,Bitmap.Config.ARGB_8888);
//                Bitmap bitmap = pdfRenderer.renderImageWithDPI(0, 300,Bitmap.Config.ARGB_8888);
//                ivBitmap.setImageBitmap(bitmap);
////                SaveImage(bitmap,null);
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);

//                Rect cropbBox = page.get();

//                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

//                bitmaps.add(bitmap);

                // close the page
                page.close();

//               Bitmap bitmapNew = Bitmap.createBitmap(bitmap, 0, (int) (bitmap.getHeight()/2.2),width/2, 54*height/100);
//               Bitmap bitmapNewQr = Bitmap.createBitmap(bitmapNew, 0, 0,(int)bitmapNew.getWidth(), (int)bitmapNew.getHeight()/2);
//               bitmapNewQrOnly = Bitmap.createBitmap(bitmapNewQr, (int)bitmapNewQr.getWidth()/2, 0,(int)bitmapNewQr.getWidth()/2, (int)(bitmapNewQr.getHeight()/1.8));


                SaveImage(bitmap,null);
                Log.e("Hey there","It's successful operation");

            }

            // close the renderer
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmap;

    }



    public  float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


}