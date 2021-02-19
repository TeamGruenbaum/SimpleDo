package de.stevensolleder.simpledo.controller;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.Calendar;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.SaveHelper;
import de.stevensolleder.simpledo.model.SimpleDo;
import de.stevensolleder.simpledo.model.Time;

import static de.stevensolleder.simpledo.controller.Main.openKeyboardIfClosed;
import static de.stevensolleder.simpledo.model.NotificationHelper.planAndSendNotification;
import static de.stevensolleder.simpledo.model.SaveHelper.getAlldayTime;
import static de.stevensolleder.simpledo.model.SaveHelper.getEntriesSize;
import static de.stevensolleder.simpledo.model.SaveHelper.getEntry;
import static de.stevensolleder.simpledo.model.SaveHelper.setAlldayTime;

public class SettingsFragment extends PreferenceFragmentCompat implements PreferenceManager.OnPreferenceTreeClickListener
{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.settings_preference);

        findPreference("allday_reminder_time_key").setSummary(getAlldayTime().toString());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference)
    {
        String preferenceKey=preference.getKey();

        switch(preferenceKey)
        {
            case "allday_reminder_time_key":
                MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(SaveHelper.getAlldayTime().getHour())
                        .setMinute(SaveHelper.getAlldayTime().getMinute())
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(v ->
                {
                    SaveHelper.setAlldayTime(new Time(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                    preference.setSummary(getAlldayTime().toString());
                });

                materialTimePicker.show(getFragmentManager(), null);

                return true;

            case "battery_optimization_key":
                Intent batteryOptimizationIntent=new Intent(SimpleDo.getAppContext(), BatteryOptimizationActivity.class);
                startActivity(batteryOptimizationIntent);
                return true;

            case "about_key":
                Intent aboutIntent=new Intent(SimpleDo.getAppContext(), AboutActivity.class);
                startActivity(aboutIntent);
                return true;

            case "imprint_key":
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://imprint.stevensolleder.de/"));
                startActivity(intent);
                return true;

            case "open_source_licences_key":
                Intent licencesIntent=new Intent(SimpleDo.getAppContext(), LicencesActivity.class);
                startActivity(licencesIntent);
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }
}
