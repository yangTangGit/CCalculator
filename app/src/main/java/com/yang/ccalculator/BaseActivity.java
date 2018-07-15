package com.yang.ccalculator;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * https://www.jianshu.com/p/0cd03c878def?open_source=weibo_search
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeDark);
    }

}