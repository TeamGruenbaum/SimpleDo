package de.stevensolleder.simpledo.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;

import com.google.android.material.button.MaterialButton;

import de.stevensolleder.simpledo.R;

public class DeveloperActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developer_activity);
    }

    public void stevensolleder(View view)
    {

    }

    public void isabellwaas(View view)
    {

    }

    public void contact(View view)
    {

    }

    public void opensource(View view)
    {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(DeveloperActivity.this);
        alertDialog.setPositiveButton("OK", (dialogInterface, i)->{});
        alertDialog.setTitle("Developers");
        alertDialog.setMessage("GSON\nUIUtil");
        alertDialog.setCancelable(false);

        alertDialog.show();
    }
}