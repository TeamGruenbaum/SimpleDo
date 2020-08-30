package de.stevensolleder.simpledo.controller;

import android.app.*;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import static de.stevensolleder.simpledo.model.ColorHelper.colorChangeMenuMenuItemToColor;
import static de.stevensolleder.simpledo.model.SaveHelper.*;

import java.lang.reflect.Field;
import java.util.Calendar;

import de.stevensolleder.simpledo.*;
import de.stevensolleder.simpledo.model.*;

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
        System.out.println("oncreate");
        //Initialize main.xml
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Initialize all attributes from main.xml
        entryRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        //sortingMaterialButtonsRelativeLayout=findViewById(R.id.walter);
        bottomAppBar=findViewById(R.id.bottomAppBar);
        changeDirectionMaterialButton=bottomAppBar.getMenu().getItem(0);
        changeCriteriumMaterialButton=bottomAppBar.getMenu().getItem(1);

        addCardMaterialCardView=findViewById(R.id.addCard);
        addCardContentEditText=findViewById(R.id.contentEditText);
        addCardDatePickerMaterialButton=findViewById(R.id.dateButton);
        addCardTimePickerMaterialButton=findViewById(R.id.timeButton);
        addCardColorMenuMaterialButton=findViewById(R.id.colorButton);
        addCardDeadlineLinearLayout=findViewById(R.id.addCardDeadline);

        addCardDateTextView=findViewById(R.id.addCardDate);
        addCardTimeTextView=findViewById(R.id.addCardTime);

        startButton=findViewById(R.id.start);

        addCardRemindMaterialButton=findViewById(R.id.remindButton);

        //Set attributes from entryRecyclerView
        entryRecyclerView.setHasFixedSize(true);
        entryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        entryRecyclerView.setVerticalScrollBarEnabled(false);

        //Create and set Adapter
        entryRecyclerViewAdapter=new EntryRecyclerViewAdapter(this);
        entryRecyclerView.setAdapter(entryRecyclerViewAdapter);

        //Create and set Animator
        //EntryListAnimator entryListAnimator=new EntryListAnimator();
        //entryRecyclerView.setItemAnimator(entryListAnimator);

        createNotificationChannel();

        //Setting swipe gestures
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder temp;
            int distance=0;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                try
                {
                    ((EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder)viewHolder).getContextMenu().close();
                }
                catch(Exception e){}

                entryRecyclerViewAdapter.moveEntry(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return viewHolder.getAdapterPosition()!=target.getAdapterPosition();
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y)
            {
                distance+=(fromPos-toPos);

                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir)
            {
                Entry temp=getEntry(viewHolder.getAdapterPosition());
                int temp2=viewHolder.getAdapterPosition();

                entryRecyclerViewAdapter.deleteEntry(temp2);

                Snackbar snackbar=Snackbar.make(findViewById(R.id.root),"Rückgängig machen?", BaseTransientBottomBar.LENGTH_SHORT);
                if(addCardMaterialCardView.getVisibility()== View.VISIBLE)
                {
                    snackbar.setAnchorView(addCardMaterialCardView);
                }
                else
                {
                    snackbar.setAnchorView(startButton);
                }

                snackbar.setAction("Ja", new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        entryRecyclerViewAdapter.insertEntry(temp, temp2);
                    }
                });
                snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
                snackbar.show();
            }

            @Override
            public void onSelectedChanged (RecyclerView.ViewHolder viewHolder, int actionState)
            {
                if(actionState == ItemTouchHelper.ACTION_STATE_DRAG)
                {
                    ((EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder) viewHolder).getCardMaterialCardView().setDragged(true);
                    temp=(EntryRecyclerViewAdapter.EntryRecyclerViewViewHolder) viewHolder;
                }

                if(actionState==ItemTouchHelper.ACTION_STATE_IDLE)
                {
                    try
                    {
                        temp.getCardMaterialCardView().setDragged(false);
                    }
                    catch(Exception ignored){}

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

        addCardRemindMaterialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                reminding = !reminding;

                Drawable temp;

                if(reminding)
                {
                    temp=getResources().getDrawable(R.drawable.ic_notifications_active, Main.this.getTheme());
                }
                else
                {
                    temp=getResources().getDrawable(R.drawable.ic_notifications_off, Main.this.getTheme());
                }

                addCardRemindMaterialButton.setIcon(temp);
            }
        });

        //Setting up date-, time- and colorpicker
        addCardDatePickerMaterialButton.setOnClickListener((View view) ->
        {
            DatePickerDialog picker = new DatePickerDialog(Main.this);

            picker.setButton(DialogInterface.BUTTON_POSITIVE, "Übernehmen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    DatePicker temp=picker.getDatePicker();

                    chosenDate=new Date(temp.getDayOfMonth(), temp.getMonth(), temp.getYear());
                    addCardTimePickerMaterialButton.setVisibility(View.VISIBLE);
                    addCardDeadlineLinearLayout.setVisibility(View.VISIBLE);
                    addCardDateTextView.setText(chosenDate.toString());

                    addCardRemindMaterialButton.setVisibility(View.VISIBLE);
                    addCardRemindMaterialButton.setEnabled(true);

                    UIUtil.showKeyboard(Main.this, addCardContentEditText);
                }
            });

            picker.setButton(DialogInterface.BUTTON_NEGATIVE, "Löschen",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            chosenDate=null;
                            chosenTime=null;
                            addCardTimePickerMaterialButton.setVisibility(View.GONE);
                            addCardDeadlineLinearLayout.setVisibility(View.GONE);

                            addCardRemindMaterialButton.setVisibility(View.GONE);
                            addCardRemindMaterialButton.setEnabled(false);

                            UIUtil.showKeyboard(Main.this, addCardContentEditText);
                        }
                    });

            UIUtil.hideKeyboard(Main.this);

            picker.show();
        });

        addCardTimePickerMaterialButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                TimePickerDialog timePickerDialog=new TimePickerDialog(Main.this, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute)
                    {
                        chosenTime=new Time(hour, minute);
                        addCardTimeTextView.setVisibility(View.VISIBLE);
                        addCardTimeTextView.setText(chosenTime.toString());

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0,0);
                    }
                }, Calendar.getInstance().get(Calendar.HOUR), Calendar.getInstance().get(Calendar.MINUTE), true);

                timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface)
                    {
                        chosenTime=null;
                        addCardTimeTextView.setVisibility(View.GONE);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0,0);
                    }
                });

                UIUtil.hideKeyboard(Main.this);

                timePickerDialog.show();

                timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Übernehmen");
                timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText("Löschen");
            }
        });

        //Setting up sortbuttons
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

        addCardColorMenuMaterialButton.setOnClickListener((view) ->
        {
                PopupMenu popupMenu=new PopupMenu(getApplicationContext(), view);

                popupMenu.getMenuInflater().inflate(R.menu.color_change_menu,popupMenu.getMenu());

                try
                {
                    //Nobody understands this
                    Field fieldMPopup=popupMenu.getClass().getDeclaredField("mPopup");
                    fieldMPopup.setAccessible(true);
                    Object mPopup=fieldMPopup.get(popupMenu);
                    mPopup.getClass().getDeclaredMethod("setForceShowIcon", boolean.class).invoke(mPopup, true);
                }catch(Exception e){}

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem)
                    {
                        chosenColor=colorChangeMenuMenuItemToColor(menuItem);
                        addCardMaterialCardView.setCardBackgroundColor(chosenColor);
                        return true;
                    }
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
            case UP:{changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_arrow_upward, Main.this.getTheme()));}break;
            case DOWN:{changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_arrow_downward, Main.this.getTheme()));}break;
            case NONE:
            {
                Drawable temp = getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme());
                temp.setAlpha(128);
                changeDirectionMaterialButton.setIcon(temp);
                changeDirectionMaterialButton.setEnabled(false);
            }break;
        }

        switch(getSortCriterium())
        {
            case TEXT:{changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_alpha, Main.this.getTheme()));}break;
            case DEADLINE:{changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_clock, Main.this.getTheme()));}break;
            case COLOR:{changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_palette, Main.this.getTheme()));}break;
            case NONE:{changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));}break;
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
        Entry temp;

        if(!(addCardContentEditText.getText().toString().trim().length() > 0))
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
                    temp=new Entry(refreshedContent, chosenDate, chosenTime, chosenColor, reminding);
                }
                else
                {
                    temp=new Entry(refreshedContent, chosenDate, chosenColor, reminding);
                }
            }
            else
            {
                temp=new Entry(refreshedContent, chosenColor, reminding);
            }
        }
        else
        {
            if(chosenDate!=null)
            {
                if(chosenTime!=null)
                {
                    temp=new Entry(refreshedContent, chosenDate, chosenTime, reminding);
                }
                else
                {
                    temp=new Entry(refreshedContent, chosenDate, reminding);
                }
            }
            else
            {
                temp=new Entry(refreshedContent, reminding);
            }
        }

        changeDirectionMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_swap_vert, Main.this.getTheme()));
        changeCriteriumMaterialButton.setIcon(getResources().getDrawable(R.drawable.ic_sort, Main.this.getTheme()));

        setSortDirection(Direction.NONE);
        setSortCriterium(Criterium.NONE);

        addCardContentEditText.getText().clear();

        entryRecyclerViewAdapter.insertEntry(temp);

        if(reminding)
        {
            NotificationHelper.planAndSendNotification(temp);
        }
    }

    public void start(View view)
    {
        Toast.makeText(this, "Gesetzt!", Toast.LENGTH_SHORT).show();

        bottomAppBar.performHide();
        startButton.hide();
        addCardMaterialCardView.setVisibility(View.VISIBLE);
        addCardContentEditText.requestFocus();

        InputMethodManager inputMethodManager=(InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}