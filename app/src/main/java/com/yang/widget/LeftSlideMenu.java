package com.yang.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

@SuppressWarnings("unused")
public class LeftSlideMenu extends FrameLayout {
    private View leftMenuView;
    private View mainView;
    private Scroller scroller;
    private int leftMenuWidth;
    /**
     * @see #onTouchEvent(MotionEvent)
     */
    private float downX;
    /**
     * @see #onTouchEvent(MotionEvent)
     */
    float deltaX = 0;

    /**
     * @see #setLeftMenuOutsideClickable(boolean)
     */
    private boolean leftMenuOutsideClickable = true;
    /**
     * @see #onInterceptTouchEvent(MotionEvent)
     */
    private boolean isToClose = false;

    /**
     * @see #openLeft()
     * @see #closeLeft()
     * @see Scroller
     */
    private int duration = 400;

    public LeftSlideMenu(Context context) {
        super(context);
        initialize();
    }

    public LeftSlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mainView = getChildAt(0);
        leftMenuView = getChildAt(1);
        leftMenuView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //设置宽度
        ViewGroup.LayoutParams lpLeft = leftMenuView.getLayoutParams();
        lpLeft.width = (int) (new Screen(getContext()).getWidth() * 0.7f);
        leftMenuView.setLayoutParams(lpLeft);
        leftMenuWidth = leftMenuView.getLayoutParams().width;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        leftMenuView.layout(-getLeftMenuWidth(), 0, 0, bottom);
        mainView.layout(0, 0, right, bottom);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) ev.getX();
                if (isLeftMenuShowing() && leftMenuOutsideClickable && downX > getLeftMenuWidth()) {
                    closeLeft();
                    isToClose = true;
                    return true;
                } else {
                    isToClose = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) (ev.getX() - downX);
                if ((!isLeftMenuShowing() && downX < 40 && Math.abs(deltaX) > 10)
                        || (isLeftMenuShowing() && downX > 40 && downX < getLeftMenuWidth())) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                deltaX = moveX - downX;
                float leftTransX = leftMenuView.getTranslationX() + deltaX;
                if (leftTransX > getLeftMenuWidth()) {
                    leftTransX = getLeftMenuWidth();
                }
                if (leftTransX < 0) {
                    leftTransX = 0;
                }
                leftMenuView.setTranslationX(leftTransX);
                mainView.setAlpha(1f - 0.8f * leftTransX / getLeftMenuWidth());
                downX = moveX;
                break;
            case MotionEvent.ACTION_UP:
                if (isToClose) {
                    return true;
                }
                if (deltaX > 0 && leftMenuView.getTranslationX() > getLeftMenuWidth() / 6) {            //向右滑动、距离大于1/6宽度
                    openLeft();
                    return true;
                } else if (deltaX < 0 && leftMenuView.getTranslationX() < getLeftMenuWidth() * 5 / 6) { //向左滑动、距离大于1/6宽度
                    closeLeft();
                    return true;
                }
                //不知目的方向才会处理下面步骤
                if (leftMenuView.getTranslationX() > getLeftMenuWidth() / 2) {
                    openLeft();
                } else {
                    closeLeft();
                }
                return true;
        }
        return true;
    }

    /**
     * @see View#invalidate()
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {       //返回true,表示动画没结束
            leftMenuView.setTranslationX(scroller.getCurrX());
            mainView.setAlpha(1f - 0.8f * scroller.getCurrX() / getLeftMenuWidth());
            invalidate();
        }
    }

    private void initialize() {
        scroller = new Scroller(getContext());
    }

    /**
     * 打开menu
     */
    public void openLeft() {
        Log.e("open left", "from " + (int) leftMenuView.getTranslationX() + " walk " + (getLeftMenuWidth() - (int) leftMenuView.getTranslationX()));
        scroller.startScroll((int) leftMenuView.getTranslationX(), 0, (getLeftMenuWidth() - (int) leftMenuView.getTranslationX()), 0, duration);
        invalidate();
    }

    /**
     * 关闭左侧menu
     */
    public void closeLeft() {
        Log.e("close left", "from " + (int) leftMenuView.getTranslationX() + " walk " + (-(int) leftMenuView.getTranslationX()));
        scroller.startScroll((int) leftMenuView.getTranslationX(), 0, -(int) leftMenuView.getTranslationX(), 0, duration);
        invalidate();
    }

    /**
     * 切换左侧菜单的开和关
     * <p>若已显示，则关闭；否则显示（打开）菜单</p>
     */
    public void toggleLeft() {
        if (isLeftMenuShowing()) {
            closeLeft();
        } else {
            openLeft();
        }
    }

    /**
     * <p>查看左侧菜单是否显示</p>
     * <p>若滑动距离大于左侧菜单宽度的一半，则返回状态为显示（true），否则返回false（关闭）</p>
     *
     * @return true表示显示，false表示其他
     */
    public boolean isLeftMenuShowing() {
        return leftMenuView.getTranslationX() > getLeftMenuWidth() / 2;
    }

    /**
     * 设置左边滑出菜单外部是否可以单击，是则关闭菜单
     *
     * @param leftMenuOutsideClickable true表示点击外部关闭菜单
     */
    public void setLeftMenuOutsideClickable(boolean leftMenuOutsideClickable) {
        this.leftMenuOutsideClickable = leftMenuOutsideClickable;
    }

    public int getLeftMenuWidth() {
        return leftMenuWidth;
    }

    private static class Screen {
        private DisplayMetrics dm;

        Screen(Context context) {
            Resources resources = context.getResources();
            dm = resources.getDisplayMetrics();
        }

        float getDensity() {
            return dm.density;
        }

        int getWidth() {
            return dm.widthPixels;
        }

        int getHeight() {
            return dm.heightPixels;
        }
    }

}