package com.tencent.test;

import android.content.Context;
import android.ext.widget.FocusManager;
import android.ext.widget.ViewUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class MyLinearLayout extends LinearLayout implements OnFocusChangeListener {
    private FocusManager mFocusManager;

    public MyLinearLayout(Context context) {
        super(context);
        initView(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        for (int i = getChildCount() - 1; i >= 0; --i) {
            final View child = getChildAt(i);
            child.setFocusable(true);
            child.setOnFocusChangeListener(this);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            mFocusManager.setFocusedView(view);
        }
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!mFocusManager.onAddFocusables(views, direction, focusableMode)) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return ViewUtils.getChildDrawingOrder(this, childCount, i);
    }

    private void initView(Context context) {
        setChildrenDrawingOrderEnabled(true);
        mFocusManager = new FocusManager(this);
    }
}
