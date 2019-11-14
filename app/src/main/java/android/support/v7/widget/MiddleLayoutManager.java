package android.support.v7.widget;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

public class MiddleLayoutManager extends LinearLayoutManager {
    public MiddleLayoutManager(Context context) {
        super(context);
    }

    public MiddleLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {
        return requestChildRectangleOnScreen(parent, child, rect, immediate, false);
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
        if (mOrientation == VERTICAL) {
            final int dy = (child.getTop() + child.getHeight() / 2) - getHeight() / 2;
            if (dy != 0) {
                parent.smoothScrollBy(0, dy);
                return true;
            }
        } else {
            final int dx = (child.getLeft() + child.getWidth() / 2) - getWidth() / 2;
            if (dx != 0) {
                parent.smoothScrollBy(dx, 0);
                return true;
            }
        }

        return super.requestChildRectangleOnScreen(parent, child, rect, immediate);
    }
}
