package de.stevensolleder.simpledo.presenter;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.List;
import java.util.function.Supplier;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.Date;
import de.stevensolleder.simpledo.model.Entry;
import de.stevensolleder.simpledo.model.IReminderSettingsAccessor;
import de.stevensolleder.simpledo.model.Time;

public class NotificationHelper implements INotificationHelper
{
    private final Supplier<List<Entry>> entries;
    private final IReminderSettingsAccessor reminderSettingsAccessor;

    public NotificationHelper(Supplier<List<Entry>> entries, IReminderSettingsAccessor reminderSettingsAccessor)
    {
        this.entries=entries;
        this.reminderSettingsAccessor=reminderSettingsAccessor;
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
    public void planAndSendNotification(Time time, Date date, String content, int id)
    {
        Calendar calendar=Calendar.getInstance();
        if(time!=null) calendar.set(date.getYear(), date.getMonth()-1, date.getDay(), time.getHour(), time.getMinute(), 0);
        else calendar.set(date.getYear(), date.getMonth()-1, date.getDay(), reminderSettingsAccessor.getAlldayTime().getHour(), reminderSettingsAccessor.getAlldayTime().getMinute(), 0);

        Intent intent=new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class);
        intent.putExtra("content", content);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(SimpleDo.getAppContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void cancelNotification(int id)
    {
        Intent intent=new Intent(SimpleDo.getAppContext(), ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(SimpleDo.getAppContext(), id, intent, 0);

        AlarmManager alarmManager = (AlarmManager) SimpleDo.getAppContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void updateAlldayNotifications()
    {
        for(Entry entry: entries.get())
        {
            if(entry.getTime()==null&entry.isNotifying())
            {
                cancelNotification(entry.getId());
                planAndSendNotification(entry.getTime(), entry.getDate(), entry.getContent(), entry.getId());
            }
        }
    }
}
