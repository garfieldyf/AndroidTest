package android.ext.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;

/**
 * Class NetworkUtils
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
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
     * Returns the mac address from the network interface.
     * @param ifname The network interface name.
     * Pass {@link #WLAN} or {@link #ETHERNET}.
     * @return The mac address or <tt>null</tt>.
     */
    public static String getMacAddress(String ifname) {
        try {
            return DeviceUtils.readDeviceFile("/sys/class/net/" + ifname + "/address");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns details about the currently active default data network.
     * @param context The <tt>Context</tt>.
     * @return A {@link NetworkInfo} object for the current default
     * network or a dummy {@link NetworkInfo} object if no network is active.
     * @see #getActiveNetworkInfo(ConnectivityManager)
     * @see ConnectivityManager#getActiveNetworkInfo()
     */
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        return getActiveNetworkInfo((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    /**
     * Returns details about the currently active default data network.
     * @param cm The {@link ConnectivityManager}.
     * @return A {@link NetworkInfo} object for the current default
     * network or a dummy {@link NetworkInfo} object if no network is active.
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
        dumpHeaders(conn, printer, " Http Request Headers ", conn.getRequestProperties());
    }

    /**
     * Prints the contents of the <em>conn</em> response headers.
     * @param conn The {@link URLConnection} response headers to print.
     * @param printer The {@link Printer} to print to.
     * @see #dumpRequestHeaders(URLConnection, Printer)
     */
    public static void dumpResponseHeaders(URLConnection conn, Printer printer) {
        dumpHeaders(conn, printer, " Http Response Headers ", conn.getHeaderFields());
    }

    /**
     * Prints the contents of the connection headers.
     */
    private static void dumpHeaders(URLConnection conn, Printer printer, String title, Map<String, List<String>> headers) {
        final StringBuilder result = new StringBuilder(80);
        DebugUtils.dumpSummary(printer, result, 80, title, (Object[])null);
        result.setLength(0);
        printer.println(result.append("  URL = ").append(conn.getURL().toString()).toString());

        if (ArrayUtils.getSize(headers) > 0) {
            for (Entry<String, List<String>> header : headers.entrySet()) {
                result.setLength(0);
                result.append("  ").append(header.getKey()).append(" = ").append(header.getValue().toString());
                printer.println(result.toString());
            }
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
     */
    public static final class DownloadRequest {
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
                dumpRequestHeaders(conn, printer);
            } else {
                dumpResponseHeaders(conn, printer);
            }
        }
    }

    /**
     * Class <tt>AsyncDownloadTask</tt> allows to download the resource from the remote HTTP server on a background
     * thread and publish results on the UI thread. This class both support HTTP "GET" and "POST" methods.
     * <h2>Usage</h2>
     * <p>Here is an example:</p>
     * <pre>
     * public final class JSONDownloadTask extends AsyncDownloadTask&lt;Object, Object, JSONObject&gt; {
     *     protected void onPostExecute(JSONObject result) {
     *         if (result != null) {
     *             Log.i("JSONDownloadTask", result.toString());
     *         }
     *     }
     * }
     *
     * final JSONDownloadTask task = new JSONDownloadTask()
     *     .newDownloadRequest(url)
     *     .connectTimeout(60000)
     *     .readTimeout(60000)
     *     .requestHeader("Content-Type", "application/json")
     *     .post(obj)
     *     .execute((Object[])null);
     * </pre>
     */
    public static class AsyncDownloadTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Cancelable {
        private DownloadRequest mRequest;
        private WeakReference<Object> mOwner;

        /**
         * Constructor
         * @see #AsyncDownloadTask(Object)
         */
        public AsyncDownloadTask() {
            DebugUtils.__checkMemoryLeaks(getClass());
        }

        /**
         * Constructor
         * @param owner The owner object. See {@link #setOwner(Object)}.
         * @see #AsyncDownloadTask()
         */
        public AsyncDownloadTask(Object owner) {
            DebugUtils.__checkMemoryLeaks(getClass());
            mOwner = new WeakReference<Object>(owner);
        }

        /**
         * Returns the object that owns this task.
         * @return The owner object or <tt>null</tt> if
         * no owner set or the owner released by the GC.
         * @see #setOwner(Object)
         */
        public final <T> T getOwner() {
            return (mOwner != null ? (T)mOwner.get() : null);
        }

        /**
         * Sets the object that owns this task.
         * @param owner The owner object.
         * @return This task.
         * @see #getOwner()
         */
        public final AsyncDownloadTask<Params, Progress, Result> setOwner(Object owner) {
            mOwner = new WeakReference<Object>(owner);
            return this;
        }

        /**
         * Returns a new {@link DownloadRequest} with the specified <em>url</em>.
         * @param url The url to connect the remote HTTP server.
         * @return The instance of <tt>DownloadRequest</tt>.
         */
        public final DownloadRequest newDownloadRequest(String url) {
            try {
                DebugUtils.__checkError(mRequest != null, "The DownloadRequest is already exists. Only one DownloadRequest may be created per " + getClass().getName());
                return (mRequest = new DownloadRequest(this, url));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected Result doInBackground(Params... params) {
            DebugUtils.__checkError(mRequest == null, "The " + getClass().getName() + " did not call newDownloadRequest()");
            Result result = null;
            try {
                if (mRequest.connect(null) == HttpURLConnection.HTTP_OK) {
                    result = onDownload(mRequest.connection, params);
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), "Couldn't download from - " + mRequest.connection.getURL().toString(), e);
            } finally {
                mRequest.connection.disconnect();
            }

            return result;
        }

        /**
         * Override this method to downloads the resource from the remote HTTP server on a background thread.
         * <p>The default implementation returns a {@link JSONObject} or {@link JSONArray} object.</p>
         * @param conn The {@link HttpURLConnection} whose connecting the remote HTTP server.
         * @param params The parameters of this task, passed earlier by {@link DownloadRequest#execute}.
         * @return A result, defined by the subclass of this task.
         * @throws Exception if an error occurs while downloading the resource.
         */
        protected Result onDownload(HttpURLConnection conn, Params[] params) throws Exception {
            return mRequest.downloadImpl(this);
        }
    }

    /**
     * A dummy <tt>NetworkInfo</tt>.
     */
    private static final class DummyNetworkInfo {
        public static final NetworkInfo sInstance;

        static {
            try {
                final Constructor<NetworkInfo> ctor = NetworkInfo.class.getConstructor(int.class, int.class, String.class, String.class);
                ctor.setAccessible(true);
                sInstance = ctor.newInstance(ConnectivityManager.TYPE_DUMMY, ConnectivityManager.TYPE_DUMMY, "DUMMY", "");
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private NetworkUtils() {
    }
}
