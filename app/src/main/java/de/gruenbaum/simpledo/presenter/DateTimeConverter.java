package de.gruenbaum.simpledo.presenter;

import java.util.Calendar;

import de.gruenbaum.simpledo.model.Date;
import de.gruenbaum.simpledo.model.Time;


public class DateTimeConverter
{
    public Date fromMillisInDate(Long millis)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(new java.util.Date(millis));
        return new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR));
    }

    public Long fromDateInMillis(Date date)
    {
        Calendar calendar=Calendar.getInstance();
        if(date!=null) calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
        else calendar.set(0, 0, 0);
        return calendar.getTimeInMillis();
    }

    public Time fromMillisInTime(Long millis)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(new java.util.Date(millis));
        return new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public Long fromTimeInMillis(Time time)
    {
        Calendar calendar=Calendar.getInstance();
        if(time!=null)
        {
            calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
            calendar.set(Calendar.MINUTE, time.getMinute());
        }
        else
        {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
        }
        return calendar.getTimeInMillis();
    }
}
