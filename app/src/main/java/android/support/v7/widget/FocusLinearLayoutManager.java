package android.support.v7.widget;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import android.content.Context;
import android.ext.widget.LayoutManagerHelper;
import android.graphics.Rect;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import java.util.ArrayList;

/**
 * Class FocusLinearLayoutManager
 * @author Garfield
 */
public class FocusLinearLayoutManager extends LinearLayoutManager implements OnFocusChangeListener {
    private int mPosition;

    public FocusLinearLayoutManager(Context context) {
        super(context);
    }

    public FocusLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public int getFocusedItem() {
        return mPosition;
    }

    public int setFocusedItem(View child) {
        return (mPosition = (child != null ? getPosition(child) : 0));
    }

    public void requestItemFocus(int position) {
        if (mRecyclerView != null) {
            mPosition = position;
            if (!requestItemFocus()) {
                scrollToPosition(mPosition);
                LayoutManagerHelper.requestItemFocus(this, mPosition);
            }
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            requestItemFocus();
        }
    }

    @Override
    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        return (recyclerView.getFocusedChild() == null && views.add(recyclerView));
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
        if (mOrientation == VERTICAL) {
            final int dy = (child.getTop() + child.getHeight() / 2) - getHeight() / 2;
            if (dy != 0) {
                scrollBy(parent, 0, dy, immediate);
                return true;
            }
        } else {
            final int dx = (child.getLeft() + child.getWidth() / 2) - getWidth() / 2;
            if (dx != 0) {
                scrollBy(parent, dx, 0, immediate);
                return true;
            }
        }

        return false;
    }

    @Override
    /* package */ void setRecyclerView(RecyclerView recyclerView) {
        super.setRecyclerView(recyclerView);
        if (recyclerView != null) {
            recyclerView.setFocusable(true);
            recyclerView.setOnFocusChangeListener(this);
            recyclerView.setDescendantFocusability(RecyclerView.FOCUS_BEFORE_DESCENDANTS);
        }
    }

    private boolean requestItemFocus() {
        final View focused = findViewByPosition(mPosition);
        return (focused != null && focused.requestFocus());
    }

    private static void scrollBy(RecyclerView parent, int dx, int dy, boolean immediate) {
        if (immediate || parent.getScrollState() != SCROLL_STATE_IDLE) {
            parent.scrollBy(dx, dy);
        } else {
            parent.smoothScrollBy(dx, dy);
        }
    }
}
