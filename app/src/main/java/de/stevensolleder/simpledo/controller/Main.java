package de.stevensolleder.simpledo.controller;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.lang.reflect.Field;
import java.util.Calendar;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import de.stevensolleder.simpledo.*;
import de.stevensolleder.simpledo.model.*;

import static de.stevensolleder.simpledo.model.ColorHelper.*;
import static de.stevensolleder.simpledo.model.NotificationHelper.*;
import static de.stevensolleder.simpledo.model.SaveHelper.*;

public class Main extends AppCompatActivity
{
    private RecyclerView entryRecyclerView;
    private EntryRecyclerViewAdapter entryRecyclerViewAdapter;

    private MaterialCardView addCardMaterialCardView;
    private EditText addCardContentEditText;
    private MaterialButton addCardDatePickerMaterialButton;
    private MaterialButton addCardTimePickerMaterialButton;
    private MaterialButton addCardColorMenuMaterialButton; //edgar by alex
    private MaterialButton addCardRemindMaterialButton;
    private View addCardDivider;
    private LinearLayout addCardDeadlineLinearLayout;
    private TextView addCardDateTextView;
    private TextView addCardTimeTextView;

    private FloatingActionButton startFloatingActionButton;

    private MenuItem changeDirectionMaterialButton;
    private MenuItem changeCriterionMaterialButton;

    private BottomAppBar bottomAppBar;

    private ItemTouchHelper itemTouchHelper;

    private Date chosenDate=null;
    private Time chosenTime=null;
    private int chosenColor=-1;

    private boolean reminding=false;

    private boolean keyboardOpen;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Initialize main.xml
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Initialize keyboard listener
        KeyboardVisibilityEvent.setEventListener(
                Main.this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        keyboardOpen=isOpen;
                    }
                });

        //Initialize all attributes from main.xml
        entryRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        entryRecyclerViewAdapter=new EntryRecyclerViewAdapter(this);

        bottomAppBar=findViewById(R.id.bottomAppBar);
        changeDirectionMaterialButton=bottomAppBar.getMenu().getItem(0);
        changeCriterionMaterialButton=bottomAppBar.getMenu().getItem(1);

        addCardMaterialCardView=findViewById(R.id.addCard);
        addCardContentEditText=findViewById(R.id.contentEditText);
        addCardDatePickerMaterialButton=findViewById(R.id.dateButton);
        addCardTimePickerMaterialButton=findViewById(R.id.timeButton);
        addCardColorMenuMaterialButton=findViewById(R.id.colorButton);
        addCardDivider=findViewById(R.id.divider1);
        addCardDeadlineLinearLayout=findViewById(R.id.addCardDeadline);

        addCardDateTextView=findViewById(R.id.addCardDate);
        addCardTimeTextView=findViewById(R.id.addCardTime);

        startFloatingActionButton=findViewById(R.id.start);

        addCardRemindMaterialButton=findViewById(R.id.remindButton);

        //Set attributes from entryRecyclerView and set Adapter
        entryRecyclerView.setHasFixedSize(true);
        entryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entryRecyclerView.setVerticalScrollBarEnabled(false);
        entryRecyclerView.setAdapter(entryRecyclerViewAdapter);

        //Create and set Animator
        EntryListAnimator entryListAnimator=new EntryListAnimator();
        entryRecyclerView.setItemAnimator(entryListAnimator);

        //Create notifcationchannel for reminders
        createNotificationChannel();


        //Setting swipe gestures
        itemTouchHelper=new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            //onMove() is called when a dragged card is dropped
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                //Sometimes getContextMenu() returns null. To avoid a crash we use try-catch.
                try
                {
                    ((EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder)viewHolder).getContextMenu().close();
                }
                catch(Exception e){}

                entryRecyclerViewAdapter.moveEntry(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return viewHolder.getAdapterPosition()!=target.getAdapterPosition();
            }

            //distance contains how many cards were passed after dropping the card after dragging the card
            int distance=0;

            //onMoved() is called when a card is in drag mode and swapped the position with another card
            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y)
            {
                distance+=(fromPos-toPos);

                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }

            //onSwiped() is called when a card is swiped successfully to the left or to the right
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
            {
                Entry entry=getEntry(viewHolder.getAdapterPosition());
                int adapterPosition=viewHolder.getAdapterPosition();

                entryRecyclerViewAdapter.deleteEntry(adapterPosition);
                if(entry.getDate()!=null)
                {
                    cancelNotification(entry);
                }

                Snackbar snackbar=Snackbar.make(findViewById(R.id.root),SimpleDo.getAppContext().getResources().getString(R.string.entry_deleted),BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);

                if(addCardMaterialCardView.getVisibility()== View.VISIBLE)
                {
                    snackbar.setAnchorView(addCardMaterialCardView);
                }
                else
                {
                    snackbar.setAnchorView(startFloatingActionButton);
                }

                snackbar.setAction(SimpleDo.getAppContext().getResources().getString(R.string.undo), (view) ->
                {
                    entryRecyclerViewAdapter.insertEntry(entry, adapterPosition);
                    if(entry.getDate()!=null)
                    {
                        planAndSendNotification(entry);
                    }
                });


                snackbar.show();

                setupLayoutClickability();
            }

            //This contains the current dragged card
            EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder entryRecyclerViewViewHolder;

            //onSelectedChanged() is called when the state of the current dragged card changes
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
            {
                if(actionState == ItemTouchHelper.ACTION_STATE_DRAG)
                {
                    ((EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder) viewHolder).getCardMaterialCardView().setDragged(true);
                    entryRecyclerViewViewHolder=(EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder) viewHolder;
                }

                if(actionState==ItemTouchHelper.ACTION_STATE_IDLE)
                {
                    try
                    {
                        entryRecyclerViewViewHolder.getCardMaterialCardView().setDragged(false);
                    }
                    catch(Exception exception){}

                    if(distance!=0)
                    {
                        Drawable temp = getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme());
                        temp.setAlpha(128);
                        changeDirectionMaterialButton.setIcon(temp);
                        changeDirectionMaterialButton.setEnabled(false);
                        setSortDirection(Direction.NONE);

                        changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
                        setSortCriterion(Criterion.NONE);

                        distance=0;
                    }
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(entryRecyclerView);

        //Setting up date-, time- and color picker and remind button
        addCardDatePickerMaterialButton.setOnClickListener((view) ->
        {
            UIUtil.hideKeyboard(Main.this);

            MaterialDatePicker<Long> materialDatePicker=MaterialDatePicker.Builder
                    .datePicker()
                    .setTheme(R.style.MaterialCalendarTheme)
                    .setSelection(chosenDate==null?Calendar.getInstance().getTimeInMillis():fromDateInMilis(chosenDate))
                    .build();



            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                @Override
                public void onPositiveButtonClick(Long selection) {

                    chosenDate=fromMilisInDate(selection);

                    addCardTimePickerMaterialButton.setVisibility(View.VISIBLE);
                    addCardDeadlineLinearLayout.setVisibility(View.VISIBLE);
                    addCardDateTextView.setText(chosenDate.toString());
                    addCardDivider.setVisibility(View.VISIBLE);
                    addCardRemindMaterialButton.setVisibility(View.VISIBLE);
                    addCardRemindMaterialButton.setEnabled(true);

                    openKeyboardIfClosed();
                }
            });

            materialDatePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    chosenDate=null;
                    chosenTime=null;

                    addCardTimePickerMaterialButton.setVisibility(View.GONE);
                    addCardDeadlineLinearLayout.setVisibility(View.GONE);
                    addCardDivider.setVisibility(View.GONE);
                    addCardRemindMaterialButton.setVisibility(View.GONE);
                    addCardRemindMaterialButton.setEnabled(false);

                    openKeyboardIfClosed();
                }
            });

            materialDatePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog)
                {
                    openKeyboardIfClosed();
                }
            });
            //materialDatePicker.getView().<Button>findViewById(com.google.android.material.R.id.confirm_button).setText("Test");
            materialDatePicker.show(getSupportFragmentManager(), "null");


        });

        addCardTimePickerMaterialButton.setOnClickListener((view)->
        {
            MaterialTimePicker materialTimePicker=new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(chosenTime==null?Calendar.getInstance().get(Calendar.HOUR_OF_DAY):chosenTime.getHour())
                    .setMinute(chosenTime==null?Calendar.getInstance().get(Calendar.MINUTE):chosenTime.getHour())
                    .build();

            materialTimePicker.addOnPositiveButtonClickListener(v ->
            {
                chosenTime=new Time(materialTimePicker.getHour(), materialTimePicker.getMinute());

                addCardTimeTextView.setVisibility(View.VISIBLE);
                addCardTimeTextView.setText(chosenTime.toString());

                openKeyboardIfClosed();
            });


            materialTimePicker.addOnCancelListener(dialog ->
            {
                openKeyboardIfClosed();
            });

            materialTimePicker.addOnNegativeButtonClickListener(dialog ->
            {
                addCardTimeTextView.setVisibility(View.GONE);
                chosenTime=null;

                openKeyboardIfClosed();
            });


            materialTimePicker.show(getSupportFragmentManager(), null);
        });

        addCardColorMenuMaterialButton.setOnClickListener((view) ->
        {
            PopupMenu popupMenu=new PopupMenu(getApplicationContext(), view);

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
                addCardMaterialCardView.setCardBackgroundColor(chosenColor);

                return true;
            });

            popupMenu.show();
        });

        addCardRemindMaterialButton.setOnClickListener((view)->
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

            addCardRemindMaterialButton.setIcon(drawable);
        });

        //Setting up sort buttons
        changeDirectionMaterialButton.setOnMenuItemClickListener((menuItem) ->
        {
            switch(getSortDirection())
            {
                case UP:
                {
                    changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));
                    setSortDirection(Direction.DOWN);
                }break;
                case DOWN:
                case NONE:
                {
                    changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, Main.this.getTheme()));
                    setSortDirection(Direction.UP);
                }break;
            }

            entryRecyclerViewAdapter.sortEntries();

            return true;
        });

        changeCriterionMaterialButton.setOnMenuItemClickListener((menuItem)->
        {
            switch(getSortCriterion())
            {
                case TEXT:
                {
                    changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_clock, Main.this.getTheme()));

                    setSortCriterion(Criterion.DEADLINE);
                }break;
                case DEADLINE:
                {
                    changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_palette, Main.this.getTheme()));

                    setSortCriterion(Criterion.COLOR);

                }break;
                case COLOR:
                case NONE:
                {
                    changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_alpha, Main.this.getTheme()));
                    setSortCriterion(Criterion.TEXT);

                    Drawable temp=getResources().getDrawable(R.drawable.ic_arrow_downward);
                    temp.setAlpha(255);
                    changeDirectionMaterialButton.setIcon(temp);
                    changeDirectionMaterialButton.setEnabled(true);

                    setSortDirection(Direction.DOWN);
                }break;
            }

            if(getSortDirection()==Direction.NONE)
            {
                changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));

                setSortDirection(Direction.DOWN);
            }

            entryRecyclerViewAdapter.sortEntries();

            return true;
        });

        bottomAppBar.setNavigationOnClickListener((view)->
        {
            PopupMenu popupMenu=new PopupMenu(getApplicationContext(), view, Gravity.END, 0, R.style.MyPopupMenu);

            MenuCompat.setGroupDividerEnabled(popupMenu.getMenu(), true);

            popupMenu.getMenuInflater().inflate(R.menu.settings_menu,popupMenu.getMenu());

            popupMenu.getMenu().getItem(0).setOnMenuItemClickListener((menuItem)->
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(Main.this, (timePicker, hour, minute)->
                {
                    setAlldayTime(new Time(hour, minute));

                    for(int i=0; i<getEntriesSize(); i++)
                    {
                        if(getEntry(i).getTime()!=null)
                        {
                            planAndSendNotification(getEntry(i));
                        }
                    }
                }, getAlldayTime().getHour(), getAlldayTime().getMinute(), true)
                {
                    @Override
                    public void onBackPressed()
                    {
                        this.dismiss();
                    }
                };

                timePickerDialog.setOnCancelListener((dialogInterface)->
                {
                    timePickerDialog.dismiss();
                });

                timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, SimpleDo.getAppContext().getResources().getString(R.string.cancel), (dialogInterface, which)->
                {
                    timePickerDialog.dismiss();
                });

                UIUtil.hideKeyboard(Main.this);

                timePickerDialog.show();

                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(SimpleDo.getAppContext().getResources().getString(R.string.ok));
                timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(SimpleDo.getAppContext().getResources().getString(R.string.cancel));

                return false;
            });

            popupMenu.getMenu().getItem(1).setOnMenuItemClickListener((menuItem)->
            {
                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(Main.this);
                alertDialogBuilder.setMessage(R.string.battery_optimization_description);
                alertDialogBuilder.setPositiveButton(R.string.ok, (dialogInterface, which)->
                {
                    Main.this.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                });

                alertDialogBuilder.show();

                return true;
            });

            popupMenu.getMenu().getItem(2).setOnMenuItemClickListener((menuItem)->
            {
                Dialog dialog=new Dialog(Main.this);
                dialog.setContentView(R.layout.about_activity);

                ((MaterialButton) dialog.findViewById(R.id.steven_solleder)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://stevensolleder.de"));
                        startActivity(intent);
                    }
                });

                ((MaterialButton) dialog.findViewById(R.id.isabellwaas)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/isabellwaas"));
                        startActivity(intent);
                    }
                });

                dialog.show();

                return true;
            });

            popupMenu.getMenu().getItem(3).setOnMenuItemClickListener((menuItem)->
            {
                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(Main.this);
                alertDialogBuilder.setMessage(Html.fromHtml(SimpleDo.getAppContext().getResources().getString(R.string.imprint_description)));
                alertDialogBuilder.setPositiveButton(R.string.ok, (dialogInterface, which)->{});
                alertDialogBuilder.show();

                return true;
            });

            popupMenu.getMenu().getItem(4).setOnMenuItemClickListener((menuItem)->
            {
                WebView webView=new WebView(Main.this);
                webView.setVerticalScrollBarEnabled(false);
                webView.setHorizontalScrollBarEnabled(false);
                webView.loadUrl("file:///android_asset/licenses.html");

                RelativeLayout relativeLayout=new RelativeLayout(Main.this);
                relativeLayout.setGravity(RelativeLayout.CENTER_VERTICAL|RelativeLayout.CENTER_VERTICAL);
                relativeLayout.addView(webView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                AlertDialog.Builder alertDialogBuilder=new AlertDialog.Builder(Main.this);
                alertDialogBuilder.setPositiveButton(R.string.ok, (dialogInterface, which)->{});
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setView(relativeLayout);

                webView.setWebViewClient(new WebViewClient()
                {
                    @Override
                    public void onPageFinished (WebView view, String url)
                    {
                        super.onPageFinished(view, url);
                        alertDialog.show();
                    }
                });

                return true;
            });

            popupMenu.show();
        });

        setupLayout();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed()
    {
        int duration=400;
        int interpolatorFactor=2;

        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(startFloatingActionButton, "scaleY", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(startFloatingActionButton, "scaleX", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(startFloatingActionButton, "alpha", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(startFloatingActionButton,"visibility", View.VISIBLE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setStartDelay((long) (duration/1.2));
        floatingActionButtonAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));

        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(addCardMaterialCardView,"visibility", View.GONE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(addCardMaterialCardView, "scaleY", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(addCardMaterialCardView, "scaleX", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(addCardMaterialCardView, "alpha", 1F, 0F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(addCardMaterialCardView, "radius", 4, 100).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius).before(addCardVisibility);
        addCardAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));


        addCardAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation)
            {
                addCardContentEditText.setEnabled(false);
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
                addCardContentEditText.setEnabled(true);

                bottomAppBar.performShow();
                bottomAppBar.setVisibility(View.VISIBLE);
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
            case UP:
            {
                changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, this.getTheme()));
            }
            break;
            case DOWN:
            {
                changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, this.getTheme()));
            }
            break;
            case NONE:
            {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
                drawable.setAlpha(128);
                changeDirectionMaterialButton.setIcon(drawable);
                changeDirectionMaterialButton.setEnabled(false);
            }
            break;
        }

        switch(getSortCriterion())
        {
            case TEXT:
            {
                changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_alpha, this.getTheme()));
            }
            break;
            case DEADLINE:
            {
                changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_clock, this.getTheme()));
            }
            break;
            case COLOR:
            {
                changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_palette, this.getTheme()));
            }
            break;
            case NONE:
            {
                changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, this.getTheme()));
            }
            break;
        }

        setupLayoutClickability();
    }

    private void setupLayoutClickability()
    {
        if(getEntriesSize()<=1)
        {
            Drawable drawable=getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
            drawable.setAlpha(128);
            changeDirectionMaterialButton.setIcon(drawable);
            changeDirectionMaterialButton.setEnabled(false);

            drawable=getResources().getDrawable(R.drawable.ic_sort, this.getTheme());
            drawable.setAlpha(128);
            changeCriterionMaterialButton.setIcon(drawable);
            changeCriterionMaterialButton.setEnabled(false);
        }
        else
        {
            Drawable drawable=getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
            drawable.setAlpha(255);
            changeDirectionMaterialButton.setIcon(drawable);
            changeDirectionMaterialButton.setEnabled(true);

            drawable=getResources().getDrawable(R.drawable.ic_sort, this.getTheme());
            drawable.setAlpha(255);
            changeCriterionMaterialButton.setIcon(drawable);
            changeCriterionMaterialButton.setEnabled(true);
        }
    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= 26)
        {
            NotificationChannel notificationChannel=new NotificationChannel("main", "Erinnerungen", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(SimpleDo.getAppContext().getResources().getString(R.string.reminders_description));

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void start(View view)
    {
        int duration=400;
        int interpolatorFactor=2;

        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(startFloatingActionButton, "scaleY", 1.5F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(startFloatingActionButton, "scaleX", 5F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(startFloatingActionButton, "alpha", 0F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(startFloatingActionButton,"visibility", View.GONE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));

        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(addCardMaterialCardView,"visibility", View.VISIBLE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(addCardMaterialCardView, "scaleY", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(addCardMaterialCardView, "scaleX", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(addCardMaterialCardView, "alpha", 0F, 1F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(addCardMaterialCardView, "radius", 100, 4).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardVisibility).with(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius);
        addCardAnimatorSet.setStartDelay((long) (duration/1.2));
        addCardAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));


        addCardAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation)
            {
                addCardContentEditText.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                addCardContentEditText.setEnabled(true);

                bottomAppBar.performHide();
                bottomAppBar.setVisibility(View.GONE);

                InputMethodManager inputMethodManager = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                addCardContentEditText.requestFocus();
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
                    entry=new Entry(addCardContentEditText.getText().toString(), chosenDate, chosenTime, chosenColor, reminding);
                }
                else
                {
                    entry=new Entry(addCardContentEditText.getText().toString(), chosenDate, chosenColor, reminding);
                }
            }
            else
            {
                entry=new Entry(addCardContentEditText.getText().toString(), chosenColor, reminding);
            }
        }
        else
        {
            if(chosenDate!=null)
            {
                if(chosenTime!=null)
                {
                    entry=new Entry(addCardContentEditText.getText().toString(), chosenDate, chosenTime, reminding);
                }
                else
                {
                    entry=new Entry(addCardContentEditText.getText().toString(), chosenDate, reminding);
                }
            }
            else
            {
                entry=new Entry(addCardContentEditText.getText().toString(), reminding);
            }
        }

        if(reminding)
        {
            if(isInPast(entry))
            {
                Snackbar snackbar=Snackbar.make(findViewById(R.id.root),"Notification liegt in der Vergangenheit", BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
                snackbar.setAnchorView(R.id.addCard);
                snackbar.show();

                return;
            }

            planAndSendNotification(entry);
        }

        changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme()));
        changeCriterionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
        setSortDirection(Direction.NONE);
        setSortCriterion(Criterion.NONE);

        entryRecyclerViewAdapter.insertEntry(entry);

        setupLayoutClickability();

        addCardContentEditText.getText().clear();
        addCardDeadlineLinearLayout.setVisibility(View.GONE);
        addCardTimePickerMaterialButton.setVisibility(View.GONE);

        chosenDate=null;
        chosenTime=null;

        entryRecyclerView.scrollToPosition(SaveHelper.getEntriesSize()-1);
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

    public void itemTouchHelperEnabled(boolean mode)
    {
        if(mode)
        {
            itemTouchHelper.attachToRecyclerView(entryRecyclerView);
        }
        else
        {
            itemTouchHelper.attachToRecyclerView(null);
        }
    }

    private Date fromMilisInDate(long milis)
    {
        java.util.Date jDate=new java.util.Date(milis);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(jDate);

        return new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.YEAR));
    }

    private long fromDateInMilis(Date date)
    {
        Calendar calendar=Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth()-1, date.getDay());

        return calendar.getTimeInMillis();
    }

    private void openKeyboardIfClosed()
    {
        if(!keyboardOpen)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0,0);
        }
    }
}