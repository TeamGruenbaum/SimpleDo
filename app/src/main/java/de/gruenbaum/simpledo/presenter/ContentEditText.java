package de.gruenbaum.simpledo.presenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import androidx.appcompat.widget.AppCompatEditText;

import java.util.function.BiConsumer;

public class ContentEditText extends AppCompatEditText
{
    private BiConsumer<Integer, KeyEvent> action;

    public ContentEditText(Context context)
    {
        super(context);
    }

    public ContentEditText(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
    }

    public ContentEditText(Context context, AttributeSet attributeSet, int defaultStyleAttribute)
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