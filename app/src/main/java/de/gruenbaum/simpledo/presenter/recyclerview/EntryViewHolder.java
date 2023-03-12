package de.gruenbaum.simpledo.presenter.recyclerview;

import android.content.Context;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

import de.gruenbaum.simpledo.R;
import de.gruenbaum.simpledo.databinding.EntryCardBinding;
import de.gruenbaum.simpledo.model.*;
import de.gruenbaum.simpledo.presenter.ColorHelper;
import de.gruenbaum.simpledo.presenter.DateTimeConverter;
import de.gruenbaum.simpledo.presenter.Main;
import de.gruenbaum.simpledo.presenter.notifications.NotificationHelper;
import de.gruenbaum.simpledo.presenter.SimpleDo;


public class EntryViewHolder extends RecyclerView.ViewHolder
{
    @NonNull private final EntryCardBinding entryCardBinding;

    @Nullable private ContextMenu contextMenu;
    private boolean contextMenuEnabled;



    public EntryViewHolder(@NonNull Main mainActivity, @NonNull EntryCardBinding entryCardBinding, @NonNull IDataAccessor dataAccessor, @NonNull IReminderSettingsAccessor reminderSettingsAccessor)
    {
        super(entryCardBinding.getRoot());

        this.entryCardBinding=entryCardBinding;
        this.contextMenu=null;
        this.contextMenuEnabled=true;

        ColorHelper colorHelper=new ColorHelper();
        NotificationHelper notificationHelper=new NotificationHelper();

        entryCardBinding.content.setKeyPreImeAction((keyCode, keyEvent) ->
        {
            if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP)
            {
                entryCardBinding.content.clearFocus();
                Entry entry=dataAccessor.getEntry(getPosition());
                entry.setContent(entryCardBinding.content.getText().toString());
                dataAccessor.changeEntry(getPosition(), entry);
                getBindingAdapter().notifyItemChanged(getPosition());
            }
        });

        entryCardBinding.content.setOnFocusChangeListener((view, hasFocus) ->
        {
            if(hasFocus)
            {
                entryCardBinding.content.setClickable(true);
                entryCardBinding.content.setCursorVisible(true);

                entryCardBinding.content.setFocusable(true);
                entryCardBinding.content.setFocusableInTouchMode(true);

                entryCardBinding.content.setSelection(entryCardBinding.content.length());

                mainActivity.itemTouchHelperEnabled(false);
                entryCardBinding.card.setLongClickable(false);
                contextMenuEnabled=false;
            }
            else
            {
                entryCardBinding.content.setClickable(false);
                entryCardBinding.content.setCursorVisible(false);

                entryCardBinding.content.setFocusable(false);
                entryCardBinding.content.setFocusableInTouchMode(false);

                mainActivity.itemTouchHelperEnabled(true);
                entryCardBinding.card.setLongClickable(true);
                contextMenuEnabled=true;

                mainActivity.hideKeyboard();

                Entry entry=dataAccessor.getEntry(getPosition());
                entry.setContent(entryCardBinding.content.getText().toString());
                dataAccessor.changeEntry(getPosition(), entry);
                if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
                {
                    notificationHelper.cancelNotification(entry.getId());
                    notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime()!=null?entry.getTime():reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
                }

            }
        });

        //Set up context menu
        entryCardBinding.card.setOnCreateContextMenuListener((contextMenu, view, menuInfo) ->
        {
            if(!contextMenuEnabled) return;

            this.contextMenu=contextMenu;
            new MenuInflater(SimpleDo.getAppContext()).inflate(R.menu.entry_change_menu, contextMenu);

            mainActivity.hideKeyboard();
            mainActivity.onBackPressed();

            if(dataAccessor.getEntry(getPosition()).getDate()!=null)
            {
                contextMenu.getItem(2).setVisible(true);
                contextMenu.getItem(3).setVisible(true);
            }

            contextMenu.getItem(3).setTitle(dataAccessor.getEntry(getPosition()).isNotifying()?SimpleDo.getAppContext().getResources().getString(R.string.deactivate_notification):SimpleDo.getAppContext().getResources().getString(R.string.activate_notification));

            contextMenu.getItem(0).setOnMenuItemClickListener((item) ->
            {
                entryCardBinding.content.setFocusableInTouchMode(true);
                entryCardBinding.content.setFocusable(true);

                entryCardBinding.content.requestFocus();
                entryCardBinding.content.postDelayed(()->
                {
                    InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(entryCardBinding.content, InputMethodManager.SHOW_IMPLICIT);
                },100);

                return true;
            });

            contextMenu.getItem(1).setOnMenuItemClickListener((item) ->
            {
                Date date=dataAccessor.getEntry(getPosition()).getDate();
                DateTimeConverter dateTimeConverter=new DateTimeConverter();

                mainActivity.showDatePicker(
                        date==null? Calendar.getInstance().getTimeInMillis(): dateTimeConverter.fromDateInMillis(date),
                        (selection) ->
                        {
                            Entry entry=dataAccessor.getEntry(getPosition());
                            if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.cancelNotification(entry.getId());

                            entry.setDate(dateTimeConverter.fromMillisInDate(selection));
                            dataAccessor.changeEntry(getPosition(), entry);
                            getBindingAdapter().notifyItemChanged(getPosition());

                            if (entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime()!=null?entry.getTime():reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
                        },
                        () ->
                        {
                            Entry entry=dataAccessor.getEntry(getPosition());

                            if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.cancelNotification(entry.getId());

                            entry.setDate(null);
                            entry.setTime(null);
                            dataAccessor.changeEntry(getPosition(), entry);
                            getBindingAdapter().notifyItemChanged(getPosition());
                        });

                return true;
            });

            contextMenu.getItem(2).setOnMenuItemClickListener((item) ->
            {
                Time time=dataAccessor.getEntry(getPosition()).getTime();

                mainActivity.showTimePicker(time==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):time.getHour(),
                        time==null?Calendar.getInstance().get(Calendar.MINUTE):time.getMinute(),
                        (hour, minute) ->
                        {
                            Entry entry = dataAccessor.getEntry(getPosition());
                            if (entry.isNotifying() && !entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
                                notificationHelper.cancelNotification(entry.getId());

                            entry.setTime(new Time(hour, minute));
                            dataAccessor.changeEntry(getPosition(), entry);
                            getBindingAdapter().notifyItemChanged(getPosition());
                            if (entry.isNotifying() && !entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
                                notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime() != null ? entry.getTime() : reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
                        },
                        () ->
                        {
                            Entry entry = dataAccessor.getEntry(getPosition());
                            if (entry.isNotifying() && !entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
                                notificationHelper.cancelNotification(entry.getId());

                            entry.setTime(null);
                            dataAccessor.changeEntry(getPosition(), entry);
                            getBindingAdapter().notifyItemChanged(getPosition());
                            if (entry.isNotifying() && !entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
                                notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime() != null ? entry.getTime() : reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
                        });

                return true;
            });

            contextMenu.getItem(3).setOnMenuItemClickListener(item ->
            {
                Entry entry=dataAccessor.getEntry(getPosition());
                if(dataAccessor.getEntry(getPosition()).isNotifying())
                {
                    entry.setNotifying(false);
                    dataAccessor.changeEntry(getPosition(), entry);
                    notificationHelper.cancelNotification(entry.getId());
                }
                else
                {
                    entry.setNotifying(true);
                    dataAccessor.changeEntry(getPosition(), entry);
                    if(!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime()!=null?entry.getTime(): reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
                }
                dataAccessor.changeEntry(getPosition(), entry);
                getBindingAdapter().notifyItemChanged(getPosition());
                return true;
            });

            MenuItem.OnMenuItemClickListener colorChanger=(subitem) ->
            {
                Entry entry=dataAccessor.getEntry(getPosition());
                entry.setColor(colorHelper.convertMenuItemColorToColor(subitem));
                dataAccessor.changeEntry(getPosition(), entry);
                getBindingAdapter().notifyItemChanged(getPosition());
                return true;
            };

            new MenuInflater(SimpleDo.getAppContext()).inflate(R.menu.color_change_menu, contextMenu.getItem(4).getSubMenu());
            colorHelper.setupThemeSpecificColorMenuIcons(contextMenu.getItem(4).getSubMenu());
            for(int i=0; i<7; i++) contextMenu.getItem(4).getSubMenu().getItem(i).setOnMenuItemClickListener(colorChanger);
        });
    }


    public void bindData(@NonNull Entry entry)
    {
        entryCardBinding.content.setText(entry.getContent());
        entryCardBinding.card.setCardBackgroundColor(new ColorHelper().convertColorToInteger(entry.getColor()));

        if(entry.isNotifying()) entryCardBinding.bell.setVisibility(View.VISIBLE);
        else entryCardBinding.bell.setVisibility(View.GONE);

        if(entry.getDate()!=null)
        {
            entryCardBinding.date.setText(entry.getDate().toString());
            entryCardBinding.deadline.setVisibility(View.VISIBLE);

            if(entry.getTime()!=null)
            {
                entryCardBinding.time.setText(entry.getTime().toString());
                entryCardBinding.time.setVisibility(View.VISIBLE);
            }
            else entryCardBinding.time.setVisibility(View.GONE);
        }
        else entryCardBinding.deadline.setVisibility(View.GONE);
    }

    @Nullable
    public ContextMenu getContextMenu()
    {
        return contextMenu;
    }

    public void setEntryDragged(boolean dragged)
    {
        entryCardBinding.card.setDragged(dragged);
    }
}
