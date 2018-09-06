package android.ext.database;

import java.util.ArrayList;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.ext.util.UIHandler;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

/**
 * Class <tt>AsyncQueryHandler</tt> is a helper class to help make
 * handling asynchronous {@link ContentResolver} queries easier.
 * @author Garfield
 * @version 1.0
 */
public abstract class AsyncQueryHandler extends DatabaseHandler<Uri> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @see #AsyncQueryHandler(Context, Object)
     */
    public AsyncQueryHandler(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncQueryHandler(Context)
     */
    public AsyncQueryHandler(Context context, Object owner) {
        super(owner);
        mContext = context.getApplicationContext();
    }

    /**
     * This method begins an asynchronous call a provider-defined method. When the call
     * is done {@link #onCallComplete} is called.
     * @param token A token passed into {@link #onCallComplete} to identify the call.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param method The provider-defined method name to call.
     * @param arg The provider-defined <tt>String</tt> argument. May be <tt>null</tt>.
     * @param extras The provider-defined <tt>Bundle</tt> argument. May be <tt>null</tt>.
     */
    public final void startCall(int token, Uri uri, String method, String arg, Bundle extras) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_CALL
         * msg.obj  - { uri, method, arg, extras }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_CALL, 0, new Object[] { uri, method, arg, extras }));
    }

    /**
     * This method begins an asynchronous insert. When the insert is done {@link #onInsertComplete} is called.
     * @param token A token passed into {@link #onInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The keys should be the
     * column names and the values the column values. Passing an empty ContentValues will create an empty row.
     */
    public final void startInsert(int token, Uri uri, ContentValues values) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_INSERT
         * msg.obj  - { uri, values }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_INSERT, 0, new Object[] { uri, values }));
    }

    /**
     * This method begins an asynchronous insert multiple rows into a given <tt>uri</tt>.
     * When the insert is done {@link #onBulkInsertComplete} is called.
     * @param token A token passed into {@link #onBulkInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The
     * keys should be the column names and the values the column values.
     */
    public final void startBulkInsert(int token, Uri uri, ContentValues[] values) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_INSERTS
         * msg.obj  - { uri, values }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_INSERTS, 0, new Object[] { uri, values }));
    }

    /**
     * This method begins an asynchronous apply each of the <tt>ContentProviderOperation</tt> objects.
     * When the apply is done {@link #onApplyBatchComplete} is called.
     * @param token A token passed into {@link #onApplyBatchComplete} to identify the apply.
     * @param authority The authority of the <tt>ContentProvider</tt> to which this batch should be applied.
     * @param operations The operations to apply.
     */
    public final void startApplyBatch(int token, String authority, ArrayList<ContentProviderOperation> operations) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_BATCH
         * msg.obj  - { authority, operations }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_BATCH, 0, new Object[] { authority, operations }));
    }

    /**
     * Returns the {@link ContentResolver} associated with this object.
     * @return The <tt>ContentResolver</tt>.
     */
    public final ContentResolver getContentResolver() {
        return mContext.getContentResolver();
    }

    @Override
    public boolean handleMessage(Message msg) {
        final ContentResolver resolver = mContext.getContentResolver();
        /*
         * msg.what - token
         * msg.arg1 - message
         * msg.obj  - params
         */
        final Object[] params = (Object[])msg.obj;
        final Object result;
        switch (msg.arg1) {
        case MESSAGE_CALL:
            // params - { uri, method, arg, extras }
            result = resolver.call((Uri)params[0], (String)params[1], (String)params[2], (Bundle)params[3]);
            break;

        case MESSAGE_BATCH:
            result = applyBatch(resolver, params);
            break;

        case MESSAGE_QUERY:
            result = execQuery(resolver, params);
            break;

        case MESSAGE_EXECUTE:
            result = onExecute(resolver, msg.what, params);
            break;

        case MESSAGE_INSERT:
            // params - { uri, values }
            result = resolver.insert((Uri)params[0], (ContentValues)params[1]);
            break;

        case MESSAGE_INSERTS:
            // params - { uri, values }
            result = resolver.bulkInsert((Uri)params[0], (ContentValues[])params[1]);
            break;

        case MESSAGE_DELETE:
            // params - { uri, whereClause, whereArgs }
            result = resolver.delete((Uri)params[0], (String)params[1], (String[])params[2]);
            break;

        case MESSAGE_UPDATE:
            // params - { uri, values, whereClause, whereArgs }
            result = resolver.update((Uri)params[0], (ContentValues)params[1], (String)params[2], (String[])params[3]);
            break;

        default:
            throw new IllegalStateException("Unknown message: " + msg.arg1);
        }

        UIHandler.sInstance.sendMessage(this, msg.arg1, msg.what, result);
        return true;
    }

    @Override
    public void dispatchMessage(int message, int token, Object result) {
        switch (message) {
        case MESSAGE_CALL:
            onCallComplete(token, (Bundle)result);
            break;

        case MESSAGE_INSERT:
            onInsertComplete(token, (Uri)result);
            break;

        case MESSAGE_INSERTS:
            onBulkInsertComplete(token, (int)result);
            break;

        case MESSAGE_BATCH:
            onApplyBatchComplete(token, (ContentProviderResult[])result);
            break;

        default:
            super.dispatchMessage(message, token, result);
        }
    }

    /**
     * Executes custom query on a background thread.
     * @param resolver The {@link ContentResolver}.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param params The parameters passed in from {@link #startExecute}.
     * @return The execution result.
     */
    protected Object onExecute(ContentResolver resolver, int token, Object[] params) {
        return null;
    }

    /**
     * Called when an asynchronous call is completed on the UI thread.
     * @param token The token to identify the call, passed in from {@link #startCall}.
     * @param result A result <tt>Bundle</tt> holding the results from the call.
     */
    protected void onCallComplete(int token, Bundle result) {
    }

    /**
     * Called when an asynchronous insert is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startInsert}.
     * @param newUri The URL of the newly created row.
     */
    protected void onInsertComplete(int token, Uri newUri) {
    }

    /**
     * Called when an asynchronous multiple inserts is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startBulkInsert}.
     * @param newRows The number of newly created rows.
     */
    protected void onBulkInsertComplete(int token, int newRows) {
    }

    /**
     * Called when an asynchronous apply is completed on the UI thread.
     * @param token The token to identify the apply, passed in from {@link #startApplyBatch}.
     * @param results The results of the applications.
     */
    protected void onApplyBatchComplete(int token, ContentProviderResult[] results) {
    }

    private Cursor execQuery(ContentResolver resolver, Object[] params) {
        Cursor cursor = null;
        try {
            // params - { uri, projection, selection, selectionArgs, sortOrder }
            cursor = resolver.query((Uri)params[0], (String[])params[1], (String)params[2], (String[])params[3], (String)params[4]);
            if (cursor != null) {
                // Calling getCount() causes the cursor window to be filled, which
                // will make the first access on the main thread a lot faster.
                cursor.getCount();
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't query from - ").append(params[0]).toString(), e);
        }

        return cursor;
    }

    @SuppressWarnings("unchecked")
    private static ContentProviderResult[] applyBatch(ContentResolver resolver, Object[] params) {
        try {
            // params - { authority, operations }
            return resolver.applyBatch((String)params[0], (ArrayList<ContentProviderOperation>)params[1]);
        } catch (Exception e) {
            throw new RuntimeException(new StringBuilder("Couldn't apply batch, authority - ").append(params[0]).toString(), e);
        }
    }
}
