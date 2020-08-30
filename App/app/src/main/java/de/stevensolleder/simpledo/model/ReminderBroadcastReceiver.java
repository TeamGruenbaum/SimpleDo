package de.stevensolleder.simpledo.model;

import android.content.*;

import de.stevensolleder.simpledo.model.ReminderService;

public class ReminderBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent temp = new Intent(context, ReminderService.class);
        temp.putExtra("CONTENT_TEXT", intent.getStringExtra("CONTENT_TEXT"));
        context.startService(temp);
    }
}
