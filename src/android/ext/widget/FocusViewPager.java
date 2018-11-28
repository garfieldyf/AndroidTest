package android.ext.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

/**
 * Class FocusableViewPager
 * @author Garfield
 */
public class FocusViewPager extends ViewPager {
    private int mLastKeyCode;
    private View mFoucsedView;

    public FocusViewPager(Context context) {
        super(context);
    }

    public FocusViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Try to give focus in this view, starting from the focused view
     * of the previous page. <p>Note: This method recommended call in
     * the {@link OnPageChangeListener#onPageSelected(int)}.</p>
     * @return Whether the child view actually took focus.
     */
    public boolean requestPageChildFocus() {
        View nextFocus = null;
        switch (mLastKeyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            nextFocus = FocusFinder.getInstance().findNextFocus(this, mFoucsedView, FOCUS_LEFT);
            break;

        case KeyEvent.KEYCODE_DPAD_RIGHT:
            nextFocus = FocusFinder.getInstance().findNextFocus(this, mFoucsedView, FOCUS_RIGHT);
            break;
        }

        return (nextFocus != null && nextFocus.requestFocus());
    }

    @Override
    public boolean executeKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            mFoucsedView = findFocus();
            mLastKeyCode = event.getKeyCode();
        }

        return super.executeKeyEvent(event);
    }
}
