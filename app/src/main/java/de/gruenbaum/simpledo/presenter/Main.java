package de.gruenbaum.simpledo.presenter;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.function.BiConsumer;

import de.gruenbaum.simpledo.*;
import de.gruenbaum.simpledo.databinding.MainActivityBinding;
import de.gruenbaum.simpledo.model.*;
import de.gruenbaum.simpledo.presenter.notifications.INotificationHelper;
import de.gruenbaum.simpledo.presenter.notifications.NotificationHelper;
import de.gruenbaum.simpledo.presenter.recyclerview.CustomItemTouchHelperCallback;
import de.gruenbaum.simpledo.presenter.recyclerview.EntryAdapter;
import de.gruenbaum.simpledo.presenter.recyclerview.EntryListAnimator;


public class Main extends AppCompatActivity
{
    private IDataAccessor dataAccessor;
    private ISortSettingsAccessor sortSettingsAccessor;
    private IReminderSettingsAccessor reminderSettingsAccessor;
    private INotificationHelper notificationHelper;


    private MainActivityBinding mainBinding;
    private boolean datePickerShown;
    private boolean timePickerShown;
    private Date chosenDate;
    private Time chosenTime;
    private Color chosenColor;
    private boolean reminding;
    private Snackbar currentSnackbar;


    private EntryAdapter entryAdapter;
    private ItemTouchHelper itemTouchHelper;



    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding=MainActivityBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        dataAccessor=new DataAccessor(SimpleDo.getAppContext());
        sortSettingsAccessor=new SortSettingsAccessor(SimpleDo.getAppContext());
        reminderSettingsAccessor=new ReminderSettingsAccessor(SimpleDo.getAppContext());
        notificationHelper=new NotificationHelper();
        notificationHelper.createNotificationChannel("main", SimpleDo.getAppContext().getResources().getString(R.string.reminders), SimpleDo.getAppContext().getResources().getString(R.string.reminders_description), NotificationManager.IMPORTANCE_HIGH);

        datePickerShown=false;
        timePickerShown=false;
        chosenDate=null;
        chosenTime=null;
        chosenColor=Color.DEFAULT;
        reminding=false;


        mainBinding.bottomAppBar.setNavigationOnClickListener(this::startSettings);
        mainBinding.bottomAppBar.findViewById(R.id.sortDirectionButton).setOnClickListener(this::sortEntriesByDirection);
        mainBinding.bottomAppBar.findViewById(R.id.sortCriterionButton).setOnClickListener(this::sortEntriesByCriterion);

        mainBinding.recyclerView.setHasFixedSize(true);
        mainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainBinding.recyclerView.setVerticalScrollBarEnabled(false);
        entryAdapter=new EntryAdapter(this, dataAccessor, reminderSettingsAccessor);
        mainBinding.recyclerView.setAdapter(entryAdapter);
        mainBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        itemTouchHelper=new ItemTouchHelper(new CustomItemTouchHelperCallback(this, dataAccessor, reminderSettingsAccessor, notificationHelper));
        itemTouchHelper.attachToRecyclerView(mainBinding.recyclerView);
    }


    public void sortEntriesByDirection(View view) {
        switch (sortSettingsAccessor.getSortDirection())
        {
            case UP: sortSettingsAccessor.setSortDirection(Direction.DOWN); break;
            case DOWN: case NONE: sortSettingsAccessor.setSortDirection(Direction.UP); break;
        }

        if (sortSettingsAccessor.getSortCriterion() == Criterion.NONE)
        {
            setSortCriterionIcon(Criterion.TEXT);
            sortSettingsAccessor.setSortCriterion(Criterion.TEXT);
        }

        dataAccessor.sortEntries(sortSettingsAccessor.getSortCriterion(), sortSettingsAccessor.getSortDirection());

        setSortDirectionIcon(sortSettingsAccessor.getSortDirection());
        refreshList();
    }

    public void sortEntriesByCriterion(View view) {
        switch (sortSettingsAccessor.getSortCriterion())
        {
            case TEXT: sortSettingsAccessor.setSortCriterion(Criterion.DEADLINE); break;
            case DEADLINE: sortSettingsAccessor.setSortCriterion(Criterion.COLOR); break;
            case NONE: enableSortability(true);
            case COLOR: sortSettingsAccessor.setSortCriterion(Criterion.TEXT); break;
        }

        if (sortSettingsAccessor.getSortDirection() == Direction.NONE)
        {
            setSortDirectionIcon(Direction.DOWN);
            sortSettingsAccessor.setSortDirection(Direction.DOWN);
        }

        dataAccessor.sortEntries(sortSettingsAccessor.getSortCriterion(), sortSettingsAccessor.getSortDirection());
        setSortCriterionIcon(sortSettingsAccessor.getSortCriterion());
        refreshList();
    }

    public void setDate(View view) {
        DateTimeConverter dateTimeConverter=new DateTimeConverter();

        showDatePicker(chosenDate==null? Calendar.getInstance().getTimeInMillis(): dateTimeConverter.fromDateInMillis(chosenDate),
                (selection) ->
                {
                    if(chosenDate==null) deadlineSectionEnabled(true);

                    chosenDate=dateTimeConverter.fromMillisInDate(selection);
                    updateDeadlineText(chosenDate, chosenTime);

                    showKeyboard();
                },
                () ->
                {
                    chosenDate=null;
                    chosenTime=null;

                    deadlineSectionEnabled(false);
                    updateDeadlineText(null, null);

                    showKeyboard();
                }
        );
    }

    public void setTime(View view) {
        showTimePicker(chosenTime==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):chosenTime.getHour(), chosenTime==null?Calendar.getInstance().get(Calendar.MINUTE):chosenTime.getMinute(),
                (hour, minute) ->
                {
                    chosenTime=new Time(hour, minute);
                    updateDeadlineText(chosenDate, chosenTime);

                    showKeyboard();
                },
                ()->
                {
                    chosenTime=null;
                    updateDeadlineText(chosenDate, chosenTime);

                    showKeyboard();
                }
        );
    }

    public void setColor(View view) {
        showColorPicker((color ->
        {
            chosenColor=color;
            ObjectAnimator
            .ofArgb(
                    mainBinding.addCard,
                    "cardBackgroundColor",
                    mainBinding.addCard.getCardBackgroundColor().getColorForState(mainBinding.addCard.getDrawableState(),
                    new ColorHelper().convertColorToInteger(Color.DEFAULT)), new ColorHelper().convertColorToInteger(chosenColor)
            )
            .setDuration(200)
            .start();
        }));
    }

    public void toggleReminding(View view) {
        reminding=!reminding;
        setReminderButtonIcon(reminding);
    }

    public void addEntry(View view) {
        Entry entry = new Entry(new IdentificationHelper().createUniqueId());
        entry.setContent(mainBinding.contentEditText.getText().toString());
        entry.setNotifying(reminding);

        if (chosenColor != Color.DEFAULT) entry.setColor(chosenColor);

        if (chosenDate != null)
        {
            entry.setDate(chosenDate);
            if (chosenTime != null) entry.setTime(chosenTime);
        }

        if (reminding)
        {
            if (entry.isInPast(reminderSettingsAccessor.getAlldayTime()))
            {
                showSnackbar(SimpleDo.getAppContext().getResources().getString(R.string.past_notification), BaseTransientBottomBar.LENGTH_SHORT, null);
                return;
            }
            notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime()!=null?entry.getTime():reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
        }

        dataAccessor.addEntry(entry);
        entryAdapter.notifyItemInserted(dataAccessor.getEntriesSize());
        mainBinding.recyclerView.scrollToPosition(dataAccessor.getEntriesSize());

        enableSortability(dataAccessor.getEntriesSize()<=1?false:true);
        setSortDirectionIcon(Direction.NONE);
        setSortCriterionIcon(Criterion.NONE);

        reminding=false;
        chosenDate=null;
        chosenTime=null;
        mainBinding.contentEditText.setText("");
        deadlineSectionEnabled(false);
        updateDeadlineText(chosenDate, chosenTime);
    }

    public void startInput(View view) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        openCard(400, 2);
        if(currentSnackbar!=null) currentSnackbar.dismiss();
    }

    public void startSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Override public void onBackPressed() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        closeCard(400, 2);
        if(currentSnackbar!=null) currentSnackbar.dismiss();
    }



    public void showSnackbar(@NonNull String message, int length, @Nullable Runnable action) {
        currentSnackbar = Snackbar.make(mainBinding.root, message, length);
        currentSnackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        currentSnackbar.setAnchorView((mainBinding.addCard.getVisibility() == View.VISIBLE)?mainBinding.addCard:mainBinding.floatingActionButton);
        if(action!=null) currentSnackbar.setAction(SimpleDo.getAppContext().getResources().getString(R.string.undo), (view)->{action.run();});

        currentSnackbar.show();
    }

    public void itemTouchHelperEnabled(boolean enabled) {
        if(enabled) itemTouchHelper.attachToRecyclerView(mainBinding.recyclerView);
        else itemTouchHelper.attachToRecyclerView(null);
    }

    public void showDatePicker(long selection, Consumer<Long> onPositiveButtonClickAction, Runnable onNegativeButtonClickAction) {
        if(datePickerShown) return;

        hideKeyboard();

        MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                .datePicker()
                .setTheme(R.style.MaterialCalendarTheme)
                .setSelection(selection)
                .build();

        materialDatePicker.addOnPositiveButtonClickListener(currentSelection ->
        {
            onPositiveButtonClickAction.accept(currentSelection);
            datePickerShown=false;
        });

        materialDatePicker.addOnNegativeButtonClickListener((view)->
        {
            onNegativeButtonClickAction.run();
            datePickerShown=false;
        });

        materialDatePicker.addOnCancelListener(dialog -> datePickerShown=false);

        materialDatePicker.show(getSupportFragmentManager(), "null");
        datePickerShown=true;
    }

    public void showTimePicker(@IntRange(from = 0L, to = 23L) int hour, @IntRange(from = 0L, to = 60L) int minute, BiConsumer<Integer, Integer> onPositiveButtonClickAction, Runnable onNegativeButtonClickAction) {
        if(timePickerShown) return;

        hideKeyboard();

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
            onNegativeButtonClickAction.run();
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

    public void enableSortability(boolean enabled) {
        Drawable directionDrawable=mainBinding.bottomAppBar.getMenu().getItem(0).getIcon();
        Drawable criterionDrawable=mainBinding.bottomAppBar.getMenu().getItem(1).getIcon();

        directionDrawable.setAlpha(enabled?255:128);
        criterionDrawable.setAlpha(enabled?255:128);

        mainBinding.bottomAppBar.getMenu().getItem(0).setIcon(directionDrawable);
        mainBinding.bottomAppBar.getMenu().getItem(1).setIcon(criterionDrawable);

        mainBinding.bottomAppBar.getMenu().getItem(0).setEnabled(enabled);
        mainBinding.bottomAppBar.getMenu().getItem(1).setEnabled(enabled);
    }

    public void setSortDirectionIcon(Direction direction) {
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

    public void setSortCriterionIcon(Criterion criterion) {
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

    private void setReminderButtonIcon(boolean enabled) {
        mainBinding.remindButton.setIcon(enabled?getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme()):getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme()));
    }

    private void deadlineSectionEnabled(boolean enabled) {
        mainBinding.divider1.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.timeButton.setVisibility(enabled?View.VISIBLE:View.GONE);
        mainBinding.remindButton.setVisibility(enabled?View.VISIBLE:View.GONE);
        setReminderButtonIcon(false);
        mainBinding.remindButton.setEnabled(enabled);

        mainBinding.addCardDeadline.setVisibility(enabled?View.VISIBLE:View.GONE);
    }

    private void updateDeadlineText(@Nullable Date newDate, @Nullable Time newTime) {
        mainBinding.addCardDate.setText((newDate!=null)?newDate.toString():"");
        mainBinding.addCardTime.setText((newTime!=null)?newTime.toString():"");
    }

    public void hideKeyboard() {
        if(!KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(this)) return;

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        if(KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(this)) return;

        mainBinding.contentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void refreshList() {
        entryAdapter.notifyItemRangeChanged(0, entryAdapter.getItemCount());
    }

    private void showColorPicker(Consumer<Color> onClickAction) {
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

    private void openCard(int duration, int interpolatorFactor) {
        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleY", 1.5F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleX", 5F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "alpha", 0F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(mainBinding.floatingActionButton,"visibility", View.GONE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));

        floatingActionButtonAnimatorSet.start();


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
                mainBinding.addCard.findViewById(R.id.contentEditText).setEnabled(false);
                mainBinding.bottomAppBar.performHide();
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mainBinding.addCard.findViewById(R.id.contentEditText).setEnabled(true);

                InputMethodManager inputMethodManager = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                mainBinding.addCard.findViewById(R.id.contentEditText).requestFocus();
            }

            @Override public void onAnimationCancel(Animator animation){}
            @Override public void onAnimationRepeat(Animator animation){}
        });

        addCardAnimatorSet.start();
    }

    private void closeCard(int duration, int interpolatorFactor) {
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
                mainBinding.addCard.findViewById(R.id.contentEditText).setEnabled(false);
            }

            @Override public void onAnimationEnd(Animator animation){}
            @Override public void onAnimationCancel(Animator animation){}
            @Override public void onAnimationRepeat(Animator animation){}
        });

        addCardAnimatorSet.start();


        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleY", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "scaleX", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(mainBinding.floatingActionButton, "alpha", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(mainBinding.floatingActionButton, "visibility", View.VISIBLE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setStartDelay((long) (duration / 1.2));
        floatingActionButtonAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));

        floatingActionButtonAnimatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation)
            {
                mainBinding.addCard.findViewById(R.id.contentEditText).setEnabled(true);
                mainBinding.bottomAppBar.performShow();
                mainBinding.bottomAppBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        floatingActionButtonAnimatorSet.start();
    }
}