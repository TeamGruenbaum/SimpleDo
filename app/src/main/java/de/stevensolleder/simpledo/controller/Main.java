package de.stevensolleder.simpledo.controller;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.lang.reflect.Field;
import java.util.Calendar;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import de.stevensolleder.simpledo.*;
import de.stevensolleder.simpledo.databinding.MainBinding;
import de.stevensolleder.simpledo.model.*;

import static de.stevensolleder.simpledo.model.ColorHelper.*;
import static de.stevensolleder.simpledo.model.NotificationHelper.*;
import static de.stevensolleder.simpledo.model.SaveHelper.*;



public class Main extends AppCompatActivity
{
    private MainBinding layout; //edgar by alex

    private EntryRecyclerViewAdapter entryRecyclerViewAdapter;

    private Date chosenDate=null;
    private Time chosenTime=null;
    private int chosenColor=-1;
    private boolean reminding=false;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        layout=MainBinding.inflate(getLayoutInflater());

        //Initialize main.xml
        super.onCreate(savedInstanceState);
        setContentView(layout.root);

        //Initialize all attributes from main.xml
        entryRecyclerViewAdapter=new EntryRecyclerViewAdapter(this);

        //Set attributes from entryRecyclerView and set Adapter
        layout.myRecyclerView.setHasFixedSize(true);
        layout.myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        layout.myRecyclerView.setVerticalScrollBarEnabled(false);
        layout.myRecyclerView.setAdapter(entryRecyclerViewAdapter);

        //Create and set Animator
        EntryListAnimator entryListAnimator=new EntryListAnimator();
        layout.myRecyclerView.setItemAnimator(entryListAnimator);

        //Create notifcationchannel for reminders
        NotificationHelper.createNotificationChannel();

        createItemTouchHelper().attachToRecyclerView(layout.myRecyclerView);

        layout.bottomAppBar.setNavigationOnClickListener((view)->
        {
            Intent intent=new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        setupLayout();
    }

    public void selectDate(View v)
    {
        UIUtil.hideKeyboard(Main.this);
        TimeConverter timeConverter=new TimeConverter();

        MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                .datePicker()
                .setTheme(R.style.MaterialCalendarTheme)
                .setSelection(chosenDate==null?Calendar.getInstance().getTimeInMillis():timeConverter.fromDateInMilis(chosenDate))
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection ->
        {
            chosenDate=timeConverter.fromMilisInDate(selection);

            layout.timeButton.setVisibility(View.VISIBLE);
            layout.addCardDeadline.setVisibility(View.VISIBLE);
            layout.addCardDate.setText(chosenDate.toString());
            layout.divider1.setVisibility(View.VISIBLE);
            layout.remindButton.setVisibility(View.VISIBLE);
            layout.remindButton.setEnabled(true);

            openKeyboardIfClosed();
        });

        materialDatePicker.addOnNegativeButtonClickListener(view ->
        {
            chosenDate=null;
            chosenTime=null;

            layout.timeButton.setVisibility(View.GONE);
            layout.addCardDeadline.setVisibility(View.GONE);
            layout.divider1.setVisibility(View.GONE);
            layout.remindButton.setVisibility(View.GONE);
            layout.remindButton.setEnabled(false);

            openKeyboardIfClosed();
        });

        materialDatePicker.addOnCancelListener(dialog -> openKeyboardIfClosed());

        materialDatePicker.show(getSupportFragmentManager(), "null");
    }

    public void selectTime(View materialButton)
    {
        Log.d("TEST", "TEST");

        MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(chosenTime==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):chosenTime.getHour())
                .setMinute(chosenTime==null?Calendar.getInstance().get(Calendar.MINUTE):chosenTime.getHour())
                .build();

            /*geht nicht:
            Button b=materialTimePicker.getView().findViewById(R.id.material_timepicker_ok_button);
            b.setText("Steven");*/

        materialTimePicker.addOnPositiveButtonClickListener(v ->
        {
            chosenTime=new Time(materialTimePicker.getHour(), materialTimePicker.getMinute());

            layout.addCardTime.setVisibility(View.VISIBLE);
            layout.addCardTime.setText(chosenTime.toString());

            openKeyboardIfClosed();
        });


        materialTimePicker.addOnCancelListener(dialog ->
        {
            openKeyboardIfClosed();
        });

        materialTimePicker.addOnNegativeButtonClickListener(dialog ->
        {
            layout.addCardTime.setVisibility(View.GONE);
            chosenTime=null;

            openKeyboardIfClosed();
        });

        materialTimePicker.show(getSupportFragmentManager(), null);
        materialTimePicker.getFragmentManager().executePendingTransactions();
        materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_ok_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.apply));
        materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_cancel_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.delete));
    }

    public void selectColor(View v)
    {
        PopupMenu popupMenu=new PopupMenu(getApplicationContext(), v);

        popupMenu.getMenuInflater().inflate(R.menu.color_change_menu,popupMenu.getMenu());

        try
        {
            //Complex reflection stuff to achieve that in lower android versions the color images in the popup menu are shown
            Field fieldMPopup=popupMenu.getClass().getDeclaredField("mPopup");
            fieldMPopup.setAccessible(true);
            Object mPopup=fieldMPopup.get(popupMenu);
            mPopup.getClass().getDeclaredMethod("setForceShowIcon", boolean.class).invoke(mPopup, true);
        }catch(Exception exception){}

        popupMenu.setOnMenuItemClickListener((menuItem)->
        {
            chosenColor=colorChangeMenuMenuItemToColor(menuItem);
            layout.addCard.setCardBackgroundColor(chosenColor);

            return true;
        });

        popupMenu.show();
    }

    public void toggleNotification(View v)
    {
        reminding=!reminding;

        Drawable drawable;

        if(reminding)
        {
            drawable=getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme());
        }
        else
        {
            drawable=getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme());
        }

        layout.remindButton.setIcon(drawable);
    }

    public void changeSortDirection(MenuItem menuItem)
    {
        switch(getSortDirection())
        {
            case UP:
            {
                layout.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
                setSortDirection(Direction.DOWN);
            }break;
            case DOWN:
            case NONE:
            {
                layout.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, Main.this.getTheme()));
                setSortDirection(Direction.UP);
            }break;
        }

        sortEntries();
        entryRecyclerViewAdapter.notifyItemRangeChanged(0, SaveHelper.getEntriesSize());
    }

    public void changeSortCriterion(MenuItem menuItem)
    {
        switch(getSortCriterion())
        {
            case TEXT:
            {
                layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, Main.this.getTheme()));
                setSortCriterion(Criterion.DEADLINE);
            }break;
            case DEADLINE:
            {
                layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, Main.this.getTheme()));
                setSortCriterion(Criterion.COLOR);
            }break;
            case COLOR:
            case NONE:
            {
                layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, Main.this.getTheme()));
                setSortCriterion(Criterion.TEXT);

                Drawable temp=getResources().getDrawable(R.drawable.ic_arrow_downward);
                temp.setAlpha(255);
                layout.bottomAppBar.getMenu().getItem(0).setIcon(temp);
                layout.bottomAppBar.getMenu().getItem(0).setEnabled(true);

                setSortDirection(Direction.DOWN);
            }break;
        }

        if(getSortDirection()==Direction.NONE)
        {
            layout.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));

            setSortDirection(Direction.DOWN);
        }

        sortEntries();
        entryRecyclerViewAdapter.notifyItemRangeChanged(0, SaveHelper.getEntriesSize());
    }

    public void start(View view)
    {
        int duration=400;
        int interpolatorFactor=2;

        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(layout.start, "scaleY", 1.5F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(layout.start, "scaleX", 5F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(layout.start, "alpha", 0F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(layout.start,"visibility", View.GONE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));

        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(layout.addCard,"visibility", View.VISIBLE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(layout.addCard, "scaleY", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(layout.addCard, "scaleX", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(layout.addCard, "alpha", 0F, 1F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(layout.addCard, "radius", 100, 4).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardVisibility).with(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius);
        addCardAnimatorSet.setStartDelay((long) (duration/1.2));
        addCardAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));



        addCardAnimatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                layout.contentEditText.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                layout.contentEditText.setEnabled(true);

                layout.bottomAppBar.performHide();
                layout.bottomAppBar.setVisibility(View.GONE);

                InputMethodManager inputMethodManager = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                layout.contentEditText.requestFocus();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        floatingActionButtonAnimatorSet.start();
        addCardAnimatorSet.start();
    }

    public void addCard(View view)
    {
        Entry entry;

        if(chosenColor!=-1)
        {
            if(chosenDate!=null)
            {
                if(chosenTime!=null)
                {
                    entry=new Entry(layout.contentEditText.getText().toString(), chosenDate, chosenTime, chosenColor, reminding);
                }
                else
                {
                    entry=new Entry(layout.contentEditText.getText().toString(), chosenDate, chosenColor, reminding);
                }
            }
            else
            {
                entry=new Entry(layout.contentEditText.getText().toString(), chosenColor, reminding);
            }
        }
        else
        {
            if(chosenDate!=null)
            {
                if(chosenTime!=null)
                {
                    entry=new Entry(layout.contentEditText.getText().toString(), chosenDate, chosenTime, reminding);
                }
                else
                {
                    entry=new Entry(layout.contentEditText.getText().toString(), chosenDate, reminding);
                }
            }
            else
            {
                entry=new Entry(layout.contentEditText.getText().toString(), reminding);
            }
        }

        if(reminding)
        {
            if(isInPast(entry))
            {
                Snackbar snackbar=Snackbar.make(findViewById(R.id.root),this.getResources().getString(R.string.past_notification), BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
                snackbar.setAnchorView(R.id.addCard);
                snackbar.show();

                return;
            }

            planAndSendNotification(entry);

        }

        layout.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme()));
        layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
        setSortDirection(Direction.NONE);
        setSortCriterion(Criterion.NONE);

        addEntry(entry);
        entryRecyclerViewAdapter.notifyItemInserted(getEntriesSize());

        setupLayoutClickability();

        layout.contentEditText.getText().clear();
        layout.addCardDeadline.setVisibility(View.GONE);
        layout.timeButton.setVisibility(View.GONE);
        layout.divider1.setVisibility(View.GONE);
        layout.remindButton.setVisibility(View.GONE);
        layout.remindButton.setIcon(getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme()));
        reminding=false;

        chosenDate=null;
        chosenTime=null;

        layout.myRecyclerView.scrollToPosition(SaveHelper.getEntriesSize()-1);
    }


    @Override
    public void onBackPressed()
    {
        int duration=400;
        int interpolatorFactor=2;

        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(layout.start, "scaleY", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(layout.start, "scaleX", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(layout.start, "alpha", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(layout.start,"visibility", View.VISIBLE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setStartDelay((long) (duration/1.2));
        floatingActionButtonAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));

        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(layout.addCard,"visibility", View.GONE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(layout.addCard, "scaleY", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(layout.addCard, "scaleX", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(layout.addCard, "alpha", 1F, 0F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(layout.addCard, "radius", 4, 100).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius).before(addCardVisibility);
        addCardAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));


        addCardAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation)
            {
                layout.contentEditText.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation){}

            @Override
            public void onAnimationCancel(Animator animation){}

            @Override
            public void onAnimationRepeat(Animator animation){}
        });

        floatingActionButtonAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation)
            {
                layout.contentEditText.setEnabled(true);

                layout.bottomAppBar.performShow();
                layout.bottomAppBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation){}

            @Override
            public void onAnimationRepeat(Animator animation){}
        });

        addCardAnimatorSet.start();
        floatingActionButtonAnimatorSet.start();
    }


    private void setupLayout()
    {
        switch(getSortDirection())
        {
            case UP: layout.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, this.getTheme())); break;
            case DOWN: layout.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, this.getTheme())); break;
            case NONE:
            {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
                drawable.setAlpha(128);
                layout.bottomAppBar.getMenu().getItem(0).setIcon(drawable);
                layout.bottomAppBar.getMenu().getItem(0).setEnabled(false);
            } break;
        }

        switch(getSortCriterion())
        {
            case TEXT: layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, this.getTheme())); break;
            case DEADLINE: layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, this.getTheme())); break;
            case COLOR: layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, this.getTheme())); break;
            case NONE: layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, this.getTheme())); break;
        }

        setupLayoutClickability();
    }

    private void setupLayoutClickability()
    {
        Drawable swapDrawable=getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
        Drawable sortDrawable=getResources().getDrawable(R.drawable.ic_sort, this.getTheme());

        if(getEntriesSize()<=1)
        {
            swapDrawable.setAlpha(128);
            sortDrawable.setAlpha(128);
            layout.bottomAppBar.getMenu().getItem(0).setEnabled(false);
            layout.bottomAppBar.getMenu().getItem(1).setEnabled(false);
        }
        else
        {
            swapDrawable.setAlpha(255);
            sortDrawable.setAlpha(255);
            layout.bottomAppBar.getMenu().getItem(0).setEnabled(true);
            layout.bottomAppBar.getMenu().getItem(1).setEnabled(true);
        }

        layout.bottomAppBar.getMenu().getItem(0).setIcon(swapDrawable);
        layout.bottomAppBar.getMenu().getItem(1).setIcon(sortDrawable);
    }

    private ItemTouchHelper createItemTouchHelper()
    {
        return new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            //onMove() is called when a dragged card is dropped
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //Sometimes getContextMenu() returns null. To avoid a crash we use try-catch.
                try {
                    ((EntryRecyclerViewViewHolder) viewHolder).getContextMenu().close();
                } catch (Exception e) {
                }

                if(viewHolder.getAdapterPosition() < target.getAdapterPosition())
                {
                    for (int i = viewHolder.getAdapterPosition(); i < target.getAdapterPosition(); i++)
                    {
                        swapEntries(i, i+1);
                    }
                }
                else
                {
                    for(int i = viewHolder.getAdapterPosition(); i > target.getAdapterPosition(); i--)
                    {
                        swapEntries(i, i - 1);
                    }
                }

                entryRecyclerViewAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return viewHolder.getAdapterPosition() != target.getAdapterPosition();
            }

            //distance contains how many cards were passed after dropping the card after dragging the card
            int distance = 0;

            //onMoved() is called when a card is in drag mode and swapped the position with another card
            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                distance += (fromPos - toPos);

                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }

            //onSwiped() is called when a card is swiped successfully to the left or to the right
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Entry entry = getEntry(viewHolder.getAdapterPosition());
                int adapterPosition = viewHolder.getAdapterPosition();

                removeEntry(adapterPosition);
                entryRecyclerViewAdapter.notifyItemRemoved(adapterPosition);

                if (entry.getDate() != null) {
                    cancelNotification(entry);
                }

                Snackbar snackbar = Snackbar.make(findViewById(R.id.root), SimpleDo.getAppContext().getResources().getString(R.string.entry_deleted), BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);

                if (layout.addCard.getVisibility() == View.VISIBLE) {
                    snackbar.setAnchorView(layout.addCard);
                } else {
                    snackbar.setAnchorView(layout.start);
                }

                snackbar.setAction(SimpleDo.getAppContext().getResources().getString(R.string.undo), (view) ->
                {
                    addEntry(adapterPosition, entry);
                    entryRecyclerViewAdapter.notifyItemInserted(adapterPosition);

                    if (entry.getDate() != null) {
                        planAndSendNotification(entry);
                    }
                });


                snackbar.show();

                setupLayoutClickability();
            }

            //This contains the current dragged card
            EntryRecyclerViewViewHolder entryRecyclerViewViewHolder;

            //onSelectedChanged() is called when the state of the current dragged card changes
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    ((EntryRecyclerViewViewHolder) viewHolder).getCardMaterialCardView().setDragged(true);
                    entryRecyclerViewViewHolder = (EntryRecyclerViewViewHolder) viewHolder;
                }

                if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    try {
                        entryRecyclerViewViewHolder.getCardMaterialCardView().setDragged(false);
                    } catch (Exception exception) {
                    }

                    if (distance != 0) {
                        Drawable temp = getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme());
                        temp.setAlpha(128);
                        layout.bottomAppBar.getMenu().getItem(0).setIcon(temp);
                        layout.bottomAppBar.getMenu().getItem(0).setEnabled(false);
                        setSortDirection(Direction.NONE);

                        layout.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
                        setSortCriterion(Criterion.NONE);

                        distance = 0;
                    }
                }
            }
        });
    }

    private boolean isInPast(Entry entry)
    {
        Calendar calendar=Calendar.getInstance();

        Date currentDate=new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR));
        Time currentTime=new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        Time allDayEventTime=getAlldayTime();

        if (entry.getDate()!=null && entry.getDate().compareTo(currentDate)==0)
        {
            if(entry.getTime()!=null)
            {
                return (entry.getTime().compareTo(currentTime))<0;
            }
            else
            {
                return allDayEventTime.compareTo(currentTime)<0;
            }

        }

        return (entry.getDate().compareTo(currentDate))<0;
    }

    private void openKeyboardIfClosed()
    {
        if(!KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(this))
        {
            InputMethodManager imm = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0,0);
        }
    }
}