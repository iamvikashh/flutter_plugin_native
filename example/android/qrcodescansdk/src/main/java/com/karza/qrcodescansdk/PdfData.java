package com.karza.qrcodescansdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.androidnetworking.error.ANError;

import org.json.JSONObject;

public class PdfData {

    public void registerListener(Context ctx){
        PdfActivity.setCustomObjectListener((PdfInterfaceListener) ctx);
    }


    public void pdfInit(Context ctx, Uri uri, String password) {
            Intent intent = new Intent(ctx, PdfActivity.class);
            intent.putExtra("image_uri",uri.toString());
            intent.putExtra("password",password);
//            intent.putExtra(INTENT_KARZATOKEN, karzaToken);
            ctx.startActivity(intent);

    }

    public interface PdfInterfaceListener {

        public void onPdfDataSuccess(String status , Bundle data);

        public void onPdfDataFailure( String status , String message);

    }





}
