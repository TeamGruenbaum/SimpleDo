package de.stevensolleder.simpledo.presenter;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import de.stevensolleder.simpledo.databinding.EntryCardBinding;
import de.stevensolleder.simpledo.model.IDataAccessor;
import de.stevensolleder.simpledo.model.ISettingsAccessor;


public class EntryAdapter extends RecyclerView.Adapter<EntryViewHolder>
{
    private Main mainActivity;
    private IDataAccessor dataAccessor;
    private ISettingsAccessor settingsAccessor;

    public EntryAdapter(Main activity, IDataAccessor dataAccessor, ISettingsAccessor settingsAccessor)
    {
        mainActivity=activity;
        this.dataAccessor=dataAccessor;
        this.settingsAccessor=settingsAccessor;
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new EntryViewHolder(mainActivity, EntryCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), this, dataAccessor, settingsAccessor);
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