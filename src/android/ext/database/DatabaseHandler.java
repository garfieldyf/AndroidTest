package android.ext.database;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import android.app.Activity;
import android.database.Cursor;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;

/**
 * Abstract class <tt>DatabaseHandler</tt>.
 * @author Garfield
 */
public abstract class DatabaseHandler implements Runnable, Factory<Runnable> {
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

    /* package */ WeakReference<Object> mOwner;
    /* package */ final Executor mExecutor;
    /* package */ final Pool<Runnable> mTaskPool;

    /**
     * Constructor
     * @param executor The serial <tt>Executor</tt>.
     * @see #DatabaseHandler(Executor, Object)
     */
    /* package */ DatabaseHandler(Executor executor) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mExecutor = executor;
        mTaskPool = Pools.synchronizedPool(Pools.newPool(this, 8));
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
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this handler.
     * @return The owner object or <tt>null</tt>
     * if the owner released by the GC.
     * @see #setOwner(Object)
     */
    @SuppressWarnings("unchecked")
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    @Override
    public final void run() {
        throw new RuntimeException("No Implementation, This method is a stub!");
    }

    /**
     * Called on the UI thread when this handler handle messages.
     * <p><b>Note: Do not call this method directly.</b></p>
     * @param message The message.
     * @param token The token to identify the operation.
     * @param result The result.
     */
    public void dispatchMessage(int message, int token, Object result) {
        switch (message) {
        case MESSAGE_EXECUTE:
            onExecuteComplete(token, result);
            break;

        case MESSAGE_UPDATE:
            onUpdateComplete(token, (int)result);
            break;

        case MESSAGE_DELETE:
            onDeleteComplete(token, (int)result);
            break;

        case MESSAGE_QUERY:
        case MESSAGE_RAWQUERY:
            onQueryComplete(token, (Cursor)result);
            break;

        default:
            throw new IllegalStateException("Unknown message: " + message);
        }
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
     * Validates the owner <tt>Object</tt> is valid.
     */
    /* package */ final boolean validateOwner() {
        if (mOwner != null) {
            final Object owner = mOwner.get();
            if (owner == null) {
                DebugUtils.__checkDebug(true, getClass().getSimpleName(), "The " + owner + " released by the GC.");
                return false;
            } else if (owner instanceof Activity) {
                final Activity activity = (Activity)owner;
                DebugUtils.__checkDebug(activity.isFinishing() || activity.isDestroyed(), getClass().getSimpleName(), "The " + activity + " has been destroyed.");
                return (!activity.isFinishing() && !activity.isDestroyed());
            }
        }

        return true;
    }
}
