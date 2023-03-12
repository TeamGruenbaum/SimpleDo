package de.gruenbaum.simpledo.model;

import android.content.Context;
import android.content.SharedPreferences;

import de.gruenbaum.simpledo.presenter.DateTimeConverter;

public class ReminderSettingsAccessor implements IReminderSettingsAccessor
{
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    public ReminderSettingsAccessor(Context appContext)
    {
        this.preferences=appContext.getSharedPreferences("reminderSettings", Context.MODE_PRIVATE);
        preferencesEditor=preferences.edit();
    }

    public Time getAlldayTime()
    {
        return new DateTimeConverter().fromMillisInTime(preferences.getLong("alldayTime", 1630130427816L));
    }

    public void setAlldayTime(Time newValue)
    {
        preferencesEditor.putLong("alldayTime", new DateTimeConverter().fromTimeInMillis(newValue)).apply();
    }
}
