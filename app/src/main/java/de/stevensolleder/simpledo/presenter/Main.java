package de.stevensolleder.simpledo.presenter;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;

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

    private EntryAdapter entryAdapter;
    private ItemTouchHelper itemTouchHelper;

    private KeyboardHelper keyboardHelper;

    private BottomBarAnimator bottomBarAnimator;

    private boolean datePickerShown;
    private boolean timePickerShown;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Set up activity
        {
            //Initialize main_activity.xml
            super.onCreate(savedInstanceState);
            mainBinding=MainActivityBinding.inflate(getLayoutInflater());
            setContentView(mainBinding.getRoot());

            //Set default values for attributes
            datePickerShown=false;
            timePickerShown=false;

            //Create Helpers for activity and others
            keyboardHelper=new KeyboardHelper(this);
            keyboardHelper.setKeyboardEnabled(false);
        }


        DataController dataController;

        //Create helpers for other classes and initialize them
        {
            IDataAccessor dataAccessor=new DataAccessor(SimpleDo.getAppContext());
            ISortSettingsAccessor sortSettingsAccessor=new SortSettingsAccessor(SimpleDo.getAppContext());
            IReminderSettingsAccessor reminderSettingsAccessor=new ReminderSettingsAccessor(SimpleDo.getAppContext());

            //Create notificationChannel for reminders
            INotificationHelper notificationHelper=new NotificationHelper();
            notificationHelper.createNotificationChannel("main", SimpleDo.getAppContext().getResources().getString(R.string.reminders), SimpleDo.getAppContext().getResources().getString(R.string.reminders_description), NotificationManager.IMPORTANCE_HIGH);

            //Set attributes from entryRecyclerView and set Adapter
            entryAdapter=new EntryAdapter(this, dataAccessor, reminderSettingsAccessor);
            mainBinding.recyclerView.setHasFixedSize(true);
            mainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
            mainBinding.recyclerView.setVerticalScrollBarEnabled(false);
            mainBinding.recyclerView.setAdapter(entryAdapter);

            dataController=new DataController(this, dataAccessor, sortSettingsAccessor);

            //Set swipe gestures
            itemTouchHelper=new ItemTouchHelper(new CustomItemTouchHelperCallback(this, dataAccessor, reminderSettingsAccessor, notificationHelper));
            itemTouchHelper.attachToRecyclerView(mainBinding.recyclerView);

            //Create and set Animators
            mainBinding.recyclerView.setItemAnimator(new EntryListAnimator());
            bottomBarAnimator=new BottomBarAnimator(mainBinding.floatingActionButton, mainBinding.bottomAppBar, mainBinding.addCard);

            dataController.initialize();
        }

        //Set up UI actions
        {
            mainBinding.floatingActionButton.setOnClickListener((view) ->
            {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                bottomBarAnimator.increaseAndHideFloatingActionButton(400, 2);
                bottomBarAnimator.showAddCard(400, 2);
            });

            mainBinding.dateButton.setOnClickListener((view) -> dataController.setDate());
            mainBinding.timeButton.setOnClickListener((view) -> dataController.setTime());
            mainBinding.colorButton.setOnClickListener((view) -> dataController.setColor());
            mainBinding.remindButton.setOnClickListener((view) -> dataController.toggleReminding());
            mainBinding.addButton.setOnClickListener((view) -> dataController.addEntry());
            mainBinding.bottomAppBar.findViewById(R.id.sortDirectionButton).setOnClickListener((view) -> dataController.sortEntriesByDirection());
            mainBinding.bottomAppBar.findViewById(R.id.sortCriterionButton).setOnClickListener((view) -> dataController.sortEntriesByCriterion());
            mainBinding.bottomAppBar.setNavigationOnClickListener((view) -> startActivity(new Intent(this, SettingsActivity.class)));
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

    public void deadlineSectionEnabled(boolean enabled)
    {
        mainBinding.divider1.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.timeButton.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.remindButton.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.remindButton.setIcon(enabled?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme()));
        mainBinding.remindButton.setEnabled(enabled);

        mainBinding.addCardDeadline.setVisibility(enabled?View.VISIBLE:View.GONE);
    }

    public void updateDeadlineText(@Nullable Date newDate, @Nullable Time newTime)
    {
        mainBinding.addCardDate.setText((newDate!=null)?newDate.toString():"");
        mainBinding.addCardTime.setText((newTime!=null)?newTime.toString():"");
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

    public void showDatePicker(long selection, MaterialPickerOnPositiveButtonClickListener<Long> onPositiveButtonClickAction, View.OnClickListener onNegativeButtonClickAction)
    {
        if(datePickerShown) return;

        keyboardHelper.setKeyboardEnabled(false);

        MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                .datePicker()
                .setTheme(R.style.MaterialCalendarTheme)
                .setSelection(selection)
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(currentSelection ->
        {
            onPositiveButtonClickAction.onPositiveButtonClick(currentSelection);
            datePickerShown=false;
        });

        materialDatePicker.addOnNegativeButtonClickListener((view)->
        {
            onNegativeButtonClickAction.onClick(view);
            datePickerShown=false;
        });

        materialDatePicker.addOnCancelListener(dialog -> datePickerShown=false);
        
        materialDatePicker.show(getSupportFragmentManager(), "null");
        datePickerShown=true;
    }

    public void showTimePicker(@IntRange(from = 0L, to = 23L) int hour, @IntRange(from = 0L, to = 60L) int minute, BiConsumer<Integer, Integer> onPositiveButtonClickAction, View.OnClickListener onNegativeButtonClickAction)
    {
        if(timePickerShown) return;

        keyboardHelper.setKeyboardEnabled(false);

        MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .build();

        materialTimePicker.addOnPositiveButtonClickListener(view ->
        {
            onPositiveButtonClickAction.accept(materialTimePicker.getHour(), materialTimePicker.getMinute());
            timePickerShown=false;
        });

        materialTimePicker.addOnNegativeButtonClickListener(view ->
        {
            onNegativeButtonClickAction.onClick(view);
            timePickerShown=false;
        });

        materialTimePicker.addOnCancelListener(dialog->
        {
            timePickerShown=false;
        });

        materialTimePicker.show(getSupportFragmentManager(), "null");
        materialTimePicker.getFragmentManager().executePendingTransactions();
        materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_ok_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.apply));
        materialTimePicker.getView().<Button>findViewById(R.id.material_timepicker_cancel_button).setText(SimpleDo.getAppContext().getResources().getString(R.string.delete));
        timePickerShown=true;
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

    public void setSortDirectionIcon(Direction direction)
    {
        Drawable directionIcon;
        switch (direction)
        {
            case UP: directionIcon=getResources().getDrawable(R.drawable.ic_arrow_upward, this.getTheme()); break;
            case DOWN: directionIcon=getResources().getDrawable(R.drawable.ic_arrow_downward, this.getTheme()); break;
            case NONE: default: directionIcon = getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme()); break;
        }
        directionIcon.setAlpha(mainBinding.bottomAppBar.getMenu().getItem(0).isEnabled()?255:128);
        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(directionIcon);
    }

    public void setSortCriterionIcon(Criterion criterion)
    {
        Drawable criterionIcon;
        switch (criterion)
        {
            case TEXT: criterionIcon=getResources().getDrawable(R.drawable.ic_alpha, this.getTheme()); break;
            case DEADLINE: criterionIcon=getResources().getDrawable(R.drawable.ic_clock, this.getTheme()); break;
            case COLOR: criterionIcon=getResources().getDrawable(R.drawable.ic_palette, this.getTheme()); break;
            case NONE: default: criterionIcon=getResources().getDrawable(R.drawable.ic_sort, this.getTheme()); break;
        }
        criterionIcon.setAlpha(mainBinding.bottomAppBar.getMenu().getItem(1).isEnabled()?255:128);
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(criterionIcon);
    }

    public void setKeyboardEnabled(boolean enabled)
    {
        keyboardHelper.setKeyboardEnabled(enabled);
    }

    public void showColorPicker(Consumer<Color> onClickAction)
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
            onClickAction.accept(colorHelper.convertMenuItemColorToColor(menuItem));
            return true;
        });
        colorMenu.show();
    }

    public void changeAddCardColor(Color newValue)
    {
        mainBinding.addCard.setCardBackgroundColor(new ColorHelper().convertColorToInteger(newValue));
    }

    public void refreshList()
    {
        entryAdapter.notifyDataSetChanged();
    }

    public void refreshAddEntry(int size)
    {
        entryAdapter.notifyItemInserted(size);
    }

    public void scrollToEntry(int position)
    {
        mainBinding.recyclerView.scrollToPosition(position);
    }

    public String getCurrentText()
    {
        return mainBinding.contentEditText.getText().toString();
    }

    public void setCurrentText(String newValue)
    {
        mainBinding.contentEditText.setText(newValue);
    }

    public void enableReminderIcon(boolean enabled)
    {
        mainBinding.remindButton.setIcon(enabled?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme()));
    }
}