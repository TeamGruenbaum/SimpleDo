package de.stevensolleder.simpledo.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Comparator;

import com.google.gson.Gson;

public class SaveHelper
{
    //Methods for editing the entries in class Save
    public static void addEntry(Entry newValue)
    {
        Save temporary=getSave();
        temporary.getEntries().add(newValue);
        save(temporary);
    }

    public static void addEntry(int index, Entry newValue)
    {
        Save temporary=getSave();
        temporary.getEntries().add(index, newValue);
        save(temporary);
    }

    public static Entry getEntry(int index)
    {
        return getSave().getEntries().get(index);
    }

    public static void changeEntry(Entry newValue, int index)
    {
        Save temporary=getSave();
        temporary.getEntries().set(index, newValue);
        save(temporary);
    }

    public static int getEntriesSize()
    {
        return getSave().getEntries().size();
    }

    public static void removeEntry(int index)
    {
        Save temporary=getSave();
        temporary.getEntries().remove(index);
        save(temporary);
    }

    public static void swapEntries(int fromIndex, int toIndex)
    {
        Save temporary=getSave();
        Collections.swap(temporary.getEntries(), fromIndex, toIndex);
        save(temporary);
    }

    //Methods for sorting the entries by different Criteria
    public static void sortEntries()
    {
        Save temporary=getSave();

        switch(getSortCriterium())
        {
            case TEXT:
            {
                temporary.getEntries().sort(new Comparator<Entry>()
                {
                    @Override
                    public int compare(Entry entry1, Entry entry2)
                    {
                        if(getSortDirection()== Direction.DOWN)
                        {
                            return entry1.getContent().toLowerCase().compareTo(entry2.getContent().toLowerCase());
                        }
                        else
                        {
                            return entry2.getContent().toLowerCase().compareTo(entry1.getContent().toLowerCase());
                        }
                    }
                });
            }break;
            case DEADLINE:
            {
                temporary.getEntries().sort(new Comparator<Entry>()
                {
                    @Override
                    public int compare(Entry entry1, Entry entry2)
                    {
                        if (entry1.getDate() != null && entry2.getDate() != null)
                        {
                            if (getSortDirection() == Direction.DOWN)
                            {
                                if (entry1.getDate().compareTo(entry2.getDate()) == 0 && entry1.getTime() != null && entry2.getTime() != null)
                                {
                                    return entry1.getTime().compareTo(entry2.getTime());
                                }

                                return entry1.getDate().compareTo(entry2.getDate());
                            }
                            else
                            {
                                if (entry2.getDate().compareTo(entry1.getDate()) == 0 && entry2.getTime() != null && entry1.getTime() != null)
                                {
                                    return entry2.getTime().compareTo(entry1.getTime());
                                }

                                return entry2.getDate().compareTo(entry1.getDate());
                            }
                        }
                        else
                        {
                            if(entry1.getDate()==null)
                            {
                                return Integer.MAX_VALUE;
                            }
                            else
                            {
                                return Integer.MIN_VALUE;
                            }
                        }
                    }
                });
            }break;
            case COLOR:
            {
                temporary.getEntries().sort(new Comparator<Entry>()
                {
                    @Override
                    public int compare(Entry entry1, Entry entry2)
                    {
                        if (getSortDirection() == Direction.DOWN)
                        {
                            return entry2.getColor() - entry1.getColor();
                        }
                        else
                        {
                            return entry1.getColor() - entry2.getColor();
                        }
                    }
                });
            }break;
        }

        save(temporary);
    }

    public static Direction getSortDirection()
    {
        return getSave().getSortDirection();
    }

    public static void setSortDirection(Direction newValue)
    {
        Save temporary=getSave();
        temporary.setSortDirection(newValue);
        save(temporary);
    }

    public static Criterium getSortCriterium()
    {
        return getSave().getSortCriterium();
    }

    public static void setSortCriterium(Criterium newValue)
    {
        Save temporary=getSave();
        temporary.setSortCriterium(newValue);
        save(temporary);
    }

    //Methods for saving
    private static Save getSave()
    {
        SharedPreferences preferences= SimpleDo.getAppContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = preferences.getString("save", null);

        if(json!=null)
        {
            return gson.fromJson(json, Save.class);
        }

        return new Save();
    }

    private static void save(Save newSave)
    {
        SharedPreferences preferences=SimpleDo.getAppContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor=preferences.edit();

        Gson gson=new Gson();
        String json=gson.toJson(newSave);
        preferencesEditor.putString("save", json);
        preferencesEditor.commit();
    }
}
