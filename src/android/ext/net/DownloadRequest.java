package android.ext.net;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.ext.util.Cancelable;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import android.support.annotation.Keep;
import android.util.JsonReader;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;

/**
 * Class <tt>DownloadRequest</tt> used to downloads the resource from the remote HTTP server.
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * final JSONObject result = new DownloadRequest(url)
 *     .connectTimeout(60000)
 *     .readTimeout(60000)
 *     .requestHeader("Content-Type", "application/json")
 *     .download(null);</pre>
 * @author Garfield
 * @version 1.0
 */
public class DownloadRequest {
    /* package */ final HttpURLConnection connection;

    /**
     * Constructor
     * @param url The url to connect the remote HTTP server.
     * @throws IOException if an error occurs while opening the connection.
     */
    @Keep
    public DownloadRequest(String url) throws IOException {
        connection = (HttpURLConnection)new URL(url).openConnection();
        connection.setInstanceFollowRedirects(true);
    }

    /**
     * Sets whether the connection allows to use caches.
     * @param useCaches <tt>true</tt> if the connection
     * allows to use caches, <tt>false</tt> otherwise.
     * @return This request.
     * @see HttpURLConnection#setUseCaches(boolean)
     */
    public final DownloadRequest useCaches(boolean useCaches) {
        connection.setUseCaches(useCaches);
        return this;
    }

    /**
     * Sets the maximum time to wait for an input stream read
     * to complete before giving up.
     * @param timeoutMillis The read timeout in milliseconds.
     * @return This request.
     * @see HttpURLConnection#setReadTimeout(int)
     */
    public final DownloadRequest readTimeout(int timeoutMillis) {
        connection.setReadTimeout(timeoutMillis);
        return this;
    }

    /**
     * Sets the maximum time in milliseconds to wait while connecting.
     * @param timeoutMillis The connect timeout in milliseconds.
     * @return This request.
     * @see HttpURLConnection#setConnectTimeout(int)
     */
    public final DownloadRequest connectTimeout(int timeoutMillis) {
        connection.setConnectTimeout(timeoutMillis);
        return this;
    }

    /**
     * Sets the value of the specified request header field.
     * @param field The request header field to be set.
     * @param value The value of the request header field.
     * @return This request.
     * @see #requestHeaders(Map)
     * @see HttpURLConnection#setRequestProperty(String, String)
     */
    public final DownloadRequest requestHeader(String field, String value) {
        connection.setRequestProperty(field, value);
        return this;
    }

    /**
     * Sets the values of the specified request header fields.
     * @param headers The request header fields to be set.
     * @return This request.
     * @see #requestHeader(String, String)
     * @see HttpURLConnection#setRequestProperty(String, String)
     */
    public final DownloadRequest requestHeaders(Map<String, String> headers) {
        for (Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        return this;
    }

    /**
     * Downloads the JSON data from the remote HTTP server with the arguments supplied to this request.
     * @param cancelable A {@link Cancelable} that can be cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray} object,
     * If the operation was cancelled before it completed normally then the returned value undefined,
     * If the operation failed return <tt>null</tt>.
     * @throws IOException if an error occurs while downloading the resource.
     * @throws JSONException if data can not be parsed.
     * @see #download(String, Cancelable, byte[])
     * @see #download(OutputStream, Cancelable, byte[])
     * @see JSONUtils#newInstance(JsonReader, Cancelable)
     */
    public final <T> T download(Cancelable cancelable) throws IOException, JSONException {
        T result = null;
        try {
            // Connects to the remote HTTP server.
            if (connect(null) == HttpURLConnection.HTTP_OK) {
                // Downloads the JSON data from the HTTP server.
                result = downloadImpl(cancelable);
            }
        } finally {
            connection.disconnect();
        }

        return result;
    }

    /**
     * Downloads the resource from the remote HTTP server with the arguments supplied to this request.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param filename The file name to write the resource, must be absolute file path.
     * @param cancelable A {@link Cancelable} that can be cancelled, or <tt>null</tt> if none. If the
     * operation was cancelled before it completed normally then the file's contents undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to store the read bytes.
     * @return The response code returned by the remote HTTP server.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download(Cancelable)
     * @see #download(OutputStream, Cancelable, byte[])
     * @see HttpURLConnection#getResponseCode()
     */
    public final int download(String filename, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        FileUtils.mkdirs(filename, FileUtils.FLAG_IGNORE_FILENAME);
        final OutputStream os = new FileOutputStream(filename);
        try {
            return download(os, cancelable, tempBuffer);
        } finally {
            os.close();
        }
    }

    /**
     * Downloads the resource from the remote HTTP server with the arguments supplied to this request.
     * @param out The {@link OutputStream} to write the resource.
     * @param cancelable A {@link Cancelable} that can be cancelled, or <tt>null</tt> if none. If the
     * operation was cancelled before it completed normally then the <em>out's</em> contents undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to store the read bytes.
     * @return The response code returned by the remote HTTP server.
     * @throws IOException if an error occurs while downloading the resource.
     * @see #download(Cancelable)
     * @see #download(String, Cancelable, byte[])
     * @see HttpURLConnection#getResponseCode()
     */
    public final int download(OutputStream out, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        InputStream is = null;
        int statusCode = -1;

        try {
            // Connects to the remote HTTP server.
            if ((statusCode = connect(tempBuffer)) == HttpURLConnection.HTTP_OK) {
                // Downloads the resource from the HTTP server.
                is = connection.getInputStream();
                FileUtils.copyStream(is, out, cancelable, tempBuffer);
            }
        } finally {
            FileUtils.close(is);
            connection.disconnect();
        }

        return statusCode;
    }

    /**
     * Connects to the remote HTTP server with the arguments supplied to this request.
     */
    /* package */ int connect(byte[] tempBuffer) throws IOException {
        DownloadRequest.__checkHeaders(connection, true);
        connectImpl("GET");
        DownloadRequest.__checkHeaders(connection, false);
        return connection.getResponseCode();
    }

    /**
     * Connects to the remote HTTP server with the specified method.
     */
    /* package */ final void connectImpl(String method) throws IOException {
        connection.setRequestMethod(method);
        connection.connect();
    }

    /**
     * Downloads the JSON data from the remote HTTP server with the arguments supplied to this request.
     */
    /* package */ final <T> T downloadImpl(Cancelable cancelable) throws IOException, JSONException {
        final JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        try {
            return JSONUtils.newInstance(reader, cancelable);
        } finally {
            reader.close();
        }
    }

    /* package */ static void __checkHeaders(URLConnection conn, boolean request) {
        final Printer printer = new LogPrinter(Log.DEBUG, DownloadRequest.class.getName());
        if (request) {
            NetworkUtils.dumpRequestHeaders(conn, printer);
        } else {
            NetworkUtils.dumpResponseHeaders(conn, printer);
        }
    }
}
