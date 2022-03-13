package de.stevensolleder.simpledo.presenter;

import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.function.Consumer;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.databinding.EntryCardBinding;
import de.stevensolleder.simpledo.model.*;



public class EntryViewHolder extends RecyclerView.ViewHolder
{
    private EntryCardBinding entryCardBinding;
    private IDataAccessor dataAccessor;
    private IReminderSettingsAccessor reminderSettingsAccessor;

    private ContextMenu contextMenu;
    private boolean contextMenuEnabled =true;



    public EntryViewHolder(Main mainActivity, EntryCardBinding entryCardBinding, IDataAccessor dataAccessor, IReminderSettingsAccessor reminderSettingsAccessor, Consumer<Integer> notifyAdapter)
    {
        super(entryCardBinding.getRoot());

        this.entryCardBinding=entryCardBinding;
        this.dataAccessor=dataAccessor;
        this.reminderSettingsAccessor=reminderSettingsAccessor;

        ColorHelper colorHelper=new ColorHelper();
        KeyboardHelper keyboardHelper=new KeyboardHelper(mainActivity);
        NotificationHelper notificationHelper=new NotificationHelper(dataAccessor::getEntries, reminderSettingsAccessor);

        entryCardBinding.content.setKeyPreImeAction((keyCode, keyEvent) ->
        {
            if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP)
            {
                entryCardBinding.content.clearFocus();
                Entry entry=dataAccessor.getEntry(getPosition());
                entry.setContent(entryCardBinding.content.getText().toString());
                dataAccessor.changeEntry(getPosition(), entry);
                notifyAdapter.accept(getPosition());
            }
        });

        entryCardBinding.content.setOnFocusChangeListener((view, hasFocus) -> {
            if(hasFocus)
            {
                entryCardBinding.content.setClickable(true);
                entryCardBinding.content.setCursorVisible(true);
                entryCardBinding.content.setFocusable(true);
                entryCardBinding.content.setFocusableInTouchMode(true);
                entryCardBinding.content.setSelection(entryCardBinding.content.length());
                mainActivity.itemTouchHelperEnabled(false);
                entryCardBinding.card.setLongClickable(false);
                setContextMenuEnabled(false);
                mainActivity.softInputAdjustmentEnabled(false);
                keyboardHelper.setKeyboardEnabled(true);
            }
            else
            {
                entryCardBinding.content.setClickable(false);
                entryCardBinding.content.setCursorVisible(false);
                entryCardBinding.content.setFocusable(false);
                entryCardBinding.content.setFocusableInTouchMode(false);
                mainActivity.itemTouchHelperEnabled(true);
                entryCardBinding.card.setLongClickable(true);
                setContextMenuEnabled(true);
                mainActivity.softInputAdjustmentEnabled(true);

                Entry entry=dataAccessor.getEntry(getPosition());
                entry.setContent(entryCardBinding.content.getText().toString());
                dataAccessor.changeEntry(getPosition(), entry);
                if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
                {
                    notificationHelper.cancelNotification(entry.getId());
                    notificationHelper.planAndSendNotification(entry.getTime(), entry.getDate(), entry.getContent(), entry.getId());
                }

            }
        });

        //Set up context menu
        entryCardBinding.card.setOnCreateContextMenuListener((contextMenu, view, menuInfo) ->
        {
            if(!contextMenuEnabled) return;

            this.contextMenu=contextMenu;
            new MenuInflater(SimpleDo.getAppContext()).inflate(R.menu.entry_change_menu, contextMenu);

            keyboardHelper.setKeyboardEnabled(false);
            mainActivity.onBackPressed();

            if(dataAccessor.getEntry(getPosition()).getDate()!=null)
            {
                contextMenu.getItem(2).setVisible(true);
                contextMenu.getItem(3).setVisible(true);
            }

            if(dataAccessor.getEntry(getPosition()).isNotifying()) contextMenu.getItem(3).setTitle(SimpleDo.getAppContext().getResources().getString(R.string.deactivate_notification));
            else contextMenu.getItem(3).setTitle(SimpleDo.getAppContext().getResources().getString(R.string.activate_notification));

            contextMenu.getItem(0).setOnMenuItemClickListener((item) ->
            {
                entryCardBinding.content.setFocusableInTouchMode(true);
                entryCardBinding.content.setFocusable(true);
                entryCardBinding.content.requestFocus();
                return true;
            });

            contextMenu.getItem(1).setOnMenuItemClickListener((item) ->
            {
                Date date=dataAccessor.getEntry(getPosition()).getDate();
                DateTimeConverter dateTimeConverter =new DateTimeConverter();

                MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                        .datePicker()
                        .setTheme(R.style.MaterialCalendarTheme)
                        .setSelection(date==null? Calendar.getInstance().getTimeInMillis(): dateTimeConverter.fromDateInMillis(date))
                        .build();

                materialDatePicker.addOnPositiveButtonClickListener(selection ->
                {
                    Entry entry=dataAccessor.getEntry(getPosition());
                    if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.cancelNotification(entry.getId());

                    entry.setDate(dateTimeConverter.fromMillisInDate(selection));
                    dataAccessor.changeEntry(getPosition(), entry);
                    notifyAdapter.accept(getPosition());

                    if (entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.planAndSendNotification(entry.getTime(), entry.getDate(), entry.getContent(), entry.getId());
                });

                materialDatePicker.addOnNegativeButtonClickListener(view1 ->
                {
                    Entry entry=dataAccessor.getEntry(getPosition());
                    entry.setDate(null);
                    entry.setTime(null);
                    dataAccessor.changeEntry(getPosition(), entry);
                    notifyAdapter.accept(getPosition());

                    if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.cancelNotification(entry.getId());
                });

                materialDatePicker.show(mainActivity.getSupportFragmentManager(), "null");
                return true;
            });

            contextMenu.getItem(2).setOnMenuItemClickListener((item) ->
            {
                Time time=dataAccessor.getEntry(getPosition()).getTime();

                MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(time==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):time.getHour())
                        .setMinute(time==null?Calendar.getInstance().get(Calendar.MINUTE):time.getMinute())
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(view1 ->
                {
                    Entry entry=dataAccessor.getEntry(getPosition());
                    if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.cancelNotification(entry.getId());

                    entry.setTime(new Time(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                    dataAccessor.changeEntry(getPosition(), entry);
                    notifyAdapter.accept(getPosition());
                    if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.planAndSendNotification(entry.getTime(), entry.getDate(), entry.getContent(), entry.getId());
                });

                materialTimePicker.addOnNegativeButtonClickListener(view1 ->
                {
                    Entry entry=dataAccessor.getEntry(getPosition());
                    if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.cancelNotification(entry.getId());

                    entry.setTime(null);
                    dataAccessor.changeEntry(getPosition(), entry);
                    notifyAdapter.accept(getPosition());
                    if(entry.isNotifying()&&!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.planAndSendNotification(entry.getTime(), entry.getDate(), entry.getContent(), entry.getId());
                });

                materialTimePicker.show(mainActivity.getSupportFragmentManager(), "null");
                materialTimePicker.getFragmentManager().executePendingTransactions();
                materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_ok_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.apply));
                materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_cancel_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.delete));
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
                    if(!entry.isInPast(reminderSettingsAccessor.getAlldayTime())) notificationHelper.planAndSendNotification(entry.getTime(), entry.getDate(), entry.getContent(), entry.getId());
                }
                dataAccessor.changeEntry(getPosition(), entry);
                notifyAdapter.accept(getPosition());
                return true;
            });

            MenuItem.OnMenuItemClickListener colorChanger=(subitem) ->
            {
                Entry entry=dataAccessor.getEntry(getPosition());
                entry.setColor(colorHelper.getMenuItemColor(subitem));
                dataAccessor.changeEntry(getPosition(), entry);
                notifyAdapter.accept(getPosition());
                return true;
            };

            new MenuInflater(SimpleDo.getAppContext()).inflate(R.menu.color_change_menu, contextMenu.getItem(4).getSubMenu());
            colorHelper.setupThemeSpecificColorMenuIcons(contextMenu.getItem(4).getSubMenu());
            for(int i=0; i<7; i++) contextMenu.getItem(4).getSubMenu().getItem(i).setOnMenuItemClickListener(colorChanger);
        });
    }

    public void bindData(Entry entry)
    {
        entryCardBinding.content.setText(entry.getContent());
        entryCardBinding.card.setCardBackgroundColor(entry.getColor());

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

    public void setContextMenuEnabled(boolean enabled)
    {
        contextMenuEnabled=enabled;
    }

    public ContextMenu getContextMenu()
    {
        return contextMenu;
    }

    public void setEntryDragged(boolean dragged)
    {
        entryCardBinding.card.setDragged(dragged);
    }
}
