package de.stevensolleder.simpledo.model;

public class Time implements Comparable<Time>
{
    private int hour;
    private int minute;

    public Time(int hour, int minute)
    {
        this.hour=hour;
        this.minute=minute;
    }

    @Override
    public String toString()
    {
        String minute=Integer.toString(this.minute);

        if(this.minute<10)
        {
            minute="0"+minute;
        }

        return hour+":"+minute;
    }

    @Override
    public int compareTo(Time comparingTime) {
        if ((hour - comparingTime.hour) == 0) {
            return (minute - comparingTime.minute);
        }
        return (hour - comparingTime.hour);
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
}