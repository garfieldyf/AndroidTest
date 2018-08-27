package android.ext.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

/**
 * Class UriUtils
 * @author Garfield
 * @version 1.0
 */
public final class UriUtils {
    /**
     * Opens an <tt>InputStream</tt> from the specified <em>uri</em>.
     * <h5>Accepts the following URI schemes:</h5>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to open.
     * @return The <tt>InputStream</tt>.
     * @throws IOException if the <em>uri</em> could not be opened.
     */
    public static InputStream openInputStream(Context context, Object uri) throws IOException {
        DebugUtils.__checkError(uri == null, "uri == null");
        if (uri instanceof Uri) {
            return context.getContentResolver().openInputStream((Uri)uri);
        } else {
            final String uriString = uri.toString();
            if (uriString.indexOf(':') == -1) {
                return new FileInputStream(uriString);
            } else {
                final int index = uriString.indexOf("android_asset");
                return (index == -1 ? context.getContentResolver().openInputStream(Uri.parse(uriString)) : context.getAssets().open(uriString.substring(index + 14), AssetManager.ACCESS_STREAMING));
            }
        }
    }

    /**
     * Returns the scheme with the specified <em>uri</em>. Example: "http".
     * @param uri The uri to parse.
     * @return The scheme or <tt>null</tt> if the <em>uri</em> has no scheme.
     */
    public static String parseScheme(Object uri) {
        DebugUtils.__checkError(uri == null, "uri == null");
        String scheme = null;
        if (uri instanceof Uri) {
            scheme = ((Uri)uri).getScheme();
        } else {
            final String uriString = uri.toString();
            final int index = uriString.indexOf(':');
            if (index != -1) {
                scheme = uriString.substring(0, index);
            }
        }

        return scheme;
    }

    /**
     * Matches the scheme of the specified <em>uri</em>. The default implementation
     * match the "http", "https" and "ftp".
     * @param uri The uri to match.
     * @return <tt>true</tt> if the scheme match successful, <tt>false</tt> otherwise.
     */
    public static boolean matchScheme(Object uri) {
        DebugUtils.__checkError(uri == null, "uri == null");
        final String scheme = (uri instanceof Uri ? ((Uri)uri).getScheme() : uri.toString());
        return ("http".regionMatches(true, 0, scheme, 0, 4) || "https".regionMatches(true, 0, scheme, 0, 5) || "ftp".regionMatches(true, 0, scheme, 0, 3));
    }

    /**
     * Constructs a scheme is "file" and authority is "android_asset" uri string. The
     * returned string such as <tt>"file:///android_asset/docs/home.html"</tt>.
     * @param filename A relative path within the assets, such as <tt>"docs/home.html"</tt>.
     * @return The uri string.
     */
    public static String getAssetUri(String filename) {
        DebugUtils.__checkError(filename == null, "filename == null");
        return ("file:///android_asset/" + filename);
    }

    /**
     * Equivalent to calling <tt>getResourceUri(context.getPackageName(), resource)</tt>.
     * @param context The <tt>Context</tt>.
     * @param resource Type {@link Integer}, or {@link String} representation of the
     * resource, such as <tt>R.drawable.ic_launcher</tt> or <tt>"drawable/ic_launcher"</tt>.
     * @return The uri string.
     * @see #getResourceUri(String, Object)
     */
    public static String getResourceUri(Context context, Object resource) {
        return getResourceUri(context.getPackageName(), resource);
    }

    /**
     * Constructs a scheme is "android.resource" uri string.
     * @param packageName The application's package name.
     * @param resource Type {@link Integer}, or {@link String} representation of the
     * resource, such as <tt>R.drawable.ic_launcher</tt> or <tt>"drawable/ic_launcher"</tt>.
     * @return The uri string.
     * @see #getResourceUri(Context, Object)
     */
    public static String getResourceUri(String packageName, Object resource) {
        DebugUtils.__checkError(packageName == null, "packageName == null");
        DebugUtils.__checkError(resource == null, "resource == null");
        return ("android.resource://" + packageName + "/" + resource);
    }

    /**
     * This utility class cannot be instantiated.
     */
    private UriUtils() {
    }
}
