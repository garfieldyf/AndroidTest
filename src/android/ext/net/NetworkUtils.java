package android.ext.net;

import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Printer;

/**
 * Class NetworkUtils
 * @author Garfield
 * @version 1.0
 */
public final class NetworkUtils {
    /**
     * The wireless local area network interface name.
     */
    public static final String WLAN = "wlan0";

    /**
     * The ethernet network interface name.
     */
    public static final String ETHERNET = "eth0";

    /**
     * Closes the {@link HttpURLConnection} and releases resources associated
     * with the <em>conn</em>, handling <tt>null</tt> <em>conn</em>.
     * @param conn The <tt>HttpURLConnection</tt> to close.
     */
    public static void close(HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
        }
    }

    /**
     * Returns the mac address from the network interface.
     * @param ifname The network interface name. Pass {@link #WLAN},
     * {@link #ETHERNET} or other interface name.
     * @return The mac address or <tt>null</tt>.
     */
    public static String getMacAddress(String ifname) {
        try {
            return DeviceUtils.readDeviceFile("/sys/class/net/" + ifname + "/address", 24);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns details about the currently active default data network.
     * @param context The <tt>Context</tt>.
     * @return A {@link NetworkInfo} object for the current default
     * network or a dummy {@link NetworkInfo} object if no network is active.
     * @see #getActiveNetworkInfo(ConnectivityManager)
     * @see ConnectivityManager#getActiveNetworkInfo()
     */
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        final NetworkInfo info = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (info != null ? info : DummyNetworkInfo.sInstance);
    }

    /**
     * Returns details about the currently active default data network.
     * @param cm The {@link ConnectivityManager}.
     * @return A {@link NetworkInfo} object for the current default
     * network or a dummy {@link NetworkInfo} object if no network is active.
     * @see #getActiveNetworkInfo(Context)
     * @see ConnectivityManager#getActiveNetworkInfo()
     */
    public static NetworkInfo getActiveNetworkInfo(ConnectivityManager cm) {
        final NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null ? info : DummyNetworkInfo.sInstance);
    }

    /**
     * Prints the contents of the <em>conn</em> request headers. This
     * method can only be called before the connection is established.
     * @param conn The {@link URLConnection} request headers to print.
     * @param printer The {@link Printer} to print to.
     * @see #dumpResponseHeaders(URLConnection, Printer)
     */
    public static void dumpRequestHeaders(URLConnection conn, Printer printer) {
        dumpHeaders(conn, printer, " %s Request Headers ", conn.getRequestProperties());
    }

    /**
     * Prints the contents of the <em>conn</em> response headers.
     * @param conn The {@link URLConnection} response headers to print.
     * @param printer The {@link Printer} to print to.
     * @see #dumpRequestHeaders(URLConnection, Printer)
     */
    public static void dumpResponseHeaders(URLConnection conn, Printer printer) {
        dumpHeaders(conn, printer, " %s Response Headers ", conn.getHeaderFields());
    }

    /**
     * Prints the contents of the connection headers.
     */
    private static void dumpHeaders(URLConnection conn, Printer printer, String format, Map<String, List<String>> headers) {
        final URL url = conn.getURL();
        final StringBuilder result = new StringBuilder(80);
        DebugUtils.dumpSummary(printer, result, 80, format, url.getProtocol().toUpperCase(Locale.getDefault()));
        result.setLength(0);
        printer.println(result.append("  URL = ").append(url.toString()).toString());

        if (ArrayUtils.getSize(headers) > 0) {
            for (Entry<String, List<String>> header : headers.entrySet()) {
                result.setLength(0);
                result.append("  ").append(header.getKey()).append(" = ").append(header.getValue().toString());
                printer.println(result.toString());
            }
        }
    }

    /**
     * A dummy <tt>NetworkInfo</tt>.
     */
    private static final class DummyNetworkInfo {
        public static final NetworkInfo sInstance;

        static {
            try {
                final Constructor<NetworkInfo> ctor = NetworkInfo.class.getConstructor(int.class, int.class, String.class, String.class);
                ctor.setAccessible(true);
                sInstance = ctor.newInstance(ConnectivityManager.TYPE_DUMMY, ConnectivityManager.TYPE_DUMMY, "DUMMY", "");
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private NetworkUtils() {
    }
}
