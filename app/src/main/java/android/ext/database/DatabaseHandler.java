package android.ext.database;

import android.app.Activity;
import android.database.Cursor;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.util.Printer;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * Abstract class <tt>DatabaseHandler</tt>.
 * @author Garfield
 */
public abstract class DatabaseHandler implements Factory<Object> {
    /* package */ static final int MESSAGE_CALL     = 1;
    /* package */ static final int MESSAGE_BATCH    = 2;
    /* package */ static final int MESSAGE_QUERY    = 3;
    /* package */ static final int MESSAGE_INSERT   = 4;
    /* package */ static final int MESSAGE_UPDATE   = 5;
    /* package */ static final int MESSAGE_DELETE   = 6;
    /* package */ static final int MESSAGE_REPLACE  = 7;
    /* package */ static final int MESSAGE_INSERTS  = 8;
    /* package */ static final int MESSAGE_EXECUTE  = 9;
    /* package */ static final int MESSAGE_RAWQUERY = 10;

    /* package */ final Executor mExecutor;
    /* package */ final Pool<Object> mTaskPool;
    /* package */ WeakReference<Object> mOwner;

    /**
     * Constructor
     * @param executor The serial <tt>Executor</tt>.
     * @see #DatabaseHandler(Executor, Object)
     */
    /* package */ DatabaseHandler(Executor executor) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mExecutor = executor;
        mTaskPool = Pools.newPool(this, 8);
    }

    /**
     * Constructor
     * @param executor The serial <tt>Executor</tt>.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #DatabaseHandler(Executor)
     */
    /* package */ DatabaseHandler(Executor executor, Object owner) {
        this(executor);
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Sets the object that owns this handler.
     * @param owner The owner object.
     * @see #getOwner()
     * @see #getOwnerActivity()
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this handler.
     * @return The owner object or <tt>null</tt>
     * if the owner released by the GC.
     * @see #setOwner(Object)
     * @see #getOwnerActivity()
     */
    @SuppressWarnings("unchecked")
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    /**
     * Alias of {@link #getOwner()}.
     * @return The <tt>Activity</tt> that owns this handler or <tt>null</tt> if
     * the owner activity has been finished or destroyed or release by the GC.
     * @see #getOwner()
     * @see #setOwner(Object)
     */
    @SuppressWarnings("unchecked")
    public final <T extends Activity> T getOwnerActivity() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        final T activity = (T)mOwner.get();
        return (activity != null && !activity.isFinishing() && !activity.isDestroyed() ? activity : null);
    }

    public final void dump(Printer printer) {
        Pools.dumpPool(mTaskPool, printer);
    }

    /**
     * Called when an asynchronous execute is completed on the UI thread.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param result The result, returned earlier by {@link #onExecute}.
     */
    protected void onExecuteComplete(int token, Object result) {
    }

    /**
     * Called when an asynchronous query is completed on the UI thread.
     * @param token The token to identify the query, passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     */
    protected void onQueryComplete(int token, Cursor cursor) {
    }

    /**
     * Called when an asynchronous update is completed on the UI thread.
     * @param token The token to identify the update, passed in from {@link #startUpdate}.
     * @param rowsAffected The number of rows affected.
     */
    protected void onUpdateComplete(int token, int rowsAffected) {
    }

    /**
     * Called when an asynchronous delete is completed on the UI thread.
     * @param token The token to identify the delete, passed in from {@link #startDelete}.
     * @param rowsAffected The number of rows affected.
     */
    protected void onDeleteComplete(int token, int rowsAffected) {
    }

    /**
     * Class <tt>AbsSQLiteTask</tt> is an implementation of a {@link Runnable}.
     * @hide
     */
    public static abstract class AbsSQLiteTask implements Runnable {
        /* package */ int token;
        /* package */ int message;
        /* package */ Object values;
        /* package */ String sortOrder;
        /* package */ String selection;
        /* package */ String[] selectionArgs;

        /**
         * Clears all fields for recycle.
         */
        /* package */ final void clearForRecycle() {
            this.values = null;
            this.sortOrder = null;
            this.selection = null;
            this.selectionArgs = null;
        }

        /**
         * Called on the UI thread when this task handle
         * messages, do not call this method directly.
         * @hide
         */
        public abstract void onPostExecute(Object result);
    }
}
