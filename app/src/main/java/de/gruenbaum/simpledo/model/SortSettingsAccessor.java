package de.gruenbaum.simpledo.model;

import android.content.Context;
import android.content.SharedPreferences;

public class SortSettingsAccessor implements ISortSettingsAccessor
{
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    public SortSettingsAccessor(Context appContext)
    {
        this.preferences=appContext.getSharedPreferences("sortSettings", Context.MODE_PRIVATE);
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
}
