package de.gruenbaum.simpledo.presenter.recyclerview;

import android.view.ContextMenu;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;

import de.gruenbaum.simpledo.R;
import de.gruenbaum.simpledo.model.*;
import de.gruenbaum.simpledo.presenter.notifications.INotificationHelper;
import de.gruenbaum.simpledo.presenter.Main;
import de.gruenbaum.simpledo.presenter.SimpleDo;


public class CustomItemTouchHelperCallback extends ItemTouchHelper.Callback
{
    private Main mainActivity;
    private IDataAccessor dataAccessor;
    private IReminderSettingsAccessor reminderSettingsAccessor;
    private INotificationHelper notificationHelper;

    private EntryViewHolder currentDraggedViewHolder;
    private int distance;

    public CustomItemTouchHelperCallback(Main mainActivity, IDataAccessor dataAccessor, IReminderSettingsAccessor reminderSettingsAccessor, INotificationHelper notificationHelper)
    {
        this.mainActivity=mainActivity;
        this.dataAccessor=dataAccessor;
        this.reminderSettingsAccessor=reminderSettingsAccessor;
        this.notificationHelper=notificationHelper;

        //distance contains how many cards were passed after dropping the card after dragging the card
        this.distance=0;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
    {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    //onMove() is called when a dragged card is dropped
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
    {
        ContextMenu contextMenu=((EntryViewHolder)viewHolder).getContextMenu();
        if(contextMenu!=null) contextMenu.close();

        int fromIndex=viewHolder.getPosition();
        int toIndex=target.getPosition();
        if(fromIndex<toIndex) for (int i=fromIndex; i<toIndex; i++) dataAccessor.swapEntries(i, i+1);
        else for(int i=fromIndex; i>toIndex; i--) dataAccessor.swapEntries(i, i-1);
        viewHolder.getBindingAdapter().notifyItemMoved(fromIndex, toIndex);

        return viewHolder.getPosition()!=target.getPosition();
    }

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
        int position=viewHolder.getPosition();
        Entry entry=dataAccessor.getEntry(position);

        dataAccessor.removeEntry(position);
        viewHolder.getBindingAdapter().notifyItemRemoved(position);
        if(entry.getDate()!=null) notificationHelper.cancelNotification(entry.getId());

        RecyclerView.Adapter<? extends RecyclerView.ViewHolder> currentAdapter=viewHolder.getBindingAdapter();
        mainActivity.showSnackbar(SimpleDo.getAppContext().getResources().getString(R.string.entry_deleted), BaseTransientBottomBar.LENGTH_SHORT, () ->
        {
            dataAccessor.addEntry(position, entry);
            currentAdapter.notifyItemInserted(position);
            if(entry.getDate()!=null && entry.isNotifying()) notificationHelper.planAndSendNotification(entry.getDate(), entry.getTime()!=null?entry.getTime():reminderSettingsAccessor.getAlldayTime(), entry.getContent(), entry.getId());
        });

        if(dataAccessor.getEntriesSize()<=1)
        {
            mainActivity.enableSortability(false);
            mainActivity.setSortDirectionIcon(Direction.NONE);
            mainActivity.setSortCriterionIcon(Criterion.NONE);
        }
    }

    //onSelectedChanged() is called when the state of the current dragged card changes
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
    {
        if(actionState == ItemTouchHelper.ACTION_STATE_DRAG)
        {
            currentDraggedViewHolder=(EntryViewHolder) viewHolder;
            currentDraggedViewHolder.setEntryDragged(true);
        }

        if(actionState==ItemTouchHelper.ACTION_STATE_IDLE)
        {
            try
            {
                currentDraggedViewHolder.setEntryDragged(false);
            }
            catch(Exception exception)
            {
                exception.printStackTrace();
            }

            if(distance!=0)
            {
                mainActivity.setSortDirectionIcon(Direction.NONE);
                mainActivity.setSortCriterionIcon(Criterion.NONE);
                distance=0;
            }
        }
    }
}
