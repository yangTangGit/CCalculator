package com.yang.ccalculator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yang.tool.Language;
import com.yang.tool.SharedPrefs;

public class MainActivity extends Activity {

    boolean autoIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = MainActivity.this;
        //设置语言环境
        Language.setLanguage(context, new SharedPrefs(context).getLangType(-1));
        setContentView(R.layout.activity_main);
        autoIntent = getIntent().getBooleanExtra("intent", true);
        new MainThread().start();
        if (!Calculator.appRunning) {
            findViewById(R.id.extra_describe).setVisibility(View.GONE);
        } else {
            findViewById(R.id.extra_describe).setVisibility(View.VISIBLE);
        }
    }

    private class MainThread extends Thread {
        @Override
        public void run() {
            super.run();
            if (!Calculator.appRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (autoIntent) {
                startActivity(new Intent(MainActivity.this, Calculator.class));
                finish();
            }
        }
    }

}