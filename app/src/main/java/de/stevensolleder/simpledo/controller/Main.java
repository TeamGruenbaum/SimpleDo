package de.stevensolleder.simpledo.controller;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.PopupMenu;

import androidx.annotation.RequiresApi;
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

import de.stevensolleder.simpledo.*;
import de.stevensolleder.simpledo.databinding.MainActivityBinding;
import de.stevensolleder.simpledo.model.*;



public class Main extends AppCompatActivity
{
    private MainActivityBinding mainBinding;
    private DataAccessor dataAccessor;

    private EntryAdapter entryAdapter;
    private ItemTouchHelper itemTouchHelper;
    private NotificationHelper<Entry> notificationHelper;
    private KeyboardHelper keyboardHelper;

    private Date chosenDate=null;
    private Time chosenTime=null;
    private int chosenColor=-1;
    private boolean reminding=false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Initialize main_activity.xml
        super.onCreate(savedInstanceState);
        mainBinding=MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        dataAccessor=new CustomDataAccessor(SimpleDo.getAppContext().getSharedPreferences("settings", Context.MODE_PRIVATE));

        //Set attributes from entryRecyclerView and set Adapter
        entryAdapter=new EntryAdapter(this, dataAccessor);
        mainBinding.myRecyclerView.setHasFixedSize(true);
        mainBinding.myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.myRecyclerView.setVerticalScrollBarEnabled(false);
        mainBinding.myRecyclerView.setAdapter(entryAdapter);

        //Create and set Animator
        EntryListAnimator entryListAnimator=new EntryListAnimator();
        mainBinding.myRecyclerView.setItemAnimator(entryListAnimator);

        //Create notificationChannel for reminders
        notificationHelper=new CustomNotificationHelper();
        notificationHelper.createNotificationChannel();

        //Setting swipe gestures
        //itemTouchHelper=new ItemTouchHelper(new CustomItemTouchHelperCallback(this, mainBinding, entryAdapter, dataAccessor, notificationHelper));
        itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            //onMove() is called when a dragged card is dropped
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                try
                {
                    ((EntryViewHolder)viewHolder).getContextMenu().close();
                }
                catch(Exception exception){ exception.printStackTrace(); }

                int fromIndex=viewHolder.getPosition();
                int toIndex=target.getPosition();

                if(fromIndex<toIndex)
                {
                    for (int i=fromIndex; i<toIndex; i++) dataAccessor.swapEntries(i, i+1);
                }
                else
                {
                    for(int i=fromIndex; i>toIndex; i--) dataAccessor.swapEntries(i, i-1);
                }
                entryAdapter.notifyItemMoved(fromIndex, toIndex);

                return viewHolder.getPosition()!=target.getPosition();
            }

            //distance contains how many cards were passed after dropping the card after dragging the card
            int distance=0;

            //onMoved() is called when a card is in drag mode and swapped the position with another card
            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromIndex, RecyclerView.ViewHolder target, int toIndex, int x, int y)
            {
                distance+=(fromIndex-toIndex);
                super.onMoved(recyclerView, viewHolder, fromIndex, target, toIndex, x, y);
            }

            //onSwiped() is called when a card is swiped successfully to the left or to the right
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
            {
                Entry entry=dataAccessor.getEntry(viewHolder.getPosition());
                int adapterPosition=viewHolder.getPosition();

                dataAccessor.removeEntry(adapterPosition);
                entryAdapter.notifyItemRemoved(adapterPosition);
                if(entry.getDate()!=null) notificationHelper.cancelNotification(entry);

                Snackbar snackbar=Snackbar.make(findViewById(R.id.root),SimpleDo.getAppContext().getResources().getString(R.string.entry_deleted),BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);

                if(mainBinding.addCard.getVisibility()==View.VISIBLE) snackbar.setAnchorView(mainBinding.addCard);
                else snackbar.setAnchorView(mainBinding.start);

                snackbar.setAction(SimpleDo.getAppContext().getResources().getString(R.string.undo), (view) ->
                {
                    dataAccessor.addEntry(adapterPosition, entry);
                    entryAdapter.notifyItemInserted(adapterPosition);
                    if(entry.getDate()!=null) notificationHelper.planAndSendNotification(entry);
                });

                snackbar.show();
                toggleSortability();
            }

            //This contains the current dragged card
            EntryViewHolder entryViewHolder;

            //onSelectedChanged() is called when the state of the current dragged card changes
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
            {
                if(actionState == ItemTouchHelper.ACTION_STATE_DRAG)
                {
                    ((EntryViewHolder) viewHolder).getMaterialCardView().setDragged(true);
                    entryViewHolder =(EntryViewHolder) viewHolder;
                }

                if(actionState==ItemTouchHelper.ACTION_STATE_IDLE)
                {
                    try
                    {
                        entryViewHolder.getMaterialCardView().setDragged(false);
                    }
                    catch(Exception exception)
                    {
                        exception.printStackTrace();
                    }

                    if(distance!=0)
                    {
                        Drawable temp = getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme());
                        temp.setAlpha(128);
                        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(temp);
                        mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(false);
                        dataAccessor.setSortDirection(Direction.NONE);

                        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
                        dataAccessor.setSortCriterion(Criterion.NONE);

                        distance=0;
                    }
                }
            }
        });

        itemTouchHelper.attachToRecyclerView(mainBinding.myRecyclerView);

        //Setting up UI
        setupLayout();
        keyboardHelper=new KeyboardHelper(this);
        keyboardHelper.setKeyboardEnabled(false);
        mainBinding.bottomAppBar.setNavigationOnClickListener((view)->startActivity(new Intent(this, SettingsActivity.class)));
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed()
    {
        int duration=400;
        int interpolatorFactor=2;

        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(mainBinding.start, "scaleY", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(mainBinding.start, "scaleX", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(mainBinding.start, "alpha", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(mainBinding.start,"visibility", View.VISIBLE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setStartDelay((long) (duration/1.2));
        floatingActionButtonAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));

        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(mainBinding.addCard,"visibility", View.GONE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(mainBinding.addCard, "scaleY", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(mainBinding.addCard, "scaleX", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(mainBinding.addCard, "alpha", 1F, 0F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(mainBinding.addCard, "radius", 4, 100).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius).before(addCardVisibility);
        addCardAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));


        addCardAnimatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                mainBinding.contentEditText.setEnabled(false);
            }

            @Override public void onAnimationEnd(Animator animation){}
            @Override public void onAnimationCancel(Animator animation){}
            @Override public void onAnimationRepeat(Animator animation){}
        });

        floatingActionButtonAnimatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mainBinding.contentEditText.setEnabled(true);
                mainBinding.bottomAppBar.performShow();
                mainBinding.bottomAppBar.setVisibility(View.VISIBLE);
            }

            @Override public void onAnimationCancel(Animator animation){}
            @Override public void onAnimationRepeat(Animator animation){}
        });

        addCardAnimatorSet.start();
        floatingActionButtonAnimatorSet.start();
    }

    private void setupLayout()
    {
        switch(dataAccessor.getSortDirection())
        {
            case UP: mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, this.getTheme())); break;
            case DOWN: mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, this.getTheme())); break;
            case NONE:
            {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
                drawable.setAlpha(128);
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(drawable);
                mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(false);
            } break;
        }

        switch(dataAccessor.getSortCriterion())
        {
            case TEXT: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, this.getTheme())); break;
            case DEADLINE: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, this.getTheme())); break;
            case COLOR: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, this.getTheme())); break;
            case NONE: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, this.getTheme())); break;
        }
        toggleSortability();
    }

    //Disables sortability if there is only one or no entry, enables it if there are two or more entries
    void toggleSortability()
    {
        Drawable directionDrawable = getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
        Drawable criterionDrawable = getResources().getDrawable(R.drawable.ic_sort, this.getTheme());

        if (dataAccessor.getEntriesSize() <= 1)
        {
            directionDrawable.setAlpha(128);
            mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(false);

            criterionDrawable.setAlpha(128);
            mainBinding.bottomAppBar.getMenu().getItem(1).setEnabled(false);
        }
        else
        {
            directionDrawable.setAlpha(255);
            mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(true);

            criterionDrawable.setAlpha(255);
            mainBinding.bottomAppBar.getMenu().getItem(1).setEnabled(true);
        }
        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(directionDrawable);
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(criterionDrawable);
    }


    public void selectDate(View dateButton)
    {
        keyboardHelper.setKeyboardEnabled(false);
        DateTimeConverter dateTimeConverter =new DateTimeConverter();

        MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                .datePicker()
                .setTheme(R.style.MaterialCalendarTheme)
                .setSelection(chosenDate==null?Calendar.getInstance().getTimeInMillis(): dateTimeConverter.fromDateInMillis(chosenDate))
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection ->
        {
            chosenDate=dateTimeConverter.fromMillisInDate(selection);

            mainBinding.timeButton.setVisibility(View.VISIBLE);
            mainBinding.addCardDeadline.setVisibility(View.VISIBLE);
            mainBinding.addCardDate.setText(chosenDate.toString());
            mainBinding.divider1.setVisibility(View.VISIBLE);
            mainBinding.remindButton.setVisibility(View.VISIBLE);
            mainBinding.remindButton.setEnabled(true);

            keyboardHelper.setKeyboardEnabled(true);
        });

        materialDatePicker.addOnNegativeButtonClickListener(view ->
        {
            chosenDate=null;
            chosenTime=null;

            mainBinding.timeButton.setVisibility(View.GONE);
            mainBinding.addCardDeadline.setVisibility(View.GONE);
            mainBinding.divider1.setVisibility(View.GONE);
            mainBinding.remindButton.setVisibility(View.GONE);
            mainBinding.remindButton.setEnabled(false);

            keyboardHelper.setKeyboardEnabled(true);
        });

        materialDatePicker.addOnCancelListener(dialog->keyboardHelper.setKeyboardEnabled(true));

        materialDatePicker.show(getSupportFragmentManager(), "null");
    }

    public void selectTime(View timeButton)
    {
        MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(chosenTime==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):chosenTime.getHour())
                .setMinute(chosenTime==null?Calendar.getInstance().get(Calendar.MINUTE):chosenTime.getMinute())
                .build();

        materialTimePicker.addOnPositiveButtonClickListener(view ->
        {
            chosenTime=new Time(materialTimePicker.getHour(), materialTimePicker.getMinute());
            mainBinding.addCardTime.setVisibility(View.VISIBLE);
            mainBinding.addCardTime.setText(chosenTime.toString());

            keyboardHelper.setKeyboardEnabled(true);
        });

        materialTimePicker.addOnNegativeButtonClickListener(dialog ->
        {
            mainBinding.addCardTime.setVisibility(View.GONE);
            chosenTime=null;

            keyboardHelper.setKeyboardEnabled(true);
        });

        materialTimePicker.addOnCancelListener(dialog->keyboardHelper.setKeyboardEnabled(true));

        materialTimePicker.show(getSupportFragmentManager(), null);
        materialTimePicker.getParentFragmentManager().executePendingTransactions();
        materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_ok_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.apply));
        materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_cancel_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.delete));
    }

    public void selectColor(View colorButton)
    {
        PopupMenu popupMenu=new PopupMenu(getApplicationContext(), colorButton);
        popupMenu.getMenuInflater().inflate(R.menu.color_change_menu, popupMenu.getMenu());

        try
        {
            //Complex reflection stuff to achieve that in lower android versions the color images in the popup menu are shown
            Field fieldPopup=popupMenu.getClass().getDeclaredField("mPopup");
            fieldPopup.setAccessible(true);
            fieldPopup.get(popupMenu).getClass().getDeclaredMethod("setForceShowIcon", boolean.class).invoke(fieldPopup.get(popupMenu), true);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        popupMenu.setOnMenuItemClickListener((menuItem)->
        {
            switch(menuItem.getItemId())
            {
                case R.id.white: chosenColor=Color.WHITE; break;
                case R.id.yellow: chosenColor=Color.parseColor("#FFF9C4"); break;
                case R.id.orange: chosenColor=Color.parseColor("#FFE0B2"); break;
                case R.id.red: chosenColor=Color.parseColor("#FFCDD2"); break;
                case R.id.green: chosenColor=Color.parseColor("#DCEDC8"); break;
                case R.id.blue: chosenColor=Color.parseColor("#BBDEFB"); break;
                case R.id.purple: chosenColor=Color.parseColor("#E1BEE7"); break;
            }
            mainBinding.addCard.setCardBackgroundColor(chosenColor);
            return true;
        });
        popupMenu.show();
    }

    public void toggleReminding(View remindButton)
    {
        reminding=!reminding;
        Drawable drawable=reminding?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme());
        mainBinding.remindButton.setIcon(drawable);
    }

    public void changeSortDirection(MenuItem sortDirectionItem)
    {
        switch (dataAccessor.getSortDirection())
        {
            case UP:
            {
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
                dataAccessor.setSortDirection(Direction.DOWN);
            }
            break;
            case DOWN:
            case NONE:
            {
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, Main.this.getTheme()));
                dataAccessor.setSortDirection(Direction.UP);
            }
            break;
        }

        dataAccessor.sortEntries();
        entryAdapter.notifyDataSetChanged();
    }

    public void changeSortCriterion(MenuItem sortCriterionItem)
    {
        switch (dataAccessor.getSortCriterion()) {
            case TEXT: {
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, Main.this.getTheme()));
                dataAccessor.setSortCriterion(Criterion.DEADLINE);
            }
            break;
            case DEADLINE: {
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, Main.this.getTheme()));
                dataAccessor.setSortCriterion(Criterion.COLOR);
            }
            break;
            case COLOR:
            case NONE: {
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, Main.this.getTheme()));
                dataAccessor.setSortCriterion(Criterion.TEXT);

                Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_downward);
                drawable.setAlpha(255);
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(drawable);
                mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(true);
                dataAccessor.setSortDirection(Direction.DOWN);
            }
            break;
        }

        if (dataAccessor.getSortDirection() == Direction.NONE) {
            mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
            dataAccessor.setSortDirection(Direction.DOWN);
        }

        dataAccessor.sortEntries();
        entryAdapter.notifyDataSetChanged();
    }

    public void start(View floatingActionButton)
    {
        int duration=400;
        int interpolatorFactor=2;

        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(floatingActionButton, "scaleY", 1.5F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(floatingActionButton, "scaleX", 5F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(floatingActionButton, "alpha", 0F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(floatingActionButton,"visibility", View.GONE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));

        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(mainBinding.addCard,"visibility", View.VISIBLE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(mainBinding.addCard, "scaleY", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(mainBinding.addCard, "scaleX", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(mainBinding.addCard, "alpha", 0F, 1F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(mainBinding.addCard, "radius", 100, 4).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardVisibility).with(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius);
        addCardAnimatorSet.setStartDelay((long) (duration/1.2));
        addCardAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));


        addCardAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation)
            {
                mainBinding.contentEditText.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mainBinding.contentEditText.setEnabled(true);

                mainBinding.bottomAppBar.performHide();
                mainBinding.bottomAppBar.setVisibility(View.GONE);

                InputMethodManager inputMethodManager = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                mainBinding.contentEditText.requestFocus();
            }

            @Override public void onAnimationCancel(Animator animation){}
            @Override public void onAnimationRepeat(Animator animation){}
        });

        floatingActionButtonAnimatorSet.start();
        addCardAnimatorSet.start();
    }

    public void addCard(View addButton)
    {
        Entry entry = new Entry();
        System.out.println(entry.getId());
        entry.setContent(mainBinding.contentEditText.getText().toString());
        entry.setNotifying(reminding);
        if (chosenColor != -1) entry.setColor(chosenColor);

        if (chosenDate != null) {
            entry.setDate(chosenDate);
            if (chosenTime != null) entry.setTime(chosenTime);
        }

        if (reminding) {
            if (entry.isInPast(dataAccessor.getAlldayTime()))
            {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.root), this.getResources().getString(R.string.past_notification), BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
                snackbar.setAnchorView(R.id.addCard);
                snackbar.show();
                return;
            }
            notificationHelper.planAndSendNotification(entry);
        }

        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme()));
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
        dataAccessor.setSortDirection(Direction.NONE);
        dataAccessor.setSortCriterion(Criterion.NONE);

        dataAccessor.addEntry(entry);
        entryAdapter.notifyItemInserted(dataAccessor.getEntriesSize());

        toggleSortability();
        mainBinding.contentEditText.getText().clear();
        mainBinding.addCardDeadline.setVisibility(View.GONE);
        mainBinding.timeButton.setVisibility(View.GONE);
        mainBinding.addCardTime.setText("");
        mainBinding.divider1.setVisibility(View.GONE);
        mainBinding.remindButton.setVisibility(View.GONE);
        mainBinding.remindButton.setIcon(getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme()));
        reminding = false;
        chosenDate = null;
        chosenTime = null;

        mainBinding.myRecyclerView.scrollToPosition(dataAccessor.getEntriesSize());
    }

    /*public void itemTouchHelperEnabled(boolean mode)
    {
        if(mode)
        {
            itemTouchHelper.attachToRecyclerView(mainBinding.myRecyclerView);
        }
        else
        {
            itemTouchHelper.attachToRecyclerView(null);
        }
    }*/
}