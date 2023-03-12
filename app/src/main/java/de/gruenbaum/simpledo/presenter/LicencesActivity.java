package de.gruenbaum.simpledo.presenter;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import de.gruenbaum.simpledo.databinding.LicensesActivityBinding;

public class LicencesActivity extends AppCompatActivity
{
    private LicensesActivityBinding licencesBinding;

    @Override
    protected void onCreate( Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        licencesBinding= LicensesActivityBinding.inflate(getLayoutInflater());
        setContentView(licencesBinding.getRoot());

        WebView webView=licencesBinding.webview;
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.loadUrl("file:///android_asset/licenses.html");
    }
}
