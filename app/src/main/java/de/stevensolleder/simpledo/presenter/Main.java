package de.stevensolleder.simpledo.presenter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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



public class Main extends AppCompatActivity
{
    private MainActivityBinding mainBinding;
    private IDataAccessor dataAccessor;
    private ISettingsAccessor settingsAccessor;

    private EntryAdapter entryAdapter;
    private ItemTouchHelper itemTouchHelper;
    private INotificationHelper notificationHelper;
    private KeyboardHelper keyboardHelper;
    private ColorHelper colorHelper;

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
        dataAccessor=new DataAccessor(SimpleDo.getAppContext());
        settingsAccessor=new SettingsAccessor(SimpleDo.getAppContext());
        colorHelper=new ColorHelper();

        //Set attributes from entryRecyclerView and set Adapter
        entryAdapter=new EntryAdapter(this, dataAccessor, settingsAccessor);
        mainBinding.myRecyclerView.setHasFixedSize(true);
        mainBinding.myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.myRecyclerView.setVerticalScrollBarEnabled(false);
        mainBinding.myRecyclerView.setAdapter(entryAdapter);

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

        //Create and set Animator
        EntryListAnimator entryListAnimator=new EntryListAnimator();
        mainBinding.myRecyclerView.setItemAnimator(entryListAnimator);

        //Create notificationChannel for reminders
        notificationHelper=new NotificationHelper(settingsAccessor, dataAccessor);
        notificationHelper.createNotificationChannel();

        //Setting swipe gestures
        itemTouchHelper=new ItemTouchHelper(new CustomItemTouchHelperCallback(this, mainBinding, entryAdapter, dataAccessor, settingsAccessor, notificationHelper));
        itemTouchHelper.attachToRecyclerView(mainBinding.myRecyclerView);

        //Setting up UI
        setupLayout();
        keyboardHelper=new KeyboardHelper(this);
        keyboardHelper.setKeyboardEnabled(false);
        mainBinding.floatingActionButton.setOnClickListener((view)->start(view));
        mainBinding.dateButton.setOnClickListener((view)->selectDate(view));
        mainBinding.timeButton.setOnClickListener((view)->selectTime(view));
        mainBinding.colorButton.setOnClickListener((view)->selectColor(view));
        mainBinding.remindButton.setOnClickListener((view)->toggleReminding(view));
        mainBinding.addButton.setOnClickListener((view)->addCard(view));
        mainBinding.bottomAppBar.setNavigationOnClickListener((view)->startActivity(new Intent(this, SettingsActivity.class)));
        mainBinding.bottomAppBar.findViewById(R.id.sortDirectionButton).setOnClickListener((view)->changeSortDirection(view));
        mainBinding.bottomAppBar.findViewById(R.id.sortCriterionButton).setOnClickListener((view)->changeSortCriterion(view));
    }


    @Override
    public void onBackPressed()
    {
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
        PopupMenu colorMenu=new PopupMenu(this, colorButton);
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

    public void toggleReminding(View remindButton)
    {
        reminding=!reminding;
        Drawable drawable=reminding?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme());
        mainBinding.remindButton.setIcon(drawable);
    }

    //Disables sortability if there is only one or no entry, enables it if there are two or more entries
    public void toggleSortability()
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

    public void changeSortDirection(View sortDirectionItem)
    {
        switch (settingsAccessor.getSortDirection())
        {
            case UP:
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
                settingsAccessor.setSortDirection(Direction.DOWN);
                break;
            case DOWN: case NONE:
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, Main.this.getTheme()));
            settingsAccessor.setSortDirection(Direction.UP);
                break;
        }

        dataAccessor.sortEntries(settingsAccessor.getSortCriterion(), settingsAccessor.getSortDirection());
        entryAdapter.notifyDataSetChanged();
    }

    public void changeSortCriterion(View sortCriterionItem)
    {
        switch (settingsAccessor.getSortCriterion()) {
            case TEXT:
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, Main.this.getTheme()));
                settingsAccessor.setSortCriterion(Criterion.DEADLINE);
                break;
            case DEADLINE:
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, Main.this.getTheme()));
                settingsAccessor.setSortCriterion(Criterion.COLOR);
                break;
            case COLOR:
            case NONE:
                mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, Main.this.getTheme()));
                settingsAccessor.setSortCriterion(Criterion.TEXT);
                Drawable drawable = getResources().getDrawable(R.drawable.ic_arrow_downward);
                drawable.setAlpha(255);
                mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(drawable);
                mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(true);
                settingsAccessor.setSortDirection(Direction.DOWN);
                break;
        }

        if (settingsAccessor.getSortDirection() == Direction.NONE)
        {
            mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
            settingsAccessor.setSortDirection(Direction.DOWN);
        }

        dataAccessor.sortEntries(settingsAccessor.getSortCriterion(), settingsAccessor.getSortDirection());
        entryAdapter.notifyDataSetChanged();
    }



    public void addCard(View addButton)
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
            if (entry.isInPast(settingsAccessor.getAlldayTime()))
            {
                Snackbar snackbar = Snackbar.make(findViewById(R.id.root), this.getResources().getString(R.string.past_notification), BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
                snackbar.setAnchorView(R.id.addCard);
                snackbar.show();
                return;
            }
            notificationHelper.planAndSendNotification(entry.getTime(), entry.getDate(), entry.getContent(), entry.getId());
        }

        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme()));
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
        settingsAccessor.setSortDirection(Direction.NONE);
        settingsAccessor.setSortCriterion(Criterion.NONE);

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

    public void itemTouchHelperEnabled(boolean enabled)
    {
        if(enabled) itemTouchHelper.attachToRecyclerView(mainBinding.myRecyclerView);
        else itemTouchHelper.attachToRecyclerView(null);
    }

    public void softInputAdjustmentEnabled(boolean enabled)
    {
        if (enabled) getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        else getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    private void setupLayout()
    {
        switch(settingsAccessor.getSortDirection())
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

        switch(settingsAccessor.getSortCriterion())
        {
            case TEXT: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_alpha, this.getTheme())); break;
            case DEADLINE: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_clock, this.getTheme())); break;
            case COLOR: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_palette, this.getTheme())); break;
            case NONE: mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(getResources().getDrawable(R.drawable.ic_sort, this.getTheme())); break;
        }
        toggleSortability();
    }
}