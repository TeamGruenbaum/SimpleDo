package de.stevensolleder.simpledo.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import de.stevensolleder.simpledo.R;

public class DeveloperActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developer_activity);

        ((TextView) findViewById(R.id.textView3)).setMovementMethod(LinkMovementMethod.getInstance());
    }
}