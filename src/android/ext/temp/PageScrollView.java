package android.ext.temp;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Class PageScrollView
 * @author Garfield
 * @version 1.0
 */
public class PageScrollView extends ScrollView {
    /**
     * The page size to scroll in pixels.
     */
    public int pageSize;

    public PageScrollView(Context context) {
        super(context);
    }

    public PageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        final int offScreenTop    = getScrollY();
        final int offScreenBottom = offScreenTop + getHeight();

        int scrollYDelta = 0;
        if (rect.bottom > offScreenBottom && rect.top > offScreenTop) {
            // Scroll to next(down) page.
            scrollYDelta = pageSize;
        } else if (rect.top < offScreenTop && rect.bottom < offScreenBottom) {
            // Scroll to previous(up) page.
            scrollYDelta = -pageSize;
        }

        return scrollYDelta;
    }

    protected int computeHorizontalScrollDeltaToGetChildRectOnScreen(Rect rect) {
        final int offScreenLeft  = getScrollX();
        final int offScreenRight = offScreenLeft + getWidth();

        int scrollXDelta = 0;
        if (rect.right > offScreenRight && rect.left > offScreenLeft) {
            // Scroll to next(right) page.
            scrollXDelta = pageSize;
        } else if (rect.left < offScreenLeft && rect.right < offScreenRight) {
            // Scroll to previous(left) page.
            scrollXDelta = -pageSize;
        }

        return scrollXDelta;
    }
}
