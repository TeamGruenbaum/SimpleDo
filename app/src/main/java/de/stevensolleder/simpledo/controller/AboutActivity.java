package de.stevensolleder.simpledo.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import de.stevensolleder.simpledo.R;

public class AboutActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
    }

    public void openDeveloperPage(View view)
    {
        String uri="";
        switch(view.getId())
        {
            case R.id.stevensolleder: uri="https://stevensolleder.de"; break;
            case R.id.isabellwaas: uri="https://github.com/isabellwaas"; break;
        }
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
    }
}
