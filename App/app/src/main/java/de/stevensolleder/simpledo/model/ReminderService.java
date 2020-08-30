package de.stevensolleder.simpledo.model;

import android.app.*;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.*;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.controller.Main;
import de.stevensolleder.simpledo.model.IdentificationHelper;
import de.stevensolleder.simpledo.model.SimpleDo;

public class ReminderService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SimpleDo.getAppContext(), "main")
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle("SimpleDo")
                .setContentText(intent.getStringExtra("CONTENT_TEXT"))
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