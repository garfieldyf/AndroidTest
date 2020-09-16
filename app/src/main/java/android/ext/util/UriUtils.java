package android.ext.util;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;
import static android.content.ContentResolver.SCHEME_FILE;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class UriUtils
 * @author Garfield
 */
public final class UriUtils {
    public static final String SCHEME_ANDROID_ASSET = "android.asset";

    /**
     * Returns the scheme with the specified <em>uri</em>. Example: "http".
     * @param uri The uri to parse.
     * @return The scheme or <tt>null</tt> if the <em>uri</em> has no scheme.
     */
    public static String parseScheme(Object uri) {
        DebugUtils.__checkError(uri == null, "Invalid parameter - uri == null");
        if (uri instanceof Uri) {
            return ((Uri)uri).getScheme();
        } else {
            final String uriString = uri.toString();
            final int index = uriString.indexOf("://");
            return (index != -1 ? uriString.substring(0, index) : null);
        }
    }

    /**
     * Opens an <tt>InputStream</tt> from the specified <em>uri</em>.
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to open.
     * @return The <tt>InputStream</tt>.
     * @throws IOException if the <em>uri</em> could not be opened.
     * @see #getFileUri(String)
     * @see #getAssetUri(String)
     * @see #getResourceUri(String, Object)
     */
    public static InputStream openInputStream(Context context, Object uri) throws IOException {
        DebugUtils.__checkError(uri == null, "Invalid parameter - uri == null");
        if (uri instanceof File) {
            return new FileInputStream((File)uri);
        }

        // The uri may be a String, Uri or Object.
        final String uriString = uri.toString();
        if (FileUtils.isAbsolutePath(uriString)) {
            return new FileInputStream(uriString);
        } else if ("file://".regionMatches(true, 0, uriString, 0, SCHEME_FILE_LENGTH)) {
            return new FileInputStream(uriString.substring(SCHEME_FILE_LENGTH) /* Skips 'file://' */);
        } else if ("android.asset://".regionMatches(true, 0, uriString, 0, SCHEME_ASSET_LENGTH)) {
            return context.getAssets().open(uriString.substring(SCHEME_ASSET_LENGTH) /* Skips 'android.asset://' */, AssetManager.ACCESS_STREAMING);
        } else {
            return context.getContentResolver().openInputStream(uri instanceof Uri ? (Uri)uri : Uri.parse(uriString));
        }
    }

    /**
     * Constructs a scheme is "file" uri from a <em>path</em>.
     * @param path The file path.
     * @return A {@link Uri} for the given <em>path</em>.
     * @see Uri#fromFile(File)
     */
    public static Uri fromPath(String path) {
        return new Uri.Builder().scheme(SCHEME_FILE).authority("").path(path).build();
    }

    /**
     * Constructs a scheme is "android.resource" uri. The returned uri such as
     * <tt>"android.resource://<em>packageName</em>/drawable/ic_launcher"</tt>.
     * @param packageName The application's package name.
     * @param resource Type {@link Integer} or {@link String} representation of the
     * resource, such as <tt>R.drawable.ic_launcher</tt> or <tt>"drawable/ic_launcher"</tt>.
     * @return A {@link Uri} for the given <em>resource</em>.
     */
    public static Uri fromResource(String packageName, Object resource) {
        DebugUtils.__checkError(packageName == null || resource == null, "Invalid parameters - packageName == null || resource == null");
        return new Uri.Builder().scheme(SCHEME_ANDROID_RESOURCE).authority(packageName).path(resource.toString()).build();
    }

    /**
     * Constructs a scheme is "file" uri string. The returned
     * string such as <tt>"file:///sdcard/docs/home.html"</tt>.
     * @param path The file path.
     * @return A uri {@link String} for the given <em>path</em>.
     */
    public static String getFileUri(String path) {
        DebugUtils.__checkError(path == null, "Invalid parameter - path == null");
        return (SCHEME_FILE + "://" + path);
    }

    /**
     * Constructs a scheme is "android.asset" uri string. The returned string such as
     * <tt>"android.asset://docs/home.html"</tt>.
     * @param filename A relative path within the assets, such as <tt>"docs/home.html"</tt>.
     * @return A uri {@link String} for the given <em>filename</em>.
     */
    public static String getAssetUri(String filename) {
        DebugUtils.__checkError(filename == null, "Invalid parameter - filename == null");
        return (SCHEME_ANDROID_ASSET + "://" + filename);
    }

    /**
     * Constructs a scheme is "android.resource" uri string. The returned string such as
     * <tt>"android.resource://<em>packageName</em>/drawable/ic_launcher"</tt>.
     * @param packageName The application's package name.
     * @param resource Type {@link Integer} or {@link String} representation of the
     * resource, such as <tt>R.drawable.ic_launcher</tt> or <tt>"drawable/ic_launcher"</tt>.
     * @return A uri {@link String} for the given <em>resource</em>.
     */
    public static String getResourceUri(String packageName, Object resource) {
        DebugUtils.__checkError(packageName == null || resource == null, "Invalid parameters - packageName == null || resource == null");
        return (SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + resource);
    }

    private static final int SCHEME_FILE_LENGTH  = 7;   /* file:// */
    private static final int SCHEME_ASSET_LENGTH = 16;  /* android.asset:// */

    /**
     * This utility class cannot be instantiated.
     */
    private UriUtils() {
    }
}
