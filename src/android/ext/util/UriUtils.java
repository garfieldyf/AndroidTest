package android.ext.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

/**
 * Class UriUtils
 * @author Garfield
 * @version 1.0
 */
public final class UriUtils {
    public static final String SCHEME_FTP   = "ftp";
    public static final String SCHEME_HTTP  = "http";
    public static final String SCHEME_HTTPS = "https";

    /**
     * Returns the scheme with the specified <em>uri</em>. Example: "http".
     * @param uri The uri to parse.
     * @return The scheme or <tt>null</tt> if the <em>uri</em> has no scheme.
     */
    public static String parseScheme(Object uri) {
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
     * Opens an <tt>InputStream</tt> from the specified <em>uri</em>.
     * <h5>Accepts the following URI schemes:</h5>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link ContentResolver#SCHEME_FILE SCHEME_FILE})</li>
     * <li>content ({@link ContentResolver#SCHEME_CONTENT SCHEME_CONTENT})</li>
     * <li>android.resource ({@link ContentResolver#SCHEME_ANDROID_RESOURCE SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to open.
     * @return The <tt>InputStream</tt>.
     * @throws FileNotFoundException if the <em>uri</em> could not be opened.
     */
    public static InputStream openInputStream(Context context, Object uri) throws FileNotFoundException {
        if (uri instanceof Uri) {
            return context.getContentResolver().openInputStream((Uri)uri);
        } else {
            final String uriString = uri.toString();
            if (uriString.indexOf(':') == -1) {
                return new FileInputStream(uriString);
            } else {
                return context.getContentResolver().openInputStream(Uri.parse(uriString));
            }
        }
    }

    /**
     * Matches the scheme of the specified <em>uri</em>. The default implementation
     * match the {@link #SCHEME_HTTP}, {@link #SCHEME_HTTPS} and {@link #SCHEME_FTP}.
     * @param uri The uri to match.
     * @return <tt>true</tt> if the scheme match successful, <tt>false</tt> otherwise.
     */
    public static boolean matchScheme(Object uri) {
        final String scheme = (uri instanceof Uri ? ((Uri)uri).getScheme() : uri.toString());
        return (SCHEME_HTTP.regionMatches(true, 0, scheme, 0, 4) || SCHEME_HTTPS.regionMatches(true, 0, scheme, 0, 5) || SCHEME_FTP.regionMatches(true, 0, scheme, 0, 3));
    }

    /**
     * This utility class cannot be instantiated.
     */
    private UriUtils() {
    }
}
