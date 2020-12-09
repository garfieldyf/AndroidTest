package android.ext.net;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import android.ext.json.JSONUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.ByteArrayBuffer;
import android.ext.util.Cancelable;
import android.ext.util.FileUtils;
import android.util.JsonReader;
import android.util.Log;
import android.util.LogPrinter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class <tt>DownloadRequest</tt> used to downloads the resource from the remote server.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * final JSONObject result = new DownloadRequest(url)
 *     .readTimeout(60000)
 *     .connectTimeout(60000)
 *     .download(null);</pre>
 * @author Garfield
 */
public class DownloadRequest {
    /**
     * Used on the DEBUG mode.
     * @hide
     */
    public boolean __checkDumpHeaders = true;
    /* package */ final URLConnection mConnection;

    /**
     * Constructor
     * @param url The url to connect the remote server.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadRequest(String)
     * @see #DownloadRequest(URL, Proxy)
     */
    public DownloadRequest(URL url) throws IOException {
        mConnection = url.openConnection();
    }

    /**
     * Constructor
     * @param url The url to connect the remote server.
     * @throws MalformedURLException if <em>url</em> could not be parsed as a {@link URL}.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadRequest(URL)
     * @see #DownloadRequest(URL, Proxy)
     */
    public DownloadRequest(String url) throws MalformedURLException, IOException {
        mConnection = new URL(url).openConnection();
    }

    /**
     * Constructor
     * @param url The url to connect the remote server.
     * @param proxy The proxy through which the connection will be established.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadRequest(URL)
     * @see #DownloadRequest(String)
     */
    public DownloadRequest(URL url, Proxy proxy) throws IOException {
        mConnection = url.openConnection(proxy);
    }

    /**
     * Sets whether the connection allows to use caches.
     * @param useCaches <tt>true</tt> if the connection
     * allows to use caches, <tt>false</tt> otherwise.
     * @return This request.
     * @see URLConnection#setUseCaches(boolean)
     */
    public final DownloadRequest useCaches(boolean useCaches) {
        mConnection.setUseCaches(useCaches);
        return this;
    }

    /**
     * Sets whether the connection follows redirects.
     * @param redirects <tt>true</tt> if the connection will
     * follows redirects, <tt>false</tt> otherwise.
     * @return This request.
     * @see HttpURLConnection#setInstanceFollowRedirects(boolean)
     */
    public final DownloadRequest redirects(boolean redirects) {
        if (mConnection instanceof HttpURLConnection) {
            ((HttpURLConnection)mConnection).setInstanceFollowRedirects(redirects);
        }

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
        mConnection.setReadTimeout(timeoutMillis);
        return this;
    }

    /**
     * Sets the maximum time in milliseconds to wait while connecting.
     * @param timeoutMillis The connect timeout in milliseconds.
     * @return This request.
     * @see URLConnection#setConnectTimeout(int)
     */
    public final DownloadRequest connectTimeout(int timeoutMillis) {
        mConnection.setConnectTimeout(timeoutMillis);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Host", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest host(String value) {
        mConnection.setRequestProperty("Host", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Accept", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest accept(String value) {
        mConnection.setRequestProperty("Accept", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Accept-Charset", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest acceptCharset(String value) {
        mConnection.setRequestProperty("Accept-Charset", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Accept-Encoding", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest acceptEncoding(String value) {
        mConnection.setRequestProperty("Accept-Encoding", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("User-Agent", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest userAgent(String value) {
        mConnection.setRequestProperty("User-Agent", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Connection", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest connection(String value) {
        mConnection.setRequestProperty("Connection", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Range", "bytes=value")</tt>.
     * @param value The value of the field.
     * @return This request.
     * @see #range(int)
     * @see #range(int, int)
     */
    public final DownloadRequest range(String value) {
        mConnection.setRequestProperty("Range", "bytes=" + value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Range", "bytes=offset-")</tt>.
     * @param offset The start bytes of the range.
     * @return This request.
     * @see #range(String)
     * @see #range(int, int)
     */
    public final DownloadRequest range(int offset) {
        mConnection.setRequestProperty("Range", "bytes=" + offset + "-");
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Range", "bytes=start-end")</tt>.
     * @param start The start bytes of the range.
     * @param end The end bytes of the range.
     * @return This request.
     * @see #range(int)
     * @see #range(String)
     */
    public final DownloadRequest range(int start, int end) {
        mConnection.setRequestProperty("Range", "bytes=" + start + "-" + end);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Keep-Alive", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest keepAlive(String value) {
        mConnection.setRequestProperty("Keep-Alive", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Content-Type", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest contentType(String value) {
        mConnection.setRequestProperty("Content-Type", value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Content-Encoding", value)</tt>.
     * @param value The value of the field.
     * @return This request.
     */
    public final DownloadRequest contentEncoding(String value) {
        mConnection.setRequestProperty("Content-Encoding", value);
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
        mConnection.setRequestProperty(field, value);
        return this;
    }

    /**
     * Sets the values of the specified request header fields.
     * @param headers The request header fields to be set.
     * @return This request.
     * @see #requestHeader(String, String)
     */
    public final DownloadRequest requestHeaders(Map<String, ?> headers) {
        if (!ArrayUtils.isEmpty(headers)) {
            for (Entry<String, ?> header : headers.entrySet()) {
                mConnection.setRequestProperty(header.getKey(), header.getValue().toString());
            }
        }

        return this;
    }

    /**
     * Downloads the JSON data from the remote server with the arguments supplied to this request.
     * @param cancelable A {@link Cancelable} can be check the download is cancelled, or <tt>null</tt> if none.
     * @return If the download succeeded return a <tt>JSONObject</tt> or <tt>JSONArray</tt> object, If the download was
     * cancelled before it completed normally the returned value is undefined, If the download failed return <tt>null</tt>.
     * @throws IOException if an error occurs while downloading the resource.
     * @see #download(File, Cancelable, byte[])
     * @see #download(DownloadCallback, Object[])
     * @see #download(OutputStream, Cancelable, byte[])
     */
    public final <T> T download(Cancelable cancelable) throws IOException {
        try {
            return (connect() == HTTP_OK ? this.<T>downloadImpl(cancelable) : null);
        } finally {
            disconnect();
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     * @param out The {@link OutputStream} to write the resource.
     * @param cancelable A {@link Cancelable} can be check the download is cancelled, or <tt>null</tt> if none.
     * If the download was cancelled before it completed normally the <em>out's</em> contents is undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for downloading.
     * @return The response code returned by the remote server, <tt>-1</tt> if no valid response code.
     * @throws IOException if an error occurs while downloading the resource.
     * @see #download(Cancelable)
     * @see #download(File, Cancelable, byte[])
     * @see #download(DownloadCallback, Object[])
     */
    public final int download(OutputStream out, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        try {
            final int statusCode = connect();
            if (statusCode == HTTP_OK || statusCode == HTTP_PARTIAL) {
                downloadImpl(out, cancelable, tempBuffer);
            }

            return statusCode;
        } finally {
            disconnect();
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param file The file to write the resource, must be absolute file path.
     * @param cancelable A {@link Cancelable} can be check the download is cancelled, or <tt>null</tt> if
     * none. If the download was cancelled before it completed normally the file's contents is undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for downloading.
     * @return The response code returned by the remote server, <tt>-1</tt> if no valid response code.
     * @throws IOException if an error occurs while downloading the resource.
     * @see #download(Cancelable)
     * @see #download(DownloadCallback, Object[])
     * @see #download(OutputStream, Cancelable, byte[])
     */
    public final int download(File file, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        try {
            final int statusCode = connect();
            switch (statusCode) {
            case HTTP_OK:
                downloadImpl(file, cancelable, tempBuffer, false);
                break;

            case HTTP_PARTIAL:
                downloadImpl(file, cancelable, tempBuffer, true);
                break;
            }

            return statusCode;
        } finally {
            disconnect();
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     * @param callback The {@link DownloadCallback} to used to downloads.
     * @param params The parameters passed into {@link DownloadCallback#onDownload}. If no parameters,
     * you can pass <em>(Params[])null</em> instead of allocating an empty array.
     * @return A result, defined by the subclass of the <tt>DownloadCallback</tt>.
     * @throws Exception if an error occurs while downloading the resource.
     * @see #download(Cancelable)
     * @see #download(File, Cancelable, byte[])
     * @see #download(OutputStream, Cancelable, byte[])
     */
    @SuppressWarnings("unchecked")
    public final <Params, Result> Result download(DownloadCallback<Params, Result> callback, Params... params) throws Exception {
        try {
            return callback.onDownload(mConnection, connect(), params);
        } finally {
            disconnect();
        }
    }

    /**
     * Returns the value of the <tt>content-length</tt> header field.
     * @param conn The {@link URLConnection}.
     * @return The content length of the resource, <tt>-1</tt> if the
     * content length is not known.
     */
    public static long getContentLength(URLConnection conn) {
        try {
            return Long.parseLong(conn.getHeaderField("content-length"));
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Connects to the remote server with the arguments supplied to this request.
     */
    /* package */ int connect() throws IOException {
        __checkDumpHeaders(true);
        mConnection.connect();
        __checkDumpHeaders(false);
        return (mConnection instanceof HttpURLConnection ? ((HttpURLConnection)mConnection).getResponseCode() : HTTP_OK);
    }

    /**
     * Disconnects the connection and release any system resources it holds.
     */
    /* package */ final void disconnect() {
        if (mConnection instanceof HttpURLConnection) {
            ((HttpURLConnection)mConnection).disconnect();
        }
    }

    /**
     * Downloads the JSON data from the remote server with the arguments supplied to this request.
     */
    /* package */ final <T> T downloadImpl(Cancelable cancelable) throws IOException {
        try (final JsonReader reader = new JsonReader(new InputStreamReader(mConnection.getInputStream(), StandardCharsets.UTF_8))) {
            return JSONUtils.parse(reader, cancelable);
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     */
    /* package */ final void downloadImpl(OutputStream out, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        try (final InputStream is = mConnection.getInputStream()) {
            if (out instanceof ByteArrayBuffer) {
                ((ByteArrayBuffer)out).readFrom(is, mConnection.getContentLength(), cancelable);
            } else {
                FileUtils.copyStream(is, out, cancelable, tempBuffer);
            }
        }
    }

    /**
     * Downloads the file from the remote server with the arguments supplied to this request.
     */
    /* package */ final void downloadImpl(File file, Cancelable cancelable, byte[] tempBuffer, boolean append) throws IOException {
        FileUtils.mkdirs(file.getPath(), FileUtils.FLAG_IGNORE_FILENAME);
        try (final OutputStream os = new FileOutputStream(file, append)) {
            downloadImpl(os, cancelable, tempBuffer);
        }
    }

    /* package */ final void __checkDumpHeaders(boolean request) {
        boolean dumpHeaders = false;
        dumpHeaders = this.__checkDumpHeaders;

        if (dumpHeaders) {
            final LogPrinter printer = new LogPrinter(Log.DEBUG, getClass().getName());
            if (request) {
                NetworkUtils.dumpRequestHeaders(mConnection, printer);
            } else {
                NetworkUtils.dumpResponseHeaders(mConnection, printer);
            }
        }
    }

    /**
     * Callback interface used to download the data from the remote server.
     */
    public static interface DownloadCallback<Params, Result> {
        /**
         * Called on a background thread to download the data from the remote server.
         * @param conn The {@link URLConnection} whose connecting the remote server.
         * @param statusCode The response code returned by the remote server.
         * @param params The parameters, passed earlier by {@link DownloadRequest#download}.
         * @return A result, defined by the subclass of this callback.
         * @throws Exception if an error occurs while downloading the data from the remote server.
         */
        Result onDownload(URLConnection conn, int statusCode, Params[] params) throws Exception;
    }
}
