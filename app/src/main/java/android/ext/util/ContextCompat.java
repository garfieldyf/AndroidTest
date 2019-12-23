package android.ext.util;

import android.content.Context;

/**
 * Class ContextCompat
 * @author Garfield
 */
public final class ContextCompat {
    /**
     * Return the {@link Context} with the specified <em>context</em>.
     * @param context The <tt>Context</tt>.
     * @return The application's <tt>Context</tt> or the <em>context</em>.
     */
    public static Context getContext(Context context) {
        final Context appContext = context.getApplicationContext();
        return (appContext != null ? appContext : context);
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ContextCompat() {
    }
}
