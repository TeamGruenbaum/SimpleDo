package de.stevensolleder.simpledo.controller;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Field;
import java.util.Calendar;

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
    private MaterialButton addCardColorMenuMaterialButton;
    private MaterialButton addCardRemindMaterialButton;
    private View addCardDivider;
    private LinearLayout addCardDeadlineLinearLayout;
    private TextView addCardDateTextView;
    private TextView addCardTimeTextView;

    private FloatingActionButton startButton;

    private MenuItem changeDirectionMaterialButton;
    private MenuItem changeCriteriumMaterialButton;

    private BottomAppBar bottomAppBar;

    private Date chosenDate=null;
    private Time chosenTime=null;
    private int chosenColor=-1;

    private boolean reminding=false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Initialize main.xml
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Initialize all attributes from main.xml
        entryRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        entryRecyclerViewAdapter=new EntryRecyclerViewAdapter(this);

        bottomAppBar=findViewById(R.id.bottomAppBar);
        changeDirectionMaterialButton=bottomAppBar.getMenu().getItem(0);
        changeCriteriumMaterialButton=bottomAppBar.getMenu().getItem(1);

        addCardMaterialCardView=findViewById(R.id.addCard);
        addCardContentEditText=findViewById(R.id.contentEditText);
        addCardDatePickerMaterialButton=findViewById(R.id.dateButton);
        addCardTimePickerMaterialButton=findViewById(R.id.timeButton);
        addCardColorMenuMaterialButton=findViewById(R.id.colorButton);
        addCardDivider=findViewById(R.id.divider1);
        addCardDeadlineLinearLayout=findViewById(R.id.addCardDeadline);

        addCardDateTextView=findViewById(R.id.addCardDate);
        addCardTimeTextView=findViewById(R.id.addCardTime);

        startButton=findViewById(R.id.start);

        addCardRemindMaterialButton=findViewById(R.id.remindButton);

        //Set attributes from entryRecyclerView and set Adapter
        entryRecyclerView.setHasFixedSize(true);
        entryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entryRecyclerView.setVerticalScrollBarEnabled(false);
        entryRecyclerView.setAdapter(entryRecyclerViewAdapter);

        //Create and set Animator
        //EntryListAnimator entryListAnimator=new EntryListAnimator();
        //entryRecyclerView.setItemAnimator(entryListAnimator);

        //Create notifcationchannel for reminders
        createNotificationChannel();

        //Setting swipe gestures
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            //onMove() is called when a dragged card is dropped
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                //Sometimes getContextMenu() return null, to avoid a crash we use try-catch
                try
                {
                    ((EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder)viewHolder).getContextMenu().close();
                }
                catch(Exception e){}

                entryRecyclerViewAdapter.moveEntry(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return viewHolder.getAdapterPosition()!=target.getAdapterPosition();
            }

            //distance contains how many cards were passed after dropping the card after a dragging the card
            int distance=0;

            //onMoved() is called when a card is in drag mode and changed the position with another card
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

                Snackbar snackbar=Snackbar.make(findViewById(R.id.root),"Rückgängig machen?", BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);

                if(addCardMaterialCardView.getVisibility()== View.VISIBLE)
                {
                    snackbar.setAnchorView(addCardMaterialCardView);
                }
                else
                {
                    snackbar.setAnchorView(startButton);
                }

                snackbar.setAction("Ja", (view) ->
                {
                    entryRecyclerViewAdapter.insertEntry(entry, adapterPosition);
                    if(entry.getDate()!=null)
                    {
                        planAndSendNotification(entry);
                    }
                });

                snackbar.show();

                setupSortLayout();
            }

            //This contains the current dragged card
            EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder entryRecyclerViewViewHolder;

            //onSelectedChanged() is called when the state of the current dragged card
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

                        changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
                        setSortCriterium(Criterium.NONE);

                        distance=0;
                    }
                }
            }
        }).attachToRecyclerView(entryRecyclerView);

        //Setting up date-, time- and color picker and remind button
        addCardDatePickerMaterialButton.setOnClickListener((view) ->
        {
            DatePickerDialog datePickerDialog=new DatePickerDialog(Main.this);

            datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Übernehmen", (dialogInterface, i)->
            {
                DatePicker temp=datePickerDialog.getDatePicker();

                chosenDate=new Date(temp.getDayOfMonth(), temp.getMonth(), temp.getYear());

                addCardTimePickerMaterialButton.setVisibility(View.VISIBLE);
                addCardDeadlineLinearLayout.setVisibility(View.VISIBLE);
                addCardDateTextView.setText(chosenDate.toString());
                addCardDivider.setVisibility(View.VISIBLE);
                addCardRemindMaterialButton.setVisibility(View.VISIBLE);
                addCardRemindMaterialButton.setEnabled(true);

                UIUtil.showKeyboard(Main.this, addCardContentEditText);
            });

            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Löschen", (dialogInterface, i)->
            {
                chosenDate=null;
                chosenTime=null;

                addCardTimePickerMaterialButton.setVisibility(View.GONE);
                addCardDeadlineLinearLayout.setVisibility(View.GONE);
                addCardDivider.setVisibility(View.GONE);
                addCardRemindMaterialButton.setVisibility(View.GONE);
                addCardRemindMaterialButton.setEnabled(false);

                UIUtil.showKeyboard(Main.this, addCardContentEditText);
            });

            datePickerDialog.setOnKeyListener((DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent)->
            {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    datePickerDialog.dismiss();

                    UIUtil.showKeyboard(Main.this, addCardContentEditText);
                }

                return true;
            });

            datePickerDialog.setCanceledOnTouchOutside(false);

            UIUtil.hideKeyboard(this);

            datePickerDialog.show();
        });

        addCardTimePickerMaterialButton.setOnClickListener((view)->
        {
            TimePickerDialog timePickerDialog=new TimePickerDialog(Main.this, (timePicker, hour, minute)->
            {
                chosenTime=new Time(hour, minute);

                addCardTimeTextView.setVisibility(View.VISIBLE);
                addCardTimeTextView.setText(chosenTime.toString());

                //UIUtil doesn't work in time picker so we use the direct methods
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,0);
            }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);

            timePickerDialog.setOnCancelListener((dialogInterface)->
            {
                chosenTime=null;

                addCardTimeTextView.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,0);
            });

            timePickerDialog.setOnKeyListener((DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent)->
            {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    timePickerDialog.dismiss();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0,0);
                }

                return true;
            });

            timePickerDialog.setCanceledOnTouchOutside(false);

            UIUtil.hideKeyboard(Main.this);

            timePickerDialog.show();

            timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Übernehmen");
            timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText("Löschen");
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

        changeCriteriumMaterialButton.setOnMenuItemClickListener((menuItem) ->
        {
            switch(getSortCriterium())
            {
                case TEXT:
                {
                    changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_clock, Main.this.getTheme()));

                    setSortCriterium(Criterium.DEADLINE);
                }break;
                case DEADLINE:
                {
                    changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_palette, Main.this.getTheme()));

                    setSortCriterium(Criterium.COLOR);

                }break;
                case COLOR:
                case NONE:
                {
                    changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_alpha, Main.this.getTheme()));
                    setSortCriterium(Criterium.TEXT);

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
            PopupMenu popupMenu=new PopupMenu(getApplicationContext(), view);

            popupMenu.getMenuInflater().inflate(R.menu.settings_menu,popupMenu.getMenu());

            popupMenu.getMenu().getItem(0).setOnMenuItemClickListener((menuItem)->
            {
                return false;
            });

            popupMenu.getMenu().getItem(1).setOnMenuItemClickListener((menuItem)->
            {
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
                alertDialog.setPositiveButton("OK", (dialogInterface, i)->{});
                alertDialog.setTitle("Developers");
                alertDialog.setMessage("Steven Solleder \nIsabell Waas");
                alertDialog.setCancelable(false);

                alertDialog.show();

                return false;
            });

            popupMenu.getMenu().getItem(2).setOnMenuItemClickListener((menuItem)->
            {
                Intent intent=new Intent(Main.this, DeveloperActivity.class);

                startActivity(intent);

                return false;
            });

            popupMenu.show();
        });

        setupLayout();
    }

    @Override
    public void onBackPressed()
    {
        addCardMaterialCardView.setVisibility(View.GONE);

        startButton.show();
        bottomAppBar.performShow();
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

        switch(getSortCriterium())
        {
            case TEXT:
            {
                changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_alpha, this.getTheme()));
            }
            break;
            case DEADLINE:
            {
                changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_clock, this.getTheme()));
            }
            break;
            case COLOR:
            {
                changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_palette, this.getTheme()));
            }
            break;
            case NONE:
            {
                changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, this.getTheme()));
            }
            break;
        }

        setupSortLayout();
    }

    public void setupSortLayout()
    {
        if(getEntriesSize()<=1)
        {
            Drawable drawable=getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
            drawable.setAlpha(128);
            changeDirectionMaterialButton.setIcon(drawable);
            changeDirectionMaterialButton.setEnabled(false);

            drawable=getResources().getDrawable(R.drawable.ic_sort, this.getTheme());
            drawable.setAlpha(128);
            changeCriteriumMaterialButton.setIcon(drawable);
            changeCriteriumMaterialButton.setEnabled(false);
        }
        else
        {
            System.out.println("ELSE");
            Drawable drawable=getResources().getDrawable(R.drawable.ic_swap_vert, this.getTheme());
            drawable.setAlpha(255);
            changeDirectionMaterialButton.setIcon(drawable);
            changeDirectionMaterialButton.setEnabled(true);

            drawable=getResources().getDrawable(R.drawable.ic_sort, this.getTheme());
            drawable.setAlpha(255);
            changeCriteriumMaterialButton.setIcon(drawable);
            changeCriteriumMaterialButton.setEnabled(true);
        }
    }

    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= 26)
        {
            NotificationChannel notificationChannel=new NotificationChannel("main", "Erinnerungen", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Tolle Erinnerungen");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void addCard(View view)
    {
        Entry entry;

        if(!(addCardContentEditText.getText().toString().trim().length()>0))
        {
            return;
        }

        String refreshedContent=addCardContentEditText.getText().toString().replaceAll("^\\s+|\\s+$", "");

        if(chosenColor!=-1)
        {
            if(chosenDate!=null)
            {
                if(chosenTime!=null)
                {
                    entry=new Entry(refreshedContent, chosenDate, chosenTime, chosenColor, reminding);
                }
                else
                {
                    entry=new Entry(refreshedContent, chosenDate, chosenColor, reminding);
                }
            }
            else
            {
                entry=new Entry(refreshedContent, chosenColor, reminding);
            }
        }
        else
        {
            if(chosenDate!=null)
            {
                if(chosenTime!=null)
                {
                    entry=new Entry(refreshedContent, chosenDate, chosenTime, reminding);
                }
                else
                {
                    entry=new Entry(refreshedContent, chosenDate, reminding);
                }
            }
            else
            {
                entry=new Entry(refreshedContent, reminding);
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
        changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));
        setSortDirection(Direction.NONE);
        setSortCriterium(Criterium.NONE);

        entryRecyclerViewAdapter.insertEntry(entry);

        setupSortLayout();

        addCardContentEditText.getText().clear();
        addCardDeadlineLinearLayout.setVisibility(View.GONE);
        chosenDate=null;
        chosenTime=null;
    }

    public void start(View view)
    {
        bottomAppBar.performHide();
        startButton.hide();

        addCardMaterialCardView.setVisibility(View.VISIBLE);

        addCardContentEditText.requestFocus();

        InputMethodManager inputMethodManager=(InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private boolean isInPast (Entry entry)
    {
        Calendar calendar=Calendar.getInstance();

        Date currentDate=new Date(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        Time currentTime=new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        Time allDayEventTime=new Time(11,0); //getAllDayEventTime()

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
}