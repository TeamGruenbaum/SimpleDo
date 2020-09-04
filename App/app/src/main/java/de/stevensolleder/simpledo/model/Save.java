package de.stevensolleder.simpledo.model;

import java.util.ArrayList;

public class Save
{
    private ArrayList<Entry> entries;

    private Direction sortDirection;
    private Criterium sortCriterium;

    private Time alldayTime;

    public Save()
    {
        entries=new ArrayList<Entry>();

        //By default the ArrayList is unsorted
        sortDirection=Direction.NONE;
        sortCriterium=Criterium.NONE;

        alldayTime=new Time(8,0);
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

    public Time getAlldayTime() {
        return alldayTime;
    }

    public void setAlldayTime(Time alldayTime) {
        this.alldayTime = alldayTime;
    }
}
