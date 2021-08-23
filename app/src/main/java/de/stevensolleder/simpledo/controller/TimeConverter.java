package de.stevensolleder.simpledo.controller;

import java.util.Calendar;

import de.stevensolleder.simpledo.model.Date;

public class TimeConverter
{
    public Date fromMilisInDate(long milis)
    {
        java.util.Date jDate=new java.util.Date(milis);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(jDate);

        return new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR));
    }

    public long fromDateInMilis(Date date)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth()-1, date.getDay());

        return calendar.getTimeInMillis();
    }
}
