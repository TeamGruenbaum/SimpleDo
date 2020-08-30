package de.stevensolleder.simpledo.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import java.util.function.BiConsumer;

public class AddCardContentEditText extends EditText
{
    private BiConsumer<Integer, KeyEvent> action;

    public AddCardContentEditText(Context context)
    {
        super(context);
    }

    public AddCardContentEditText(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    public AddCardContentEditText(Context context, AttributeSet attributeSet, int defaultStyleAttribute)
    {
        super(context, attributeSet, defaultStyleAttribute);
    }

    public void setKeyPreImeAction(BiConsumer<Integer, KeyEvent> action)
    {
        this.action=action;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent keyEvent)
    {
        action.accept(keyCode, keyEvent);

        return super.onKeyPreIme( keyCode, keyEvent);
    }
}