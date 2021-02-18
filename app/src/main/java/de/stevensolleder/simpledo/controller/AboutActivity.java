package de.stevensolleder.simpledo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import de.stevensolleder.simpledo.R;

public class AboutActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        ((MaterialButton) findViewById(R.id.steven_solleder)).setOnClickListener((View.OnClickListener) view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://stevensolleder.de"));
            startActivity(intent);
        });

        ((MaterialButton) findViewById(R.id.isabellwaas)).setOnClickListener((View.OnClickListener) view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/isabellwaas"));
                startActivity(intent);
        });
    }
}
