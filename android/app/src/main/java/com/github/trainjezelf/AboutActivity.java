package com.github.trainjezelf;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Displays an about screen.
 */
public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Set up action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
        textViewAppVersion.setText(String.format("%s %s", getResources().getString(R.string.about_version),
                versionName));

        // Fill web view with static content from assets folder
        final WebView webView = (WebView)findViewById(R.id.about_webview);
        String fileName = String.format("about-%s.html", Locale.getDefault().getLanguage());
        try {
            List list = Arrays.asList(getResources().getAssets().list(""));
            if (!Arrays.asList(getResources().getAssets().list("")).contains(fileName)) {
                fileName = "about.html";
            }
        } catch (IOException e) {
            fileName = "about.html";
        }
        final String url = String.format("file:///android_asset/%s", fileName);
        webView.loadUrl(url);
        // Set webview background color to transparent (alpha = 0)
        webView.setBackgroundColor(0x00000000);
    }
}
