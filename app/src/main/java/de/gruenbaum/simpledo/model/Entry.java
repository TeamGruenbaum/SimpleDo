package de.gruenbaum.simpledo.model;


import java.util.Calendar;


public class Entry
{
    private final int id;
    private String content;
    private Date date;
    private Time time;
    private Color color;
    private boolean notifying;



    public Entry(int id)
    {
        this.id=id;
        this.content=null;
        this.date=null;
        this.time=null;
        this.color=Color.DEFAULT;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
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
