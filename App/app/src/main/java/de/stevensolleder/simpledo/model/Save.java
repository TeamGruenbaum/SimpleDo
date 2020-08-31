package de.stevensolleder.simpledo.model;

import java.util.ArrayList;

public class Save
{
    private ArrayList<Entry> entries;

    private Direction sortDirection;
    private Criterium sortCriterium;

    public Save()
    {
        entries=new ArrayList<Entry>();

        //By default the ArrayList is unsorted
        sortDirection=Direction.NONE;
        sortCriterium=Criterium.NONE;
    }

    public ArrayList<Entry> getEntries()
    {
        return entries;
    }

    public Direction getSortDirection()
    {
        return sortDirection;
    }

    public void setSortDirection(Direction newValue)
    {
        sortDirection = newValue;
    }

    public Criterium getSortCriterium()
    {
        return sortCriterium;
    }

    public void setSortCriterium(Criterium newValue)
    {
        sortCriterium = newValue;
    }
}
