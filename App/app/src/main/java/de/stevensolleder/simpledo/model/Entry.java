package de.stevensolleder.simpledo.model;

import android.graphics.Color;

import java.io.Serializable;

public class Entry implements Serializable
{
    private String content;
    private Date date=null;
    private Time time=null;
    private int color=Color.WHITE;
    private boolean notifying;
    private int ID;

    public Entry(String content, boolean notifying)
    {
        this.content=content;
        this.notifying=notifying;
        this.ID=IdentificationHelper.createUniqueID();
    }

    public Entry(String content, Date date, boolean hasNotifcation)
    {
        this(content, hasNotifcation);
        this.date=date;
    }

    public Entry(String content, Date date,  Time time, boolean hasNotifcation)
    {
        this(content, date, hasNotifcation);
        this.time=time;
    }

    public Entry(String content, int color, boolean hasNotifcation)
    {
        this(content, hasNotifcation);
        this.color=color;
    }

    public Entry(String content, Date date,  int color, boolean hasNotifcation)
    {
        this(content, date, hasNotifcation);
        this.color=color;
    }

    public Entry(String content, Date date, Time time, int color, boolean hasNotifcation)
    {
        this(content, date, color, hasNotifcation);
        this.time=time;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String newValue)
    {
        content = newValue;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date newValue)
    {
        date = newValue;
    }

    public Time getTime()
    {
        return time;
    }

    public void setTime(Time newValue)
    {
        time = newValue;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int newValue)
    {
        color = newValue;
    }

    public boolean isNotifying() {
        return notifying;
    }

    public void setNotifying(boolean newValue)
    {
        this.notifying = newValue;
    }

    public int getID()
    {
        return ID;
    }
}
