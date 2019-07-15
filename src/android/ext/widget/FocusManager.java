package android.ext.widget;

import java.util.ArrayList;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;

/**
 * Class FocusManager
 * @author Garfield
 */
public final class FocusManager implements OnFocusChangeListener, OnGlobalFocusChangeListener {
    private View mFocused;
    private boolean mBlockDescendants;
    private final ViewGroup mRootView;

    /**
     * Constructor
     * @param rootView The root <tt>View</tt>.
     */
    public FocusManager(ViewGroup rootView) {
        mRootView = rootView;
        rootView.setFocusable(true);
        rootView.setOnFocusChangeListener(this);
        rootView.getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }

    /**
     * Returns the root view associated with this manager.
     * @return The root <tt>View</tt>.
     */
    @SuppressWarnings("unchecked")
    public <T extends ViewGroup> T getRootView() {
        return (T)mRootView;
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
    public void onDestroy() {
        mRootView.getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
    }

    /**
     * Called to populate focusable views within the root view. <p>Note: This method
     * recommended call in {@link View#addFocusables(ArrayList, int, int)}.</p>
     * @param views The <tt>List</tt> of output views.
     * @param direction The direction of the focus.
     * @param focusableMode The type of focusables to be added.
     * @return Returns <tt>true</tt> to skip the default behavior, <tt>false</tt> to
     * add default focusables after this method returns.
     */
    public boolean onAddFocusables(ArrayList<View> views, int direction, int focusableMode) {
        return (mBlockDescendants && mRootView.isFocusable() && views.add(mRootView));
    }

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
        mBlockDescendants = (mRootView.getFocusedChild() == null);
    }
}
