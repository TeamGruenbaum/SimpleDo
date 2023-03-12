package de.gruenbaum.simpledo.presenter;

import android.app.Application;
import android.content.Context;

public class SimpleDo extends Application
{
    private static Context applicationContext;

    public void onCreate()
    {
        super.onCreate();
        SimpleDo.applicationContext=getApplicationContext();
    }

    public static Context getAppContext()
    {
        return SimpleDo.applicationContext;
    }
}