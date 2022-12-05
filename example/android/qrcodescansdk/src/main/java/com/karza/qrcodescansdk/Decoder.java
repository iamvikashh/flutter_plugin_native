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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import com.gemalto.jp2.JP2Decoder;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.karza.qrcodescansdk.model.AadhaarCard;
import com.karza.qrcodescansdk.model.AadhaarXMLParser;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.karza.qrcodescansdk.Utils.printE;

final class Decoder {
    private final MultiFormatReader mReader;
    private final DecoderThread mDecoderThread;
    private final StateListener mStateListener;
    private final Map<DecodeHintType, Object> mHints;
    private final Object mTaskLock = new Object();
    private volatile DecodeCallback mCallback;
    private volatile DecodeTask mTask;
    private volatile State mState;

    public Decoder(@NonNull final StateListener stateListener,
                   @NonNull final List<BarcodeFormat> formats, @Nullable final DecodeCallback callback) {
        mReader = new MultiFormatReader();
        mDecoderThread = new DecoderThread();
        mHints = new EnumMap<>(DecodeHintType.class);
//        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//        mHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        mReader.setHints(mHints);
        mCallback = callback;
        mStateListener = stateListener;
        mState = State.INITIALIZED;
    }

    public void setFormats(@NonNull final List<BarcodeFormat> formats) {
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        mReader.setHints(mHints);
    }

    public void setCallback(@Nullable final DecodeCallback callback) {
        mCallback = callback;
    }

    public void decode(@NonNull final DecodeTask task) {
        synchronized (mTaskLock) {
            if (mState != State.STOPPED) {
                mTask = task;
                mTaskLock.notify();
            }
        }
    }

    public void start() {
        if (mState != State.INITIALIZED) {
            throw new IllegalStateException("Illegal decoder state");
        }
        mDecoderThread.start();
    }

    public void shutdown() {
        mDecoderThread.interrupt();
        mTask = null;
    }

    @NonNull
    public State getState() {
        return mState;
    }

    private boolean setState(@NonNull final State state) {
        mState = state;
        return mStateListener.onStateChanged(state);
    }

    private final class DecoderThread extends Thread {
        public DecoderThread() {
            super("cs-decoder");
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            mainLoop:
            for (; ; ) {
                setState(Decoder.State.IDLE);
                Result result = null;
                try {
                    final DecodeTask task;
                    for (; ; ) {
                        synchronized (mTaskLock) {
                            final DecodeTask t = mTask;
                            if (t != null) {
                                mTask = null;
                                task = t;
                                break;
                            }
                            try {
                                mTaskLock.wait();
                            } catch (final InterruptedException e) {
                                setState(Decoder.State.STOPPED);
                                break mainLoop;
                            }
                        }
                    }
                    setState(Decoder.State.DECODING);
                    result = task.decode(mReader);
                } catch (final ReaderException ignored) {
                } finally {
                    if (result != null) {
                        mTask = null;
                        if (setState(Decoder.State.DECODED)) {
                            final DecodeCallback callback = mCallback;
                            if (callback != null) {
                                printE(Decoder.class.getSimpleName(), result.toString());

                                if(result.getText().toString().contains("<?xml")) {
                                    printE(Decoder.class.getSimpleName(), "old_aadhar");
                                }
                                else {
                                    printE(Decoder.class.getSimpleName(), "new_aadhar");
                                }
                                printE("adhar raw",result.getText());
                                String text = result.getText();
//                                text = "5072908984599537669232132071557459208144347234275327030177206626317094470348166013888862711398064801397631199771032231781176164538898316828698743530377300688377326024428279508886562872456249784400203947093193519516649468951135127479710723466989239232786621141444279694460368951817569491888144671971382886035888631574708599724919503284545155160356811921685266082975532836523872795383808165801096893017257672851585262076084317726402778074698397145637028034605753859313417105059509627622135144073685856645884736314610261690508245685804465387492976733545598932410824870177764019676495291775558973121747146896132682386422949808866169136775640817025402393720284554715020465157297932621074920022443486839690268420098928973020441391344351403362619755791083006895853766173622728625332066208801573161549573470074750960840617804692068532547017317451496558648459508167409151240236186554081355281921899862325493253328202954728326744474268097635321860980652360060113143250840359622011182622997825106462031321656732201214638344945090420187484498666051648737607698146528178568682850797586191503453641491651088857476951602245798608728656285824883317020530563070821551079242382863294080702901878166241231750840839797094823040529657765868681063714818508978140500418195815360521861802017704484094848065549671777587493889635993374094897173404748184229817525827996954549502694852316885566687929371406583643214556274771459834972487987605756168037220208647839135006977448816413639941696683610837864926978384426185449261027090959813033598870598256433611984449614611141797195448747188445742150708933032223753512832691655964435278356121633287334445197478313413386596012276935450338608007768863184148381856796808986786176720257038214240332877610142191740260598182204925664989646747975443627299290236335751547328035403533032345283306732528360383254489034738394738234483138805415548611305700446959224136735209631352500851253521407617171772795910758084445499664804564063678837235850590978188104714359621870707323754813451078408149361383446634250670030401664597834234079468988492336204827656231515652977470865190811313605969762808658413721415852106024719907563415886705469141039986266341279628138200121276286649139044739164032442813038752290173812545220892755602970551869337740811603022527257218504242493924726013953627875017417323132756050084159700454211719819056499001799073524499037953781715959975773312968796950757566921451685949793471442070295408149344854691355949670314895319226812647986595426398880940831490452355392257528170246977658358742625418049569309236764800918938345611226648536225842891604560140343928298956646531560118305803443492036317253180955864316265359428656666405742489618087595035618200030259729181933174607582879021033411835418300519049388094885814598002537071926400451341819631654264978482156367776683684551596784338802335011556201586099114445419638359121895360783642996527616769562552514447934700746779418632586727803469709062514763224048292494885999282407951167461515481865560291445007975841734727339540440054883526787857696213770038992957532255878112997723808401217684237428995128356255269056059219777428012031277881944848153470722700573410289174579837130345933820662161257685164588552209418276936880086324740245425722528641712203695311250706692583894855899744472517861052674015232";
//                                if (text.contains("n=")) {
//                                    Bundle bundle = new Bundle();
//                                    bundle.putString("name", text.substring(text.indexOf("n=") + 3, text.indexOf("u=") - 2));
//                                    bundle.putString("maskedAadhaar", text.substring(text.indexOf("u=") + 3, text.indexOf("g=") - 2));
//                                    bundle.putString("gender", text.substring(text.indexOf("g=") + 3, text.indexOf("d=") - 2));
//                                    bundle.putString("dob", text.substring(text.indexOf("d=") + 3, text.indexOf("a=") - 2));
//                                    if (text.contains("i=")) {
//                                        bundle.putString("address", text.substring(text.indexOf("a=") + 3, text.indexOf("i=") - 2));
//                                        bundle.putString("image", text.substring(text.indexOf("i=") + 3, text.indexOf("x=") - 2));
//                                    } else {
//                                        bundle.putString("address", text.substring(text.indexOf("a=") + 3, text.indexOf("x=") - 2));
//                                    }
//                                    callback.onDecoded(bundle);
//                                }
                                try {
//                                    byte[] bytes = convertToByteArray(text);
                                    Bundle bundle =  result.getText().toString().contains("<PrintLetterBarcodeData") ? readAadharXmlData(text):readByteArray(convertToByteArray(text)) ;
//                                    Bundle bundle = readByteArray(convertToByteArray(text)) ;
                                    if(bundle.getBoolean("isError")){//this will be true in case of new version2 aadhar.
                                        ApiService.getInstance().setApiCallback(new ApiService.ApiCallback() {
                                            @Override
                                            public void onSuccess(String activity, JSONObject jsonObject) {
                                                callback.onDecoded(bundle);
                                            }

                                            @Override
                                            public void onError(String activity, String error, int code) {

                                                // Log.e("base_url", BuildConfig.BASE_URL.replace("{env}", kEnvironment.env));
                                                Bundle bundle = new Bundle();
                                                try {
                                                    bundle.putInt("code", code);
                                                    bundle.putBoolean("isError", true);
                                                    bundle.putString("transactionId",ApiService.transactionId);
                                                    bundle.putString("message", error != null ? error: "");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                callback.onDecoded(bundle);
                                            }
                                        });
                                        ApiService.getInstance().apiPostWithAadhaarData("REPORT_GEN_FAILED",bundle.getString("message"));
                                    }
                                    else{
                                        ApiService.getInstance().setApiCallback(new ApiService.ApiCallback() {
                                            @Override
                                            public void onSuccess(String activity, JSONObject jsonObject) {
                                                bundle.putString("transactionId",ApiService.transactionId);
                                                callback.onDecoded(bundle);
                                                ApiService.reassignTransId();
                                            }

                                            @Override
                                            public void onError(String activity, String error, int code) {
                                                // Log.e("base_url", BuildConfig.BASE_URL.replace("{env}", kEnvironment.env));
                                                Bundle bundle = new Bundle();
                                                try {
                                                    bundle.putInt("code", code);
                                                    bundle.putBoolean("isError", true);
                                                    bundle.putString("message", error != null ? error: "");
                                                    bundle.putString("transactionId",ApiService.transactionId);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                callback.onDecoded(bundle);

                                            }
                                        });
                                        ApiService.getInstance().apiPost("REPORT_GEN_SUCCESSFULLY");
                                    }
//

                                } catch (Exception e) {
                                    printE(Decoder.class.getSimpleName(), e.toString());
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("code", 400);
                                    bundle.putBoolean("isError", true);
                                    bundle.putString("transactionId",ApiService.transactionId);
                                    bundle.putString("message", "Unable to scan Aadhaar QR code properly. Kindly check if it is tampered/invalid or try again.");

                                    ApiService.getInstance().setApiCallback(new ApiService.ApiCallback() {
                                        @Override
                                        public void onSuccess(String activity, JSONObject jsonObject) {
                                            Log.d("TESTING","Adhaar fail success");
                                            callback.onDecoded(bundle);


                                        }

                                        @Override
                                        public void onError(String activity, String error, int code) {
                                            Log.d("TESTING","Adhaar fail  error");


                                            // Log.e("base_url", BuildConfig.BASE_URL.replace("{env}", kEnvironment.env));
                                            Bundle bundle = new Bundle();
                                            try {
                                                bundle.putInt("code", code);
                                                bundle.putBoolean("isError", true);
                                                bundle.putString("transactionId",ApiService.transactionId);
                                                bundle.putString("message", error != null ? error: "");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            callback.onDecoded(bundle);
                                        }
                                    });
                                    ApiService.getInstance().apiPostWithAadhaarData("REPORT_GEN_FAILED", "Unable to scan Aadhaar QR code properly. Kindly check if it is tampered/invalid or try again.");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public interface StateListener {
        boolean onStateChanged(@NonNull State state);
    }

    public enum State {
        INITIALIZED,
        IDLE,
        DECODING,
        DECODED,
        STOPPED
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
        Log.e(Decoder.class.getSimpleName(), s);
        String delimiter = "ÿ";
        char isMobileEmailPresent = s.charAt(0);
        String temp=s.substring(0, s.indexOf(delimiter,0));
        if(temp.equals("V2")){
            Bundle bundle1 = new Bundle();
            bundle1.putInt("code", 400);
            bundle1.putBoolean("isError", true);
            bundle1.putString("transactionId",ApiService.transactionId);
            bundle1.putString("message", "Scanned QR not supported");
            return bundle1;
        }

        int index = 1;
        bundle.putString("referenceId", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("name", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("dob", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("gender", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_careof", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_dist", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_landmark", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_house", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_loc", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_pc", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_po", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_state", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_street", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_subdist", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_vtc", s.substring(index + 1, s.indexOf(delimiter, index + 1)));
        index = s.indexOf(delimiter, index + 1);
        String address = bundle.getString("address_careof")+" "+bundle.getString("address_house")+" "+bundle.getString("address_loc")+" "+bundle.getString("address_landmark")
                +" "+bundle.getString("address_street")+", "+bundle.getString("address_subdist")+" "+bundle.getString("address_dist")+" "+bundle.getString("address_po")
                +", "+bundle.getString("address_vtc")+" "+bundle.getString("address_state")+" "+bundle.getString("address_pc");
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


        try {
        String jp2 = s.substring(index + 1, endIndex);
//            String jp2 = s.substring(index + 1, endIndex);
//            Log.e("ENCODED STRING=====>", jp2 + "start ==> " + (index + 1) + "endIndex ===> "+ endIndex);
//            byte[] encodedBytes = jp2.getBytes(StandardCharsets.ISO_8859_1);
//            Log.e("ENCODED BYTES=====>", encodedBytes.toString());


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
        bundle.putString("address_careof", "");
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_dist", newCard.dist);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_landmark",newCard.lm);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_house",newCard.house);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_loc", newCard.loc);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_pc", newCard.pincode);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_po",newCard.po);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_state", newCard.state);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_street", "");
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_subdist", newCard.subdist);
//        index = s.indexOf(delimiter, index + 1);
        bundle.putString("address_vtc",newCard.vtc);
//        index = s.indexOf(delimiter, index + 1);
        String address = bundle.getString("address_careof")+" "+bundle.getString("address_house")+" "+bundle.getString("address_loc")+" "+bundle.getString("address_landmark")
                +" "+bundle.getString("address_street")+", "+bundle.getString("address_subdist")+" "+bundle.getString("address_dist")+" "+bundle.getString("address_po")
                +", "+bundle.getString("address_vtc")+" "+bundle.getString("address_state")+" "+bundle.getString("address_pc");
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

}
