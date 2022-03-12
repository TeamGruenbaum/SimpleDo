package de.stevensolleder.simpledo.model;

import androidx.core.content.ContextCompat;

import java.util.Calendar;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.presenter.SimpleDo;


public class Entry
{
    private final int id;
    private String content;
    private Date date;
    private Time time;
    private int color;
    private boolean notifying;



    public Entry(int id)
    {
        this.id=id;
        this.content=null;
        this.date=null;
        this.time=null;
        this.color=ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardDefault);
        this.notifying=false;
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
        Date currentDate=new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR));
        Time currentTime=new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        System.out.println("Now is the " + currentDate.toString() + " and the time is " + currentTime.toString());

        if (this.date!=null && this.date.compareTo(currentDate)==0)
        {
            return (this.time!=null)?(this.time.compareTo(currentTime))<0:alldayTime.compareTo(currentTime)<0;
        }
        return (this.date.compareTo(currentDate))<0;
    }
}
