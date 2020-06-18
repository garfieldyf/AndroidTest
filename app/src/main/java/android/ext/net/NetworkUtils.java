package android.ext.net;

import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.StringUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Printer;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Class NetworkUtils
 * @author Garfield
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
     * @param ifname The network interface name. May be {@link #WLAN},
     * {@link #ETHERNET} or other interface name.
     * @return The MAC address or <tt>null</tt> if it has no address.
     * @see #getMacAddress(String, String)
     */
    public static byte[] getMacAddress(String ifname) {
        try {
            return NetworkInterface.getByName(ifname).getHardwareAddress();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the MAC address from the network interface.
     * @param ifname The network interface name. May be {@link #WLAN},
     * {@link #ETHERNET} or other interface name.
     * @return The MAC address or <tt>fallback</tt>.
     * @see #getMacAddress(String)
     */
    public static String getMacAddress(String ifname, String fallback) {
        final byte[] macAddress = getMacAddress(ifname);
        return (macAddress != null ? formatMacAddress(macAddress) : fallback);
    }

    /**
     * Equivalent to calling <tt>toMacAddress(macAddress, new byte[6])</tt>.
     * @see #toMacAddress(String, byte[])
     */
    public static byte[] toMacAddress(String macAddress) {
        return toMacAddress(macAddress, new byte[6]);
    }

    /**
     * Returns the byte array MAC address from the <em>macAddress</em>.
     * @param inAddress The MAC address in <tt>XX:XX:XX:XX:XX:XX</tt>,
     * <tt>XX-XX-XX-XX-XX-XX</tt> or any separated by white space character
     * ('\s') in <em>inAddress</em>.
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
     * Returns the numeric IP address from the network interface (such as "127.0.0.1").
     * @param ifname The network interface name. May be {@link #WLAN}, {@link #ETHERNET}
     * or other interface name.
     * @return The IP address or <tt>fallback</tt>.
     */
    public static String getIPAddress(String ifname, String fallback) {
        final InetAddress address = getInetAddress(ifname);
        return (address != null ? address.getHostAddress() : fallback);
    }

    /**
     * Returns the {@link InetAddress} from the network interface.
     * @param ifname The network interface name. May be {@link #WLAN},
     * {@link #ETHERNET} or other interface name.
     * @return The <tt>InetAddress</tt> or <tt>null</tt> if it has no address.
     */
    public static InetAddress getInetAddress(String ifname) {
        try {
            final Enumeration<InetAddress> addresses = NetworkInterface.getByName(ifname).getInetAddresses();
            while (addresses.hasMoreElements()) {
                final InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                    return address;
                }
            }
        } catch (Exception e) {
            DebugUtils.__checkLogError(true, NetworkUtils.class.getName(), "Couldn't get InetAddress from network interface - " + ifname);
        }

        return null;
    }

    /**
     * Equivalent to calling <tt>formatMacAddress(new StringBuilder(17), macAddress, ':').toString()</tt>.
     * @param macAddress The MAC address to format.
     * @return A formatted MAC address string.
     * @see #formatMacAddress(Appendable, byte[], char)
     */
    public static String formatMacAddress(byte[] macAddress) {
        return formatMacAddress(new StringBuilder(17), macAddress, ':').toString();
    }

    /**
     * Returns a formatted string with the specified <em>macAddress</em>.
     * @param out The <tt>Appendable</tt> to append the MAC address.
     * @param macAddress The MAC address to format.
     * @param separator The MAC address component's separator to format. May be ':', '-', or other character.
     * @return The <em>out</em>.
     * @see #formatMacAddress(byte[])
     */
    public static Appendable formatMacAddress(Appendable out, byte[] macAddress, char separator) {
        DebugUtils.__checkError(macAddress == null || macAddress.length < 6, "macAddress == null || macAddress.length < 6");
        return new Formatter(out).format("%02x%c%02x%c%02x%c%02x%c%02x%c%02x", macAddress[0], separator, macAddress[1], separator, macAddress[2], separator, macAddress[3], separator, macAddress[4], separator, macAddress[5]).out();
    }

    /**
     * Indicates whether the currently active network is connected.
     * @param context The <tt>Context</tt>.
     * @return <tt>true</tt> if network is connected, <tt>false</tt> otherwise.
     * @see #isNetworkConnected(ConnectivityManager)
     */
    public static boolean isNetworkConnected(Context context) {
        final NetworkInfo info = ((ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * Indicates whether the currently active network is connected.
     * @param cm The {@link ConnectivityManager}.
     * @return <tt>true</tt> if network is connected, <tt>false</tt> otherwise.
     * @see #isNetworkConnected(Context)
     */
    public static boolean isNetworkConnected(ConnectivityManager cm) {
        final NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * Returns details about the currently active default data network.
     * @param context The <tt>Context</tt>.
     * @return A {@link NetworkInfo} object for the current default network
     * or <tt>null</tt> if no network is active.
     * @see ConnectivityManager#getActiveNetworkInfo()
     */
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        return ((ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    }

    /**
     * Prints the contents of the <em>conn</em> request headers. This
     * method can only be called before the connection is established.
     * @param conn The {@link URLConnection} request headers to print.
     * @param printer The {@link Printer} to print to.
     * @see #dumpResponseHeaders(URLConnection, Printer)
     */
    public static void dumpRequestHeaders(URLConnection conn, Printer printer) {
        final Map<String, Object> extraHeaders = new HashMap<String, Object>();
        extraHeaders.put("Read-Timeout", Collections.singletonList(conn.getReadTimeout()));
        extraHeaders.put("Connect-Timeout", Collections.singletonList(conn.getConnectTimeout()));
        if (conn instanceof HttpURLConnection) {
            final HttpURLConnection connection = (HttpURLConnection)conn;
            extraHeaders.put("Method", Collections.singletonList(connection.getRequestMethod()));
            extraHeaders.put("Redirects", Collections.singletonList(connection.getInstanceFollowRedirects()));
        }

        dumpHeaders(printer, conn.getURL(), " %s Request Headers ", conn.getRequestProperties(), extraHeaders);
    }

    /**
     * Prints the contents of the <em>conn</em> response headers.
     * @param conn The {@link URLConnection} response headers to print.
     * @param printer The {@link Printer} to print to.
     * @see #dumpRequestHeaders(URLConnection, Printer)
     */
    public static void dumpResponseHeaders(URLConnection conn, Printer printer) {
        dumpHeaders(printer, conn.getURL(), " %s Response Headers ", conn.getHeaderFields(), null);
    }

    /**
     * Prints the contents of the connection headers.
     */
    private static void dumpHeaders(Printer printer, StringBuilder result, Map<String, ?> headers) {
        if (ArrayUtils.getSize(headers) > 0) {
            for (Entry<String, ?> header : headers.entrySet()) {
                result.setLength(0);
                result.append("  ").append(header.getKey()).append(" = ").append(header.getValue());
                printer.println(result.toString());
            }
        }
    }

    /**
     * Prints the contents of the connection headers.
     */
    private static void dumpHeaders(Printer printer, URL url, String format, Map<String, ?> headers, Map<String, ?> extraHeaders) {
        final StringBuilder result = new StringBuilder(80);
        DebugUtils.dumpSummary(printer, result, 80, format, url.getProtocol().toUpperCase(Locale.getDefault()));
        printer.println("  URL = " + url.toString());

        dumpHeaders(printer, result, headers);
        dumpHeaders(printer, result, extraHeaders);
    }

    /**
     * This utility class cannot be instantiated.
     */
    private NetworkUtils() {
    }
}
