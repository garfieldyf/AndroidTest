package android.ext.widget;

import java.util.ArrayList;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;

/**
 * Class FocusManager
 * @author Garfield
 */
public class FocusManager implements OnFocusChangeListener {
    private final ViewGroup mRootView;
    private View mFocused;
    private OnItemSelectedListener mListener;

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
    public <T extends ViewGroup> T getRootView() {
        return (T)mRootView;
    }

    /**
     * Returns the focused child in the root view.
     * @return The focused child or <tt>null</tt>.
     * @see #setFocusedChild(View, boolean)
     */
    public View getFocusedChild() {
        return mFocused;
    }

    /**
     * Sets the focused child in the root view.
     * @param focused The focused child.
     * @param changeSelection Whether to change the
     * selection state of <em>focused</em> view.
     * @see #getFocusedChild()
     */
    public void setFocusedChild(View focused, boolean changeSelection) {
        if (mFocused != focused) {
            final View oldFocused = mFocused;
            mFocused = focused;
            if (changeSelection) {
                if (oldFocused != null) {
                    oldFocused.setSelected(false);
                }

                if (focused != null) {
                    focused.setSelected(true);
                }

                if (mListener != null) {
                    mListener.onItemSelected(mRootView, focused, oldFocused);
                }
            }
        }
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

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
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
     * Callback interface to be invoked when a child view has been selected.
     */
    public static interface OnItemSelectedListener {
        /**
         * Callback method to be invoked when a child view has been selected.
         * @param parent The {@link ViewGroup} where the selection happened.
         * @param newView The newly selected <tt>View</tt>, or <tt>null</tt>.
         * @param oldView The previously selected <tt>View</tt>, or <tt>null</tt>.
         */
        void onItemSelected(ViewGroup parent, View newView, View oldView);
    }
}
