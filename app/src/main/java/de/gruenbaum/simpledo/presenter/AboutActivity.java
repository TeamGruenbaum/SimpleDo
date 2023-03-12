package de.gruenbaum.simpledo.presenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.gruenbaum.simpledo.databinding.AboutActivityBinding;

public class AboutActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AboutActivityBinding aboutBinding = AboutActivityBinding.inflate(getLayoutInflater());
        setContentView(aboutBinding.getRoot());

        aboutBinding.isabellwaas.setOnClickListener((view)->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/isabellwaas"))));
        aboutBinding.stevensolleder.setOnClickListener((view)->startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://stevensolleder.de"))));
    }
}
