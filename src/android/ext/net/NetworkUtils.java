package android.ext.net;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.StringUtils;
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
     * Returns the MAC address from the network interface.
     * @param ifname The network interface name. Pass {@link #WLAN},
     * {@link #ETHERNET} or other interface name.
     * @return The MAC address or <tt>fallback</tt>.
     */
    public static String getMacAddress(String ifname, String fallback) {
        try {
            return DeviceUtils.readDeviceFile("/sys/class/net/" + ifname + "/address", 24);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * Returns the byte array MAC address from the <em>macAddress</em>.
     * @param inAddress The MAC address in <tt>XX:XX:XX:XX:XX:XX</tt>,
     * <tt>XX-XX-XX-XX-XX-XX</tt> or any separate components by white
     * space character ('\s') in <em>inAddress</em>.
     * @return The byte array MAC address.
     * @see #toMacAddress(String, byte[])
     */
    public static byte[] toMacAddress(String macAddress) {
        return toMacAddress(macAddress, new byte[6]);
    }

    /**
     * Returns the byte array MAC address from the <em>macAddress</em>.
     * @param inAddress The MAC address in <tt>XX:XX:XX:XX:XX:XX</tt>,
     * <tt>XX-XX-XX-XX-XX-XX</tt> or any separate components by white
     * space character ('\s') in <em>inAddress</em>.
     * @param outAddress The byte array to store the MAC address.
     * @return The <em>outAddress</em>.
     * @see #toMacAddress(String)
     */
    public static byte[] toMacAddress(String inAddress, byte[] outAddress) {
        DebugUtils.__checkError(!Pattern.matches("([A-Fa-f0-9]{2}[-:\\s]){5}[A-Fa-f0-9]{2}", inAddress), "Invalid MAC address: " + inAddress);
        DebugUtils.__checkError(outAddress == null || outAddress.length < 6, "outAddress == null || outAddress.length < 6");
        final int inLength  = StringUtils.getLength(inAddress);
        final int outLength = ArrayUtils.getSize(outAddress);
        for (int i = 0, j = 0; i < outLength && j < inLength; ++i, j += 3) {
            outAddress[i] = (byte)((Character.digit((int)inAddress.charAt(j), 16) << 4) + Character.digit((int)inAddress.charAt(j + 1), 16));
        }

        return outAddress;
    }

    /**
     * Returns details about the currently active default data network.
     * @param context The <tt>Context</tt>.
     * @return A {@link NetworkInfo} object for the current default network
     * or a dummy <tt>NetworkInfo</tt> object if no network is active.
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
     * @return A {@link NetworkInfo} object for the current default network
     * or a dummy <tt>NetworkInfo</tt> object if no network is active.
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
        final Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>(conn.getRequestProperties());
        requestHeaders.put("Read-Timeout", Collections.singletonList(Integer.toString(conn.getReadTimeout())));
        requestHeaders.put("Connect-Timeout", Collections.singletonList(Integer.toString(conn.getConnectTimeout())));
        if (conn instanceof HttpURLConnection) {
            requestHeaders.put("Redirects", Collections.singletonList(Boolean.toString(((HttpURLConnection)conn).getInstanceFollowRedirects())));
        }

        dumpHeaders(conn, printer, " %s Request Headers ", requestHeaders);
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
        public static final NetworkInfo sInstance = new NetworkInfo(ConnectivityManager.TYPE_DUMMY, ConnectivityManager.TYPE_DUMMY, "DUMMY", "");
    }

    /**
     * This utility class cannot be instantiated.
     */
    private NetworkUtils() {
    }
}
