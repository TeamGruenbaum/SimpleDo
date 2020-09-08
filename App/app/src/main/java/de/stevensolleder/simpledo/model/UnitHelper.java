package de.stevensolleder.simpledo.model;

import android.util.DisplayMetrics;

public class UnitHelper
{
    public static int dpToPx(int dp)
    {
        return (int) (dp * ((float) SimpleDo.getAppContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
