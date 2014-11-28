package com.github.trainjezelf;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;


public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        WebView webView = (WebView)findViewById(R.id.text_about);

        String html = "<html>";
        html += "<p><b>Break free!</b></p>";
        html += "<p>Naar een idee van Ronald Bos.</p>";
        html += "<p>Ontwikkeld door:";
        html += "<ul>";
        html += "<li><a href=\"mailto:ronald.bos.msc@gmail.com\">Ronald Bos</a></li>";
        html += "<li><a href=\"mailto:marten@tamerius.nl\">Marten Tamerius</a></li>";
        html += "</ul>";
        html += "<p>Broncode op GitHub: <a href=\"https://github.com/martentamerius/TrainJezelf/tree/android\">https://github.com/martentamerius/TrainJezelf/tree/android</a>.</p>";
        html += "<p>Dit programma maakt gebruik van Joda Time Android onder de <a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache 2.0 licentie<a>.</p>";
        html += "</html>";

        webView.loadDataWithBaseURL("file:///android_res/drawable/", html, "text/html", "UTF-8", null);
    }
}
