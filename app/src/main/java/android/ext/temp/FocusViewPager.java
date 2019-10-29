package android.ext.temp;

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

    public final View getFoucsedView() {
        return mFoucsedView;
    }

    public final int getFocusDirection() {
        switch (mLastKeyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            return FOCUS_LEFT;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            return FOCUS_RIGHT;

        default:
            return 0;
        }
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
            final int direction = getFocusDirection();
            if (direction != 0) {
                final View target = FocusFinder.getInstance().findNextFocus(FocusViewPager.this, mFoucsedView, direction);
                if (target != null) {
                    target.requestFocus();
                }
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
