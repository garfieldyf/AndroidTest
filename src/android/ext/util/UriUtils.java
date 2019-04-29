package android.ext.util;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;
import static android.content.ContentResolver.SCHEME_FILE;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

/**
 * Class UriUtils
 * @author Garfield
 */
public final class UriUtils {
    public static final String SCHEME_FTP   = "ftp";
    public static final String SCHEME_HTTP  = "http";
    public static final String SCHEME_HTTPS = "https";

    /**
     * Opens an <tt>InputStream</tt> from the specified <em>uri</em>.
     * <h3>Accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android_asset ({@link #SCHEME_FILE})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to open.
     * @return The <tt>InputStream</tt>.
     * @throws IOException if the <em>uri</em> could not be opened.
     */
    public static InputStream openInputStream(Context context, Object uri) throws IOException {
        DebugUtils.__checkError(uri == null, "uri == null");
        if (uri instanceof File) {
            return new FileInputStream((File)uri);
        } else {
            // The uri may be a String, Uri or Object.
            final String uriString = uri.toString();
            if (FileUtils.isAbsolutePath(uriString)) {
                return new FileInputStream(uriString);
            } else if (SCHEME_FILE.regionMatches(true, 0, uriString, 0, 4)) {
                return openInputStream(context, uriString);
            } else {
                return context.getContentResolver().openInputStream(uri instanceof Uri ? (Uri)uri : Uri.parse(uriString));
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
        if (uri instanceof Uri) {
            return ((Uri)uri).getScheme();
        } else {
            final String uriString = uri.toString();
            final int index = uriString.indexOf(SCHEME_SEPARATOR);
            return (index != -1 ? uriString.substring(0, index) : null);
        }
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
        return (SCHEME_HTTP.regionMatches(true, 0, scheme, 0, 4) || SCHEME_HTTPS.regionMatches(true, 0, scheme, 0, 5) || SCHEME_FTP.regionMatches(true, 0, scheme, 0, 3));
    }

    /**
     * Constructs a scheme is "file" uri string.
     * @param path The file path, must be absolute file path.
     * @return The uri string.
     */
    public static String getFileUri(String path) {
        DebugUtils.__checkError(path == null, "path == null");
        return (SCHEME_FILE + SCHEME_SEPARATOR + path);
    }

    /**
     * Constructs a scheme is "file" and authority is "android_asset" uri string.
     * The returned string such as <tt>"file:///android_asset/docs/home.html"</tt>.
     * @param filename A relative path within the assets, such as <tt>"docs/home.html"</tt>.
     * @return The uri string.
     */
    public static String getAssetUri(String filename) {
        DebugUtils.__checkError(filename == null, "filename == null");
        return (SCHEME_FILE + SCHEME_SEPARATOR + DIR_ANDROID_ASSET + filename);
    }

    /**
     * Equivalent to calling <tt>getResourceUri(context.getPackageName(), resource)</tt>.
     * @param context The <tt>Context</tt>.
     * @param resource Type {@link Integer} or {@link String} representation of the
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
     * @param resource Type {@link Integer} or {@link String} representation of the
     * resource, such as <tt>R.drawable.ic_launcher</tt> or <tt>"drawable/ic_launcher"</tt>.
     * @return The uri string.
     * @see #getResourceUri(Context, Object)
     */
    public static String getResourceUri(String packageName, Object resource) {
        DebugUtils.__checkError(packageName == null, "packageName == null");
        DebugUtils.__checkError(resource == null, "resource == null");
        return (SCHEME_ANDROID_RESOURCE + SCHEME_SEPARATOR + packageName + '/' + resource);
    }

    private static InputStream openInputStream(Context context, String uri) throws IOException {
        DebugUtils.__checkError(uri.length() <= 7, "Invalid uri - " + uri);
        if (uri.indexOf(DIR_ANDROID_ASSET, 7) == -1) {
            // Skips the prefix 'file://'
            return new FileInputStream(uri.substring(7));
        } else {
            // Skips the prefix 'file:///android_asset/'
            DebugUtils.__checkError(uri.length() <= 22, "Invalid uri - " + uri);
            return context.getAssets().open(uri.substring(22), AssetManager.ACCESS_STREAMING);
        }
    }

    private static final String SCHEME_SEPARATOR  = "://";
    private static final String DIR_ANDROID_ASSET = "/android_asset/";

    /**
     * This utility class cannot be instantiated.
     */
    private UriUtils() {
    }
}
