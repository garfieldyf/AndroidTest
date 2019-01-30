package android.ext.widget;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.State;
import android.util.Printer;
import android.view.View;

/**
 * Class MarginItemDecoration
 * @author Garfield
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
     * @see #MarginItemDecoration(MarginItemDecoration)
     */
    public MarginItemDecoration() {
    }

    /**
     * Copy constructor
     * @param from The decoration to copy.
     * @see #MarginItemDecoration()
     * @see #MarginItemDecoration(Resources, int)
     * @see #MarginItemDecoration(int, int, int, int)
     */
    public MarginItemDecoration(MarginItemDecoration from) {
        this.leftMargin   = from.leftMargin;
        this.topMargin    = from.topMargin;
        this.rightMargin  = from.rightMargin;
        this.bottomMargin = from.bottomMargin;
    }

    /**
     * Constructor
     * @param res The <tt>Resources</tt>.
     * @param id The resource id of the margin dimension.
     * @see #MarginItemDecoration()
     * @see #MarginItemDecoration(int, int, int, int)
     * @see #MarginItemDecoration(MarginItemDecoration)
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
     * @see #MarginItemDecoration(MarginItemDecoration)
     */
    public MarginItemDecoration(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        this.leftMargin   = leftMargin;
        this.topMargin    = topMargin;
        this.rightMargin  = rightMargin;
        this.bottomMargin = bottomMargin;
    }

    public final void dump(Printer printer) {
        printer.println(new StringBuilder(80)
            .append("MarginItemDecoration { leftMargin = ").append(leftMargin)
            .append(", topMargin = ").append(topMargin)
            .append(", rightMargin = ").append(rightMargin)
            .append(", bottomMargin = ").append(bottomMargin)
            .append(" }").toString());
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        outRect.set(leftMargin, topMargin, rightMargin, bottomMargin);
    }
}
