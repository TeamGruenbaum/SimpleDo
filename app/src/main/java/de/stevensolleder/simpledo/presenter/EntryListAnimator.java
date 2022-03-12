package de.stevensolleder.simpledo.presenter;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;


public class EntryListAnimator extends DefaultItemAnimator
{
    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder)
    {
        return false;
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder)
    {
        TranslateAnimation anim =  new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_PARENT, 1f,
                Animation.RELATIVE_TO_SELF, 0f);
        anim.setDuration(500);
        holder.itemView.startAnimation(anim);
        dispatchAddFinished(holder);
        return true;
    }
}