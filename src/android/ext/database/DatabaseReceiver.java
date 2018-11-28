package android.ext.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Class <tt>DatabaseReceiver</tt> used to listen the specified table's content has changed.
 * @author Garfield
 */
public abstract class DatabaseReceiver extends BroadcastReceiver {
    /**
     * Local Broadcast Action: The table content has changed. <p>May include the following extras:
     * <ul><li>{@link #EXTRA_STATEMENT} containing the integer SQL statement type, May be one of
     * <tt>STATEMENT_XXX</tt> constants.
     * <li>{@link #EXTRA_RESULT} containing the {@link Bundle} object by user-defined or <tt>null</tt>.
     * </ul>
     */
    public static final String ACTION_TABLE_CONTENT_CHANGED = "{C620F8F3-59EB-4EA7-887E-813EFC58295A}";

    /**
     * The name of the extra used to define the result.
     * @see #ACTION_TABLE_CONTENT_CHANGED
     */
    public static final String EXTRA_RESULT = "result";

    /**
     * The name of the extra used to define the SQL statement type.
     * @see #ACTION_TABLE_CONTENT_CHANGED
     */
    public static final String EXTRA_STATEMENT = "statement";

    /**
     * The key of row ID of the result.
     */
    public static final String KEY_ROW_ID = "rowID";

    /**
     * The key of the number of rows affected of the result.
     */
    public static final String KEY_ROWS_AFFECTED = "rowsAffected";

    /**
     * The type of the SQL statement unknown.
     */
    public static final int STATEMENT_UNKNOWN = 0;

    /**
     * The type of the SQL statement INSERT.
     */
    public static final int STATEMENT_INSERT = 1;

    /**
     * The type of the SQL statement UPDATE.
     */
    public static final int STATEMENT_UPDATE = 2;

    /**
     * The type of the SQL statement DELETE.
     */
    public static final int STATEMENT_DELETE = 3;

    /**
     * The type of the SQL statement REPLACE.
     */
    public static final int STATEMENT_REPLACE = 4;

    /**
     * Register this receiver for the local broadcasts.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match.
     * May be <tt>"databasename.tablename"</tt>.
     * @see #unregister(Context)
     */
    public final void register(Context context, String scheme) {
        registerReceiver(context, scheme, this);
    }

    /**
     * Unregister this receiver.
     * @param context The <tt>Context</tt>.
     * @see #register(Context, String)
     */
    public final void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    /**
     * Register a receive for any local broadcasts that match the given <em>scheme</em>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match. May be <tt>"databasename.tablename"</tt>
     * @param receiver The {@link BroadcastReceiver} to handle the broadcast.
     * @see LocalBroadcastManager#unregisterReceiver(BroadcastReceiver)
     */
    public static void registerReceiver(Context context, String scheme, BroadcastReceiver receiver) {
        final IntentFilter filter = new IntentFilter(ACTION_TABLE_CONTENT_CHANGED);
        filter.addDataScheme(scheme);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    /**
     * Broadcasts the given the <em>scheme</em> to all interested <tt>BroadcastReceivers</tt>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match.
     * @param statement The SQL statement type, May be one of <tt>STATEMENT_XXX</tt> constants.
     * @param rowID The row ID of the inserted row.
     * @see #sendBroadcast(Context, String, int, Bundle)
     */
    public static void sendBroadcast(Context context, String scheme, int statement, long rowID) {
        final Bundle result = new Bundle();
        result.putLong(KEY_ROW_ID, rowID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(resolveIntent(scheme, statement, result));
    }

    /**
     * Broadcasts the given the <em>scheme</em> to all interested <tt>BroadcastReceivers</tt>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match.
     * @param statement The SQL statement type, May be one of <tt>STATEMENT_XXX</tt> constants.
     * @param rowsAffected the number of rows affected for update/delete.
     * @see #sendBroadcast(Context, String, int, Bundle)
     */
    public static void sendBroadcast(Context context, String scheme, int statement, int rowsAffected) {
        final Bundle result = new Bundle();
        result.putInt(KEY_ROWS_AFFECTED, rowsAffected);
        LocalBroadcastManager.getInstance(context).sendBroadcast(resolveIntent(scheme, statement, result));
    }

    /**
     * Broadcasts the given the <em>scheme</em> to all interested <tt>BroadcastReceivers</tt>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match.
     * @param statement The SQL statement type, May be one of <tt>STATEMENT_XXX</tt> constants.
     * @param result The SQL statement perform the result or <tt>null</tt>.
     * @see #resolveIntent(String, int, Bundle)
     * @see LocalBroadcastManager#sendBroadcast(Intent)
     */
    public static void sendBroadcast(Context context, String scheme, int statement, Bundle result) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(resolveIntent(scheme, statement, result));
    }

    /**
     * Returns the {@link Intent} that should be used to send local broadcast.
     * @param scheme The <tt>Intent</tt> data scheme to match.
     * @param statement The SQL statement type, May be one of <tt>STATEMENT_XXX</tt> constants.
     * @param result The SQL statement perform the result or <tt>null</tt>.
     * @see #sendBroadcast(Context, String, int, Bundle)
     */
    public static Intent resolveIntent(String scheme, int statement, Bundle result) {
        final Intent intent = new Intent(ACTION_TABLE_CONTENT_CHANGED, Uri.parse(scheme + "://contents"));
        intent.putExtra(EXTRA_RESULT, result);
        intent.putExtra(EXTRA_STATEMENT, statement);
        return intent;
    }

    public static void dump(String tag, Intent intent) {
        Log.d(tag, new StringBuilder()
           .append("Intent { action = ").append(intent.getAction())
           .append(", scheme = ").append(intent.getScheme())
           .append(", statement = ").append(toString(intent.getIntExtra(EXTRA_STATEMENT, STATEMENT_UNKNOWN)))
           .append(", result = ").append(intent.getBundleExtra(EXTRA_RESULT))
           .append(" }").toString());
    }

    private static String toString(int statement) {
        switch (statement) {
        case STATEMENT_UNKNOWN:
            return "UNKNOWN";

        case STATEMENT_INSERT:
            return "INSERT";

        case STATEMENT_UPDATE:
            return "UPDATE";

        case STATEMENT_DELETE:
            return "DELETE";

        case STATEMENT_REPLACE:
            return "REPLACE";

        default:
            return Integer.toString(statement);
        }
    }
}
