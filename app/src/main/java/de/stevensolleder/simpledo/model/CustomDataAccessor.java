package de.stevensolleder.simpledo.model;



import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class CustomDataAccessor implements DataAccessor
{
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;



    public CustomDataAccessor(SharedPreferences preferences)
    {
        this.preferences=preferences;
        preferencesEditor=preferences.edit();
    }


    public List<Entry> getEntries()
    {
        String json = preferences.getString("entries", "");
        if(json.isEmpty()) return new ArrayList<>();
        return new Gson().fromJson(json, new TypeToken<List<Entry>>(){}.getType());
    }

    public void setEntries(List<Entry> entries)
    {
        preferencesEditor.putString("entries", new Gson().toJson(entries)).apply();
    }

    public void addEntry(Entry newValue)
    {
        List<Entry> entries=getEntries();
        entries.add(newValue);
        setEntries(entries);
    }

    public void addEntry(int index, Entry newValue)
    {
        List<Entry> entries=getEntries();
        entries.add(index, newValue);
        setEntries(entries);
    }

    public Entry getEntry(int index)
    {
        return getEntries().get(index);
    }

    public void changeEntry(Entry newValue, int index)
    {
        List<Entry> entries=getEntries();
        entries.set(index, newValue);
        setEntries(entries);
    }

    public int getEntriesSize()
    {
        return getEntries().size();
    }

    public void removeEntry(int index)
    {
        List<Entry> entries=getEntries();
        entries.remove(index);
        setEntries(entries);
    }

    public void swapEntries(int fromIndex, int toIndex)
    {
        List<Entry> entries=getEntries();
        Collections.swap(entries, fromIndex, toIndex);
        setEntries(entries);
    }

    public void sortEntries()
    {
        List<Entry> entries=getEntries();
        switch(getSortCriterion())
        {
            case TEXT: entries.sort((entry1, entry2) -> (getSortDirection()==Direction.DOWN)?(entry1.getContent().toLowerCase().compareTo(entry2.getContent().toLowerCase())):(entry2.getContent().toLowerCase().compareTo(entry1.getContent().toLowerCase()))); break;
            case DEADLINE:
            {
                entries.sort((entry1, entry2) ->
                {
                    if (entry1.getDate()!=null && entry2.getDate()!=null)
                    {
                        if (getSortDirection() == Direction.DOWN) return (entry1.getDate().compareTo(entry2.getDate()) == 0 && entry1.getTime() != null && entry2.getTime() != null)?(entry1.getTime().compareTo(entry2.getTime())):(entry1.getDate().compareTo(entry2.getDate()));
                        else return (entry2.getDate().compareTo(entry1.getDate()) == 0 && entry2.getTime() != null && entry1.getTime() != null)?(entry2.getTime().compareTo(entry1.getTime())):(entry2.getDate().compareTo(entry1.getDate()));
                    }
                    else return (entry1.getDate()==null)?(Integer.MAX_VALUE):Integer.MIN_VALUE;
                });
            } break;
            case COLOR: entries.sort((entry1, entry2) -> (getSortDirection()==Direction.DOWN)?(entry2.getColor() - entry1.getColor()):(entry1.getColor() - entry2.getColor())); break;
        }
        setEntries(entries);
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
