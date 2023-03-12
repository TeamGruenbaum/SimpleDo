package de.gruenbaum.simpledo.presenter.recyclerview;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import de.gruenbaum.simpledo.databinding.EntryCardBinding;
import de.gruenbaum.simpledo.model.IDataAccessor;
import de.gruenbaum.simpledo.model.IReminderSettingsAccessor;
import de.gruenbaum.simpledo.presenter.Main;


public class EntryAdapter extends RecyclerView.Adapter<EntryViewHolder>
{
    private Main mainActivity;
    private IDataAccessor dataAccessor;
    private IReminderSettingsAccessor reminderSettingsAccessor;

    public EntryAdapter(Main activity, IDataAccessor dataAccessor, IReminderSettingsAccessor reminderSettingsAccessor)
    {
        mainActivity=activity;
        this.dataAccessor=dataAccessor;
        this.reminderSettingsAccessor=reminderSettingsAccessor;
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new EntryViewHolder(mainActivity, EntryCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), dataAccessor, reminderSettingsAccessor);
    }

    @Override
    public void onBindViewHolder(EntryViewHolder entryViewHolder, int position)
    {
        entryViewHolder.bindData(dataAccessor.getEntry(position));
    }

    @Override
    public int getItemCount()
    {
        return dataAccessor.getEntriesSize();
    }
}