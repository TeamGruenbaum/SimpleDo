package de.stevensolleder.simpledo.controller;

import android.content.Context;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.Calendar;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.databinding.EntryCardBinding;
import de.stevensolleder.simpledo.model.DateTimeConverter;
import de.stevensolleder.simpledo.model.CustomNotificationHelper;
import de.stevensolleder.simpledo.model.DataAccessor;
import de.stevensolleder.simpledo.model.Date;
import de.stevensolleder.simpledo.model.Entry;
import de.stevensolleder.simpledo.model.NotificationHelper;
import de.stevensolleder.simpledo.model.SimpleDo;
import de.stevensolleder.simpledo.model.Time;

public class EntryChangeMenu extends PopupMenu
{
    private NotificationHelper<Entry> notificationHelper;

    public EntryChangeMenu(Main mainActivity, Context context, View anchor, int position, DataAccessor dataAccessor, EntryCardBinding entryCardBinding)
    {
        super(context, anchor);
        this.inflate(R.menu.entry_change_menu);
        notificationHelper=new CustomNotificationHelper();
        Entry entry=dataAccessor.getEntry(position);

        if(entry.getDate()!=null)
        {
            this.getMenu().getItem(2).setVisible(true);
            this.getMenu().getItem(3).setVisible(true);
        }

        if(entry.isNotifying())
        {
            this.getMenu().getItem(3).setTitle(SimpleDo.getAppContext().getResources().getString(R.string.deactivate_notification));
        }
        else
        {
            this.getMenu().getItem(3).setTitle(SimpleDo.getAppContext().getResources().getString(R.string.activate_notification));
        }

        this.getMenu().getItem(0).setOnMenuItemClickListener((item) ->
        {
            entryCardBinding.content.setFocusableInTouchMode(true);
            entryCardBinding.content.setFocusable(true);
            entryCardBinding.content.requestFocus();

            return true;
        });

        this.getMenu().getItem(1).setOnMenuItemClickListener((item) ->
        {
            entryCardBinding.content.clearFocus();
            UIUtil.hideKeyboard(mainActivity);

            Date date=entry.getDate();

            MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                    .datePicker()
                    .setTheme(R.style.MaterialCalendarTheme)
                    .setSelection(date==null? Calendar.getInstance().getTimeInMillis():new DateTimeConverter().fromDateInMillis(date))
                    .build();

            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                entry.setDate(new DateTimeConverter().fromMillisInDate(selection));
                dataAccessor.changeEntry(entry, position);

                if (entry.isNotifying()) notificationHelper.planAndSendNotification(entry);
            });

            materialDatePicker.addOnNegativeButtonClickListener(view1 -> {
                entry.setDate(null);
                entry.setTime(null);
                dataAccessor.changeEntry(entry, position);

                if(entry.isNotifying()) notificationHelper.cancelNotification(entry);
            });

            materialDatePicker.show(mainActivity.getSupportFragmentManager(), "null");

            return true;
        });

        this.getMenu().getItem(2).setOnMenuItemClickListener((item) ->
        {
            entryCardBinding.content.clearFocus();
            UIUtil.hideKeyboard(mainActivity);

            Time time=entry.getTime();

            MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(time==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):time.getHour())
                    .setMinute(time==null?Calendar.getInstance().get(Calendar.MINUTE):time.getMinute())
                    .build();

            materialTimePicker.addOnPositiveButtonClickListener(view -> {
                entry.setTime(new Time(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                dataAccessor.changeEntry(entry, position);

                if(entry.isNotifying()) notificationHelper.planAndSendNotification(entry);
            });

            materialTimePicker.addOnNegativeButtonClickListener(view1 ->
            {
                entry.setTime(null);
                dataAccessor.changeEntry(entry, position);

                if(entry.isNotifying()) notificationHelper.planAndSendNotification(entry);
            });

            materialTimePicker.show(mainActivity.getSupportFragmentManager(), "null");
            materialTimePicker.getFragmentManager().executePendingTransactions();
            materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_ok_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.apply));
            materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_cancel_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.delete));

            return true;
        });

        this.getMenu().getItem(3).setOnMenuItemClickListener(item ->
        {
            if(entry.isNotifying())
            {
                entry.setNotifying(false);
                dataAccessor.changeEntry(entry, position);
                notificationHelper.cancelNotification(entry);
            }
            else
            {
                entry.setNotifying(true);
                dataAccessor.changeEntry(entry, position);
                notificationHelper.planAndSendNotification(entry);
            }
            dataAccessor.changeEntry(entry, position);
            return true;
        });

        MenuItem.OnMenuItemClickListener colorChanger=(subitem) ->
        {
            switch(subitem.getItemId())
            {
                case R.id.white: entry.setColor(Color.WHITE); break;
                case R.id.yellow: entry.setColor(Color.parseColor("#FFF9C4")); break;
                case R.id.orange:entry.setColor(Color.parseColor("#FFE0B2")); break;
                case R.id.red: entry.setColor(Color.parseColor("#FFCDD2")); break;
                case R.id.green: entry.setColor(Color.parseColor("#DCEDC8")); break;
                case R.id.blue: entry.setColor(Color.parseColor("#BBDEFB")); break;
                case R.id.purple: entry.setColor(Color.parseColor("#E1BEE7")); break;
            }
            dataAccessor.changeEntry(entry, position);

            return true;
        };

        for(int i=0; i<7; i++)
        {
            this.getMenu().getItem(4).getSubMenu().getItem(i).setOnMenuItemClickListener(colorChanger);
        }

        this.show();
    }
}
