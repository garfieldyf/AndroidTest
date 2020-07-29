package android.ext.net;

import android.ext.json.JSONArray;
import android.ext.json.JSONObject;
import android.ext.json.JSONUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.util.JsonWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Class <tt>DownloadPostRequest</tt> used to downloads the resource from the
 * remote HTTP server. This class both support HTTP "GET" and "POST" methods.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * final JSONObject result = new DownloadPostRequest(url)
 *     .post(obj)
 *     .readTimeout(60000)
 *     .connectTimeout(60000)
 *     .contentType("application/json")
 *     .download(null);</pre>
 * @author Garfield
 */
public final class DownloadPostRequest extends DownloadRequest {
    private Object mData;
    private Object[] mParams;

    /**
     * Constructor
     * @param url The url to connect the remote HTTP server.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadPostRequest(String)
     * @see #DownloadPostRequest(URL, Proxy)
     */
    public DownloadPostRequest(URL url) throws IOException {
        super(url);
    }

    /**
     * Constructor
     * @param url The url to connect the remote HTTP server.
     * @throws MalformedURLException if <em>url</em> could not be parsed as a {@link URL}.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadPostRequest(URL)
     * @see #DownloadPostRequest(URL, Proxy)
     */
    public DownloadPostRequest(String url) throws MalformedURLException, IOException {
        super(url);
    }

    /**
     * Constructor
     * @param url The url to connect the remote server.
     * @param proxy The proxy through which the connection will be established.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadPostRequest(URL)
     * @see #DownloadPostRequest(String)
     */
    public DownloadPostRequest(URL url, Proxy proxy) throws IOException {
        super(url, proxy);
    }

    /**
     * Sets the <em>data</em> to post to the remote HTTP server.
     * @param data May be a <tt>JSONObject, JSONArray, File, InputStream</tt>
     * or the collections(<tt>Array, Collection, Map</tt>).
     * @return This request.
     * @see #post(byte[], int, int)
     * @see #post(String, int, int)
     * @see #post(PostCallback, Object[])
     * @see JSONUtils#writeObject(JsonWriter, Object)
     */
    public final DownloadPostRequest post(Object data) {
        DebugUtils.__checkWarning(mData != null, "DownloadPostRequest", "The POST data is already exists. Do you want overrides it.");
        mData = data;
        return this;
    }

    /**
     * Sets the byte array to post to the remote HTTP server.
     * @param data The byte array to post.
     * @param offset The start position in <em>data</em> from where to get bytes.
     * @param count The number of bytes from <em>data</em> to write to.
     * @return This request.
     * @see #post(Object)
     * @see #post(String, int, int)
     * @see #post(PostCallback, Object[])
     */
    public final DownloadPostRequest post(byte[] data, int offset, int count) {
        DebugUtils.__checkRange(offset, count, data.length);
        return postImpl(data, offset, count);
    }

    /**
     * Sets the <tt>String</tt> to post to the remote HTTP server.
     * @param data The string to post.
     * @param offset The start position in <em>data</em> from where to get characters.
     * @param count The number of characters from <em>data</em> to write to.
     * @return This request.
     * @see #post(Object)
     * @see #post(byte[], int, int)
     * @see #post(PostCallback, Object[])
     */
    public final DownloadPostRequest post(String data, int offset, int count) {
        DebugUtils.__checkRange(offset, count, data.length());
        return postImpl(data, offset, count);
    }

    /**
     * Sets the {@link PostCallback} to post the data to the remote HTTP server.
     * @param callback The <tt>PostCallback</tt> to set.
     * @param params The parameters passed into {@link PostCallback#onPostData}.
     * If no parameters, you can pass <em>(Object[])null</em> instead of allocating
     * an empty array.
     * @see #post(Object)
     * @see #post(byte[], int, int)
     * @see #post(String, int, int)
     */
    public final DownloadPostRequest post(PostCallback callback, Object... params) {
        DebugUtils.__checkWarning(mData != null, "DownloadPostRequest", "The POST data is already exists. Do you want overrides it.");
        mParams = params;
        mData = callback;
        return this;
    }

    @Override
    /* package */ int connect() throws IOException {
        __checkDumpHeaders(true);
        if (mData instanceof JSONObject || mData instanceof JSONArray || mData instanceof org.json.JSONObject || mData instanceof org.json.JSONArray || mData instanceof Collection || mData instanceof Map || mData instanceof Object[]) {
            connectImpl();
            postData(mData);
        } else if (mData instanceof File) {
            connectImpl();
            postData((File)mData);
        } else if (mData instanceof InputStream) {
            connectImpl();
            postData((InputStream)mData);
        } else if (mData instanceof PostCallback) {
            connectImpl();
            ((PostCallback)mData).onPostData(mConnection, mParams);
        } else if (mData instanceof byte[]) {
            connectImpl();
            postData((byte[])mData, (int)mParams[0], (int)mParams[1]);
        } else if (mData instanceof String) {
            connectImpl();
            postData((String)mData, (int)mParams[0], (int)mParams[1]);
        } else {
            DebugUtils.__checkError(mData != null, DebugUtils.toString(mData, new StringBuilder("Unsupported POST type - ")).toString());
            mConnection.connect();
        }

        // Clears the mData and mParams to prevent memory leaks.
        mData = mParams = null;
        __checkDumpHeaders(false);
        return ((HttpURLConnection)mConnection).getResponseCode();
    }

    /**
     * Connects to the remote HTTP server with the specified method.
     */
    private void connectImpl() throws IOException {
        ((HttpURLConnection)mConnection).setRequestMethod("POST");
        mConnection.setDoOutput(true);
        mConnection.connect();
    }

    /**
     * Sets the <em>data</em> to post to the remote HTTP server.
     */
    private DownloadPostRequest postImpl(Object data, int offset, int count) {
        DebugUtils.__checkWarning(mData != null, "DownloadPostRequest", "The POST data is already exists. Do you want overrides it.");
        mData = data;
        mParams = new Object[] { offset, count };
        return this;
    }

    /**
     * Posts the <tt>File</tt> contents to the remote HTTP server.
     */
    private void postData(File file) throws IOException {
        try (final InputStream is = new FileInputStream(file)) {
            postData(is);
        }
    }

    /**
     * Posts the <tt>InputStream</tt> contents to the remote HTTP server.
     */
    private void postData(InputStream is) throws IOException {
        try (final OutputStream os = mConnection.getOutputStream()) {
            FileUtils.copyStream(is, os, null, null);
        }
    }

    /**
     * Posts the data to the remote HTTP server.
     */
    private void postData(Object data) throws IOException {
        try (final JsonWriter writer = new JsonWriter(new OutputStreamWriter(mConnection.getOutputStream(), StandardCharsets.UTF_8))) {
            JSONUtils.writeObject(writer, data);
        }
    }

    /**
     * Posts the byte array to the remote HTTP server.
     */
    private void postData(byte[] data, int offset, int count) throws IOException {
        try (final OutputStream os = mConnection.getOutputStream()) {
            os.write(data, offset, count);
        }
    }

    /**
     * Posts the <tt>String</tt> to the remote HTTP server.
     */
    private void postData(String data, int offset, int count) throws IOException {
        try (final OutputStreamWriter writer = new OutputStreamWriter(mConnection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(data, offset, count);
        }
    }

    /**
     * Callback interface used to post the data to the remote server.
     */
    public static interface PostCallback {
        /**
         * Called on a background thread to post the data to the remote server.
         * @param conn The {@link URLConnection} whose connecting the remote server.
         * @param params The parameters, passed earlier by {@link DownloadPostRequest#post}.
         * @throws IOException if an error occurs while writing the data to the remote server.
         */
        void onPostData(URLConnection conn, Object[] params) throws IOException;
    }
}
