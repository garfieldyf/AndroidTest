package com.tencent.test;

import java.util.ArrayList;
import android.content.Context;
import android.ext.temp.RecyclerAdapter;
import android.ext.widget.ViewUtils;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class ImageRecyclerView extends RecyclerView {
    public ImageRecyclerView(Context context) {
        super(context);
        initView(context);
    }

    public ImageRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ImageRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!((RecyclerAdapter<?>)getAdapter()).getFocusManager().onAddFocusables(views, direction, focusableMode)) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        return ViewUtils.getChildDrawingOrder(this, childCount, i);
    }

    private void initView(Context context) {
        setChildrenDrawingOrderEnabled(true);
        setFocusable(false);
    }
}
