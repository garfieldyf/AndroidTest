package android.ext.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

/**
 * Class FocusViewPager
 * @author Garfield
 */
@SuppressWarnings("deprecation")
public class FocusViewPager extends ViewPager {
    /* package */ int mLastKeyCode;
    /* package */ View mFoucsedView;

    public FocusViewPager(Context context) {
        super(context);
        setOnPageChangeListener(mListener);
    }

    public FocusViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPageChangeListener(mListener);
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            mFoucsedView = findFocus();
            mLastKeyCode = event.getKeyCode();
        }

        return super.executeKeyEvent(event);
    }

    private final OnPageChangeListener mListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            View nextFocus = null;
            switch (mLastKeyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                nextFocus = FocusFinder.getInstance().findNextFocus(FocusViewPager.this, mFoucsedView, FOCUS_LEFT);
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                nextFocus = FocusFinder.getInstance().findNextFocus(FocusViewPager.this, mFoucsedView, FOCUS_RIGHT);
                break;
            }

            if (nextFocus != null) {
                nextFocus.requestFocus();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }
    };
}
