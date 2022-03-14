package de.stevensolleder.simpledo.presenter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.stevensolleder.simpledo.R;



public class BottomBarAnimator
{
    private FloatingActionButton floatingActionButton;
    private BottomAppBar bottomAppBar;
    private MaterialCardView addCard;

    public BottomBarAnimator(FloatingActionButton floatingActionButton, BottomAppBar bottomAppBar, MaterialCardView addCard)
    {
        this.floatingActionButton=floatingActionButton;
        this.bottomAppBar=bottomAppBar;
        this.addCard=addCard;
    }

    public void increaseAndHideFloatingActionButton(int duration, int interpolatorFactor)
    {
        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(floatingActionButton, "scaleY", 1.5F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(floatingActionButton, "scaleX", 5F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(floatingActionButton, "alpha", 0F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(floatingActionButton,"visibility", View.GONE).setDuration(duration);

        AnimatorSet floatingActionButtonAnimatorSet=new AnimatorSet();
        floatingActionButtonAnimatorSet.play(floatingActionButtonScaleX).with(floatingActionButtonScaleY).with(floatingActionButtonAlpha).with(floatingActionButtonVisibility);
        floatingActionButtonAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));

        floatingActionButtonAnimatorSet.start();
    }

    public void showFloatingActionButton(int duration, int interpolatorFactor)
    {
        ObjectAnimator floatingActionButtonScaleX=ObjectAnimator.ofFloat(floatingActionButton, "scaleY", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonScaleY=ObjectAnimator.ofFloat(floatingActionButton, "scaleX", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonAlpha=ObjectAnimator.ofFloat(floatingActionButton, "alpha", 1F).setDuration(duration);
        ObjectAnimator floatingActionButtonVisibility=ObjectAnimator.ofInt(floatingActionButton, "visibility", View.VISIBLE).setDuration(duration);

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
                addCard.findViewById(R.id.contentEditText).setEnabled(true);
                bottomAppBar.performShow();
                bottomAppBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        floatingActionButtonAnimatorSet.start();
    }

    public void decreaseAndHideAddCard(int duration, int interpolatorFactor)
    {
        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(addCard,"visibility", View.GONE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(addCard, "scaleY", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(addCard, "scaleX", 1F, 0.2F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(addCard, "alpha", 1F, 0F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(addCard, "radius", 4, 100).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius).before(addCardVisibility);
        addCardAnimatorSet.setInterpolator(new AccelerateInterpolator(interpolatorFactor));

        addCardAnimatorSet.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                addCard.findViewById(R.id.contentEditText).setEnabled(false);
            }

            @Override public void onAnimationEnd(Animator animation){}
            @Override public void onAnimationCancel(Animator animation){}
            @Override public void onAnimationRepeat(Animator animation){}
        });

        addCardAnimatorSet.start();
    }

    public void showAddCard(int duration, int interpolatorFactor)
    {
        ObjectAnimator addCardVisibility=ObjectAnimator.ofInt(addCard,"visibility", View.VISIBLE).setDuration(10);
        ObjectAnimator addCardScaleY=ObjectAnimator.ofFloat(addCard, "scaleY", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardScaleX=ObjectAnimator.ofFloat(addCard, "scaleX", 0.2F, 1F).setDuration(duration);
        ObjectAnimator addCardAlpha=ObjectAnimator.ofFloat(addCard, "alpha", 0F, 1F).setDuration(duration);
        ObjectAnimator addCardRadius=ObjectAnimator.ofFloat(addCard, "radius", 100, 4).setDuration(duration);

        AnimatorSet addCardAnimatorSet=new AnimatorSet();
        addCardAnimatorSet.play(addCardVisibility).with(addCardScaleY).with(addCardScaleX).with(addCardAlpha).with(addCardRadius);
        addCardAnimatorSet.setStartDelay((long) (duration/1.2));
        addCardAnimatorSet.setInterpolator(new DecelerateInterpolator(interpolatorFactor));


        addCardAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation)
            {
                addCard.findViewById(R.id.contentEditText).setEnabled(false);
                bottomAppBar.performHide();
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                addCard.findViewById(R.id.contentEditText).setEnabled(true);

                InputMethodManager inputMethodManager = (InputMethodManager) SimpleDo.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                addCard.findViewById(R.id.contentEditText).requestFocus();
            }

            @Override public void onAnimationCancel(Animator animation){}
            @Override public void onAnimationRepeat(Animator animation){}
        });

        addCardAnimatorSet.start();
    }
}
