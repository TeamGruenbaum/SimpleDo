package de.stevensolleder.simpledo.model;

public class Date implements Comparable<Date>
{
    private int day;
    private int month;
    private int year;

    public Date(int day, int month, int year)
    {
        this.day=day;
        this.month=month+1;
        this.year=year;
    }

    @Override
    public String toString()
    {
        String day=Integer.toString(this.day);
        String month=Integer.toString(this.month);

        if(this.day<10)
        {
            day="0"+day;
        }

        if(this.month<10)
        {
            month="0"+month;
        }

        return day+"."+month+"."+this.year;
    }

    @Override
    public int compareTo(Date comparingDate)
    {
        if((year-comparingDate.year)==0)
        {
            if((month-comparingDate.month)==0)
            {
                return (day-comparingDate.day);
            }
            return (month-comparingDate.month);
        }
        return (year-comparingDate.year);
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}