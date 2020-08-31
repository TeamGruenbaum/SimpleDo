package de.stevensolleder.simpledo.model;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class NotificationHelper
{
    public static void planAndSendNotification(Entry entry)
    {
        Calendar calendar=Calendar.getInstance();

        //Determine the exact point in time for the notification
        if(entry.getTime()!=null)
        {
            calendar.set(entry.getDate().getYear(), entry.getDate().getMonth()-1, entry.getDate().getDay(), entry.getTime().getHour(), entry.getTime().getMinute(), 0);
        }
        else
        {
            calendar.set(entry.getDate().getYear(), entry.getDate().getMonth()-1, entry.getDate().getDay(), 8, 0, 0);
        }

        Intent intent=new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class);
        intent.putExtra("CONTENT", entry.getContent());
        PendingIntent pendingIntent=PendingIntent.getBroadcast(SimpleDo.getAppContext(), entry.getID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelNotification(Entry entry)
    {
        Intent intent=new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(SimpleDo.getAppContext(), entry.getID(), intent, 0);

        AlarmManager alarmManager = (AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
