package com.yang.ccalculator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yang.tool.SharedPrefs;
import com.yang.widget.LanguageDialog;
import com.yang.widget.LeftSlideMenu;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Calculator extends BaseActivity implements View.OnClickListener {

    Context context;
    Activity activity;
    MessageHandler handler;

    EditText edit;
    //数字类
    TextView tvNumber0, tvNumber1, tvNumber2, tvNumber3, tvNumber4;
    TextView tvNumber5, tvNumber6, tvNumber7, tvNumber8, tvNumber9;
    TextView tvNumberDot;
    //操作类
    TextView tvOpAC, tvOpDEL;
    TextView tvOpPercent, tvOpDiv, tvOpMul, tvOpSub, tvOpAdd;
    TextView tvOpEqual;
    //菜单里面的视图
    LeftSlideMenu slideMenu;
    TextView tvTheme, tvLang, tvAbout;

    boolean restartCalc = false;
    static boolean appRunning = false; //执行onCreate时会设置为true，表示此APP在运行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        //setContentView(R.layout.activity_calculator_table);
        initOnCreate();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {     //按下返回键
            if (slideMenu != null && slideMenu.isLeftMenuShowing()) {
                //若菜单正在显示，则关闭菜单
                slideMenu.closeLeft();
                return true;
            }
        }
        //其他的交由系统处理
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        Editable editable = edit.getEditableText();
        //后面一个条件是排除掉按键为运算符的情况，单独处理
        if (restartCalc && !CalcData.containOperator(context, ((TextView) v).getText().toString())) {
            editable.clear();
            restartCalc = false;
        }
        switch (v.getId()) {
            //数字0-9
            case R.id.number_0:
            case R.id.number_1:
            case R.id.number_2:
            case R.id.number_3:
            case R.id.number_4:
            case R.id.number_5:
            case R.id.number_6:
            case R.id.number_7:
            case R.id.number_8:
            case R.id.number_9:
                //若是输入数字，则直接附加到尾部
                editable.append(((TextView) v).getText());
                break;

            //小数点
            case R.id.number_dot:
                String dot = context.getString(R.string.number_dot);
                //没有文本而输入小数点时先在前面补一个0，保持数字规范
                if (editable.length() == 0) {
                    editable.append("0").append(dot);
                    break;
                }
                //记录最后一个运算符的位置索引
                int opI = CalcData.lastIndexOfOperator(context, editable.toString());
                if (opI > -1) {     //存在运算符
                    String last = editable.toString().substring(opI + 1);
                    if (last.equals("")) {  //运算符后没有其他字符
                        editable.append("0").append(dot);
                        break;
                    } else if (!last.contains(dot)) {   //运算符之后的字符串不包含小数点
                        editable.append(dot);
                        break;
                    }
                } else {            //不存在运算符
                    if (!editable.toString().contains(dot)) {
                        editable.append(dot);
                    }
                }
                break;
            //清空按钮
            case R.id.op_ac:
                editable.clear();
                restartCalc = false;
                break;
            //删除按钮
            case R.id.op_del:
                if (editable.length() >= 1) {
                    //当长度大于或者等于1时，每次删除结尾的一个字符
                    editable.delete(edit.getText().length() - 1, edit.getText().length());
                }
                break;
            //双目运算符
            case R.id.op_pct:
            case R.id.op_div:
            case R.id.op_mul:
            case R.id.op_sub:
            case R.id.op_add:
                //获取当前的运算符
                String op = ((TextView) v).getText().toString();
                //等号
                String strEql = context.getString(R.string.op_eql);
                //是否继续执行判断流程
                boolean continueIf = true;
                if (restartCalc && editable.toString().contains(strEql)) {
                    //获取上一次的运算结果
                    CharSequence lastResult = editable.subSequence(editable.toString().lastIndexOf(strEql) + 1, editable.length());
                    editable.clear();       //清空之前的计算
                    editable.append(lastResult);
                    continueIf = false;     //表示此处执行之后，不再执行后面的判断语句
                    restartCalc = false;
                }
                //若此长度为0，则在最前面添加一个0
                if (continueIf && editable.length() == 0) {
                    if (!op.equals(context.getString(R.string.op_sub))) {  //输入减号除外
                        editable.append("0");
                    }
                    continueIf = false;     //表示此处执行之后，不再执行后面的判断语句
                }
                //若以小数点结尾，则先附加一个0
                if (continueIf && editable.toString().endsWith(context.getString(R.string.number_dot))) {
                    editable.append("0");
                    continueIf = false;     //表示此处执行之后，不再执行后面的判断语句
                }
                //若以运算符结尾，则先删除该运算符
                if (continueIf && CalcData.containOperator(context, editable.toString().substring(editable.length() - 1))) {
                    editable.delete(editable.length() - 1, editable.length());
                    if (editable.toString().equals("")) {   //若之前只有一个减号，则这里的字符串长度为0
                        editable.append("0");   //添加一个0
                    }
                }
                //判断从索引1开始是否含有运算符
                if (CalcData.lastIndexOfOperator(context, editable.toString()) >= 1) {  //包含
                    try {
                        //调用计算的方法
                        calculate(op);
                    } catch (NumberFormatException e) {
                        handler.sendEmptyMessage(MessageHandler.ERROR_FORMULA);
                        e.printStackTrace();
                    }
                } else {                                                                //不包含
                    editable.append(op);
                }
                break;
            //等号（计算结果）
            case R.id.op_equal:
                //从索引1开始含有运算符
                if (CalcData.lastIndexOfOperator(context, editable.toString()) >= 1) {
                    try {
                        calculate(context.getString(R.string.op_eql));
                    } catch (NumberFormatException e) {
                        handler.sendEmptyMessage(MessageHandler.ERROR_FORMULA);
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.set_theme:
                Log.e("click", "theme");
                break;
            case R.id.set_lang:
                new LanguageDialog(context).setActivity(activity).showDialog();
                break;
            case R.id.set_about:
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("intent", false);
                startActivity(intent);
                break;
        }
        CalcView.updateFont(edit);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initOnCreate() {
        appRunning = true;
        //初始化一般变量
        context = Calculator.this;
        activity = Calculator.this;
        handler = new MessageHandler(context);

        //初始化视图变量
        edit = findViewById(R.id.edit_content);
        tvNumber0 = findViewById(R.id.number_0);
        tvNumber1 = findViewById(R.id.number_1);
        tvNumber2 = findViewById(R.id.number_2);
        tvNumber3 = findViewById(R.id.number_3);
        tvNumber4 = findViewById(R.id.number_4);
        tvNumber5 = findViewById(R.id.number_5);
        tvNumber6 = findViewById(R.id.number_6);
        tvNumber7 = findViewById(R.id.number_7);
        tvNumber8 = findViewById(R.id.number_8);
        tvNumber9 = findViewById(R.id.number_9);
        tvNumberDot = findViewById(R.id.number_dot);
        tvOpAC = findViewById(R.id.op_ac);
        tvOpDEL = findViewById(R.id.op_del);
        tvOpPercent = findViewById(R.id.op_pct);
        tvOpDiv = findViewById(R.id.op_div);
        tvOpMul = findViewById(R.id.op_mul);
        tvOpSub = findViewById(R.id.op_sub);
        tvOpAdd = findViewById(R.id.op_add);
        tvOpEqual = findViewById(R.id.op_equal);
        slideMenu = findViewById(R.id.cal_slide_menu);
        tvTheme = findViewById(R.id.set_theme);
        tvLang = findViewById(R.id.set_lang);
        tvAbout = findViewById(R.id.set_about);

        //创建和设置背景
        tvTheme.setBackground(ViewState.menuBtn(context));
        tvLang.setBackground(ViewState.menuBtn(context));
        tvAbout.setBackground(ViewState.menuBtn(context));

        //设置监听事件
        tvNumber0.setOnClickListener(this);
        tvNumber1.setOnClickListener(this);
        tvNumber2.setOnClickListener(this);
        tvNumber3.setOnClickListener(this);
        tvNumber4.setOnClickListener(this);
        tvNumber5.setOnClickListener(this);
        tvNumber6.setOnClickListener(this);
        tvNumber7.setOnClickListener(this);
        tvNumber8.setOnClickListener(this);
        tvNumber9.setOnClickListener(this);
        tvNumberDot.setOnClickListener(this);
        tvOpAC.setOnClickListener(this);
        tvOpDEL.setOnClickListener(this);
        tvOpPercent.setOnClickListener(this);
        tvOpDiv.setOnClickListener(this);
        tvOpMul.setOnClickListener(this);
        tvOpSub.setOnClickListener(this);
        tvOpAdd.setOnClickListener(this);
        tvOpEqual.setOnClickListener(this);
        tvTheme.setOnClickListener(this);
        tvLang.setOnClickListener(this);
        tvAbout.setOnClickListener(this);

        CalcView.disableShowInput(edit);
        edit.setFocusable(false);
        edit.setLongClickable(false);
        edit.setTextIsSelectable(false);

        //如果是第一次进入APP，则显示菜单，否则不显示菜单
        SharedPrefs sharedPrefs = new SharedPrefs(context);
        if (sharedPrefs.isFirstInAPP(true)) {
            slideMenu.openLeft();
            //第一次进入之后，更改此值，以后不会在进入APP时显示菜单
            sharedPrefs.putFirstInAPP(false);
        }
    }

    private void calculate(final String operator) throws NumberFormatException {
        Log.e("info", "running calculate method.");
        //获取可编辑文本
        Editable editable = edit.getEditableText();
        //以下条件成立时退出，不处理
        if (!Pattern.compile("[0-9]").matcher(editable.toString()).find()) {    //不包含数字
            return;
        }
        //是否以运算符结尾
        boolean endWithOperator;
        try {
            endWithOperator = CalcData.containOperator(context, editable.charAt(editable.length() - 1) + "");
        } catch (IndexOutOfBoundsException e) {
            endWithOperator = false;
            e.printStackTrace();
        }
        //若以运算符结尾，提示用户
        if (endWithOperator) {
            handler.sendEmptyMessage(MessageHandler.ERROR_FORMULA);
            return;
        }
        //算式中最后一个运算符的位置
        int lastIndex = CalcData.lastIndexOfOperator(context, editable.toString());
        if (lastIndex > 0) {
            BigDecimal num1 = new BigDecimal(editable.subSequence(0, lastIndex).toString());
            BigDecimal num2 = new BigDecimal(editable.subSequence(lastIndex + 1, editable.length()).toString());
            //截取当前算式中的运算符
            String centralOp = editable.charAt(lastIndex) + "";
            String calcResult = "";
            if (context.getString(R.string.op_add).equals(centralOp)) {
                calcResult = num1.add(num2).toString();
            } else if (context.getString(R.string.op_sub).equals(centralOp)) {
                calcResult = num1.subtract(num2).toString();
            } else if (context.getString(R.string.op_mul).equals(centralOp)) {
                calcResult = num1.multiply(num2).toString();
            } else if (context.getString(R.string.op_div).equals(centralOp)) {
                if (num2.compareTo(new BigDecimal(0)) == 0) {       //除数为0
                    handler.sendEmptyMessage(MessageHandler.ERROR_ZERO);
                    restartCalc = true;
                    return;
                }
                if (num1.compareTo(BigDecimal.valueOf(0)) == 0) {   //被除数为0
                    //这样单独处理，可以避免出现0除以一个常数出现无穷小的情况
                    calcResult = "0";
                } else {                                            //都不为0
                    calcResult = num1.divide(num2, 7, RoundingMode.FLOOR).toString();
                }
            } else if (context.getString(R.string.op_pct).equals(centralOp)) {
                calcResult = num1.divideAndRemainder(num2)[1].toString();
            }
            //由于内部处理是针对小数，所以前提是包含小数点
            if (calcResult.contains(context.getString(R.string.number_dot))) {
                //循环去除尾部多余的0
                while (calcResult.endsWith("0")) {
                    calcResult = calcResult.substring(0, calcResult.length() - 1);
                }
                //若以小数点结尾，则去掉该小数点
                if (calcResult.endsWith(".")) {
                    calcResult = calcResult.substring(0, calcResult.length() - 1);
                }
            }
            //显示运算结果
            if (!context.getString(R.string.op_eql).equals(operator)) {     //当输入等号时，不清楚输入内容，并且插入等号
                editable.clear();
            } else {
                editable.append(context.getString(R.string.op_eql));
            }
            editable.append(calcResult);
        }
        if (context.getString(R.string.op_eql).equals(operator)) {      //若当前点击的是等号
            restartCalc = true;         //表示下一次进入新一轮的运算
        }
        if (CalcData.containOperator(context, operator)) {       //若当前点击的是五个运算符之一
            editable.append(operator);  //计算完成后将运算符追加到末尾
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class MessageHandler extends Handler {
        public static final int ERROR_ZERO = 1;
        public static final int ERROR_FORMULA = 2;
        private Context context;

        public MessageHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ERROR_ZERO:
                    Toast.makeText(context, context.getString(R.string.zero_err), Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_FORMULA:
                    Toast.makeText(context, context.getString(R.string.invalid_formula), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class CalcView {
        @SuppressLint("ObsoleteSdkInt")
        public static void disableShowInput(EditText editText) {
            if (android.os.Build.VERSION.SDK_INT > 10) {
                Class<EditText> cls = EditText.class;
                Method method;
                try {
                    method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                    method.setAccessible(true);
                    method.invoke(editText, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                    method.setAccessible(true);
                    method.invoke(editText, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                editText.setInputType(InputType.TYPE_NULL);
            }
        }

        /**
         * 更新EditText的文字颜色和大小
         *
         * @param edit 编辑框
         */
        public static void updateFont(EditText edit) {
            SpannableString spannable = new SpannableString(edit.getText());
            List<String> operators = CalcData.operatorList(edit.getContext());
            String text = edit.getText().toString();
            int colorOne = ContextCompat.getColor(edit.getContext(), R.color.editor_text_operator);
            int pxSize = (int) (edit.getTextSize() + sp2px(edit.getContext(), 3));
            //循环查找运算符的位置
            for (int index = text.length() - 1; index >= 0; index--) {
                if (index < 0) {
                    break;
                }
                String operator = text.charAt(index) + "";
                if (CalcData.containOperator(edit.getContext(), operator)
                        || edit.getContext().getString(R.string.op_eql).equals(operator)) {
                    spannable.setSpan(new ForegroundColorSpan(colorOne), index, index + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new AbsoluteSizeSpan(pxSize), index, index + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    text = text.substring(0, index);
                }
            }
            edit.setText(spannable);
            //设置文字大小
            int len = edit.length();
            if (len == 0) {
                edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            } else if (len <= 10) {
                edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
            } else if (len <= 18) {
                edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            } else {
                edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }
            int index = edit.length() - 1;
            if (index >= 0) {
                edit.setSelection(index);
            }
        }

        /**
         * convert px to its equivalent sp
         * <p>
         * 将px转换为sp
         */
        public static int px2sp(Context context, float pxValue) {
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (pxValue / fontScale + 0.5f);
        }

        /**
         * convert sp to its equivalent px
         * <p>
         * 将sp转换为px
         */
        public static int sp2px(Context context, float spValue) {
            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            return (int) (spValue * fontScale + 0.5f);
        }
    }

    @SuppressWarnings("WeakerAccess")
    private static class CalcData {
        public static List<String> operatorList(Context context) {
            List<String> list = new ArrayList<>();
            list.add(context.getString(R.string.op_pct));
            list.add(context.getString(R.string.op_div));
            list.add(context.getString(R.string.op_mul));
            list.add(context.getString(R.string.op_sub));
            list.add(context.getString(R.string.op_add));
            return list;
        }

        public static boolean containOperator(Context context, String containedOperator) {
            List<String> operators = operatorList(context);
            return containedOperator != null && operators.contains(containedOperator);
        }

        /**
         * 寻找指定字符串中最后一个运算符的索引，索引号从0开始，若无，则返回-1
         *
         * @param str 指定查找此字符串
         * @return 索引号
         */
        public static int lastIndexOfOperator(Context context, String str) {
            if (str == null) {
                throw new NullPointerException("str is null.");
            }
            //从最后一个字符开始查找
            for (int i = str.length() - 1; i >= 0; i--) {
                if (CalcData.containOperator(context, str.charAt(i) + "")) {
                    return i;
                }
            }
            return -1;
        }
    }

    private static class ViewState {
        static StateListDrawable menuBtn(Context context) {
            StateListDrawable d = new StateListDrawable();
            d.addState(new int[]{-android.R.attr.state_pressed}, ContextCompat.getDrawable(context, R.color.menu_btn_bg));
            d.addState(new int[]{android.R.attr.state_pressed}, ContextCompat.getDrawable(context, R.color.menu_btn_bg_pressed));
            return d;
        }
    }

}