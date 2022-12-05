package com.codingwithtashi.web_view

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding


class WebViewPlugin :FlutterPlugin,ActivityAware{
  private var activity: Activity? = null
  private var flutterPluginBinding: FlutterPluginBinding? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding
  }

  override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
    flutterPluginBinding = null
  }

  override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {

    bind(activityPluginBinding)
  }

  override fun onReattachedToActivityForConfigChanges(activityPluginBinding: ActivityPluginBinding) {
    bind(activityPluginBinding)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  private fun bind(activityPluginBinding: ActivityPluginBinding) {
    activity = activityPluginBinding.activity
    flutterPluginBinding!!.platformViewRegistry.registerViewFactory(
      "plugins.codingwithtashi/flutter_web_view",
      WebViewFactory(flutterPluginBinding!!.binaryMessenger, activity!!)
    )
  }
}
