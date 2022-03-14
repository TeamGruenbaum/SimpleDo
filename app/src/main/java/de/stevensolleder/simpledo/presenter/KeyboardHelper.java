package de.stevensolleder.simpledo.presenter;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

public class KeyboardHelper
{
    private final Activity activity;

    public KeyboardHelper(Activity activity)
    {
        this.activity=activity;
    }

    public void setKeyboardEnabled(boolean enabled)
    {
        if((enabled && !KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(activity)) || (!enabled && KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(activity)))
        {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
}
