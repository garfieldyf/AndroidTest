package android.ext.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import android.support.annotation.Keep;
import android.util.JsonWriter;

/**
 * Class <tt>DownloadPostRequest</tt> used to downloads the resource from the
 * remote HTTP server. This class both support HTTP "GET" and "POST" methods.
 * <h2>Usage</h2>
 * <p>Here is an example:</p><pre>
 * final JSONObject result = new DownloadPostRequest(url)
 *     .post(obj)
 *     .readTimeout(60000)
 *     .connectTimeout(60000)
 *     .contentType("application/json")
 *     .download(null);</pre>
 * @author Garfield
 * @version 1.0
 */
public final class DownloadPostRequest extends DownloadRequest {
    private int offset;
    private int count;
    private Object data;

    /**
     * Constructor
     * @param url The url to connect the remote HTTP server.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadPostRequest(String)
     */
    @Keep
    public DownloadPostRequest(URL url) throws IOException {
        super(url);
    }

    /**
     * Constructor
     * @param url The url to connect the remote HTTP server.
     * @throws IOException if an error occurs while opening the connection.
     * @see #DownloadPostRequest(URL)
     */
    @Keep
    public DownloadPostRequest(String url) throws IOException {
        super(new URL(url));
    }

    /**
     * Equivalent to calling <tt>post(data, 0, data.length)</tt>.
     * @param data The byte array to post.
     * @return This request.
     * @see #post(Object)
     * @see #post(byte[], int, int)
     * @see #post(PostCallback, int)
     */
    public final DownloadPostRequest post(byte[] data) {
        return post(data, 0, data.length);
    }

    /**
     * Sets the <em>data</em> to post to the remote HTTP server.
     * @param data May be an <tt>InputStream</tt> or a <tt>JSONObject, JSONArray,
     * String</tt> or their collections(<tt>Array, Collection, Map</tt>).
     * @return This request.
     * @see #post(byte[])
     * @see #post(byte[], int, int)
     * @see #post(PostCallback, int)
     * @see JSONUtils#writeObject(JsonWriter, Object)
     */
    public final DownloadPostRequest post(Object data) {
        DebugUtils.__checkWarning(this.data != null, "DownloadPostRequest", "The POST data is already exists. Do you want overrides it.");
        this.data = data;
        return this;
    }

    /**
     * Sets the {@link PostCallback} to post the data to the remote HTTP server.
     * @param callback The <tt>PostCallback</tt> to set.
     * @param token A token passed into the {@link PostCallback#onPostData} to
     * identify the post.
     * @return This request.
     * @see #post(byte[])
     * @see #post(Object)
     * @see #post(byte[], int, int)
     */
    public final DownloadPostRequest post(PostCallback callback, int token) {
        DebugUtils.__checkWarning(this.data != null, "DownloadPostRequest", "The POST data is already exists. Do you want overrides it.");
        this.count = token;
        this.data  = callback;
        return this;
    }

    /**
     * Sets the byte array to post to the remote HTTP server.
     * @param data The byte array to post.
     * @param offset The start position in <em>data</em> from where to get bytes.
     * @param count The number of bytes from <em>data</em> to write to.
     * @return This request.
     * @see #post(byte[])
     * @see #post(Object)
     * @see #post(PostCallback, int)
     */
    public final DownloadPostRequest post(byte[] data, int offset, int count) {
        DebugUtils.__checkWarning(this.data != null, "DownloadPostRequest", "The POST data is already exists. Do you want overrides it.");
        ArrayUtils.checkRange(offset, count, data.length);
        this.data   = data;
        this.count  = count;
        this.offset = offset;
        return this;
    }

    @Override
    /* package */ int connectImpl(byte[] tempBuffer) throws IOException {
        __checkHeaders(true);
        if (data instanceof JSONObject || data instanceof JSONArray || data instanceof Collection || data instanceof Map || data instanceof Object[]) {
            connectImpl("POST");
            postData(data);
        } else if (data instanceof byte[]) {
            connectImpl("POST");
            postData((byte[])data, offset, count);
        } else if (data instanceof InputStream) {
            connectImpl("POST");
            postData((InputStream)data, tempBuffer);
        } else if (data instanceof String) {
            final byte[] data = ((String)this.data).getBytes();
            connectImpl("POST");
            postData(data, 0, data.length);
        } else if (data instanceof PostCallback) {
            connectImpl("POST");
            ((PostCallback)data).onPostData(connection, count, tempBuffer);
        } else {
            connectImpl("GET");
        }

        // Clears the data to avoid potential memory leaks.
        this.data = null;
        __checkHeaders(false);
        return ((HttpURLConnection)connection).getResponseCode();
    }

    /**
     * Connects to the remote HTTP server with the specified method.
     */
    private void connectImpl(String method) throws IOException {
        ((HttpURLConnection)connection).setRequestMethod(method);
        connection.connect();
    }

    /**
     * Posts the <tt>InputStream</tt> contents to the remote HTTP server.
     */
    private void postData(InputStream is, byte[] tempBuffer) throws IOException {
        final OutputStream os = connection.getOutputStream();
        try {
            FileUtils.copyStream(is, os, null, tempBuffer);
        } finally {
            os.close();
        }
    }

    /**
     * Posts the byte array to the remote HTTP server.
     */
    private void postData(byte[] data, int offset, int count) throws IOException {
        final OutputStream os = connection.getOutputStream();
        try {
            os.write(data, offset, count);
        } finally {
            os.close();
        }
    }

    /**
     * Posts the data to the remote HTTP server.
     */
    private void postData(Object data) throws IOException {
        final JsonWriter writer = new JsonWriter(new OutputStreamWriter(connection.getOutputStream()));
        try {
            JSONUtils.writeObject(writer, data);
        } finally {
            writer.close();
        }
    }

    /**
     * Callback interface used to post the data to the remote server.
     */
    public static interface PostCallback {
        /**
         * Called on a background thread to post the data to the remote server.
         * @param conn The {@link URLConnection} whose connecting the remote server.
         * @param token A token to identify the post, passed earlier by {@link DownloadPostRequest#post}.
         * @param tempBuffer May be <tt>null</tt>. The temporary byte array to use for post,
         * passed earlier by {@link DownloadRequest#download}.
         * @throws IOException if an error occurs while writing the data to the remote server.
         */
        void onPostData(URLConnection conn, int token, byte[] tempBuffer) throws IOException;
    }
}
