package com.yang.tool;

import android.content.Context;
import android.content.SharedPreferences;

@SuppressWarnings({"unused", "WeakerAccess"})
public class SharedPrefs {
    private Context context;
    private String preferFileName = "my_calculate";
    private SharedPreferences sp;

    public SharedPrefs(Context context) {
        this.context = context;
        sp = context.getSharedPreferences(preferFileName, Context.MODE_PRIVATE);
    }

    public void putString(String title, String str) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(title, str);
        editor.apply();
    }

    public String getString(String title, String d) {     //default
        return sp.getString(title, d);
    }

    public void putInt(String title, int in) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(title, in);
        editor.apply();
    }

    public int getInt(String title, int d) {
        return sp.getInt(title, d);
    }

    public void putBoolean(String title, boolean in) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(title, in);
        editor.apply();
    }

    public boolean getBoolean(String title, boolean d) {
        return sp.getBoolean(title, d);
    }

    public void remove(String title) {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(title);
        editor.apply();
    }

    public Context getContext() {
        return context;
    }

    public String getPreferFileName() {
        return preferFileName;
    }

    /**
     * @param defValue 默认值
     * @return 类型值
     * @see #putLangType(int)
     */
    public int getLangType(int defValue) {
        return sp.getInt("lang_type", defValue);
    }

    /**
     * 取得值
     *
     * @param langType 语言类型
     * @see #getLangType(int)
     */
    public void putLangType(int langType) {
        putInt("lang_type", langType);
    }

    /**
     * 是否第一次进入APP
     *
     * @return true表示第一次进入，否则不是第一次进入
     * @see #putFirstInAPP(boolean)
     */
    public boolean isFirstInAPP(boolean defValue) {
        return getBoolean("first_in", defValue);
    }

    /**
     * 设置值
     *
     * @param isFirstInAPP 是否第一次进入
     * @see #isFirstInAPP(boolean)
     */
    public void putFirstInAPP(boolean isFirstInAPP) {
        putBoolean("first_in", isFirstInAPP);
    }

}