package com.tencent.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import com.tencent.temp.ViewPager;

public class ViewPagerActivity extends Activity {
    private static final int[] PAGE_COLOR = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.GRAY, Color.MAGENTA, };

    /* package */ ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        mViewPager = (ViewPager)findViewById(R.id.content);
        for (int i = 0; i < PAGE_COLOR.length; ++i) {
            addPage(i, PAGE_COLOR[i]);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mViewPager.scrollToPrevPage(true);
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mViewPager.scrollToNextPage(true);
                break;

            default:
                break;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    private void addPage(int page, int color) {
        final View child = new PageView(this, page);
        child.setBackgroundColor(color);
        mViewPager.addView(child);
    }

    private static final class PageView extends View {
        private final int mPage;

        public PageView(Context context, int page) {
            super(context);
            mPage = page;
        }

        @Override
        public void draw(Canvas canvas) {
            Log.i("yf", "page " + mPage + " draw");
            super.draw(canvas);
        }
    }
}
