package de.stevensolleder.simpledo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.stevensolleder.simpledo.databinding.AboutActivityBinding;

public class AboutActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AboutActivityBinding layout=AboutActivityBinding.inflate(getLayoutInflater());

        setContentView(layout.rootDeveloperActivity);

        layout.stevenSolleder.setOnClickListener(view ->
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://stevensolleder.de"));
            startActivity(intent);
        });

        layout.isabellwaas.setOnClickListener(view ->
        {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/isabellwaas"));
                startActivity(intent);
        });
    }
}
