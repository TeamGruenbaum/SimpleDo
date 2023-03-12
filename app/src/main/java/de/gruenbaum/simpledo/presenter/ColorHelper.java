package de.gruenbaum.simpledo.presenter;

import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;

import de.gruenbaum.simpledo.R;
import de.gruenbaum.simpledo.model.Color;


public class ColorHelper
{
    public ColorHelper(){}

    public Color convertMenuItemColorToColor(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.colorYellow: return Color.YELLOW;
            case R.id.colorOrange: return Color.ORANGE;
            case R.id.colorRed: return Color.RED;
            case R.id.colorGreen: return Color.GREEN;
            case R.id.colorBlue: return Color.BLUE;
            case R.id.colorPurple: return Color.PURPLE;
            case R.id.colorDefault: default: return Color.DEFAULT;
        }
    }
    
    public int convertColorToInteger(Color color)
    {
        switch(color)
        {
            case YELLOW: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardYellow);
            case ORANGE: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardOrange);
            case RED: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardRed);
            case GREEN: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardGreen);
            case BLUE: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardBlue);
            case PURPLE: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardPurple);
            case DEFAULT: default: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardDefault);
        }
    }
    
    public void setupThemeSpecificColorMenuIcons(Menu menu)
    {
        switch (SimpleDo.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) 
        {
            case Configuration.UI_MODE_NIGHT_YES:
                menu.findItem(R.id.colorDefault).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_default_dark, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorYellow).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_yellow_dark, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorOrange).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_orange_dark, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorRed).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_red_dark, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorPurple).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_purple_dark, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorGreen).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_green_dark, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorBlue).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_blue_dark, SimpleDo.getAppContext().getTheme()));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                menu.findItem(R.id.colorDefault).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_default_light, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorYellow).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_yellow_light, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorOrange).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_orange_light, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorRed).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_red_light, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorPurple).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_purple_light, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorGreen).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_green_light, SimpleDo.getAppContext().getTheme()));
                menu.findItem(R.id.colorBlue).setIcon(SimpleDo.getAppContext().getResources().getDrawable(R.drawable.ic_blue_light, SimpleDo.getAppContext().getTheme()));
                break;
        }
    }
}
