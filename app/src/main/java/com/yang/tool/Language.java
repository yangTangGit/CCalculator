package com.yang.tool;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class Language {
    public static final int LANG_TYPE_SYSTEM = 0;
    public static final int LANG_TYPE_SIMPLE_CHINESE = 1;
    public static final int LANG_TYPE_ENGLISH = 2;

    public static void setLanguage(Context context, int type) {
        Resources resources = context.getResources();
        final Configuration config = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        switch (type) {
            case LANG_TYPE_SIMPLE_CHINESE:
                config.setLocale(Locale.SIMPLIFIED_CHINESE);
                break;
            case LANG_TYPE_ENGLISH:
                config.setLocale(Locale.ENGLISH);
                break;
            case LANG_TYPE_SYSTEM:
            default:
                config.setLocale(Locale.getDefault());
                break;
        }
        context.getResources().updateConfiguration(config, displayMetrics);
    }

}