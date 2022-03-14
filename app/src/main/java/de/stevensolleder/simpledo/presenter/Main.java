package de.stevensolleder.simpledo.presenter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import de.stevensolleder.simpledo.presenter.notifications.INotificationHelper;
import de.stevensolleder.simpledo.presenter.notifications.NotificationHelper;
import de.stevensolleder.simpledo.presenter.recyclerview.CustomItemTouchHelperCallback;
import de.stevensolleder.simpledo.presenter.recyclerview.EntryAdapter;
import de.stevensolleder.simpledo.presenter.recyclerview.EntryListAnimator;


public class Main extends AppCompatActivity
{
    private MainActivityBinding mainBinding;
    private Date chosenDate=null;
    private Time chosenTime=null;
    private int chosenColor=-1;
    private boolean reminding=false;
    private IDataAccessor dataAccessor;
    private ISortSettingsAccessor sortSettingsAccessor;
    private IReminderSettingsAccessor reminderSettingsAccessor;
    private EntryAdapter entryAdapter;
    private ItemTouchHelper itemTouchHelper;
    private INotificationHelper notificationHelper;

    private KeyboardHelper keyboardHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Initialize main_activity.xml
        super.onCreate(savedInstanceState);
        mainBinding=MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        dataAccessor=new DataAccessor(SimpleDo.getAppContext());
        sortSettingsAccessor=new SortSettingsAccessor(SimpleDo.getAppContext());
        reminderSettingsAccessor=new ReminderSettingsAccessor(SimpleDo.getAppContext());

        //Set attributes from entryRecyclerView and set Adapter
        entryAdapter=new EntryAdapter(this, dataAccessor, reminderSettingsAccessor);
        mainBinding.myRecyclerView.setHasFixedSize(true);
        mainBinding.myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.myRecyclerView.setVerticalScrollBarEnabled(false);
        mainBinding.myRecyclerView.setAdapter(entryAdapter);

        //Setting swipe gestures
        itemTouchHelper=new ItemTouchHelper(new CustomItemTouchHelperCallback(this, dataAccessor, notificationHelper));
        itemTouchHelper.attachToRecyclerView(mainBinding.myRecyclerView);

        //Create and set Animator
        mainBinding.myRecyclerView.setItemAnimator(new EntryListAnimator());

        //Create notificationChannel for reminders
        notificationHelper=new NotificationHelper();
        notificationHelper.createNotificationChannel("main", SimpleDo.getAppContext().getResources().getString(R.string.reminders), SimpleDo.getAppContext().getResources().getString(R.string.reminders_description), NotificationManager.IMPORTANCE_HIGH);

        //React to theme changes
        Entry currentEntry;
        for(int index=0; index<dataAccessor.getEntries().size(); index++)
        {
            currentEntry=dataAccessor.getEntries().get(index);
            switch(currentEntry.getColor())
            {
                case  -14606047: case -1: currentEntry.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCardDefault)); break;
                case -3422573: case -1596: currentEntry.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCardYellow)); break;
                case -3428734: case -8014: currentEntry.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCardOrange)); break;
                case -3433311: case -12846: currentEntry.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCardRed)); break;
                case -5588073: case -2298424: currentEntry.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCardGreen)); break;
                case -7689016: case -4464901: currentEntry.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCardBlue)); break;
                case -5271883: case -1982745: currentEntry.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCardPurple)); break;
            }
            dataAccessor.changeEntry(index, currentEntry);
        }
        entryAdapter.notifyDataSetChanged();

        //Setting up UI
        {
            setupLayout();
            keyboardHelper = new KeyboardHelper(this);
            keyboardHelper.setKeyboardEnabled(false);
            mainBinding.floatingActionButton.setOnClickListener((view) -> start());
            mainBinding.dateButton.setOnClickListener((view) -> selectDate());
            mainBinding.timeButton.setOnClickListener((view) -> selectTime());
            mainBinding.colorButton.setOnClickListener((view) -> selectColor());
            mainBinding.remindButton.setOnClickListener((view) -> toggleReminding());
            mainBinding.addButton.setOnClickListener((view) -> addCard());
            mainBinding.bottomAppBar.setNavigationOnClickListener((view) -> startActivity(new Intent(this, SettingsActivity.class)));
            mainBinding.bottomAppBar.findViewById(R.id.sortDirectionButton).setOnClickListener((view) -> changeSortDirection());
            mainBinding.bottomAppBar.findViewById(R.id.sortCriterionButton).setOnClickListener((view) -> changeSortCriterion());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed()
            {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                int duration=400;
                int interpolatorFactor=2;

                ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleY", 1F).setDuration(duration);
                ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleX", 1F).setDuration(duration);
                ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "alpha", 1F).setDuration(duration);
                ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(mainBinding.floatingActionButton,"visibility", View.VISIBLE).setDuration(duration);

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
        });
    }


    private void start()
    {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        int duration=400;
        int interpolatorFactor=2;

        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleY", 1.5F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleX", 5F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "alpha", 0F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(mainBinding.floatingActionButton,"visibility", View.GONE).setDuration(duration);

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
                mainBinding.bottomAppBar.performHide();
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mainBinding.contentEditText.setEnabled(true);



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

    private void selectDate()
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

    private void selectTime()
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

    private void selectColor()
    {
        ColorHelper colorHelper=new ColorHelper();
        PopupMenu colorMenu=new PopupMenu(this, mainBinding.colorButton);
        colorMenu.getMenuInflater().inflate(R.menu.color_change_menu, colorMenu.getMenu());
        try
        {
            //Complex reflection stuff to achieve that in lower android versions the color images in the popup menu are shown
            Field fieldPopup=colorMenu.getClass().getDeclaredField("mPopup");
            fieldPopup.setAccessible(true);
            fieldPopup.get(colorMenu).getClass().getDeclaredMethod("setForceShowIcon", boolean.class).invoke(fieldPopup.get(colorMenu), true);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        colorHelper.setupThemeSpecificColorMenuIcons(colorMenu.getMenu());

        colorMenu.setOnMenuItemClickListener((menuItem)->
        {
            chosenColor=colorHelper.getMenuItemColor(menuItem);
            mainBinding.addCard.setCardBackgroundColor(chosenColor);
            return true;
        });
        colorMenu.show();
    }

    private void toggleReminding()
    {
        reminding=!reminding;
        Drawable drawable=reminding?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme());
        mainBinding.remindButton.setIcon(drawable);
    }

    private void changeSortDirection()
    {
        switch (sortSettingsAccessor.getSortDirection())
        {
            case UP:
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
                sortSettingsAccessor.setSortDirection(Direction.DOWN);
                break;
            case DOWN: case NONE:
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, Main.this.getTheme()));
            sortSettingsAccessor.setSortDirection(Direction.UP);
                break;
        }

        dataAccessor.sortEntries(sortSettingsAccessor.getSortCriterion(), sortSettingsAccessor.getSortDirection());
        entryAdapter.notifyDataSetChanged();
    }

    private void changeSortCriterion()
    {
        switch (sortSettingsAccessor.getSortCriterion()) {
            case TEXT:
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, Main.this.getTheme()));
                sortSettingsAccessor.setSortCriterion(Criterion.DEADLINE);
                break;
            case DEADLINE:
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, Main.this.getTheme()));
                sortSettingsAccessor.setSortCriterion(Criterion.COLOR);
                break;
            case COLOR:
            case NONE:
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, Main.this.getTheme()));
                sortSettingsAccessor.setSortCriterion(Criterion.TEXT);
                Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_downward);
                drawable.setAlpha(255);
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(drawable);
                mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(true);
                sortSettingsAccessor.setSortDirection(Direction.DOWN);
                break;
        }

        if (sortSettingsAccessor.getSortDirection() == Direction.NONE)
        {
            mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
            sortSettingsAccessor.setSortDirection(Direction.DOWN);
        }

        dataAccessor.sortEntries(sortSettingsAccessor.getSortCriterion(), sortSettingsAccessor.getSortDirection());
        entryAdapter.notifyDataSetChanged();
    }



    private void addCard()
    {
        Entry entry = new Entry(new IdentificationHelper().createUniqueId());
        entry.setContent(mainBinding.contentEditText.getText().toString());
        entry.setNotifying(reminding);
        if (chosenColor != -1) entry.setColor(chosenColor);

        if (chosenDate != null)
        {
            entry.setDate(chosenDate);
            if (chosenTime != null) entry.setTime(chosenTime);
        }

        if (reminding)
        {
            if (entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
            {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.root), this.getResources().getString(R.string.past_notification), BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
                snackbar.setAnchorView(R.id.addCard);
                snackbar.show();
                return;
            }
            notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime(),  entry.getContent(), entry.getId());
        }

        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme()));
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));

        dataAccessor.addEntry(entry);
        entryAdapter.notifyItemInserted(dataAccessor.getEntriesSize());

        if(dataAccessor.getEntriesSize()<=1)
        {
            enableSortability(false);
            resetSortability();
        }
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

    private void setupLayout()
    {
        switch(sortSettingsAccessor.getSortDirection())
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

        switch(sortSettingsAccessor.getSortCriterion())
        {
            case TEXT: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, this.getTheme())); break;
            case DEADLINE: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, this.getTheme())); break;
            case COLOR: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, this.getTheme())); break;
            case NONE: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, this.getTheme())); break;
        }

        //TODO
    }

    public void showSnackbar(String message, int length, View.OnClickListener action)
    {
        Snackbar snackbar = Snackbar.make(mainBinding.root, message, length);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);

        if (mainBinding.addCard.getVisibility() == View.VISIBLE)
            snackbar.setAnchorView(mainBinding.addCard);
        else snackbar.setAnchorView(mainBinding.floatingActionButton);

        snackbar.setAction(SimpleDo.getAppContext().getResources().getString(R.string.undo), action);

        snackbar.show();
    }

    public void itemTouchHelperEnabled(boolean enabled)
    {
        if(enabled) itemTouchHelper.attachToRecyclerView(mainBinding.myRecyclerView);
        else itemTouchHelper.attachToRecyclerView(null);
    }

    public void enableBottomAppBar(boolean enabled)
    {
        if(enabled)
        {
            mainBinding.bottomAppBar.setVisibility(View.VISIBLE);
            mainBinding.bottomAppBar.performShow();
            mainBinding.floatingActionButton.setVisibility(View.VISIBLE);
            mainBinding.floatingActionButton.show();
        }
        else
        {
            mainBinding.bottomAppBar.setVisibility(View.GONE);
            mainBinding.floatingActionButton.setVisibility(View.GONE);
            mainBinding.bottomAppBar.performHide();
            mainBinding.floatingActionButton.hide();
        }

        //if (enabled) getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //else getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    //Disables sortability if there is only one or no entry, enables it if there are two or more entries
    public void enableSortability(boolean enabled)
    {
        Drawable directionDrawable=mainBinding.bottomAppBar.getMenu().getItem(0).getIcon();
        Drawable criterionDrawable=mainBinding.bottomAppBar.getMenu().getItem(1).getIcon();

        directionDrawable.setAlpha(enabled?255:128);
        criterionDrawable.setAlpha(enabled?255:128);

        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(directionDrawable);
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(criterionDrawable);

        mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(enabled);
        mainBinding.bottomAppBar.getMenu().getItem(1).setEnabled(enabled);


    }

    //Marks that entries are not sorted
    public void resetSortability()
    {
        Drawable directionDrawable = getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
        Drawable criterionDrawable = getResources().getDrawable(R.drawable.ic_sort, this.getTheme());

        directionDrawable.setAlpha(mainBinding.bottomAppBar.getMenu().getItem(0).isEnabled()?255:128);
        criterionDrawable.setAlpha(mainBinding.bottomAppBar.getMenu().getItem(1).isEnabled()?255:128);

        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(directionDrawable);
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(criterionDrawable);

        sortSettingsAccessor.setSortDirection(Direction.NONE);
        sortSettingsAccessor.setSortCriterion(Criterion.NONE);
    }
}