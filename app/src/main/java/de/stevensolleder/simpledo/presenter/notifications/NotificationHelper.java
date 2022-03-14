package de.stevensolleder.simpledo.presenter.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.List;
import java.util.function.Supplier;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.Date;
import de.stevensolleder.simpledo.model.Entry;
import de.stevensolleder.simpledo.model.IReminderSettingsAccessor;
import de.stevensolleder.simpledo.model.Time;
import de.stevensolleder.simpledo.presenter.SimpleDo;

public class NotificationHelper implements INotificationHelper
{
    public NotificationHelper(){}

    @Override
    public void createNotificationChannel(@NonNull String id, @NonNull String name, @NonNull String description, @NonNull int importance)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel=new NotificationChannel(id, name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = SimpleDo.getAppContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public void planAndSendNotification(@NonNull Date date, @NonNull Time time, @NonNull String content, @NonNull int id)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth()-1, date.getDay(), time.getHour(), time.getMinute(), 0);

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
}
