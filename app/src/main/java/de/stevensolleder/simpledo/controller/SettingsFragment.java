package de.stevensolleder.simpledo.controller;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.judemanutd.autostarter.AutoStartPermissionHelper;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.SimpleDo;
import de.stevensolleder.simpledo.model.Time;

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
                //bitte im TimePicker setzen
                preference.setSummary(getAlldayTime().toString());
                
                /*TimePickerDialog timePickerDialog=new TimePickerDialog(SimpleDo.getAppContext(), (timePicker, hour, minute)->
                {
                    setAlldayTime(new Time(hour, minute));

                    for(int i=0; i<getEntriesSize(); i++)
                    {
                        if(getEntry(i).getTime()!=null)
                        {
                            planAndSendNotification(getEntry(i));
                        }
                    }
                }, getAlldayTime().getHour(), getAlldayTime().getMinute(), true)
                {
                    @Override
                    public void onBackPressed()
                    {
                        this.dismiss();
                    }
                };

                timePickerDialog.setOnCancelListener((dialogInterface)->
                {
                    timePickerDialog.dismiss();
                });

                timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, SimpleDo.getAppContext().getResources().getString(R.string.cancel), (dialogInterface, which)->
                {
                    timePickerDialog.dismiss();
                });

                //UIUtil.hideKeyboard(Main);

                timePickerDialog.show();

                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(SimpleDo.getAppContext().getResources().getString(R.string.ok));
                timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(SimpleDo.getAppContext().getResources().getString(R.string.cancel));*/

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
