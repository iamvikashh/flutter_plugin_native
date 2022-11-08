import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

typedef FlutterWebViewCreatedCallback = void Function(
    WebViewController controller);

class KliveNessView extends StatelessWidget {
  final FlutterWebViewCreatedCallback onMapViewCreated;
  const KliveNessView({Key? key, required this.onMapViewCreated}) : super(key: key);
  @override
  Widget build(BuildContext context) {
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return AndroidView(
          viewType: 'plugins.codingwithtashi/flutter_web_view',
          onPlatformViewCreated: _onPlatformViewCreated,
        );/*PlatformViewLink(
          viewType: 'plugins.codingwithtashi/flutter_web_view',
          surfaceFactory:
              (context, controller) {
            return AndroidViewSurface(
              controller: controller as AndroidViewController,
              gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
              hitTestBehavior: PlatformViewHitTestBehavior.opaque,
            );
          },
          onCreatePlatformView: (params) {
            return PlatformViewsService.initSurfaceAndroidView(
              id: params.id,
              viewType: 'plugins.codingwithtashi/flutter_web_view',
              layoutDirection: TextDirection.ltr,
              creationParamsCodec: const StandardMessageCodec(),
              onFocus: () {
                params.onFocusChanged(true);
              },
            )
              ..addOnPlatformViewCreatedListener(_onPlatformViewCreated)
              ..create();
          },
        );*/
      case TargetPlatform.iOS:
        return UiKitView(
          viewType: 'plugins.codingwithtashi/flutter_web_view',
          onPlatformViewCreated: _onPlatformViewCreated,
        );
      default:
        return Text(
            '$defaultTargetPlatform is not yet supported by the web_view plugin');
    }
  }

  // Callback method when platform view is created
  void _onPlatformViewCreated(int id) =>
      onMapViewCreated(WebViewController._(id));
}

// WebView Controller class to set url etc
class WebViewController {
  WebViewController._(int id)
      : _channel =
            MethodChannel('plugins.codingwithtashi/flutter_web_view_$id');

  final MethodChannel _channel;

  Future<void> setUrl({required String url}) async {
    return _channel.invokeMethod('setUrl', url);
  }


}
