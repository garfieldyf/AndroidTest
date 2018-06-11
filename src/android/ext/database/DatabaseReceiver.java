package android.ext.database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Class DatabaseReceiver
 * @author Garfield
 * @version 1.0
 */
public abstract class DatabaseReceiver extends BroadcastReceiver {
    /**
     * Local Broadcast Action: The table content has changed. <p>May include the following extras:
     * <ul><li>{@link #EXTRA_STATEMENT} containing the integer SQL statement type, May be one of
     * <tt>STATEMENT_XXX</tt> constants.
     * <li>{@link #EXTRA_RESULT} containing the long value, May be the row ID of the inserted row
     * or the number of rows affected for update/delete.</ul>
     */
    public static final String ACTION_CONTENT_CHANGED = "{C620F8F3-59EB-4EA7-887E-813EFC58295A}";

    /**
     * The name of the extra used to define the result.
     * @see #ACTION_CONTENT_CHANGED
     */
    public static final String EXTRA_RESULT = "result";

    /**
     * The name of the extra used to define the SQL statement type.
     * @see #ACTION_CONTENT_CHANGED
     */
    public static final String EXTRA_STATEMENT = "statement";

    /**
     * The type of the SQL statement UNKNOWN.
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
     * The scheme of the intent's data.
     */
    public final String scheme;

    /**
     * Constructor
     * @param databaseName The database name, or <tt>null</tt> for an in-memory database.
     * @param tableName The table name.
     */
    public DatabaseReceiver(String databaseName, String tableName) {
        this.scheme = buildScheme(databaseName, tableName);
    }

    /**
     * Register this receiver for the local broadcasts.
     * @param context The <tt>Context</tt>.
     * @see #unregister(Context)
     */
    public final void register(Context context) {
        registerReceiver(context, scheme, this);
    }

    /**
     * Unregister this receiver.
     * @param context The <tt>Context</tt>.
     * @see #register(Context)
     */
    public final void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    /**
     * Register a receive for any local broadcasts that match the given <em>databaseName</em> and <em>tableName</em>.
     * @param context The <tt>Context</tt>.
     * @param databaseName The database name to be registered, or <tt>null</tt> for an in-memory database.
     * @param tableName The table name to be registered.
     * @param receiver The {@link BroadcastReceiver} to handle the broadcast.
     * @see #buildScheme(String, String)
     * @see LocalBroadcastManager#unregisterReceiver(BroadcastReceiver)
     */
    public static void registerReceiver(Context context, String databaseName, String tableName, BroadcastReceiver receiver) {
        registerReceiver(context, buildScheme(databaseName, tableName), receiver);
    }

    /**
     * Broadcasts the given the <em>databaseName</em> and <em>tableName</em> to all interested <tt>BroadcastReceiver</tt>s.
     * @param context The <tt>Context</tt>.
     * @param databaseName The database name to match, or <tt>null</tt> for an in-memory database.
     * @param tableName The table name to be match.
     * @param statement The SQL statement type, May be one of <tt>STATEMENT_XXX</tt> constants.
     * @param result The SQL statement perform the result.
     * @see #buildScheme(String, String)
     * @see #resolveIntent(String, String, int, long)
     * @see LocalBroadcastManager#sendBroadcast(Intent)
     */
    public static void sendBroadcast(Context context, String databaseName, String tableName, int statement, long result) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(resolveIntent(databaseName, tableName, statement, result));
    }

    /**
     * Returns the {@link Intent} that should be used to send local broadcast.
     * @param databaseName The database name, or <tt>null</tt> for an in-memory database.
     * @param tableName The table name.
     * @param statement The SQL statement type, May be one of <tt>STATEMENT_XXX</tt> constants.
     * @param result The SQL statement perform the result.
     * @see #buildScheme(String, String)
     */
    public static Intent resolveIntent(String databaseName, String tableName, int statement, long result) {
        final Intent intent = new Intent(ACTION_CONTENT_CHANGED);
        intent.putExtra(EXTRA_RESULT, result);
        intent.putExtra(EXTRA_STATEMENT, statement);
        intent.setData(Uri.parse(buildScheme(databaseName, tableName) + "://contents"));
        return intent;
    }

    /**
     * Returns the scheme of the intent's data with the given <em>databaseName</em> and <em>tableName</em>.
     * @param databaseName The database name, or <tt>null</tt> for an in-memory database.
     * @param tableName The table name.
     * @return The scheme of the <tt>Intent</tt>.
     * @see #resolveIntent(String, String, int, long)
     */
    public static String buildScheme(String databaseName, String tableName) {
        return new StringBuilder(32).append(TextUtils.isEmpty(databaseName) ? "[memory]" : databaseName).append('.').append(tableName).toString();
    }

    public static void dump(String tag, Intent intent) {
        Log.d(tag, new StringBuilder(128)
           .append("Intent { action = ").append(intent.getAction())
           .append(", scheme = ").append(intent.getScheme())
           .append(", statement = ").append(toString(intent.getIntExtra(EXTRA_STATEMENT, STATEMENT_UNKNOWN)))
           .append(", result = ").append(intent.getLongExtra(EXTRA_RESULT, -1))
           .append(" }").toString());
    }

    private static String toString(int statement) {
        switch (statement) {
        case STATEMENT_INSERT:
            return "INSERT";

        case STATEMENT_UPDATE:
            return "UPDATE";

        case STATEMENT_DELETE:
            return "DELETE";

        case STATEMENT_REPLACE:
            return "REPLACE";

        default:
            return "UNKNOWN";
        }
    }

    private static void registerReceiver(Context context, String scheme, BroadcastReceiver receiver) {
        final IntentFilter filter = new IntentFilter(ACTION_CONTENT_CHANGED);
        filter.addDataScheme(scheme);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }
}
