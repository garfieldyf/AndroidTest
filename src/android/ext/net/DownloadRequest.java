package android.ext.net;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
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
import org.json.JSONException;
import android.ext.util.ArrayUtils;
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
    public boolean __checkDumpHeaders = true;
    /* package */ final URLConnection mConnection;

    /**
     * Constructor
     * @param url The url to connect the remote server.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadRequest(String)
     */
    @Keep
    public DownloadRequest(URL url) throws IOException {
        mConnection = url.openConnection();
        redirects(true);
    }

    /**
     * Constructor
     * @param url The url to connect the remote server.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadRequest(URL)
     */
    @Keep
    public DownloadRequest(String url) throws IOException {
        this(new URL(url));
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
     * Equivalent to calling <tt>requestHeader("Range", "bytes=<em>value</em>")</tt>.
     * @param value The value of the field.
     * @return This request.
     * @see #range(int)
     */
    public final DownloadRequest range(String value) {
        mConnection.setRequestProperty("Range", "bytes=" + value);
        return this;
    }

    /**
     * Equivalent to calling <tt>requestHeader("Range", "bytes=<em>offset</em>-")</tt>.
     * @param offset The start byte of the range.
     * @return This request.
     * @see #range(String)
     */
    public final DownloadRequest range(int offset) {
        mConnection.setRequestProperty("Range", "bytes=" + offset + "-");
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
    public final DownloadRequest requestHeaders(Map<String, String> headers) {
        if (ArrayUtils.getSize(headers) > 0) {
            for (Entry<String, String> header : headers.entrySet()) {
                mConnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }

        return this;
    }

    /**
     * Returns the specified response header value.
     * @param field The header field name.
     * @return The value of the header field, or
     * <tt>null</tt> if no field has been found.
     * @see #responseHeaderInt(String, int)
     */
    public final String responseHeader(String field) {
        return mConnection.getHeaderField(field);
    }

    /**
     * Returns the specified response header value as a number.
     * @param field The header field name.
     * @param defaultValue The default value if no field has been found.
     * @return The value of the header field.
     * @see #responseHeader(String)
     */
    public final int responseHeaderInt(String field, int defaultValue) {
        return mConnection.getHeaderFieldInt(field, defaultValue);
    }

    /**
     * Connects to the remote server with the arguments supplied to this request. <p>Note: This method
     * will not download any resources.</p>
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for post.
     * @throws IOException if an error occurs while connecting to the remote server.
     * @return The response code returned by the remote server, <tt>-1</tt> if no valid response code.
     */
    public final int connect(byte[] tempBuffer) throws IOException {
        try {
            return connectImpl(tempBuffer);
        } finally {
            disconnect();
        }
    }

    /**
     * Downloads the JSON data from the remote server with the arguments supplied to this request.
     * @param cancelable A {@link Cancelable} can be check the download is cancelled, or <tt>null</tt> if none.
     * @return If the download succeeded return a <tt>JSONObject</tt> or <tt>JSONArray</tt> object, If the download was
     * cancelled before it completed normally then the returned value is undefined, If the download failed return <tt>null</tt>.
     * @throws IOException if an error occurs while downloading the resource.
     * @throws JSONException if data can not be parsed.
     * @see #download(String, Cancelable, byte[])
     * @see #download(OutputStream, Cancelable, byte[])
     * @see JSONUtils#newInstance(JsonReader, Cancelable)
     */
    public final <T> T download(Cancelable cancelable) throws IOException, JSONException {
        try {
            return (connectImpl(null) == HTTP_OK ? this.<T>downloadImpl(cancelable) : null);
        } finally {
            disconnect();
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param filename The file name to write the resource, must be absolute file path.
     * @param cancelable A {@link Cancelable} can be check the download is cancelled, or <tt>null</tt> if
     * none. If the download was cancelled before it completed normally then the file's contents is undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for downloading.
     * @return The response code returned by the remote server, <tt>-1</tt> if no valid response code.
     * @throws IOException if an error occurs while downloading to the resource.
     * @see #download(Cancelable)
     * @see #download(OutputStream, Cancelable, byte[])
     */
    public final int download(String filename, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        try {
            final int statusCode = connectImpl(tempBuffer);
            switch (statusCode) {
            case HTTP_OK:
                downloadImpl(filename, cancelable, tempBuffer, false);
                break;

            case HTTP_PARTIAL:
                downloadImpl(filename, cancelable, tempBuffer, true);
                break;
            }

            return statusCode;
        } finally {
            disconnect();
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     * @param out The {@link OutputStream} to write the resource.
     * @param cancelable A {@link Cancelable} can be check the download is cancelled, or <tt>null</tt> if none.
     * If the download was cancelled before it completed normally then the <em>out's</em> contents is undefined.
     * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for downloading.
     * @return The response code returned by the remote server, <tt>-1</tt> if no valid response code.
     * @throws IOException if an error occurs while downloading the resource.
     * @see #download(Cancelable)
     * @see #download(String, Cancelable, byte[])
     */
    public final int download(OutputStream out, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        try {
            final int statusCode = connectImpl(tempBuffer);
            if (statusCode == HTTP_OK || statusCode == HTTP_PARTIAL) {
                downloadImpl(out, cancelable, tempBuffer);
            }

            return statusCode;
        } finally {
            disconnect();
        }
    }

    /**
     * Connects to the remote server with the arguments supplied to this request.
     */
    /* package */ int connectImpl(byte[] tempBuffer) throws IOException {
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
    /* package */ final <T> T downloadImpl(Cancelable cancelable) throws IOException, JSONException {
        final JsonReader reader = new JsonReader(new InputStreamReader(mConnection.getInputStream()));
        try {
            return JSONUtils.newInstance(reader, cancelable);
        } finally {
            reader.close();
        }
    }

    /**
     * Downloads the resource from the remote server with the arguments supplied to this request.
     */
    /* package */ final void downloadImpl(OutputStream out, Cancelable cancelable, byte[] tempBuffer) throws IOException {
        final InputStream is = mConnection.getInputStream();
        try {
            FileUtils.copyStream(is, out, cancelable, tempBuffer);
        } finally {
            is.close();
        }
    }

    /**
     * Downloads the file from the remote server with the arguments supplied to this request.
     */
    /* package */ final void downloadImpl(String filename, Cancelable cancelable, byte[] tempBuffer, boolean append) throws IOException {
        FileUtils.mkdirs(filename, FileUtils.FLAG_IGNORE_FILENAME);
        final OutputStream os = new FileOutputStream(filename, append);
        try {
            downloadImpl(os, cancelable, tempBuffer);
        } finally {
            os.close();
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
}
