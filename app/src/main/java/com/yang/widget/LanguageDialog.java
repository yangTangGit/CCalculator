package com.yang.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.yang.ccalculator.MainActivity;
import com.yang.ccalculator.R;
import com.yang.tool.Language;
import com.yang.tool.SharedPrefs;

public class LanguageDialog extends AlertDialog.Builder {
    private AlertDialog alertDialog;
    private Activity activity;
    private SharedPrefs sharedPrefs;

    public LanguageDialog(Context context) {
        super(context);
        setDialog();
    }

    private void setDialog() {
        sharedPrefs = new SharedPrefs(getContext());
        String[] lang = {
                getContext().getString(R.string.lang_default),
                getContext().getString(R.string.lang_sim_chinese),
                getContext().getString(R.string.lang_english)
        };
        setTitle(R.string.lang);
        setSingleChoiceItems(lang, sharedPrefs.getLangType(0), new ChoiceListener(getContext()));
    }

    public LanguageDialog setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public void showDialog() {
        alertDialog = this.create();
        alertDialog.show();
    }

    private class ChoiceListener implements DialogInterface.OnClickListener {
        private Context context;

        ChoiceListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case Language.LANG_TYPE_SYSTEM:
                    sharedPrefs.putLangType(Language.LANG_TYPE_SYSTEM);
                    break;
                case Language.LANG_TYPE_SIMPLE_CHINESE:
                    sharedPrefs.putLangType(Language.LANG_TYPE_SIMPLE_CHINESE);
                    break;
                case Language.LANG_TYPE_ENGLISH:
                    sharedPrefs.putLangType(Language.LANG_TYPE_ENGLISH);
                    break;
            }
            alertDialog.dismiss();
            activity.finish();
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
        }
    }
}
