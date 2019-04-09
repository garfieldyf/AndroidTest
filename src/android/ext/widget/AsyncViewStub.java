package android.ext.widget;

import java.util.concurrent.Executor;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * Like as {@link ViewStub}, but this class can be inflated a layout resource on a background thread.
 * <p>Note: For a layout to be inflated asynchronously it needs to have a parent whose
 * {@link ViewGroup#generateLayoutParams(AttributeSet)} is thread-safely and all the <tt>Views</tt>
 * being constructed as part of inflation must NOT call {@link Looper#myLooper()}.</p>
 * <p>Note: This <tt>AsyncViewStub</tt> does not support inflating layouts that contain fragments.</p>
 * @author Garfield
 */
public final class AsyncViewStub extends View {
    private static final int[] VIEW_STUB_ATTRS = {
        android.R.attr.id,
        android.R.attr.layout,
        android.R.attr.inflatedId,
    };

    /* package */ int mLayoutId;
    /* package */ int mInflatedId;

    public AsyncViewStub(Context context) {
        this(context, null, 0);
    }

    public AsyncViewStub(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsyncViewStub(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);

        final TypedArray a = context.obtainStyledAttributes(attrs, VIEW_STUB_ATTRS, defStyleAttr, 0);
        setId(a.getResourceId(0 /* android.R.attr.id */, NO_ID));
        mLayoutId   = a.getResourceId(1 /* android.R.attr.layout */, 0);
        mInflatedId = a.getResourceId(2 /* android.R.attr.inflatedId */, NO_ID);
        a.recycle();

        setVisibility(GONE);
        setWillNotDraw(true);
    }

    /**
     * Returns the id taken by the inflated view. If the inflated id
     * is {@link View#NO_ID}, the inflated view keeps its original id.
     * @return A positive integer used to identify the inflated view
     * or {@link View#NO_ID} if the inflated view should keep its id.
     * @see #setInflatedId(int)
     */
    public final int getInflatedId() {
        return mInflatedId;
    }

    /**
     * Defines the id taken by the inflated view. If the inflated id is
     * {@link View#NO_ID}, the inflated view keeps its original id.
     * @param inflatedId A positive integer used to identify the inflated
     * view or {@link View#NO_ID} if the inflated view should keep its id.
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
     * @see #inflate(Executor, OnInflateListener)
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
     * @see #inflate(Executor, OnInflateListener)
     */
    public final void setLayoutResource(int layoutId) {
        mLayoutId = layoutId;
    }

    /**
     * Inflates the layout resource on a background thread and replaces this <tt>AsyncViewStub</tt> in its
     * parent by the inflated <tt>View</tt> on the UI thread.
     * @param executor The <tt>Executor</tt> to executing inflation.
     * @param listener May be <tt>null</tt>. The {@link OnInflateListener} to notify of successful inflation.
     */
    public final void inflate(Executor executor, OnInflateListener listener) {
        final ViewGroup parent = (ViewGroup)getParent();
        DebugUtils.__checkError(mLayoutId == 0, "AsyncViewStub must have a valid layout resource id");
        DebugUtils.__checkError(parent == null, "AsyncViewStub must have a non-null ViewGroup parent");
        new AsyncInflateTask(parent, listener).executeOnExecutor(executor, (Object[])null);
    }

    @Override
    public void draw(Canvas canvas) {
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    /**
     * Called on the UI thread after this <tt>AsyncViewStub</tt> has successfully inflated its layout resource.
     */
    /* package */ final void onFinishInflate(View view, ViewGroup parent) {
        // Sets the inflated view id.
        if (mInflatedId != NO_ID) {
            view.setId(mInflatedId);
        }

        // Removes this AsyncViewStub from its parent.
        final int index = parent.indexOfChild(this);
        parent.removeViewsInLayout(index, 1);

        // Adds the inflated view to its parent.
        final LayoutParams params = getLayoutParams();
        if (params == null) {
            parent.addView(view, index);
        } else {
            parent.addView(view, index, params);
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
    private final class AsyncInflateTask extends AsyncTask<Object, Object, View> {
        private final ViewGroup mParent;
        private final LayoutInflater mInflater;
        private final OnInflateListener mListener;

        /**
         * Constructor
         * @param parent The <tt>ViewGroup</tt> to be the parent of the generated hierarchy.
         * @param listener The {@link OnInflateListener} to notify of successful inflation.
         */
        public AsyncInflateTask(ViewGroup parent, OnInflateListener listener) {
            mParent   = parent;
            mListener = listener;
            mInflater = new BasicLayoutInflater(getContext());
        }

        @Override
        protected View doInBackground(Object... params) {
            try {
                return mInflater.inflate(mLayoutId, mParent, false);
            } catch (RuntimeException e) {
                Log.e(AsyncViewStub.class.getName(), "Failed to inflate resource - ID #0x" + Integer.toHexString(mLayoutId) + " in the background! Retrying on the UI thread - parent = " + mParent.getClass().getSimpleName(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(View result) {
            // Failed to inflate mLayoutId in the background.
            // Inflating it on the UI thread.
            if (result == null) {
                result = mInflater.inflate(mLayoutId, mParent, false);
            }

            onFinishInflate(result, mParent);
            if (mListener != null) {
                mListener.onFinishInflate(AsyncViewStub.this, result, mLayoutId);
            }
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
