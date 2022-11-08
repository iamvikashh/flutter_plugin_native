import 'package:flutter/material.dart';
import 'package:web_view/web_view.dart';

void main() => runApp(const MaterialApp(home: WebViewExample()));

class WebViewExample extends StatefulWidget {
  const WebViewExample({Key? key}) : super(key: key);

  @override
  State<WebViewExample> createState() => _WebViewExampleState();
}

class _WebViewExampleState extends State<WebViewExample> {
  late final TextEditingController _urlController;
  late final WebViewController _webViewController;
  @override
  void initState() {
    _urlController = TextEditingController(text: 'https://flutter.dev/');
    super.initState();
  }

  @override
  void dispose() {
    _urlController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Flutter WebView example')),
      body: Center(
        child: Container(
          height: 200,
          width: 200,
          //color: Colors.grey,
          child: KliveNessView(
            onMapViewCreated: _onMapViewCreated,
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(onPressed: (){
        _webViewController.setUrl(url: "");
      }),
    );
  }

  // load default
  void _onMapViewCreated(WebViewController controller) {
    _webViewController = controller;
    controller.setUrl(url: _urlController.text);
  }
}
