package de.stevensolleder.simpledo.controller;

import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import static de.stevensolleder.simpledo.model.SaveHelper.*;



public class EntryRecyclerViewAdapter extends RecyclerView.Adapter<EntryRecyclerViewViewHolder>
{
    AppCompatActivity holdingActivity;

    public EntryRecyclerViewAdapter(AppCompatActivity holdingActivity)
    {
        this.holdingActivity=holdingActivity;
    }

    @Override
    public EntryRecyclerViewViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new EntryRecyclerViewViewHolder(parent, holdingActivity);
    }

    @Override
    public void onBindViewHolder(EntryRecyclerViewViewHolder entryViewViewHolder, int position)
    {
        entryViewViewHolder.getContentEditText().setText(getEntry(position).getContent());

        if(getEntry(position).isNotifying())
        {
            entryViewViewHolder.getBellImageView().setVisibility(View.VISIBLE);
        }
        else
        {
            entryViewViewHolder.getBellImageView().setVisibility(View.GONE);
        }

        if(getEntry(position).getDate()!=null)
        {
            entryViewViewHolder.getDateTextView().setText(getEntry(position).getDate().toString());
            entryViewViewHolder.getDeadlineLinearLayout().setVisibility(View.VISIBLE);

            if(getEntry(position).getTime()!=null)
            {
                entryViewViewHolder.getTimeTextView().setText(getEntry(position).getTime().toString());
                entryViewViewHolder.getTimeTextView().setVisibility(View.VISIBLE);
            }
            else
            {
                entryViewViewHolder.getTimeTextView().setVisibility(View.GONE);
            }
        }
        else
        {
            entryViewViewHolder.getDeadlineLinearLayout().setVisibility(View.GONE);
        }

        entryViewViewHolder.getCardMaterialCardView().setCardBackgroundColor(getEntry(position).getColor());
    }

    @Override
    public int getItemCount()
    {
        return getEntriesSize();
    }
}