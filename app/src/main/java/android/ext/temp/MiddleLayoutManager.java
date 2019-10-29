package android.ext.temp;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class MiddleLayoutManager extends LinearLayoutManager {

    private static final String TAG = "MiddleLayoutManager";
    private int mOrientation;
    private int mParentCenter;
    public static final int DEFAULT_ANIMATION_DURATION = 200;

    protected Interpolator mInterpolator = new DecelerateInterpolator();
    protected int mDuration = DEFAULT_ANIMATION_DURATION;

    public MiddleLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mOrientation = orientation;
    }

    public int getOrientation(){
        return mOrientation;
    }

    @Override
    public void onLayoutChildren(Recycler recycler, State state) {

        try{
            super.onLayoutChildren(recycler, state);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {

        if (mParentCenter == 0) {
            if(mOrientation == GridLayoutManager.VERTICAL){
                mParentCenter = getHeight() / 2;
            }else{
                mParentCenter = getWidth() / 2;
            }
        }

        if(mOrientation == GridLayoutManager.VERTICAL){
            int dy = getScrollDy(child);
            if (dy != 0) {
                parent.smoothScrollBy(0, dy);
//                parent.smoothScrollBy(0, dy, mDuration, mInterpolator);
                return true;
            }
        }else{
            int dx = getScrollDx(child);
            if (dx != 0) {
                parent.smoothScrollBy(dx, 0);
//                parent.smoothScrollBy(dx, 0, mDuration, mInterpolator);
                return true;
            }
        }
        return super.requestChildRectangleOnScreen(parent, child, rect, immediate);
    }

    public int getScrollDy(View child) {

        if (mParentCenter == 0) {
            mParentCenter = getHeight() / 2;
        }
        Log.d(TAG, "child.getTop():" + child.getTop() + ", child.getHeight():" + child.getHeight() / 2 + ", mParentCenter:" + mParentCenter);
        int childCenter = child.getTop() + child.getHeight() / 2;
        return childCenter - mParentCenter;
    }

    public int getScrollDx(View child) {

        if (mParentCenter == 0) {
            mParentCenter = getWidth() / 2;
        }
        Log.d(TAG, "child.getLeft():" + child.getLeft() + ", child.getWidth():" + child.getWidth() / 2 + ", mParentCenter:" + mParentCenter);
        int childCenter = child.getLeft() + child.getWidth() / 2;
        return childCenter - mParentCenter;
    }
}
