package de.stevensolleder.simpledo.model;

import java.text.SimpleDateFormat;

import java.util.Locale;

public class IdentificationHelper
{
    //Method for creating a unique ID for each entry
    public static int createUniqueID()
    {
        return new SimpleDateFormat("ddHHmmssSS",  Locale.US).format(new java.util.Date()).hashCode();
    }
}
