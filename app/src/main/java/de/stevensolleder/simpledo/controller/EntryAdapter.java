package de.stevensolleder.simpledo.controller;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import de.stevensolleder.simpledo.databinding.EntryCardBinding;
import de.stevensolleder.simpledo.model.DataAccessor;


public class EntryAdapter extends RecyclerView.Adapter<EntryViewHolder>
{
    private Main mainActivity;
    private DataAccessor dataAccessor;

    public EntryAdapter(Main activity, DataAccessor dataAccessor)
    {
        mainActivity=activity;
        this.dataAccessor=dataAccessor;
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new EntryViewHolder(mainActivity, EntryCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), this, dataAccessor);
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