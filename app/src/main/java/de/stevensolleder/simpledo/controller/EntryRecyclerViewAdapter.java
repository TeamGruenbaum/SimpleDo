package de.stevensolleder.simpledo.controller;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.Calendar;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.*;

import static de.stevensolleder.simpledo.model.ColorHelper.colorChangeMenuMenuItemToColor;
import static de.stevensolleder.simpledo.model.SaveHelper.*;

public class EntryRecyclerViewAdapter extends RecyclerView.Adapter<EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder>
{
    Main mainActivity;

    public EntryRecyclerViewAdapter(Main activity)
    {
        mainActivity=activity;
    }

    @Override
    public EntryRecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new EntryRecyclerViewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_card_material_card_view, parent, false));
    }

    @Override
    public void onBindViewHolder(EntryRecyclerViewViewHolder entryViewViewHolder, int position)
    {
        entryViewViewHolder.getContentEditText().setText(getEntry(position).getContent());

        if(getEntry(position).isNotifying())
        {
            entryViewViewHolder.bellImageView.setVisibility(View.VISIBLE);
        }
        else
        {
            entryViewViewHolder.bellImageView.setVisibility(View.GONE);
        }

        if(getEntry(position).getDate()!=null)
        {
            entryViewViewHolder.getDateTextView().setText(getEntry(position).getDate().toString());
            entryViewViewHolder.getDeadlineLinearLayout().setVisibility(View.VISIBLE);

            if(getEntry(position).getTime()!=null)
            {
                entryViewViewHolder.getTimeTextView().setText(getEntry(position).getTime().toString());
                entryViewViewHolder.getTimeTextView().setVisibility(View.VISIBLE);
            }
            else
            {
                entryViewViewHolder.getTimeTextView().setVisibility(View.GONE);
            }
        }
        else
        {
            entryViewViewHolder.getDeadlineLinearLayout().setVisibility(View.GONE);
        }

        entryViewViewHolder.getCardMaterialCardView().setCardBackgroundColor(getEntry(position).getColor());
    }

    //Unified and only Methods to change the entrylist
    @Override
    public int getItemCount()
    {
        return getEntriesSize();
    }

    public void insertEntry(Entry newEntry)
    {
        addEntry(newEntry);

        notifyItemInserted(getEntriesSize());
    }

    public void insertEntry(Entry newEntry, int position)
    {
        addEntry(position, newEntry);

        notifyItemInserted(position);
    }

    public void moveEntry(int fromIndex, int toIndex)
    {
        if(fromIndex < toIndex)
        {
            for (int i = fromIndex; i < toIndex; i++)
            {
                swapEntries(i, i+1);
            }
        }
        else
            {
            for(int i = fromIndex; i > toIndex; i--)
            {
                swapEntries(i, i - 1);
            }
        }

        notifyItemMoved(fromIndex, toIndex);
    }

    public void deleteEntry(int index)
    {
        removeEntry(index);

        notifyItemRemoved(index);
    }

    public void changeEntry(Entry newEntry, int index)
    {
        SaveHelper.changeEntry(newEntry, index);

        notifyItemChanged(index);
    }

    public void sortEntries()
    {
        SaveHelper.sortEntries();

        notifyItemRangeChanged(0, SaveHelper.getEntriesSize());
    }

    public class EntryRecyclerViewViewHolder extends RecyclerView.ViewHolder
    {
        private MaterialCardView cardMaterialCardView;
        private AddCardContentEditText contentEditText;
        private LinearLayout deadlineLinearLayout;
        private TextView dateTextView;
        private TextView timeTextView;
        private ImageView bellImageView;

        private ContextMenu contextMenu;

        boolean contextMenuState=true;

        public EntryRecyclerViewViewHolder(View itemView)
        {
            super(itemView);

            cardMaterialCardView=itemView.findViewById(R.id.card);
            contentEditText=itemView.findViewById(R.id.content);
            deadlineLinearLayout=itemView.findViewById(R.id.deadline);
            dateTextView=itemView.findViewById(R.id.date);
            timeTextView=itemView.findViewById(R.id.time);
            bellImageView=itemView.findViewById(R.id.bell);

            contentEditText.setKeyPreImeAction((keyCode, keyEvent) ->
            {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP)
                {
                        contentEditText.clearFocus();

                        Entry entry = SaveHelper.getEntry(getPosition());
                        entry.setContent(contentEditText.getText().toString());
                        changeEntry(entry, getPosition());
                }
            });

            contentEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View view, boolean b)
                {
                    if(b)
                    {
                        contentEditText.setClickable(true);
                        contentEditText.setCursorVisible(true);
                        contentEditText.setFocusable(true);
                        contentEditText.setFocusableInTouchMode(true);
                        contentEditText.setSelection(contentEditText.length());
                        mainActivity.itemTouchHelperEnabled(false);
                        cardMaterialCardView.setLongClickable(false);
                        setContextMenuEnabled(false);

                        InputMethodManager inputMethodManager=(InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                    else
                    {
                        contentEditText.setClickable(false);
                        contentEditText.setCursorVisible(false);
                        contentEditText.setFocusable(false);
                        contentEditText.setFocusableInTouchMode(false);
                        mainActivity.itemTouchHelperEnabled(true);
                        cardMaterialCardView.setLongClickable(true);
                        setContextMenuEnabled(true);
                    }
                }
            });

            cardMaterialCardView.setOnCreateContextMenuListener((contextMenu, v, menuInfo) ->
            {
                if(!contextMenuState)
                {
                    return;
                }

                this.contextMenu=contextMenu;

                MenuInflater menuInflater=new MenuInflater(SimpleDo.getAppContext());
                menuInflater.inflate(R.menu.entry_change_menu, contextMenu);

                if(SaveHelper.getEntry(getPosition()).getDate()!=null)
                {
                    contextMenu.getItem(2).setVisible(true);
                    contextMenu.getItem(3).setVisible(true);
                }

                if(SaveHelper.getEntry(getPosition()).isNotifying())
                {
                    contextMenu.getItem(3).setTitle(SimpleDo.getAppContext().getResources().getString(R.string.deactivate_notification));
                }else
                {
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
                    UIUtil.hideKeyboard(mainActivity);

                    Date date=SaveHelper.getEntry(getPosition()).getDate();

                    MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                            .datePicker()
                            .setTheme(R.style.MaterialCalendarTheme)
                            .setSelection(date==null?Calendar.getInstance().getTimeInMillis():Main.fromDateInMilis(date))
                            .build();

                    materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                        @Override
                        public void onPositiveButtonClick(Long selection)
                        {
                            Entry entry = SaveHelper.getEntry(getPosition());
                            entry.setDate(Main.fromMilisInDate(selection));
                            changeEntry(entry, getPosition());

                            if (entry.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(entry);
                            }
                        }
                    });

                    materialDatePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Entry temp=SaveHelper.getEntry(getPosition());
                            temp.setDate(null);
                            temp.setTime(null);
                            changeEntry(temp, getPosition());

                            if(temp.isNotifying())
                            {
                                NotificationHelper.cancelNotification(temp);
                            }
                        }
                    });

                    materialDatePicker.show(mainActivity.getSupportFragmentManager(), "null");

                    return true;
                });

                contextMenu.getItem(2).setOnMenuItemClickListener((item) ->
                {
                    contentEditText.clearFocus();
                    UIUtil.hideKeyboard(mainActivity);

                    Time time=SaveHelper.getEntry(getPosition()).getTime();

                    MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                            .setTimeFormat(TimeFormat.CLOCK_24H)
                            .setHour(time==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):time.getHour())
                            .setMinute(time==null?Calendar.getInstance().get(Calendar.MINUTE):time.getHour())
                            .build();

                    materialTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Entry temp=SaveHelper.getEntry(getPosition());
                            temp.setTime(new Time(materialTimePicker.getHour(), materialTimePicker.getMinute()));
                            changeEntry(temp, getPosition());

                            if(temp.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(temp);
                            }
                        }
                    });

                    materialTimePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Entry temp=SaveHelper.getEntry(getPosition());

                            temp.setTime(null);
                            changeEntry(temp, getPosition());

                            if(temp.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(temp);
                            }
                        }
                    });

                    materialTimePicker.show(mainActivity.getSupportFragmentManager(), "null");

                    return true;
                });

                contextMenu.getItem(3).setOnMenuItemClickListener(item ->
                {
                    Entry entry=SaveHelper.getEntry(getPosition());

                    if(SaveHelper.getEntry(getPosition()).isNotifying())
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

                MenuItem.OnMenuItemClickListener colorChanger=(subitem) ->
                {
                    Entry entry=SaveHelper.getEntry(getPosition());
                    entry.setColor(colorChangeMenuMenuItemToColor(subitem));
                    changeEntry(entry, getPosition());

                    return true;
                };

                for(int i=0; i<7; i++)
                {
                    contextMenu.getItem(4).getSubMenu().getItem(i).setOnMenuItemClickListener(colorChanger);
                }
            });
        }

        public void setContextMenuEnabled(boolean newState)
        {
            contextMenuState=newState;
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

        public ContextMenu getContextMenu()
        {
            return contextMenu;
        }


        class RecyclerViewDiffUtilCallback extends DiffUtil.Callback
        {
            private ArrayList<Entry> oldEntryList;
            private ArrayList<Entry> newEntryList;

            public RecyclerViewDiffUtilCallback(ArrayList<Entry> oldEntryList, ArrayList<Entry> newEntryList)
            {
                this.oldEntryList=oldEntryList;
                this.newEntryList=newEntryList;
            }

            @Override
            public int getOldListSize() {
                return oldEntryList.size();
            }

            @Override
            public int getNewListSize() {
                return newEntryList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldEntryPosition, int newEntryPosition) {
                return oldEntryList.get(oldEntryPosition).getID()==newEntryList.get(newEntryPosition).getID();
            }

            @Override
            public boolean areContentsTheSame(int oldEntryPosition, int newEntryPosition) {
                return oldEntryList.get(oldEntryPosition)==newEntryList.get(newEntryPosition);
            }
        }
    }
}