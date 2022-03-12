package de.stevensolleder.simpledo.presenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.databinding.AboutActivityBinding;
import de.stevensolleder.simpledo.databinding.MainActivityBinding;

public class AboutActivity extends AppCompatActivity
{
    private AboutActivityBinding aboutBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        aboutBinding=AboutActivityBinding.inflate(getLayoutInflater());
        setContentView(aboutBinding.getRoot());

        aboutBinding.isabellwaas.setOnClickListener((view)->openDeveloperPage(view));
        aboutBinding.stevensolleder.setOnClickListener((view)->openDeveloperPage(view));
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
