package de.stevensolleder.simpledo.presenter;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

public class KeyboardHelper
{
    private Main mainActivity;

    public KeyboardHelper(Main mainActivity)
    {
        this.mainActivity=mainActivity;
    }

    public void setKeyboardEnabled(boolean enabled)
    {
        if((enabled && !KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(mainActivity)) || (!enabled && KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(mainActivity)))
        {
            ((InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0,0);
        }
    }
}
