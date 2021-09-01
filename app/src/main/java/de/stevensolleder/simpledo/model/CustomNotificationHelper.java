package de.stevensolleder.simpledo.model;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.controller.Main;

public class CustomNotificationHelper implements NotificationHelper<Entry>
{
    private final DataAccessor dataAccessor;

    public CustomNotificationHelper()
    {
        dataAccessor=new CustomDataAccessor(SimpleDo.getAppContext().getSharedPreferences("settings", Context.MODE_PRIVATE));
    }

    @Override
    public void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel=new NotificationChannel("main", "Erinnerungen", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(SimpleDo.getAppContext().getResources().getString(R.string.reminders_description));

            NotificationManager notificationManager = SimpleDo.getAppContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void planAndSendNotification(Entry entry)
    {
        Calendar calendar=Calendar.getInstance();
        if(entry.getTime()!=null) calendar.set(entry.getDate().getYear(), entry.getDate().getMonth()-1, entry.getDate().getDay(), entry.getTime().getHour(), entry.getTime().getMinute(), 0);
        else calendar.set(entry.getDate().getYear(), entry.getDate().getMonth()-1, entry.getDate().getDay(), dataAccessor.getAlldayTime().getHour(), dataAccessor.getAlldayTime().getMinute(), 0);

        Intent intent=new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class);
        intent.putExtra("content", entry.getContent());
        PendingIntent pendingIntent=PendingIntent.getBroadcast(SimpleDo.getAppContext(), entry.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void cancelNotification(Entry entry)
    {
        Intent intent=new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(SimpleDo.getAppContext(), entry.getId(), intent, 0);

        AlarmManager alarmManager = (AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
