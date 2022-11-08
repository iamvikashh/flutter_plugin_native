package com.codingwithtashi.web_view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sdk.karzalivness.KLivenessView
import com.sdk.karzalivness.enums.*
import com.sdk.karzalivness.interfaces.KLivenessCallbacks
import com.sdk.karzalivness.models.KLiveResult
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.platform.PlatformView


class FlutterWebView internal constructor(
    context: Context,
    messenger: BinaryMessenger,
    id: Int,
    activity: Activity
) :
    PlatformView,KLivenessCallbacks, MethodCallHandler, AppCompatActivity() {
    val activity=activity
    val context=context
    private val kLivenessView: KLivenessView
    private  var methodChannel: MethodChannel
    override fun getView(): View {
        return kLivenessView//this is the object of UI class provided by plugin
    }

    init {

        kLivenessView = KLivenessView(context)

        kLivenessView.initialize(
            (activity as FragmentActivity).supportFragmentManager,
            this,
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXF1ZXN0X2lkIjoiNjZiMWJkNjYtNGZmOS00NGU4LTk1MjEtZDYwM2NiZjFiZTBkIiwidXNlcl9pZCI6MTk3MzA4LCJzY29wZSI6WyJsaXZlbmVzcyJdLCJlbnYiOiJ0ZXN0IiwiY2xpZW50X2lkIjoiS2FyemFfVGVjaF90VmpJdVoiLCJzdGFnZSI6InRlc3QiLCJ1c2VyX3R5cGUiOiJvcGVuIiwiZXhwaXJ5X3RpbWUiOiIyMS0xMC0yMDIyVDA5OjE0OjUyIn0._W8n-g8xcf49I-OQUzWhGAqdeyJswKx1wdXiZeJiCxQ",
            KEnvironment.TEST,
            null,
            CameraFacing.FRONT
        )

        methodChannel = MethodChannel(messenger, "plugins.codingwithtashi/flutter_web_view_$id")
        // Init methodCall Listener
        methodChannel.setMethodCallHandler(this)

      //  webView.initialize(supportFragmentManager,this,"token",KEnvironment.TEST, null, CameraFacing.FRONT)
        // Set client so that you can interact within WebView
            //webView.webViewClient = WebViewClient()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {

            // Checking whether user granted the permission or not.
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                kLivenessView.initialize(
                    (activity as FragmentActivity).supportFragmentManager,
                    this,
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXF1ZXN0X2lkIjoiNjZiMWJkNjYtNGZmOS00NGU4LTk1MjEtZDYwM2NiZjFiZTBkIiwidXNlcl9pZCI6MTk3MzA4LCJzY29wZSI6WyJsaXZlbmVzcyJdLCJlbnYiOiJ0ZXN0IiwiY2xpZW50X2lkIjoiS2FyemFfVGVjaF90VmpJdVoiLCJzdGFnZSI6InRlc3QiLCJ1c2VyX3R5cGUiOiJvcGVuIiwiZXhwaXJ5X3RpbWUiOiIyMS0xMC0yMDIyVDA5OjE0OjUyIn0._W8n-g8xcf49I-OQUzWhGAqdeyJswKx1wdXiZeJiCxQ",
                    KEnvironment.TEST,
                    null,
                    CameraFacing.FRONT
                )

                // Showing the toast message
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMethodCall(methodCall: MethodCall, result: MethodChannel.Result) {
        Log.d("Priority","inside methodCall")
        when (methodCall.method) {
            "setUrl" -> setText(methodCall, result)
            else -> result.notImplemented()
        }
    }

    // set and load new Url
    private fun setText(methodCall: MethodCall, result: MethodChannel.Result ) {
        val url = methodCall.arguments as String


      //  webView.loadUrl(url)
        kLivenessView.initialize(
            (activity as FragmentActivity).supportFragmentManager,
            this,
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyZXF1ZXN0X2lkIjoiNjZiMWJkNjYtNGZmOS00NGU4LTk1MjEtZDYwM2NiZjFiZTBkIiwidXNlcl9pZCI6MTk3MzA4LCJzY29wZSI6WyJsaXZlbmVzcyJdLCJlbnYiOiJ0ZXN0IiwiY2xpZW50X2lkIjoiS2FyemFfVGVjaF90VmpJdVoiLCJzdGFnZSI6InRlc3QiLCJ1c2VyX3R5cGUiOiJvcGVuIiwiZXhwaXJ5X3RpbWUiOiIyMS0xMC0yMDIyVDA5OjE0OjUyIn0._W8n-g8xcf49I-OQUzWhGAqdeyJswKx1wdXiZeJiCxQ",
            KEnvironment.TEST,
            null,
            CameraFacing.FRONT
        )
        result.success("hii")
    }

    // Destroy WebView when PlatformView is destroyed
    override fun dispose() {
       // webView.destroy()
    }

    override fun needPermissions(vararg p0: String?) {
        // Checking if permission is not granted


        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            //Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showLoader() {
       // Toast.makeText(context, "show Loader", Toast.LENGTH_SHORT).show()

    }

    override fun hideLoader() {
     //   Toast.makeText(context, "hide Loader", Toast.LENGTH_SHORT).show()


    }

    override fun onReceiveKLiveResult(p0: KLiveStatus?, p1: KLiveResult?) {
      //  Toast.makeText(context, "got result", Toast.LENGTH_SHORT).show()

    }

    override fun faceStatus(p0: FaceStatus?, p1: FaceTypeStatus?) {
       // Toast.makeText(context, "face Loader", Toast.LENGTH_SHORT).show()

    }

    override fun onError(p0: String?) {
       // Toast.makeText(context, " error", Toast.LENGTH_SHORT).show()

    }


}