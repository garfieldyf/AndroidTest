package android.ext.database;

import static android.os.PatternMatcher.PATTERN_LITERAL;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.ext.util.DebugUtils;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * Class <tt>DatabaseReceiver</tt> used to listen the specified table's content has changed.
 * @author Garfield
 */
public abstract class DatabaseReceiver extends BroadcastReceiver {
    /**
     * Local Broadcast Action: The table content has changed.
     */
    public static final String ACTION_TABLE_CONTENT_CHANGED = "{C620F8F3-59EB-4EA7-887E-813EFC58295A}";

    /**
     * Intent extra used to define the row ID.
     */
    public static final String EXTRA_ROW_ID = "_rowID!";

    /**
     * Intent extra used to define the SQL statement type.
     * See <tt>STATEMENT_XXX</tt> constants.
     */
    public static final String EXTRA_STATEMENT = "_statement!";

    /**
     * Intent extra used to define the number of rows affected.
     */
    public static final String EXTRA_ROWS_AFFECTED = "_rowsAffected!";

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
     * The scheme specific part for the local broadcasts.
     */
    private static final String SSP_PREFIX = "//contents";

    /**
     * Equivalent to calling <tt>registerReceiver(context, scheme, Long.toString(id), this)</tt>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match. May be <tt>"databasename.tablename"</tt>.
     * @param id The row ID to match.
     * @see #registerReceiver(Context, String, String, DatabaseReceiver)
     */
    public final void register(Context context, String scheme, long id) {
        registerReceiver(context, scheme, Long.toString(id), this);
    }

    /**
     * Equivalent to calling <tt>registerReceiver(context, scheme, path, this)</tt>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match. May be <tt>"databasename.tablename"</tt>.
     * @param path May be <tt>null</tt>. The path to match.
     * @see #registerReceiver(Context, String, String, DatabaseReceiver)
     */
    public final void register(Context context, String scheme, String path) {
        registerReceiver(context, scheme, path, this);
    }

    /**
     * Unregister this receiver.
     * @param context The <tt>Context</tt>.
     */
    public final void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    /**
     * Register a receive for any local broadcasts that match the given <em>scheme</em> and <em>path</em>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match. May be <tt>"databasename.tablename"</tt>
     * @param path May be <tt>null</tt>. The path to match.
     * @param receiver The {@link DatabaseReceiver} to handle the broadcast.
     * @see LocalBroadcastManager#registerReceiver(BroadcastReceiver, IntentFilter)
     */
    public static void registerReceiver(Context context, String scheme, String path, DatabaseReceiver receiver) {
        final IntentFilter filter = new IntentFilter(ACTION_TABLE_CONTENT_CHANGED);
        filter.addDataScheme(scheme);
        if (!TextUtils.isEmpty(path)) {
            final String ssp = SSP_PREFIX + '/' + path;
            DebugUtils.__checkDebug(true, "DatabaseReceiver", "Intent data ssp = " + ssp);
            filter.addDataSchemeSpecificPart(ssp, PATTERN_LITERAL);
        }

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    /**
     * Equivalent to calling <tt>resolveIntent(scheme, Long.toString(id))</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match.
     * @param id The row ID to match.
     * @return The <tt>Intent</tt> used to send local broadcast.
     * @see #resolveIntent(String, String)
     */
    public static Intent resolveIntent(String scheme, long id) {
        final Intent intent = resolveIntent(scheme, Long.toString(id));
        intent.putExtra(EXTRA_ROW_ID, id);
        return intent;
    }

    /**
     * Returns the {@link Intent} that should be used to send local broadcast.
     * @param scheme The <tt>Intent</tt> data scheme to match.
     * @param path May be <tt>null</tt>. The path to match.
     * @return The <tt>Intent</tt> used to send local broadcast.
     * @see #resolveIntent(String, long)
     */
    public static Intent resolveIntent(String scheme, String path) {
        final StringBuilder uri = new StringBuilder(scheme).append(':').append(SSP_PREFIX);
        if (!TextUtils.isEmpty(path)) {
            uri.append('/').append(path);
        }

        final String data = uri.toString();
        DebugUtils.__checkDebug(true, "DatabaseReceiver", "Intent data = " + data);
        return new Intent(ACTION_TABLE_CONTENT_CHANGED, Uri.parse(data));
    }

    public static void dump(String tag, Intent intent) {
        Log.d(tag, new StringBuilder(96)
           .append("Intent { action = ").append(intent.getAction())
           .append(", scheme = ").append(intent.getScheme())
           .append(", data = ").append(intent.getDataString())
           .append(", extras = ").append(intent.getExtras())
           .append(" }").toString());
    }
}
