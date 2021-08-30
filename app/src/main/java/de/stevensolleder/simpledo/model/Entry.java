package de.stevensolleder.simpledo.model;

import android.graphics.Color;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class Entry
{
    private final int id;
    private String content;
    private Date date=null;
    private Time time=null;
    private int color=Color.WHITE;
    private boolean notifying;

    public Entry()
    {
        this.id=new SimpleDateFormat("ddHHmmssSS",  Locale.US).format(new java.util.Date()).hashCode();
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isNotifying() {
        return notifying;
    }

    public void setNotifying(boolean notifying) {
        this.notifying = notifying;
    }

    public boolean isInPast(Time alldayTime)
    {
        Calendar calendar=Calendar.getInstance();
        Date currentDate=new Date(Calendar.DAY_OF_MONTH, calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR));
        Time currentTime=new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        if (this.date!=null && this.date.compareTo(currentDate)==0)
        {
            if(this.time!=null)
            {
                return (this.time.compareTo(currentTime))<0;
            }
            else
            {
                return alldayTime.compareTo(currentTime)<0;
            }
        }

        return (this.date.compareTo(currentDate))<0;
    }
}
