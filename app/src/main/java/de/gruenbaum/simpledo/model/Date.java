package de.gruenbaum.simpledo.model;

public class Date implements Comparable<Date>
{
    private int day;
    private int month;
    private int year;

    public Date(int day, int month, int year)
    {
        setDay(day);
        setMonth(month);
        setYear(year);
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

    //Method for sorting by deadline
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

    public int getDay()
    {
        return day;
    }

    public void setDay(int day)
    {
        if(day<1 || day>31) throw new IllegalArgumentException();
        this.day=day;
    }

    public int getMonth()
    {
        return month;
    }

    public void setMonth(int month)
    {
        if(month<1 || month>12) throw new IllegalArgumentException();
        this.month=month;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year=year;
    }
}