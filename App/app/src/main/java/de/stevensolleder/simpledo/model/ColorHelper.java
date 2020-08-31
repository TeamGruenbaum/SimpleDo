package de.stevensolleder.simpledo.model;

import android.graphics.Color;
import android.view.MenuItem;

import de.stevensolleder.simpledo.R;

public class ColorHelper
{
    public static int colorChangeMenuMenuItemToColor(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.white: return Color.WHITE;
            case R.id.yellow: return Color.parseColor("#FFF9C4");
            case R.id.orange: return Color.parseColor("#FFE0B2");
            case R.id.red: return Color.parseColor("#FFCDD2");
            case R.id.green: return Color.parseColor("#DCEDC8");
            case R.id.blue: return Color.parseColor("#BBDEFB");
            case R.id.purple: return Color.parseColor("#E1BEE7");
        }

        return Color.WHITE;
    }
}