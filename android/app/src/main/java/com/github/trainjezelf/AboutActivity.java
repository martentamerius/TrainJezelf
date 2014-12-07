package com.github.trainjezelf;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Displays an about screen.
 */
public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set header text
        final String appName = getResources().getString(R.string.app_name);
        String versionName = "<onbekend>";
        try {
            final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Intentionally discard exception
        }
        final TextView textViewAppName = (TextView)findViewById(R.id.about_appname);
        textViewAppName.setText(String.format("%s", appName));
        final TextView textViewAppVersion = (TextView)findViewById(R.id.about_appversion);
        textViewAppVersion.setText(String.format("Versie %s", versionName));

        // Fill web view with static content from assets folder
        final WebView webView = (WebView)findViewById(R.id.about_webview);
        webView.loadUrl("file:///android_asset/about.html");
        // Set webview background color to transparent (alpha = 0)
        webView.setBackgroundColor(0x00000000);
    }
}
