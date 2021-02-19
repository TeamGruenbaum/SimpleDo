package de.stevensolleder.simpledo.controller;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.SimpleDo;

public class BatteryOptimizationActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_optimization_activity);

        TextView batteryOptimizationTextView=(TextView) findViewById(R.id.batteryOptimizationTextView);
        batteryOptimizationTextView.setText(Html.fromHtml(SimpleDo.getAppContext().getResources().getString(R.string.battery_optimization_description)));


        MaterialButton autostartManagerButton=(MaterialButton) findViewById(R.id.autostartManagerButton);
        TextView autostartManagerTextView=(TextView) findViewById(R.id.autostartManagerTextView);

        autostartManagerButton.setOnClickListener(view ->
        {
            if(AutoStartPermissionHelper.getInstance().getAutoStartPermission(BatteryOptimizationActivity.this)==false)
            {
                (new AlertDialog.Builder(BatteryOptimizationActivity.this)).setMessage(R.string.battery_optimization_hint).show();
            };
        });

    }
}
