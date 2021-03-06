package android.ext.widget;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import java.util.ArrayList;

/**
 * Class FocusManager
 * @author Garfield
 */
public class FocusManager implements OnFocusChangeListener {
    protected View mFocused;
    protected final ViewGroup mRootView;
    protected OnChildFocusChangeListener mListener;

    /**
     * Constructor
     * @param rootView The root <tt>View</tt>.
     */
    public FocusManager(ViewGroup rootView) {
        mRootView = rootView;
        rootView.setFocusable(true);
        rootView.setOnFocusChangeListener(this);
    }

    /**
     * Returns the root view associated with this manager.
     * @return The root <tt>View</tt>.
     */
    @SuppressWarnings("unchecked")
    public final <T extends ViewGroup> T getRootView() {
        return (T)mRootView;
    }

    /**
     * Returns the focused view in the root view.
     * @return The focused view or <tt>null</tt>.
     * @see #setFocusedView(View)
     */
    public View getFocusedView() {
        return mFocused;
    }

    /**
     * Sets the focused view in the root view.
     * @param focused The focused <tt>View</tt>.
     * @see #getFocusedView()
     */
    public void setFocusedView(View focused) {
        if (mFocused != focused) {
            final View oldFocus = mFocused;
            mFocused = focused;
            if (mListener != null) {
                mListener.onFocusChanged(mRootView, focused, oldFocus);
            }
        }
    }

    /**
     * Called when a child of the root view wants focus.
     * @param index The index of a child.
     */
    public void requestChildFocus(int index) {
        final View child = mRootView.getChildAt(index);
        if (child != null && child.requestFocus()) {
            setFocusedView(child);
        }
    }

    /**
     * Give focus to the previous focus child in the root view.
     * @return Whether the child actually took focus.
     */
    public boolean restoreChildFocus() {
        return (mRootView.getChildCount() > 0 && mRootView.getFocusedChild() == null && mFocused != null && mFocused.requestFocus());
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
        return (mRootView.getFocusedChild() == null && views.add(mRootView));
    }

    public final void setOnChildFocusChangeListener(OnChildFocusChangeListener listener) {
        mListener = listener;
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

    /**
     * Callback interface to be invoked when a child view focus state changes.
     */
    public static interface OnChildFocusChangeListener {
        /**
         * Callback method to be invoked when a child view focus state changes.
         * @param parent The {@link ViewGroup} whose child view focus state changes.
         * @param newFocus The newly focused view, or <tt>null</tt>.
         * @param oldFocus The previously focused view, or <tt>null</tt>.
         */
        void onFocusChanged(ViewGroup parent, View newFocus, View oldFocus);
    }
}
