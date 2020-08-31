package de.stevensolleder.simpledo.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.controller.Main;

public class ReminderService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SimpleDo.getAppContext(), "main")
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(intent.getStringExtra("CONTENT"))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(SimpleDo.getAppContext(), 0, new Intent(SimpleDo.getAppContext(), Main.class), 0));

        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(SimpleDo.getAppContext());
        notificationManagerCompat.notify(IdentificationHelper.createUniqueID(), builder.build());

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}