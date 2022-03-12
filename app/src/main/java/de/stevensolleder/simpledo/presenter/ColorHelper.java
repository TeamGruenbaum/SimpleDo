package de.stevensolleder.simpledo.presenter;

import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;

import de.stevensolleder.simpledo.R;


public class ColorHelper
{
    public ColorHelper(){}

    public int getMenuItemColor(MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            case R.id.colorYellow: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardYellow);
            case R.id.colorOrange: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardOrange);
            case R.id.colorRed: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardRed);
            case R.id.colorGreen: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardGreen);
            case R.id.colorBlue: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardBlue);
            case R.id.colorPurple: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardPurple);
            case R.id.colorDefault: default: return ContextCompat.getColor(SimpleDo.getAppContext(), R.color.colorCardDefault);
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
