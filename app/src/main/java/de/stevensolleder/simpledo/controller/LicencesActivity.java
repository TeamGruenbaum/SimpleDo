package de.stevensolleder.simpledo.controller;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import de.stevensolleder.simpledo.R;

public class LicencesActivity extends AppCompatActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.licenses_activity);

        WebView webView=findViewById(R.id.webview);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.loadUrl("file:///android_asset/licenses.html");
    }
}
