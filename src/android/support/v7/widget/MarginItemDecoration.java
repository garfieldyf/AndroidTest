package android.support.v7.widget;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * Class MarginItemDecoration
 * @author Garfield
 * @version 1.0
 */
public final class MarginItemDecoration extends ItemDecoration {
    /**
     * The left margin in pixels of the children.
     */
    public int leftMargin;

    /**
     * The top margin in pixels of the children.
     */
    public int topMargin;

    /**
     * The right margin in pixels of the children.
     */
    public int rightMargin;

    /**
     * The bottom margin in pixels of the children.
     */
    public int bottomMargin;

    /**
     * Constructor
     * @see #MarginItemDecoration(Resources, int)
     * @see #MarginItemDecoration(int, int, int, int)
     */
    public MarginItemDecoration() {
    }

    /**
     * Constructor
     * @param res The <tt>Resources</tt>.
     * @param id The resource id of the margin dimension.
     * @see #MarginItemDecoration()
     * @see #MarginItemDecoration(int, int, int, int)
     */
    public MarginItemDecoration(Resources res, int id) {
        leftMargin = topMargin = rightMargin = bottomMargin = res.getDimensionPixelSize(id);
    }

    /**
     * Constructor
     * @param leftMargin The left margin in pixels of the children.
     * @param topMargin The top margin in pixels of the children.
     * @param rightMargin The right margin in pixels of the children.
     * @param bottomMargin The bottom margin in pixels of the children.
     * @see #MarginItemDecoration()
     * @see #MarginItemDecoration(Resources, int)
     */
    public MarginItemDecoration(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        this.leftMargin   = leftMargin;
        this.topMargin    = topMargin;
        this.rightMargin  = rightMargin;
        this.bottomMargin = bottomMargin;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        outRect.set(leftMargin, topMargin, rightMargin, bottomMargin);
    }

    @Override
    public String toString() {
        return new StringBuilder(80)
            .append("MarginItemDecoration { leftMargin = ").append(leftMargin)
            .append(", topMargin = ").append(topMargin)
            .append(", rightMargin = ").append(rightMargin)
            .append(", bottomMargin = ").append(bottomMargin)
            .append(" }").toString();
    }
}
