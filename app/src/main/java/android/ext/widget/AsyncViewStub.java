package android.ext.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.content.AsyncTask;
import android.ext.util.DebugUtils;
import android.graphics.Canvas;
import android.os.Process;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * Like as {@link ViewStub}, but this class can be inflated a layout resource on a background thread.
 * <ul><li>For a layout to be inflated asynchronously it needs to have a parent whose
 * {@link ViewGroup#generateLayoutParams(AttributeSet)} is thread-safely and all the <tt>Views</tt>
 * being constructed as part of inflation must NOT call {@link Looper#myLooper()}.</li>
 * <li>This <tt>AsyncViewStub</tt> does not support inflating layouts that contain fragments.</li></ul>
 * @author Garfield
 */
public final class AsyncViewStub extends View {
    private static final int[] VIEW_STUB_ATTRS = {
        android.R.attr.id,
        android.R.attr.layout,
        android.R.attr.inflatedId,
    };

    private static final int ID_INDEX     = 0;
    private static final int LAYOUT_INDEX = 1;
    private static final int INFLATED_ID_INDEX = 2;

    /* package */ int mLayoutId;
    /* package */ int mInflatedId;

    public AsyncViewStub(Context context) {
        super(context);

        setVisibility(GONE);
        setWillNotDraw(true);
    }

    public AsyncViewStub(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncViewStub(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context);

        final TypedArray a = context.obtainStyledAttributes(attrs, VIEW_STUB_ATTRS, defStyleAttr, 0);
        setId(a.getResourceId(ID_INDEX /* android.R.attr.id */, NO_ID));
        mLayoutId   = a.getResourceId(LAYOUT_INDEX /* android.R.attr.layout */, 0);
        mInflatedId = a.getResourceId(INFLATED_ID_INDEX /* android.R.attr.inflatedId */, NO_ID);
        a.recycle();
    }

    /**
     * Returns the id taken by the inflated view. If the inflated id
     * is {@link #NO_ID}, the inflated view keeps its original id.
     * @return A positive integer used to identify the inflated view
     * or {@link #NO_ID} if the inflated view should keep its id.
     * @see #setInflatedId(int)
     */
    public final int getInflatedId() {
        return mInflatedId;
    }

    /**
     * Defines the id taken by the inflated view. If the inflated id is
     * {@link #NO_ID}, the inflated view keeps its original id.
     * @param inflatedId A positive integer used to identify the inflated
     * view or {@link #NO_ID} if the inflated view should keep its id.
     * @see #getInflatedId()
     */
    public final void setInflatedId(int inflatedId) {
        mInflatedId = inflatedId;
    }

    /**
     * Returns the layout resource that will be used by {@link #inflate} to
     * replace this <tt>AsyncViewStub</tt> in its parent.
     * @return The layout resource id used to inflate the new <tt>View</tt>.
     * @see #setLayoutResource(int)
     */
    public final int getLayoutResource() {
        return mLayoutId;
    }

    /**
     * Specifies the layout resource to inflate when {@link #inflate} is invoked. The
     * <tt>View</tt> created by inflating the layout resource is used to replace this
     * <tt>AsyncViewStub</tt> in its parent.
     * @param layoutId A valid layout resource id.
     * @see #getLayoutResource()
     */
    public final void setLayoutResource(int layoutId) {
        mLayoutId = layoutId;
    }

    /**
     * Inflates the layout resource on a background thread and replaces this <tt>AsyncViewStub</tt> in its
     * parent by the inflated <tt>View</tt> on the UI thread.
     * @param listener May be <tt>null</tt>. The {@link OnInflateListener} to notify of successful inflation.
     */
    public final void inflate(OnInflateListener listener) {
        new AsyncInflateTask(getContext()).execute(this, listener);
    }

    @Override
    @SuppressLint("MissingSuperCall")
    public void draw(Canvas canvas) {
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    /* package */ final void onFinishInflate(ViewGroup parent, View view, OnInflateListener listener) {
        // Sets the inflated view id.
        if (mInflatedId != NO_ID) {
            view.setId(mInflatedId);
        }

        // Removes the AsyncViewStub from its parent.
        final int index = parent.indexOfChild(this);
        parent.removeViewsInLayout(index, 1);

        // Adds the inflated view to its parent.
        final LayoutParams params = getLayoutParams();
        if (params == null) {
            parent.addView(view, index);
        } else {
            parent.addView(view, index, params);
        }

        if (listener != null) {
            listener.onFinishInflate(this, view, mLayoutId);
        }
    }

    /**
     * Callback interface used to receive a notification after an {@link AsyncViewStub}
     * has successfully inflated its layout resource.
     */
    public static interface OnInflateListener {
        /**
         * Called on the UI thread after an {@link AsyncViewStub} has successfully
         * inflated its layout resource.
         * @param stub The <tt>AsyncViewStub</tt> whose inflated its layout resource.
         * @param view The inflated <tt>View</tt>.
         * @param layoutId The layout resource id to inflate.
         */
        void onFinishInflate(AsyncViewStub stub, View view, int layoutId);
    }

    /**
     * Class <tt>AsyncInflateTask</tt> is an implementation of an {@link AsyncTask}.
     */
    private static final class AsyncInflateTask extends AsyncTask<Object, Object, View> {
        private final LayoutInflater mInflater;

        public AsyncInflateTask(Context context) {
            mInflater = new BasicLayoutInflater(context);
        }

        @Override
        protected View doInBackground(Object[] params) {
            final int priority = Process.getThreadPriority(Process.myTid());
            final AsyncViewStub viewStub = (AsyncViewStub)params[0];
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                return mInflater.inflate(viewStub.mLayoutId, (ViewGroup)viewStub.getParent(), false);
            } catch (RuntimeException e) {
                DebugUtils.__checkWarning(true, AsyncViewStub.class.getName(), "Failed to inflate resource - ID #0x" + Integer.toHexString(viewStub.mLayoutId) + " in the background! Retrying on the UI thread\n" + e);
                return null;
            } finally {
                Process.setThreadPriority(priority);
            }
        }

        @Override
        protected void onPostExecute(Object[] params, View view) {
            final AsyncViewStub viewStub = (AsyncViewStub)params[0];
            final ViewGroup parent = (ViewGroup)viewStub.getParent();
            if (parent == null) {
                DebugUtils.__checkWarning(true, AsyncViewStub.class.getName(), "The AsyncViewStub (ID #0x" + Integer.toHexString(viewStub.getId()) + ") has no parent, can not add the inflated view to its parent.");
                return;
            }

            if (view == null) {
                // Failed to inflate mLayoutId in the background, inflating it on the UI thread.
                view = mInflater.inflate(viewStub.mLayoutId, parent, false);
            }

            viewStub.onFinishInflate(parent, view, (OnInflateListener)params[1]);
        }
    }

    /**
     * Class <tt>BasicLayoutInflater</tt> is an implementation of a {@link LayoutInflater}.
     */
    private static final class BasicLayoutInflater extends LayoutInflater {
        private static final String[] sPrefixes = {
            "android.widget.",
            "android.app.",
            "android.webkit.",
        };

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public BasicLayoutInflater(Context context) {
            super(context);
        }

        @Override
        public LayoutInflater cloneInContext(Context context) {
            return new BasicLayoutInflater(context);
        }

        @Override
        protected View onCreateView(String name, AttributeSet attrs) throws ClassNotFoundException {
            for (String prefix : sPrefixes) {
                try {
                    final View view = createView(name, prefix, attrs);
                    if (view != null) {
                        return view;
                    }
                } catch (ClassNotFoundException e) {
                    // In this case we want to let the base class take a crack at it.
                }
            }

            return super.onCreateView(name, attrs);
        }
    }
}
