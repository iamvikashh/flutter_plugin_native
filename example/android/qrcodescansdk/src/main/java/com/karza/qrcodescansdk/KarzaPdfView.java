package com.karza.qrcodescansdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class KarzaPdfView {

    public void pdfInit(Context ctx,PdfInterfaceListener pdfInterfaceListener, Uri uri, String password) {
        KarzaPdfData karzaPdfData = new KarzaPdfData();
        karzaPdfData.setCustomObjectListener(ctx,pdfInterfaceListener,uri.toString(),password);

    }

    public interface PdfInterfaceListener {

        public void onPdfDataSuccess(String status, Bundle data);

        public void onPdfDataFailure(String status, String message);

        public void onPdfPermissionFailure(String status, String message);

    }





}
