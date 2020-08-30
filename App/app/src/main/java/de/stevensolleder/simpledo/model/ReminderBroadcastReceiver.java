package de.stevensolleder.simpledo.model;

import android.content.*;

import de.stevensolleder.simpledo.model.ReminderService;

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
