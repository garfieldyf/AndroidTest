package android.ext.net;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.ext.util.ArrayUtils;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.JSONUtils;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;

/**
 * Class <tt>DownloadRequest</tt> used to downloads the resource from the
 * remote HTTP server. This class both support HTTP "GET" and "POST" methods.
 * <h2>Usage</h2>
 * <p>Here is an example:</p>
 * <pre>
 * final JSONObject result = new DownloadRequest(url)
 *     .connectTimeout(60000)
 *     .readTimeout(60000)
 *     .requestHeader("Content-Type", "application/json")
 *     .post(data)
 *     .download(null);
 * </pre>
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class DownloadRequest {
    /* package */ int offset;
    /* package */ int count;
    /* package */ Object data;
    /* package */ AsyncTask task;
    /* package */ final HttpURLConnection connection;

    /**
     * Constructor
     * @param url The url to connect the remote HTTP server.
     * @throws IOException if an error occurs while opening the connection.
     */
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
     * Equivalent to calling <tt>post(data, 0, data.length)</tt>.
     * @param data The byte array to post.
     * @return This request.
     * @see #post(Object)
     * @see #post(byte[], int, int)
     * @see #post(PostCallback, int)
     */
    public final DownloadRequest post(byte[] data) {
        return post(data, 0, data.length);
    }

    /**
     * Sets the <em>data</em> to post to the remote HTTP server.
     * @param data May be a <tt>JSONObject, JSONArray, String, InputStream</tt>
     * or a container of the <tt>JSONArray or JSONObject</tt>.
     * @return This request.
     * @see #post(byte[])
     * @see #post(byte[], int, int)
     * @see #post(PostCallback, int)
     * @see JSONUtils#writeObject(JsonWriter, Object)
     */
    public final DownloadRequest post(Object data) {
        DebugUtils.__checkWarning(this.data != null, "DownloadRequest", "The post data is already exists. Do you want overrides it.");
        this.data = data;
        return this;
    }

    /**
     * Sets the {@link PostCallback} to post the data to the remote HTTP server.
     * @param callback The <tt>PostCallback</tt>.
     * @param arg The user-defined data passed by the {@link PostCallback#onPostData}.
     * @return This request.
     * @see #post(byte[])
     * @see #post(Object)
     * @see #post(byte[], int, int)
     */
    public final DownloadRequest post(PostCallback callback, int arg) {
        DebugUtils.__checkWarning(this.data != null, "DownloadRequest", "The post data is already exists. Do you want overrides it.");
        this.count = arg;
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
    public final DownloadRequest post(byte[] data, int offset, int count) {
        DebugUtils.__checkWarning(this.data != null, "DownloadRequest", "The post data is already exists. Do you want overrides it.");
        ArrayUtils.checkRange(offset, count, data.length);
        this.data   = data;
        this.count  = count;
        this.offset = offset;
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
     * Executes the download task with the arguments supplied to this request. The <tt>DownloadRequest</tt>
     * instance must be call {@link AsyncDownloadTask#newDownloadRequest(String)} to create.
     * @param params The parameters of the task.
     * @return The instance of task.
     * @see #execute(Executor, Params[])
     * @see AsyncDownloadTask#newDownloadRequest(String)
     */
    public final <Params, T extends AsyncTask<Params, ?, ?>> T execute(Params... params) {
        DebugUtils.__checkError(task == null, "The DownloadRequest must be call AsyncDownloadTask.newDownloadRequest() to create");
        return (T)task.execute(params);
    }

    /**
     * Executes the download task with the arguments supplied to this request. The <tt>DownloadRequest</tt>
     * instance must be call {@link AsyncDownloadTask#newDownloadRequest(String)} to create.
     * @param exec The {@link Executor} to use.
     * @param params The parameters of the task.
     * @return The instance of task.
     * @see #execute(Params[])
     * @see AsyncDownloadTask#newDownloadRequest(String)
     */
    public final <Params, T extends AsyncTask<Params, ?, ?>> T execute(Executor exec, Params... params) {
        DebugUtils.__checkError(task == null, "The DownloadRequest must be call AsyncDownloadTask.newDownloadRequest() to create");
        return (T)task.executeOnExecutor(exec, params);
    }

    /**
     * Constructor
     * @param task The owner {@link AsyncTask}.
     * @param url The url to connect the remote HTTP server.
     * @throws IOException if an error occurs while opening the connection.
     */
    /* package */ DownloadRequest(AsyncTask task, String url) throws IOException {
        this(url);
        this.task = task;
    }

    /**
     * Connects to the remote HTTP server with the arguments supplied to this request.
     */
    /* package */ final int connect(byte[] tempBuffer) throws IOException {
        DownloadRequest.__checkHeaders(connection, true);
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
            data = null;  // Clears the callback to avoid potential memory leaks.
        } else {
            connectImpl("GET");
        }

        DownloadRequest.__checkHeaders(connection, false);
        return connection.getResponseCode();
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

    /**
     * Connects to the remote HTTP server with the specified method.
     */
    private void connectImpl(String method) throws IOException {
        connection.setRequestMethod(method);
        connection.connect();
    }

    /**
     * Posts the <tt>InputStream</tt> contents to the remote HTTP server.
     */
    private void postData(InputStream is, byte[] tempBuffer) throws IOException {
        final OutputStream os = connection.getOutputStream();
        try {
            FileUtils.copyStream(is, os, null, tempBuffer).flush();
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

    private static void __checkHeaders(URLConnection conn, boolean request) {
        final Printer printer = new LogPrinter(Log.DEBUG, DownloadRequest.class.getName());
        if (request) {
            NetworkUtils.dumpRequestHeaders(conn, printer);
        } else {
            NetworkUtils.dumpResponseHeaders(conn, printer);
        }
    }

    /**
     * Callback interface used to post the data to the remote HTTP server.
     */
    public static interface PostCallback {
        /**
         * Called on a background thread to post the data to the remote HTTP server.
         * @param conn The {@link HttpURLConnection} whose connecting the remote HTTP server.
         * @param arg The user-defined data, passed earlier by {@link DownloadRequest#post}.
         * @param tempBuffer May be <tt>null</tt>. The temporary byte array used to post,
         * passed earlier by {@link DownloadRequest#download}.
         * @throws IOException if an error occurs while posting the data.
         */
        void onPostData(HttpURLConnection conn, int arg, byte[] tempBuffer) throws IOException;
    }
}
