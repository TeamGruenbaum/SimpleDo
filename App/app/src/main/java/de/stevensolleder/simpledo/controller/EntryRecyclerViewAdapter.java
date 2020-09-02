package de.stevensolleder.simpledo.controller;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
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

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

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
            entryViewViewHolder.bell.setVisibility(View.VISIBLE);
        }
        else
        {
            entryViewViewHolder.bell.setVisibility(View.GONE);
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
        private ImageView bell;

        private ContextMenu contextMenu;

        public EntryRecyclerViewViewHolder(View itemView)
        {
            super(itemView);

            cardMaterialCardView=itemView.findViewById(R.id.card);
            contentEditText=itemView.findViewById(R.id.content);
            deadlineLinearLayout=itemView.findViewById(R.id.deadline);
            dateTextView=itemView.findViewById(R.id.date);
            timeTextView=itemView.findViewById(R.id.time);
            bell=itemView.findViewById(R.id.bell);

            contentEditText.setKeyPreImeAction((keyCode, keyEvent) ->
            {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP)
                {
                    if(!(contentEditText.getText().toString().trim().length()>0))
                    {
                        Snackbar snackbar=Snackbar.make(mainActivity.findViewById(R.id.root),"Zu wenig Zeichen", BaseTransientBottomBar.LENGTH_SHORT);
                        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
                        snackbar.setAnchorView(R.id.addCard);
                        snackbar.show();
                        return;
                    }
                    else
                    {
                        String refreshedContent=contentEditText.getText().toString().replaceAll("^\\s+|\\s+$", "");

                        contentEditText.clearFocus();

                        Entry entry = SaveHelper.getEntry(getPosition());
                        entry.setContent(refreshedContent);
                        changeEntry(entry, getPosition());
                    }
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
                    }
                }
            });

            cardMaterialCardView.setOnCreateContextMenuListener((contextMenu, v, menuInfo) ->
            {
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
                    contextMenu.getItem(3).setTitle("Deactivate Notif");
                }else
                {
                    contextMenu.getItem(3).setTitle("Activate Notif");
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

                    DatePickerDialog datePickerDialog=new DatePickerDialog(mainActivity);

                    datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Übernehmen", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            DatePicker datePicker=datePickerDialog.getDatePicker();

                            Entry entry = SaveHelper.getEntry(getPosition());
                            entry.setDate(new Date(datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear()));
                            changeEntry(entry, getPosition());

                            if (entry.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(entry);
                            }
                        }
                    });

                    datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Löschen", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
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

                    datePickerDialog.setCanceledOnTouchOutside(false);

                    datePickerDialog.show();

                    datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Übernehmen");
                    datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText("Löschen");

                    return true;
                });

                contextMenu.getItem(2).setOnMenuItemClickListener((item) ->
                {
                    contentEditText.clearFocus();
                    UIUtil.hideKeyboard(mainActivity);

                    TimePickerDialog timePickerDialog=new TimePickerDialog(mainActivity, new TimePickerDialog.OnTimeSetListener()
                    {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute)
                        {
                            Entry temp=SaveHelper.getEntry(getPosition());
                            temp.setTime(new Time(hour, minute));
                            changeEntry(temp, getPosition());

                            timePicker.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

                            if(temp.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(temp);
                            }
                        }
                    }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);

                    timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            Entry temp=SaveHelper.getEntry(getPosition());

                            temp.setTime(null);
                            changeEntry(temp, getPosition());

                            if(temp.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(temp);
                            }
                        }
                    });

                    timePickerDialog.setOnKeyListener(new Dialog.OnKeyListener()
                    {
                        @Override
                        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event)
                        {
                            if (keyCode == KeyEvent.KEYCODE_BACK)
                            {
                                timePickerDialog.dismiss();
                            }

                            return true;
                        }
                    });

                    timePickerDialog.setCanceledOnTouchOutside(false);

                    timePickerDialog.show();

                    timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Übernehmen");
                    timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText("Löschen");

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
    }
}