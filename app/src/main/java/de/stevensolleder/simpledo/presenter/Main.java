package de.stevensolleder.simpledo.presenter;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private IDataAccessor dataAccessor;
    private ISortSettingsAccessor sortSettingsAccessor;
    private IReminderSettingsAccessor reminderSettingsAccessor;

    private EntryAdapter entryAdapter;
    private ItemTouchHelper itemTouchHelper;

    private INotificationHelper notificationHelper;
    private KeyboardHelper keyboardHelper;

    private BottomBarAnimator bottomBarAnimator;

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

        //Create Accessors and Helpers
        dataAccessor=new DataAccessor(SimpleDo.getAppContext());
        sortSettingsAccessor=new SortSettingsAccessor(SimpleDo.getAppContext());
        reminderSettingsAccessor=new ReminderSettingsAccessor(SimpleDo.getAppContext());
        keyboardHelper=new KeyboardHelper(this);
        keyboardHelper.setKeyboardEnabled(false);

        //Set attributes from entryRecyclerView and set Adapter
        entryAdapter=new EntryAdapter(this, dataAccessor, reminderSettingsAccessor);
        mainBinding.recyclerView.setHasFixedSize(true);
        mainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.recyclerView.setVerticalScrollBarEnabled(false);
        mainBinding.recyclerView.setAdapter(entryAdapter);

        //Create and set Animators
        mainBinding.recyclerView.setItemAnimator(new EntryListAnimator());
        bottomBarAnimator=new BottomBarAnimator(mainBinding.floatingActionButton, mainBinding.bottomAppBar, mainBinding.addCard);

        //Create notificationChannel for reminders
        notificationHelper=new NotificationHelper();
        notificationHelper.createNotificationChannel("main", SimpleDo.getAppContext().getResources().getString(R.string.reminders), SimpleDo.getAppContext().getResources().getString(R.string.reminders_description), NotificationManager.IMPORTANCE_HIGH);

        //Set swipe gestures
        itemTouchHelper=new ItemTouchHelper(new CustomItemTouchHelperCallback(this, dataAccessor, reminderSettingsAccessor, notificationHelper));
        itemTouchHelper.attachToRecyclerView(mainBinding.recyclerView);

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

        //Set up UI
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

        //Set onBackPress functionality
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed()
            {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                bottomBarAnimator.decreaseAndHideAddCard(400, 2);
                bottomBarAnimator.showFloatingActionButton(400, 2);
            }
        });
    }

    private void start()
    {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        bottomBarAnimator.increaseAndHideFloatingActionButton(400, 2);
        bottomBarAnimator.showAddCard(400, 2);
    }

    private void selectDate()
    {
        keyboardHelper.setKeyboardEnabled(false);
        DateTimeConverter dateTimeConverter=new DateTimeConverter();

        MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                .datePicker()
                .setTheme(R.style.MaterialCalendarTheme)
                .setSelection(chosenDate==null?Calendar.getInstance().getTimeInMillis(): dateTimeConverter.fromDateInMillis(chosenDate))
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(selection ->
        {
            chosenDate=dateTimeConverter.fromMillisInDate(selection);

            deadlineSectionEnabled(true);
            keyboardHelper.setKeyboardEnabled(true);
        });

        materialDatePicker.addOnNegativeButtonClickListener(view ->
        {
            chosenDate=null;
            chosenTime=null;

            deadlineSectionEnabled(false);
            keyboardHelper.setKeyboardEnabled(true);
        });

        materialDatePicker.addOnCancelListener(dialog->keyboardHelper.setKeyboardEnabled(true));

        materialDatePicker.show(getSupportFragmentManager(), "null");
    }

    private void deadlineSectionEnabled(boolean enabled)
    {
        mainBinding.divider1.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.timeButton.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.remindButton.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.remindButton.setIcon(reminding?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme()));
        mainBinding.remindButton.setEnabled(enabled);

        mainBinding.addCardDeadline.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.addCardDate.setText((enabled && chosenDate!=null)?chosenDate.toString():"");
        mainBinding.addCardTime.setText((enabled && chosenTime!=null)?chosenTime.toString():"");
    }

    private void selectTime()
    {
        keyboardHelper.setKeyboardEnabled(false);

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
            chosenTime=null;
            mainBinding.addCardTime.setVisibility(View.GONE);

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
        mainBinding.remindButton.setIcon(reminding?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme()));
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
                showSnackbar(this.getResources().getString(R.string.past_notification), BaseTransientBottomBar.LENGTH_SHORT, null);
                return;
            }
            notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime()!=null?entry.getTime():reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
        }

        dataAccessor.addEntry(entry);
        entryAdapter.notifyItemInserted(dataAccessor.getEntriesSize());
        mainBinding.recyclerView.scrollToPosition(dataAccessor.getEntriesSize());

        enableSortability(dataAccessor.getEntriesSize()<=1?false:true);
        resetSortability();

        reminding=false;
        chosenDate=null;
        chosenTime=null;
        mainBinding.contentEditText.getText().clear();
        deadlineSectionEnabled(false);
    }

    public void showSnackbar(@NonNull String message, int length, @Nullable View.OnClickListener action)
    {
        Snackbar snackbar = Snackbar.make(mainBinding.root, message, length);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        snackbar.setAnchorView((mainBinding.addCard.getVisibility() == View.VISIBLE)?mainBinding.addCard:mainBinding.floatingActionButton);
        if(action!=null) snackbar.setAction(SimpleDo.getAppContext().getResources().getString(R.string.undo), action);

        snackbar.show();
    }

    public void itemTouchHelperEnabled(boolean enabled)
    {
        if(enabled) itemTouchHelper.attachToRecyclerView(mainBinding.recyclerView);
        else itemTouchHelper.attachToRecyclerView(null);
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