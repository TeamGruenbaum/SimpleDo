package de.gruenbaum.simpledo.presenter.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import de.gruenbaum.simpledo.R;
import de.gruenbaum.simpledo.presenter.IdentificationHelper;
import de.gruenbaum.simpledo.presenter.Main;
import de.gruenbaum.simpledo.presenter.SimpleDo;

public class ReminderBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(SimpleDo.getAppContext(), "main")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(SimpleDo.getAppContext().getResources().getString(R.string.app_name))
                .setContentText(intent.getStringExtra("content"))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(SimpleDo.getAppContext(), 0, new Intent(SimpleDo.getAppContext(), Main.class), PendingIntent.FLAG_IMMUTABLE));

        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(SimpleDo.getAppContext());
        notificationManagerCompat.notify(new IdentificationHelper().createUniqueId(), builder.build());
    }
}
