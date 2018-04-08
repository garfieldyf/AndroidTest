package android.ext.widget;

import java.util.ArrayList;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;

/**
 * Class FocusManager
 * @author Garfield
 * @version 1.0
 */
public final class FocusManager<T extends ViewGroup> implements OnFocusChangeListener, OnGlobalFocusChangeListener {
    /**
     * Indicates the root view can receive the focus.
     */
    public static final int FLAG_VIEW_FOCUSABLE = 0x00100000;

    /**
     * Indicates the root view has the drawing order flag.
     */
    public static final int FLAG_CHILD_DRAWING_ORDER = 0x00200000;

    /**
     * Indicates the root view will block any of its descendants will add focusables.
     */
    private static final int FLAG_BLOCK_DESCENDANTS = 0x00400000;

    /* package */ int mFlags;
    /* package */ View mFocused;
    /* package */ final T mRootView;

    /**
     * Constructor
     * @param rootView The root <tt>View</tt>.
     * @param flags Flags used to determine the behavior of this manager.
     * May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     */
    public FocusManager(T rootView, int flags) {
        mFlags = flags;
        mRootView = rootView;
        if ((flags & FLAG_VIEW_FOCUSABLE) != 0) {
            rootView.setFocusable(true);
            rootView.setOnFocusChangeListener(this);
            rootView.getViewTreeObserver().addOnGlobalFocusChangeListener(this);
        }
    }

    /**
     * Returns the root view associated with this manager.
     * @return The root <tt>View</tt>.
     */
    public T getRootView() {
        return mRootView;
    }

    /**
     * Returns the focused child in the root view.
     * @return The focused child or <tt>null</tt>.
     * @see #setFocusedChild(View)
     */
    public View getFocusedChild() {
        return mFocused;
    }

    /**
     * Sets the focused child in the root view.
     * @param focused The focused child.
     * @see #getFocusedChild()
     */
    public void setFocusedChild(View focused) {
        mFocused = focused;
    }

    /**
     * Called when this manager is no longer attached to the root view.
     * <p>Note: This method recommended call in view <tt>onDetachedFromWindow()</tt>
     * or activity <tt>onDestroy()</tt>.</p>
     */
    public void onDetach() {
        if ((mFlags & FLAG_VIEW_FOCUSABLE) != 0) {
            mRootView.getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
        }
    }

    /**
     * Returns the index of the child to draw for this iteration.
     * @param childCount The number of child to draw.
     * @param i The current iteration.
     * @return The index of the child to draw this iteration.
     */
    public int getChildDrawingOrder(int childCount, int i) {
        final View focused = mRootView.getFocusedChild();
        if (focused != null) {
            if (mRootView.getChildAt(i) == focused) {
                // Move the focused child order to last.
                return childCount - 1;
            } else if (i == childCount - 1) {
                // Move the last child order to the focused child order.
                return mRootView.indexOfChild(focused);
            }
        }

        return i;
    }

    /**
     * Sets the root view will block any of its descendants will add focusables.
     * @param blockDescendants Whether to block the root view descendants.
     * @see #onAddFocusables(ArrayList, int, int)
     */
    public void setFocusBlockDescendants(boolean blockDescendants) {
        if (blockDescendants) {
            mFlags |= FLAG_BLOCK_DESCENDANTS;
        } else {
            mFlags &= ~FLAG_BLOCK_DESCENDANTS;
        }
    }

    /**
     * Called to populate focusable views within the root view. <p>Note: This method
     * recommended call in {@link View#addFocusables(ArrayList, int, int)}.</p>
     * @param views The <tt>List</tt> of output views.
     * @param direction One of {@link View#FOCUS_UP}, {@link View#FOCUS_DOWN}, {@link View#FOCUS_LEFT},
     * {@link View#FOCUS_RIGHT}, {@link View#FOCUS_BACKWARD}, {@link View#FOCUS_FORWARD}.
     * @param focusableMode The type of focusables to be added.
     * @return Returns <tt>true</tt> to skip the default behavior, <tt>false</tt> to
     * add default focusables after this method returns.
     * @see #setFocusBlockDescendants(boolean)
     */
    public boolean onAddFocusables(ArrayList<View> views, int direction, int focusableMode) {
        return ((mFlags & FLAG_BLOCK_DESCENDANTS) != 0 && mRootView.isFocusable() && views.add(mRootView));
    }

    /**
     * Called when the focus state of a child view has changed. <p>Note: This
     * method recommended call in the child view <tt>onFocusChange</tt>.</p>
     * @param child The child view whose foucs state has changed.
     * @param hasFocus The new focus state of <em>child</em>.
     * @param animatorManager The {@link AnimatorManager}. May be <tt>null</tt>.
     */
    public void onChildFocusChange(View child, boolean hasFocus, AnimatorManager animatorManager) {
        // Save the focus child view.
        if (hasFocus) {
            mFocused = child;
        }

        // Start the focus state animation.
        if (animatorManager != null) {
            if (hasFocus && (mFlags & FLAG_CHILD_DRAWING_ORDER) != 0) {
                mRootView.invalidate();
            }

            animatorManager.startAnimation(child, hasFocus);
        }
    }

    /**
     * Called when the focus state of a root view has changed. <p>Note: This
     * method recommended call in the root view <tt>onFocusChange</tt>.</p>
     * @param view The view whose foucs state has changed.
     * @param hasFocus The new focus state of <em>view</em>.
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            if (mFocused == null) {
                mFocused = mRootView.getChildAt(0);
            }

            if (mFocused != null) {
                mFocused.requestFocus();
            }
        }
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        setFocusBlockDescendants(mRootView.getFocusedChild() == null);
    }
}
