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

import java.util.List;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.DataAccessor;
import de.stevensolleder.simpledo.model.Entry;
import de.stevensolleder.simpledo.model.IReminderSettingsAccessor;
import de.stevensolleder.simpledo.model.ReminderSettingsAccessor;
import de.stevensolleder.simpledo.model.Time;
import de.stevensolleder.simpledo.presenter.notifications.INotificationHelper;
import de.stevensolleder.simpledo.presenter.notifications.NotificationHelper;


public class SettingsFragment extends PreferenceFragmentCompat implements PreferenceManager.OnPreferenceTreeClickListener
{
    private IReminderSettingsAccessor reminderSettingsAccessor;
    private INotificationHelper notificationHelper;

    public SettingsFragment()
    {
        reminderSettingsAccessor=new ReminderSettingsAccessor(SimpleDo.getAppContext());
        notificationHelper=new NotificationHelper();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.settings_preference);
        findPreference("allday_reminder_time_key").setSummary(reminderSettingsAccessor.getAlldayTime().toString());
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
                        .setHour(reminderSettingsAccessor.getAlldayTime().getHour())
                        .setMinute(reminderSettingsAccessor.getAlldayTime().getMinute())
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(view ->
                {
                    reminderSettingsAccessor.setAlldayTime(new Time(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                    preference.setSummary(reminderSettingsAccessor.getAlldayTime().toString());

                    List<Entry> allEntries=new DataAccessor(SimpleDo.getAppContext()).getEntries();
                    for(Entry entry: allEntries)
                    {
                        if(entry.getTime()==null&entry.isNotifying())
                        {
                            notificationHelper.cancelNotification(entry.getId());
                            notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime(), entry.getContent(), entry.getId());
                        }
                    }
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
