package android.ext.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
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
 *     .connectTimeout(60000)
 *     .readTimeout(60000)
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
     */
    @Keep
    public DownloadPostRequest(String url) throws IOException {
        super(url);
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
        DebugUtils.__checkWarning(this.data != null, "DownloadPostRequest", "The post data is already exists. Do you want overrides it.");
        this.data = data;
        return this;
    }

    /**
     * Sets the {@link PostCallback} to post the data to the remote HTTP server.
     * @param callback The <tt>PostCallback</tt>.
     * @param param The user-defined data passed by the {@link PostCallback#onPostData}.
     * @return This request.
     * @see #post(byte[])
     * @see #post(Object)
     * @see #post(byte[], int, int)
     */
    public final DownloadPostRequest post(PostCallback callback, int param) {
        DebugUtils.__checkWarning(this.data != null, "DownloadPostRequest", "The post data is already exists. Do you want overrides it.");
        this.count = param;
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
        DebugUtils.__checkWarning(this.data != null, "DownloadPostRequest", "The post data is already exists. Do you want overrides it.");
        ArrayUtils.checkRange(offset, count, data.length);
        this.data   = data;
        this.count  = count;
        this.offset = offset;
        return this;
    }

    @Override
    /* package */ int connect(byte[] tempBuffer) throws IOException {
        DownloadRequest.__checkHeaders(connection, getClass().getName(), true);
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

        this.data = null;  // Clears the data to avoid potential memory leaks.
        DownloadRequest.__checkHeaders(connection, getClass().getName(), false);
        return connection.getResponseCode();
    }

    /**
     * Posts the <tt>InputStream</tt> contents to the remote HTTP server.
     */
    private void postData(InputStream is, byte[] tempBuffer) throws IOException {
        final OutputStream os = connection.getOutputStream();
        try {
            FileUtils.copyStream(is, os, null, tempBuffer);
            os.flush();
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
            os.flush();
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
            JSONUtils.writeObject(writer, data).flush();
        } finally {
            writer.close();
        }
    }

    /**
     * Callback interface used to post the data to the remote HTTP server.
     */
    public static interface PostCallback {
        /**
         * Called on a background thread to post the data to the remote HTTP server.
         * @param conn The {@link HttpURLConnection} whose connecting the remote HTTP server.
         * @param param The user-defined data, passed earlier by {@link DownloadPostRequest#post}.
         * @param tempBuffer May be <tt>null</tt>. The temporary byte array used to post,
         * passed earlier by {@link DownloadRequest#download}.
         * @throws IOException if an error occurs while writing the data to the remote HTTP server.
         */
        void onPostData(HttpURLConnection conn, int param, byte[] tempBuffer) throws IOException;
    }
}
