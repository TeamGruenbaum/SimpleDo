package de.stevensolleder.simpledo.controller;

import static de.stevensolleder.simpledo.model.ColorHelper.colorChangeMenuMenuItemToColor;
import static de.stevensolleder.simpledo.model.SaveHelper.changeEntry;

import android.content.Context;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.Calendar;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.*;



public class EntryRecyclerViewViewHolder extends RecyclerView.ViewHolder
{
    private MaterialCardView cardMaterialCardView;
    private AddCardContentEditText contentEditText;
    private LinearLayout deadlineLinearLayout;
    private TextView dateTextView;
    private TextView timeTextView;
    private ImageView bellImageView;

    private AppCompatActivity holdingActivity;

    private ContextMenu contextMenu;

    boolean contextMenuState = true;



    public EntryRecyclerViewViewHolder(ViewGroup parent, AppCompatActivity holdingActivity)
    {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_card_material_card_view, parent,false));

        this.holdingActivity=holdingActivity;



        cardMaterialCardView = itemView.findViewById(R.id.card);
        contentEditText = itemView.findViewById(R.id.content);
        deadlineLinearLayout = itemView.findViewById(R.id.deadline);
        dateTextView = itemView.findViewById(R.id.date);
        timeTextView = itemView.findViewById(R.id.time);
        bellImageView = itemView.findViewById(R.id.bell);



        contentEditText.setKeyPreImeAction((keyCode, keyEvent) ->
        {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                contentEditText.clearFocus();

                Entry entry = SaveHelper.getEntry(getPosition());
                entry.setContent(contentEditText.getText().toString());
                changeEntry(entry, getPosition());
            }
        });



        contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    contentEditText.setClickable(true);
                    contentEditText.setCursorVisible(true);
                    contentEditText.setFocusable(true);
                    contentEditText.setFocusableInTouchMode(true);
                    contentEditText.setSelection(contentEditText.length());
                    //holdingActivity.itemTouchHelperEnabled(false);
                    cardMaterialCardView.setLongClickable(false);
                    //setContextMenuEnabled(false);

                    InputMethodManager inputMethodManager = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                } else {
                    contentEditText.setClickable(false);
                    contentEditText.setCursorVisible(false);
                    contentEditText.setFocusable(false);
                    contentEditText.setFocusableInTouchMode(false);
                    //mainActivity.itemTouchHelperEnabled(true);
                    cardMaterialCardView.setLongClickable(true);
                    //setContextMenuEnabled(true);
                }
            }
        });

        cardMaterialCardView.setOnCreateContextMenuListener((contextMenu, v, menuInfo) ->
        {
            if (!contextMenuState) {
                return;
            }

            this.contextMenu = contextMenu;

            MenuInflater menuInflater = new MenuInflater(SimpleDo.getAppContext());
            menuInflater.inflate(R.menu.entry_change_menu, contextMenu);

            if (SaveHelper.getEntry(getPosition()).getDate() != null) {
                contextMenu.getItem(2).setVisible(true);
                contextMenu.getItem(3).setVisible(true);
            }

            if (SaveHelper.getEntry(getPosition()).isNotifying()) {
                contextMenu.getItem(3).setTitle(SimpleDo.getAppContext().getResources().getString(R.string.deactivate_notification));
            } else {
                contextMenu.getItem(3).setTitle(SimpleDo.getAppContext().getResources().getString(R.string.activate_notification));
            }

            contextMenu.getItem(0).setOnMenuItemClickListener((item) ->
            {
                contentEditText.setFocusableInTouchMode(true);
                contentEditText.setFocusable(true);
                contentEditText.requestFocus();

                return true;
            });

            contextMenu.getItem(1).setOnMenuItemClickListener((item) ->
            {
                contentEditText.clearFocus();
                UIUtil.hideKeyboard(holdingActivity);

                TimeConverter timeConverter = new TimeConverter();

                Date date = SaveHelper.getEntry(getPosition()).getDate();

                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder
                        .datePicker()
                        .setTheme(R.style.MaterialCalendarTheme)
                        .setSelection(date == null ? Calendar.getInstance().getTimeInMillis() : timeConverter.fromDateInMilis(date))
                        .build();

                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        Entry entry = SaveHelper.getEntry(getPosition());
                        entry.setDate(timeConverter.fromMilisInDate(selection));
                        changeEntry(entry, getPosition());

                        if (entry.isNotifying()) {
                            NotificationHelper.planAndSendNotification(entry);
                        }
                    }
                });

                materialDatePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Entry temp = SaveHelper.getEntry(getPosition());
                        temp.setDate(null);
                        temp.setTime(null);
                        changeEntry(temp, getPosition());

                        if (temp.isNotifying()) {
                            NotificationHelper.cancelNotification(temp);
                        }
                    }
                });

                materialDatePicker.show(holdingActivity.getSupportFragmentManager(), "null");

                return true;
            });

            contextMenu.getItem(2).setOnMenuItemClickListener((item) ->
            {
                contentEditText.clearFocus();
                UIUtil.hideKeyboard(holdingActivity);

                Time time = SaveHelper.getEntry(getPosition()).getTime();

                MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(time == null ? Calendar.getInstance().get(Calendar.HOUR_OF_DAY) : time.getHour())
                        .setMinute(time == null ? Calendar.getInstance().get(Calendar.MINUTE) : time.getHour())
                        .build();

                materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Entry temp = SaveHelper.getEntry(getPosition());
                        temp.setTime(new Time(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                        changeEntry(temp, getPosition());

                        if (temp.isNotifying()) {
                            NotificationHelper.planAndSendNotification(temp);
                        }
                    }
                });

                materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Entry temp = SaveHelper.getEntry(getPosition());

                        temp.setTime(null);
                        changeEntry(temp, getPosition());

                        if (temp.isNotifying()) {
                            NotificationHelper.planAndSendNotification(temp);
                        }
                    }
                });

                materialTimePicker.show(holdingActivity.getSupportFragmentManager(), "null");
                materialTimePicker.getFragmentManager().executePendingTransactions();
                materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_ok_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.apply));
                materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_cancel_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.delete));

                return true;
            });

            contextMenu.getItem(3).setOnMenuItemClickListener(item ->
            {
                Entry entry = SaveHelper.getEntry(getPosition());

                if (SaveHelper.getEntry(getPosition()).isNotifying())
                {
                    entry.setNotifying(false);
                    changeEntry(entry, getPosition());

                    NotificationHelper.cancelNotification(entry);
                }
                else
                {
                    entry.setNotifying(true);
                    changeEntry(entry, getPosition());

                    NotificationHelper.planAndSendNotification(entry);
                }

                return true;
            });

            MenuItem.OnMenuItemClickListener colorChanger = (subitem) ->
            {
                Entry entry = SaveHelper.getEntry(getPosition());
                entry.setColor(colorChangeMenuMenuItemToColor(subitem));
                changeEntry(entry, getPosition());

                return true;
            };

            for (int i = 0; i < 7; i++) {
                contextMenu.getItem(4).getSubMenu().getItem(i).setOnMenuItemClickListener(colorChanger);
            }
        });
    }

    public MaterialCardView getCardMaterialCardView()
    {
        return cardMaterialCardView;
    }

    public AddCardContentEditText getContentEditText()
    {
        return contentEditText;
    }

    public LinearLayout getDeadlineLinearLayout()
    {
        return deadlineLinearLayout;
    }

    public TextView getDateTextView()
    {
        return dateTextView;
    }

    public TextView getTimeTextView()
    {
        return timeTextView;
    }

    public ImageView getBellImageView()
    {
        return bellImageView;
    }

    public ContextMenu getContextMenu()
    {
        return contextMenu;
    }
}
