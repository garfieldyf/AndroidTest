package android.ext.database;

import android.annotation.SuppressLint;
import android.annotation.UiThread;
import android.app.Activity;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleOwner;
import android.content.ContentProviderResult;
import android.database.Cursor;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.ext.widget.UIHandler.MessageRunnable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Printer;
import java.lang.ref.WeakReference;

/**
 * Abstract class <tt>DatabaseHandler</tt>.
 * @author Garfield
 */
@SuppressLint("RestrictedApi")
/* package */ abstract class DatabaseHandler implements Factory<Object>, GenericLifecycleObserver {
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

    /**
     * Indicates the onwer has been destroyed.
     */
    private boolean mDestroyed;

    /**
     * The onwer that owns this handler.
     */
    private WeakReference<Object> mOwner;

    /**
     * The {@link AbsSQLiteTask} pool.
     */
    /* package */ final Pool<Object> mTaskPool;

    /**
     * Constructor
     */
    /* package */ DatabaseHandler() {
        DebugUtils.__checkMemoryLeaks(getClass());
        mTaskPool = Pools.newPool(this, 8);
    }

    /**
     * Sets the object that owns this handler.
     * @param owner May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
     * @see #getOwner()
     */
    public final void setOwner(Object owner) {
        DebugUtils.__checkUIThread("setOwner");
        DebugUtils.__checkError(owner == null, "Invalid parameter - owner == null");
        DebugUtils.__checkError(mOwner != null, "The owner is already exists (a DatabaseHandler can be set owner only once)");
        mOwner = new WeakReference<Object>(owner);
        addLifecycleObserver(owner);
    }

    @UiThread
    public final void dump(Printer printer) {
        DebugUtils.__checkUIThread("dump");
        Pools.dumpPool(mTaskPool, printer);
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Event event) {
        if (event == Event.ON_DESTROY) {
            mDestroyed = true;
            removeLifecycleObserver();
            DebugUtils.__checkDebug(true, getClass().getName(), "The LifecycleOwner - " + DeviceUtils.toString(source) + " has been destroyed.");
        }
    }

    /**
     * Returns the object that owns this handler.
     * @return The owner object or <tt>null</tt> if the owner released by the GC.
     * @see #setOwner(Object)
     */
    @SuppressWarnings("unchecked")
    protected final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    /**
     * Called when this handler has been destroyed on the UI thread.
     * @param token The token.
     * @param result The result.
     */
    @UiThread
    protected void onDestroy(int token, Object result) {
        DebugUtils.__checkDebug(true, getClass().getName(), "onDestroy token = " + token + ", result = " + result);
        // Closes the Cursor to avoid memory leak.
        if (result instanceof Cursor) {
            ((Cursor)result).close();
        }
    }

    /**
     * Called when an asynchronous call is completed on the UI thread.
     * @param token The token to identify the call, passed in from {@link #startCall}.
     * @param result A result <tt>Bundle</tt> holding the results from the call.
     */
    @UiThread
    protected void onCallComplete(int token, Bundle result) {
    }

    /**
     * Called when an asynchronous execute is completed on the UI thread.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param result The result, returned earlier by {@link #onExecute}.
     */
    @UiThread
    protected void onExecuteComplete(int token, Object result) {
    }

    /**
     * Called when an asynchronous query is completed on the UI thread.
     * @param token The token to identify the query, passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     */
    @UiThread
    protected void onQueryComplete(int token, Cursor cursor) {
    }

    /**
     * Called when an asynchronous replace is completed on the UI thread.
     * @param token The token to identify the replace, passed in from {@link #startReplace}.
     * @param id The row ID of the newly inserted row, or -1 if an error occurred.
     */
    @UiThread
    protected void onReplaceComplete(int token, long id) {
    }

    /**
     * Called when an asynchronous insert is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startInsert}.
     * @param id The row ID of the newly inserted row, or -1 if an error occurred.
     */
    @UiThread
    protected void onInsertComplete(int token, long id) {
    }

    /**
     * Called when an asynchronous insert is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startInsert}.
     * @param newUri The URL of the newly created row.
     */
    @UiThread
    protected void onInsertComplete(int token, Uri newUri) {
    }

    /**
     * Called when an asynchronous update is completed on the UI thread.
     * @param token The token to identify the update, passed in from {@link #startUpdate}.
     * @param rowsAffected The number of rows affected.
     */
    @UiThread
    protected void onUpdateComplete(int token, int rowsAffected) {
    }

    /**
     * Called when an asynchronous delete is completed on the UI thread.
     * @param token The token to identify the delete, passed in from {@link #startDelete}.
     * @param rowsAffected The number of rows affected.
     */
    @UiThread
    protected void onDeleteComplete(int token, int rowsAffected) {
    }

    /**
     * Called when an asynchronous multiple inserts is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startBulkInsert}.
     * @param newRows The number of newly created rows.
     */
    @UiThread
    protected void onBulkInsertComplete(int token, int newRows) {
    }

    /**
     * Called when an asynchronous apply is completed on the UI thread.
     * @param token The token to identify the apply, passed in from {@link #startApplyBatch}.
     * @param results The results of the applications.
     */
    @UiThread
    protected void onApplyBatchComplete(int token, ContentProviderResult[] results) {
    }

    /**
     * Tests if this handler is destroyed.
     */
    @UiThread
    /* package */ final boolean isDestroyed() {
        if (mDestroyed) {
            return true;
        }

        if (mOwner != null) {
            final Object owner = mOwner.get();
            if (owner instanceof Activity) {
                final Activity activity = (Activity)owner;
                return (activity.isFinishing() || activity.isDestroyed());
            }
        }

        return false;
    }

    /**
     * Adds a <tt>LifecycleObserver</tt> that will be notified when the <tt>Lifecycle</tt> changes state.
     */
    private void addLifecycleObserver(Object owner) {
        if (owner instanceof Lifecycle) {
            ((Lifecycle)owner).addObserver(this);
        } else if (owner instanceof LifecycleOwner) {
            ((LifecycleOwner)owner).getLifecycle().addObserver(this);
        }
    }

    /**
     * Removes the <tt>LifecycleObserver</tt> from the <tt>Lifecycle</tt>.
     */
    private void removeLifecycleObserver() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        final Object owner = mOwner.get();
        if (owner instanceof Lifecycle) {
            ((Lifecycle)owner).removeObserver(this);
        } else if (owner instanceof LifecycleOwner) {
            ((LifecycleOwner)owner).getLifecycle().removeObserver(this);
        }
    }

    /**
     * Class <tt>AbsSQLiteTask</tt> is an implementation of a {@link Runnable}.
     */
    /* package */ static abstract class AbsSQLiteTask implements MessageRunnable {
        /* package */ int token;
        /* package */ int message;
        /* package */ Object values;
        /* package */ String sortOrder;
        /* package */ String selection;
        /* package */ String[] selectionArgs;
        /* package */ DatabaseHandler handler;

        @Override
        public final void handleMessage(Message msg) {
            final Object result = msg.obj;
            if (handler.isDestroyed()) {
                handler.onDestroy(token, result);
                return;
            }

            switch (message) {
            case MESSAGE_INSERT:
                onInsertComplete(result);
                break;

            case MESSAGE_CALL:
                handler.onCallComplete(token, (Bundle)result);
                break;

            case MESSAGE_QUERY:
            case MESSAGE_RAWQUERY:
                handler.onQueryComplete(token, (Cursor)result);
                break;

            case MESSAGE_EXECUTE:
                handler.onExecuteComplete(token, result);
                break;

            case MESSAGE_UPDATE:
                handler.onUpdateComplete(token, (int)result);
                break;

            case MESSAGE_DELETE:
                handler.onDeleteComplete(token, (int)result);
                break;

            case MESSAGE_REPLACE:
                handler.onReplaceComplete(token, (long)result);
                break;

            case MESSAGE_INSERTS:
                handler.onBulkInsertComplete(token, (int)result);
                break;

            case MESSAGE_BATCH:
                handler.onApplyBatchComplete(token, (ContentProviderResult[])result);
                break;

            default:
                throw new IllegalStateException("Unknown message: " + message);
            }

            recycle(handler.mTaskPool);
        }

        private void recycle(Pool<Object> taskPool) {
            values  = null;
            handler = null;
            selectionArgs = null;
            taskPool.recycle(this);
        }

        private void onInsertComplete(Object result) {
            if (result instanceof Long) {
                handler.onInsertComplete(token, (long)result);
            } else {
                handler.onInsertComplete(token, (Uri)result);
            }
        }
    }
}
