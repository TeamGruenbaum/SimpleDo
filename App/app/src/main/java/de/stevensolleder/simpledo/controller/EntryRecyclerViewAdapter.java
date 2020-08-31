package de.stevensolleder.simpledo.controller;

import android.app.*;
import android.content.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;

import de.stevensolleder.simpledo.R;
import de.stevensolleder.simpledo.model.*;

import static de.stevensolleder.simpledo.model.ColorHelper.colorChangeMenuMenuItemToColor;
import static de.stevensolleder.simpledo.model.SaveHelper.*;

public class EntryRecyclerViewAdapter extends RecyclerView.Adapter<EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder>
{
    Activity mainActivity;

    public EntryRecyclerViewAdapter(Activity activity)
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

                    contentEditText.clearFocus();

                    Entry temp = SaveHelper.getEntry(getPosition());
                    temp.setContent(contentEditText.getText().toString());
                    changeEntry(temp, getPosition());
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

                        InputMethodManager inputMethodManager=(InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
                    else
                    {
                        contentEditText.setClickable(false);
                        contentEditText.setCursorVisible(false);
                        contentEditText.setFocusable(false);
                        contentEditText.setFocusableInTouchMode(false);
                    }
                }
            });

            itemView.setOnCreateContextMenuListener((contextMenu, v, menuInfo) ->
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
                    DatePickerDialog picker = new DatePickerDialog(mainActivity, new DatePickerDialog.OnDateSetListener()
                    {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day)
                        {
                            Entry temp=SaveHelper.getEntry(getPosition());

                            temp.setDate(new Date(day, month, year));
                            changeEntry(temp, getPosition());

                            if(temp.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(temp);
                            }
                        }
                    }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

                    picker.setOnCancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            Entry temp=SaveHelper.getEntry(getPosition());

                            temp.setDate(null);
                            changeEntry(temp, getPosition());

                            if(temp.isNotifying())
                            {
                                NotificationHelper.cancelNotification(temp);
                            }
                        }
                    });

                    picker.show();

                    picker.getButton(DialogInterface.BUTTON_POSITIVE).setText("Übernehmen");
                    picker.getButton(DialogInterface.BUTTON_NEGATIVE).setText("Löschen");

                    return true;
                });

                contextMenu.getItem(2).setOnMenuItemClickListener((item) ->
                {
                    TimePickerDialog timePickerDialog=new TimePickerDialog(mainActivity, new TimePickerDialog.OnTimeSetListener()
                    {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int hour, int minute)
                        {
                            Entry temp=SaveHelper.getEntry(getPosition());

                            temp.setTime(new Time(hour, minute));
                            changeEntry(temp, getPosition());

                            if(temp.isNotifying())
                            {
                                NotificationHelper.planAndSendNotification(temp);
                            }
                        }
                    }, Calendar.getInstance().get(Calendar.HOUR), Calendar.getInstance().get(Calendar.MINUTE), true);

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

                    timePickerDialog.show();

                    timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Übernehmen");
                    timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText("Löschen");

                    return true;
                });

                contextMenu.getItem(3).setOnMenuItemClickListener(item ->
                {
                    Entry temp=SaveHelper.getEntry(getPosition());

                    if(SaveHelper.getEntry(getPosition()).isNotifying())
                    {
                        temp.setNotifying(false);

                        changeEntry(temp, getPosition());

                        NotificationHelper.cancelNotification(temp);
                    }
                    else
                    {
                        temp.setNotifying(true);

                        changeEntry(temp, getPosition());

                        NotificationHelper.planAndSendNotification(temp);
                    }

                    return true;
                });

                MenuItem.OnMenuItemClickListener colorChanger=(subitem) ->
                {
                    Entry temp=SaveHelper.getEntry(getPosition());

                    temp.setColor(colorChangeMenuMenuItemToColor(subitem));
                    changeEntry(temp, getPosition());

                    return true;
                };

                for(int i=0; i<7; i++)
                {
                    contextMenu.getItem(4).getSubMenu().getItem(i).setOnMenuItemClickListener(colorChanger);
                }
            });
        }

        public MaterialCardView getCardMaterialCardView() {
            return cardMaterialCardView;
        }

        public void setCardMaterialCardView(MaterialCardView cardMaterialCardView) {
            this.cardMaterialCardView = cardMaterialCardView;
        }

        public AddCardContentEditText getContentEditText() {
            return contentEditText;
        }

        public void setContentEditText(AddCardContentEditText contentEditText) {
            this.contentEditText = contentEditText;
        }

        public LinearLayout getDeadlineLinearLayout() {
            return deadlineLinearLayout;
        }

        public void setDeadlineLinearLayout(LinearLayout deadlineLinearLayout) {
            this.deadlineLinearLayout = deadlineLinearLayout;
        }

        public TextView getDateTextView() {
            return dateTextView;
        }

        public void setDateTextView(TextView dateTextView) {
            this.dateTextView = dateTextView;
        }

        public TextView getTimeTextView() {
            return timeTextView;
        }

        public void setTimeTextView(TextView timeTextView) {
            this.timeTextView = timeTextView;
        }

        public ContextMenu getContextMenu() {
            return contextMenu;
        }

        public void setContextMenu(ContextMenu contextMenu) {
            this.contextMenu = contextMenu;
        }
    }
}