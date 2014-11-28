package com.github.trainjezelf;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Displays an about screen.
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        WebView webView = (WebView)findViewById(R.id.text_about);
        webView.loadUrl("file:///android_asset/about.html");
        // Set webview background color to transparent (alpha = 0)
        webView.setBackgroundColor(0x00000000);
    }
}
