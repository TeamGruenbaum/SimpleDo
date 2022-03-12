package de.stevensolleder.simpledo.model;

import android.content.Context;
import android.content.SharedPreferences;

import de.stevensolleder.simpledo.presenter.DateTimeConverter;

public class SettingsAccessor implements ISettingsAccessor
{
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    public SettingsAccessor(Context appContext)
    {
        this.preferences=appContext.getSharedPreferences("settings", Context.MODE_PRIVATE);
        preferencesEditor=preferences.edit();
    }

    public Direction getSortDirection()
    {
        return Direction.valueOf(preferences.getString("sortDirection", Direction.NONE.name()));
    }

    public void setSortDirection(Direction newValue)
    {
        preferencesEditor.putString("sortDirection", newValue.name()).apply();
    }

    public Criterion getSortCriterion()
    {
        return Criterion.valueOf(preferences.getString("sortCriterion", Criterion.NONE.name()));
    }

    public void setSortCriterion(Criterion newValue)
    {
        preferencesEditor.putString("sortCriterion", newValue.name()).apply();
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
