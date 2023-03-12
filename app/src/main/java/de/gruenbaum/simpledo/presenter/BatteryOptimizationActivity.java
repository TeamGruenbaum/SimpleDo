package de.gruenbaum.simpledo.presenter;

import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.judemanutd.autostarter.AutoStartPermissionHelper;

import de.gruenbaum.simpledo.R;
import de.gruenbaum.simpledo.databinding.BatteryOptimizationActivityBinding;

public class BatteryOptimizationActivity extends AppCompatActivity
{
    private BatteryOptimizationActivityBinding batteryOptimizationBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        batteryOptimizationBinding=BatteryOptimizationActivityBinding.inflate(getLayoutInflater());
        setContentView(batteryOptimizationBinding.getRoot());

        batteryOptimizationBinding.batteryOptimizationTextView.setText(Html.fromHtml(SimpleDo.getAppContext().getResources().getString(R.string.battery_optimization_description)));
        batteryOptimizationBinding.autostartManagerButton.setOnClickListener((view)->openBatteryOptimizationSettings(view));
    }

    public void openBatteryOptimizationSettings(View view)
    {
        if(!AutoStartPermissionHelper.getInstance().getAutoStartPermission(BatteryOptimizationActivity.this))
        {
            (new AlertDialog.Builder(BatteryOptimizationActivity.this)).setMessage(R.string.battery_optimization_hint).show();
        }
    }
}
