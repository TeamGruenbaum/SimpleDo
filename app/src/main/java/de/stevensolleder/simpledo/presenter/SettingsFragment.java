package de.stevensolleder.simpledo.presenter;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.DataAccessor;
import de.stevensolleder.simpledo.model.SettingsAccessor;
import de.stevensolleder.simpledo.model.ISettingsAccessor;
import de.stevensolleder.simpledo.model.Time;


public class SettingsFragment extends PreferenceFragmentCompat implements PreferenceManager.OnPreferenceTreeClickListener
{
    private ISettingsAccessor settingsAccessor;
    private INotificationHelper notificationHelper;

    public SettingsFragment()
    {
        settingsAccessor=new SettingsAccessor(SimpleDo.getAppContext());
        notificationHelper=new NotificationHelper(settingsAccessor, new DataAccessor(SimpleDo.getAppContext()));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.settings_preference);
        findPreference("allday_reminder_time_key").setSummary(settingsAccessor.getAlldayTime().toString());
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
                        .setHour(settingsAccessor.getAlldayTime().getHour())
                        .setMinute(settingsAccessor.getAlldayTime().getMinute())
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(view ->
                {
                    settingsAccessor.setAlldayTime(new Time(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                    preference.setSummary(settingsAccessor.getAlldayTime().toString());
                    notificationHelper.updateAlldayNotifications();
                });

                materialTimePicker.show(getParentFragmentManager(), null);
                materialTimePicker.getParentFragmentManager().executePendingTransactions();
                materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_ok_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.apply));
                materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_cancel_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.cancel));
                return true;
            case "battery_optimization_key":
                startActivity(new Intent(SimpleDo.getAppContext(), BatteryOptimizationActivity.class));
                return true;
            case "about_key":
                startActivity(new Intent(SimpleDo.getAppContext(), AboutActivity.class));
                return true;
            case "imprint_key":
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://imprint.stevensolleder.de/")));
                return true;
            case "open_source_licences_key":
                startActivity(new Intent(SimpleDo.getAppContext(), LicencesActivity.class));
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }
}
