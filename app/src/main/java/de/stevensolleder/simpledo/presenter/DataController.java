package de.stevensolleder.simpledo.presenter;

import android.graphics.drawable.Drawable;

import com.google.android.material.snackbar.BaseTransientBottomBar;

import java.util.Calendar;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.Color;
import de.stevensolleder.simpledo.model.Criterion;
import de.stevensolleder.simpledo.model.Date;
import de.stevensolleder.simpledo.model.Direction;
import de.stevensolleder.simpledo.model.Entry;
import de.stevensolleder.simpledo.model.IDataAccessor;
import de.stevensolleder.simpledo.model.IReminderSettingsAccessor;
import de.stevensolleder.simpledo.model.ISortSettingsAccessor;
import de.stevensolleder.simpledo.model.ReminderSettingsAccessor;
import de.stevensolleder.simpledo.model.Time;
import de.stevensolleder.simpledo.presenter.notifications.INotificationHelper;

public class DataController
{
    private Main main;

    private IDataAccessor dataAccessor;
    private ISortSettingsAccessor sortSettingsAccessor;
    private IReminderSettingsAccessor reminderSettingsAccessor;
    private INotificationHelper notificationHelper;

    private Date chosenDate;
    private Time chosenTime;
    private Color chosenColor;
    private boolean reminding;

    public DataController(Main main, IDataAccessor dataAccessor, ISortSettingsAccessor sortSettingsAccessor)
    {
        this.main=main;

        chosenDate=null;
        chosenTime=null;
        chosenColor=Color.DEFAULT;
        reminding=false;

        this.dataAccessor=dataAccessor;
        this.sortSettingsAccessor=sortSettingsAccessor;
        reminderSettingsAccessor=new ReminderSettingsAccessor(SimpleDo.getAppContext());
    }

    public void initialize()
    {
        main.setSortCriterionIcon(sortSettingsAccessor.getSortCriterion());
        main.setSortDirectionIcon(sortSettingsAccessor.getSortDirection());
    }

    public void setDate()
    {
        DateTimeConverter dateTimeConverter=new DateTimeConverter();

        main.showDatePicker(chosenDate==null? Calendar.getInstance().getTimeInMillis(): dateTimeConverter.fromDateInMillis(chosenDate),
            (selection) ->
            {
                if(chosenDate==null) main.deadlineSectionEnabled(true);

                chosenDate=dateTimeConverter.fromMillisInDate(selection);
                main.updateDeadlineText(chosenDate, chosenTime);

                main.setKeyboardEnabled(true);
            },
            (view1) ->
            {
                chosenDate=null;
                chosenTime=null;

                main.deadlineSectionEnabled(false);
                main.updateDeadlineText(null, null);

                main.setKeyboardEnabled(true);
            });
    }

    public void setTime()
    {
        main.showTimePicker(chosenTime==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):chosenTime.getHour(), chosenTime==null?Calendar.getInstance().get(Calendar.MINUTE):chosenTime.getMinute(),
            (hour, minute) ->
            {
                chosenTime=new Time(hour, minute);
                main.updateDeadlineText(chosenDate, chosenTime);

                main.setKeyboardEnabled(true);
            },
            (dialog)->
            {
                chosenTime=null;
                main.updateDeadlineText(chosenDate, chosenTime);

                main.setKeyboardEnabled(true);
            });
    }

    public void setColor()
    {
        main.showColorPicker((color ->
        {
            chosenColor=color;
            main.changeAddCardColor(chosenColor);
        }));
    }

    public void addEntry()
    {
        Entry entry = new Entry(new IdentificationHelper().createUniqueId());
        entry.setContent(main.getCurrentText());
        entry.setNotifying(reminding);

        if (chosenColor != Color.DEFAULT) entry.setColor(chosenColor);

        if (chosenDate != null)
        {
            entry.setDate(chosenDate);
            if (chosenTime != null) entry.setTime(chosenTime);
        }

        if (reminding)
        {
            if (entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
            {
                main.showSnackbar(SimpleDo.getAppContext().getResources().getString(R.string.past_notification), BaseTransientBottomBar.LENGTH_SHORT, null);
                return;
            }
            notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime()!=null?entry.getTime():reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
        }

        dataAccessor.addEntry(entry);
        main.refreshAddEntry(dataAccessor.getEntriesSize());
        main.scrollToEntry(dataAccessor.getEntriesSize());

        main.enableSortability(dataAccessor.getEntriesSize()<=1?false:true);
        main.setSortDirectionIcon(Direction.NONE);
        main.setSortCriterionIcon(Criterion.NONE);

        reminding=false;
        chosenDate=null;
        chosenTime=null;
        main.setCurrentText("");
        main.deadlineSectionEnabled(false);
        main.updateDeadlineText(chosenDate, chosenTime);
    }

    //Switch sort icons, sort entries and tell list to refresh
    public void sortEntriesByDirection()
    {
        switch (sortSettingsAccessor.getSortDirection())
        {
            case UP: sortSettingsAccessor.setSortDirection(Direction.DOWN); break;
            case DOWN: case NONE: sortSettingsAccessor.setSortDirection(Direction.UP); break;
        }

        if (sortSettingsAccessor.getSortCriterion() == Criterion.NONE)
        {
            main.setSortCriterionIcon(Criterion.TEXT);
            sortSettingsAccessor.setSortCriterion(Criterion.TEXT);
        }

        dataAccessor.sortEntries(sortSettingsAccessor.getSortCriterion(), sortSettingsAccessor.getSortDirection());

        main.setSortDirectionIcon(sortSettingsAccessor.getSortDirection());
        main.refreshList();
    }

    public void sortEntriesByCriterion()
    {
        switch (sortSettingsAccessor.getSortCriterion())
        {
            case TEXT: sortSettingsAccessor.setSortCriterion(Criterion.DEADLINE); break;
            case DEADLINE: sortSettingsAccessor.setSortCriterion(Criterion.COLOR); break;
            case NONE: main.enableSortability(true);
            case COLOR: sortSettingsAccessor.setSortCriterion(Criterion.TEXT); break;
        }

        if (sortSettingsAccessor.getSortDirection() == Direction.NONE)
        {
            main.setSortDirectionIcon(Direction.DOWN);
            sortSettingsAccessor.setSortDirection(Direction.DOWN);
        }

        dataAccessor.sortEntries(sortSettingsAccessor.getSortCriterion(), sortSettingsAccessor.getSortDirection());
        main.setSortCriterionIcon(sortSettingsAccessor.getSortCriterion());
        main.refreshList();
    }

    public void toggleReminding()
    {
        reminding=!reminding;
        main.enableReminderIcon(reminding);
    }
}
