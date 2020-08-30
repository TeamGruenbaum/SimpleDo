package de.stevensolleder.simpledo.model;

import android.app.*;
import android.content.*;

import java.util.Calendar;

public class NotificationHelper
{
    public static void planAndSendNotification(Entry entry)
    {
        Calendar calendar=Calendar.getInstance();

        if(entry.getTime()!=null)
        {
            calendar.set(entry.getDate().getYear(), entry.getDate().getMonth()-1, entry.getDate().getDay(), entry.getTime().getHour(), entry.getTime().getMinute(), 0);
        }
        else
        {
            calendar.set(entry.getDate().getYear(), entry.getDate().getMonth()-1, entry.getDate().getDay());
        }

        System.out.println((calendar.getTimeInMillis()-System.currentTimeMillis())/1000);

        Intent intent=new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class);
        intent.putExtra("CONTENT_TEXT", entry.getContent());
        PendingIntent pendingIntent=PendingIntent.getBroadcast(SimpleDo.getAppContext(), entry.getID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void cancelNotification(Entry entry)
    {
        ((AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE)).cancel(PendingIntent.getBroadcast(SimpleDo.getAppContext(), entry.getID(), new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class), 0));
    }
}
