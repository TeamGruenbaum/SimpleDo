package de.stevensolleder.simpledo.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent newIntent = new Intent(context, ReminderService.class);
        newIntent.putExtra("CONTENT", intent.getStringExtra("CONTENT"));
        context.startService(newIntent);
    }
}
