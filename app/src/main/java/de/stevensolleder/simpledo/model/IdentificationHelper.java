package de.stevensolleder.simpledo.model;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class IdentificationHelper
{
    public IdentificationHelper() {}

    public int createUniqueId()
    {
        return new SimpleDateFormat("ddHHmmssSS",  Locale.US).format(new java.util.Date()).hashCode();
    }
}
