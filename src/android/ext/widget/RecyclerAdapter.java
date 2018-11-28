package android.ext.widget;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;

/**
 * Abstract class RecyclerAdapter
 * @author Garfield
 */
public abstract class RecyclerAdapter<VH extends ViewHolder> extends Adapter<VH> implements OnFocusChangeListener {
    /**
     * Indicates this adapter item can receive the focus.
     */
    public static final int FLAG_ITEM_FOCUSABLE = 0x001;

    /**
     * Indicates the <tt>RecyclerView</tt> can receive the focus.
     */
    public static final int FLAG_VIEW_FOCUSABLE = FocusManager.FLAG_VIEW_FOCUSABLE;

    /**
     * Indicates the <tt>RecyclerView</tt> has the drawing order flag.
     */
    public static final int FLAG_CHILD_DRAWING_ORDER = FocusManager.FLAG_CHILD_DRAWING_ORDER;

    /* package */ final AnimatorManager mAnimatorManager;
    /* package */ final FocusManager<RecyclerView> mFocusManager;

    /**
     * Constructor
     * @param view The {@link RecyclerView}.
     * @param flags Flags used to determine the behavior of this adapter.
     * May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @see #RecyclerAdapter(RecyclerView, AnimatorManager, int)
     */
    public RecyclerAdapter(RecyclerView view, int flags) {
        this(view, null, flags);
    }

    /**
     * Constructor
     * @param view The {@link RecyclerView}.
     * @param animatorManager May be <tt>null</tt>. The {@link AnimatorManager}.
     * @param flags Flags used to determine the behavior of this adapter. May be
     * <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @see #RecyclerAdapter(RecyclerView, int)
     */
    public RecyclerAdapter(RecyclerView view, AnimatorManager animatorManager, int flags) {
        mAnimatorManager = animatorManager;
        mFocusManager = new FocusManager<RecyclerView>(view, flags);
    }

    /**
     * Returns the {@link RecyclerView} associated with this adapter.
     * @return The <tt>RecyclerView</tt>.
     */
    public final RecyclerView getRecyclerView() {
        return mFocusManager.mRootView;
    }

    /**
     * Returns the {@link FocusManager} associated with this adapter.
     * @return The <tt>FocusManager</tt>.
     */
    public final FocusManager<RecyclerView> getFocusManager() {
        return mFocusManager;
    }

    /**
     * Returns the {@link AnimatorManager} associated with this adapter.
     * @return The <tt>AnimatorManager</tt> or <tt>null</tt>.
     */
    public final AnimatorManager getAnimatorManager() {
        return mAnimatorManager;
    }

    /**
     * Called when this adapter is no longer used.
     */
    public void onDestroy() {
        mFocusManager.onDetach();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        final VH viewHolder = onCreateViewHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
        if ((mFocusManager.mFlags & FLAG_ITEM_FOCUSABLE) != 0) {
            viewHolder.itemView.setFocusable(true);
            viewHolder.itemView.setOnFocusChangeListener(this);
        }

        return viewHolder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onFocusChange(View view, boolean hasFocus) {
        mFocusManager.onChildFocusChange(view, hasFocus, mAnimatorManager);
        onItemFocusChange((VH)mFocusManager.mRootView.getChildViewHolder(view), hasFocus);
    }

    /**
     * Called when the focus state of an item view has changed.
     * @param viewHolder The {@link ViewHolder#itemView} state has changed.
     * @param hasFocus The new focus state of the {@link ViewHolder#itemView}.
     */
    protected void onItemFocusChange(VH viewHolder, boolean hasFocus) {
    }

    /**
     * Called when <tt>RecyclerView</tt> needs a new {@link ViewHolder} of the given type to represent an item.
     * <p>The new <tt>ViewHolder</tt> should be constructed with a new View that can represent the items of the
     * given type. You can either create a new View manually or inflate it from an XML layout file.<p>
     * @param inflater The <tt>LayoutInflater</tt> object that can be used to inflate any views.
     * @param parent The <tt>ViewGroup</tt> into which the new <tt>View</tt> will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new <tt>View</tt>.
     * @return A new <tt>ViewHolder</tt> that holds a <tt>View</tt> of the given view type.
     */
    protected abstract VH onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);
}
