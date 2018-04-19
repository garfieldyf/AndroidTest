package android.ext.net;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import android.ext.util.Cancelable;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import android.support.annotation.Keep;
import android.util.JsonReader;
import android.util.Log;
import android.util.LogPrinter;

/**
 * Class <tt>DownloadRequest</tt> used to downloads the resource from the remote server.
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * final JSONObject result = new DownloadRequest(url)
 *     .readTimeout(60000)
 *     .connectTimeout(60000)
 *     .contentType("application/json")
 *     .download(null);</pre>
 * @author Garfield
 * @version 1.0
 */
public class DownloadRequest {
    public boolean __checkHeaders = true;
    /* package */ final URLConnection connection;

    /**
     * Constructor
     * @param url The url to connect the remote server.
     * @throws IOException if an error occurs while opening the connection.
     */
    @Keep
    public DownloadRequest(String url) throws IOException {
        connection = new URL(url).openConnection();
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection)connection).setInstanceFollowRedirects(true);
        }
    }

    /**
     * Sets whether the connection allows to use caches.
     * @param useCaches <tt>true</tt> if the connection
     * allows to use caches, <tt>false</tt> otherwise.
     * @return This request.
     * @see URLConnection#setUseCaches(boolean)
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
     * @see URLConnection#setReadTimeout(int)
     */
    public final DownloadRequest readTimeout(int timeoutMillis) {
        connection.setReadTimeout(timeoutMillis);
        return this;
    }

    /**
     * Sets the maximum time in milliseconds to wait while connecting.
     * @param timeoutMillis The connect timeout in milliseconds.
     * @return This request.
     * @see URLConnection#setConnectTimeout(int)
     */
    public final DownloadRequest connectTimeout(int timeoutMillis) {
        connection.setConnectTimeout(timeoutMillis);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Accept", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest accept(String value) {
        connection.setRequestProperty("Accept", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("User-Agent", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest userAgent(String value) {
        connection.setRequestProperty("User-Agent", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Connection", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest connection(String value) {
        connection.setRequestProperty("Connection", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Content-Type", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest contentType(String value) {
        connection.setRequestProperty("Content-Type", value);
        return this;
    }

    /**
     * Sets the value of the specified request header field.
     * @param field The request header field to be set.
     * @param value The value of the request header field.
     * @return This request.
     * @see #requestHeaders(Map)
     * @see URLConnection#setRequestProperty(String, String)
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
     */
    public final DownloadRequest requestHeaders(Map<String, String> headers) {
        for (Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        return this;
    }

    /**
     * Downloads the JSON data from the remote server with the arguments supplied to this request.
     * @param cancelable A {@link Cancelable} that can be cancelled, or <tt>null</tt> if none.
     * @return If the download succeeded return a <tt>JSONObject</tt> or <tt>JSONArray</tt> object,
     * If the download was cancelled before it completed normally then the returned value undefined,
     * If the download failed return <tt>null</tt>.
     * @throws IOException if an error occurs while downloading the resource.
     * @throws JSONException if data can not be parsed.
     * @see #download(String, Cancelable, byte[])
     * @see #download(OutputStream, Cancelable, byte[])
     * @see JSONUtils#newInstance(JsonReader, Cancelable)
     */
    public final <T> T download(Cancelable cancelable) throws IOException, JSONException {
        try {
            return (connect(null) == HttpURLConnection.HTTP_OK ? this.<T>downloadImpl(cancelable) : null);
        } finally {
            disconnect();
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param filename The file name to write the resource, must be absolute file path.
     * @param cancelable A {@link Cancelable} that can be cancelled, or <tt>null</tt> if none. If
     * the download was cancelled before it completed normally then the file's contents undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to store the read bytes.
     * @return The response code returned by the remote server.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download(Cancelable)
     * @see #download(OutputStream, Cancelable, byte[])
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
     * Downloads the resource from the remote server with the arguments supplied to this request.
     * @param out The {@link OutputStream} to write the resource.
     * @param cancelable A {@link Cancelable} that can be cancelled, or <tt>null</tt> if none. If the
     * download was cancelled before it completed normally then the <em>out's</em> contents undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to store the read bytes.
     * @return The response code returned by the remote server.
     * @throws IOException if an error occurs while downloading the resource.
     * @see #download(Cancelable)
     * @see #download(String, Cancelable, byte[])
     */
    public final int download(OutputStream out, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        InputStream is = null;
        try {
            final int statusCode = connect(tempBuffer);
            if (statusCode == HttpURLConnection.HTTP_OK) {
                is = connection.getInputStream();
                FileUtils.copyStream(is, out, cancelable, tempBuffer);
            }

            return statusCode;
        } finally {
            disconnect();
            FileUtils.close(is);
        }
    }

    /**
     * Connects to the remote server with the arguments supplied to this request.
     */
    /* package */ int connect(byte[] tempBuffer) throws IOException {
        __checkHeaders(true);
        connection.connect();
        __checkHeaders(false);
        return (connection instanceof HttpURLConnection ? ((HttpURLConnection)connection).getResponseCode() : HttpURLConnection.HTTP_OK);
    }

    /**
     * Disconnects the connection and release any system resources it holds.
     */
    /* package */ final void disconnect() {
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection)connection).disconnect();
        }
    }

    /**
     * Downloads the JSON data from the remote server with the arguments supplied to this request.
     */
    /* package */ final <T> T downloadImpl(Cancelable cancelable) throws IOException, JSONException {
        final JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));
        try {
            return JSONUtils.newInstance(reader, cancelable);
        } finally {
            reader.close();
        }
    }

    /* package */ final void __checkHeaders(boolean request) {
        if (getCheckHeaders()) {
            final LogPrinter printer = new LogPrinter(Log.DEBUG, getClass().getName());
            if (request) {
                NetworkUtils.dumpRequestHeaders(connection, printer);
            } else {
                NetworkUtils.dumpResponseHeaders(connection, printer);
            }
        }
    }

    private boolean getCheckHeaders() {
        Field field = null;
        try {
            field = DownloadRequest.class.getField("__checkHeaders");
            return (field != null && field.getBoolean(this));
        } catch (Throwable e) {
            return false;
        }
    }
}
