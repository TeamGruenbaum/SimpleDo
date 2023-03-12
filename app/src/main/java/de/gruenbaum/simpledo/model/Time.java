package de.gruenbaum.simpledo.model;

import androidx.annotation.NonNull;

public class Time implements Comparable<Time>
{
    private int hour;
    private int minute;

    public Time(int hour, int minute)
    {
        setHour(hour);
        setMinute(minute);
    }

    @NonNull
    @Override
    public String toString()
    {
        String minute=Integer.toString(this.minute);
        if(this.minute<10) minute="0"+minute;
        return hour+":"+minute;
    }

    //Method for sorting by deadline
    @Override
    public int compareTo(Time comparingTime)
    {
        if ((hour-comparingTime.hour)==0) {
            return (minute-comparingTime.minute);
        }
        return (hour-comparingTime.hour);
    }

    public int getHour()
    {
        return hour;
    }

    public void setHour(int hour)
    {
       if(hour<0 || hour>23) throw new IllegalArgumentException();
       this.hour=hour;
    }

    public int getMinute()
    {
        return minute;
    }

    public void setMinute(int minute)
    {
        if(minute<0 || minute>59) throw new IllegalArgumentException();
        this.minute=minute;
    }
}