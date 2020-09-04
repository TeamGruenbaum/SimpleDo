package de.stevensolleder.simpledo.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import de.stevensolleder.simpledo.R;

public class AboutActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
    }

    public void stevensolleder(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://stevensolleder.de"));
        startActivity(intent);
    }

    public void isabellwaas(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/isabellwaas"));
        startActivity(intent);
    }

    public void contact(View view)
    {
        String mail="kontakt@stevensolleder.de";

        Intent intent=new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto://"+mail));
        try
        {
            startActivity(intent);
        }
        catch(Exception exception)
        {
            Snackbar mSnackbar=Snackbar.make(findViewById(R.id.root_developer_activity), mail, Snackbar.LENGTH_LONG);
            View mView=mSnackbar.getView();
            ((TextView) mView.findViewById(com.google.android.material.R.id.snackbar_text)).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            mSnackbar.show();
        }
    }

    public void opensource(View view)
    {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(AboutActivity.this);
        alertDialog.setPositiveButton("OK", (dialogInterface, i)->{});
        alertDialog.setTitle("Open-Source-Lizenzen");
        alertDialog.setMessage("GSON\nUIUtil");
        alertDialog.setCancelable(false);

        alertDialog.show();
    }
}