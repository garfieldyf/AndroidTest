package android.ext.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.ext.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Process;
import android.text.format.DateFormat;
import android.util.JsonWriter;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Class ProcessUtils
 * @author Garfield
 */
public final class ProcessUtils {
    /**
     * Returns the current process group id.
     * @return The group id.
     */
    public static native int myGid();

    /**
     * Returns the current process user name.
     * @return The user name if the operation succeeded,
     * <tt>null</tt> otherwise.
     * @see #myGroupName()
     */
    public static native String myUserName();

    /**
     * Returns the current process group name.
     * @return The group name if the operation succeeded,
     * <tt>null</tt> otherwise.
     * @see #myUserName()
     */
    public static native String myGroupName();

    /**
     * Returns the user name assigned to a particular <em>uid</em>.
     * @param uid The user id.
     * @return The user name if the operation succeeded, <tt>null</tt> otherwise.
     * @see #getGroupName(int)
     */
    public static native String getUserName(int uid);

    /**
     * Returns the group name assigned to a particular <em>gid</em>.
     * @param gid The group id.
     * @return The group name if the operation succeeded, <tt>null</tt> otherwise.
     * @see #getUserName(int)
     */
    public static native String getGroupName(int gid);

    /**
     * Returns the current process name.
     * @param context The <tt>Context</tt>.
     * @return The current process name.
     */
    public static String myProcessName(Context context) {
        return getRunningProcessInfo(context, Process.myPid()).processName;
    }

    /**
     * Returns the {@link RunningAppProcessInfo} with the specified <em>pid</em>.
     * @param context The <tt>Context</tt>.
     * @param pid The id of the process.
     * @return The <tt>RunningAppProcessInfo</tt> of the <em>pid</em> or <tt>null</tt>.
     */
    public static RunningAppProcessInfo getRunningProcessInfo(Context context, int pid) {
        final List<RunningAppProcessInfo> infos = ((ActivityManager)context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        for (int i = 0, size = ArrayUtils.getSize(infos); i < size; ++i) {
            final RunningAppProcessInfo info = infos.get(i);
            if (info.pid == pid) {
                return info;
            }
        }

        return null;
    }

    /**
     * Equivalent to calling <tt>AsyncTask.setDefaultExecutor(exec)</tt>.
     */
    public static void setDefaultExecutor(Executor exec) {
        AsyncTask.setDefaultExecutor(exec);
    }

    /**
     * Install the uncaught exception handler. This handler is invoked
     * in case any thread dies due to an unhandled exception.
     * @param context The <tt>Context</tt>.
     */
    public static void installUncaughtExceptionHandler(Context context) {
        new CrashHandler(context);
    }

    /**
     * Class <tt>CrashDatabase</tt> store the application crash infos.
     */
    public static final class CrashDatabase extends SQLiteOpenHelper {
        /**
         * The crash date column index of the table.
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final int DATE = 0;

        /**
         * The application version code column index of the table.
         * <P>Type: INTEGER</P>
         */
        public static final int VERSION_CODE = 1;

        /**
         * The application version name column index of the table.
         * <P>Type: TEXT</P>
         */
        public static final int VERSION_NAME = 2;

        /**
         * The process name column index of the table.
         * <P>Type: TEXT</P>
         */
        public static final int PROCESS = 3;

        /**
         * The thread name column index of the table.
         * <P>Type: TEXT</P>
         */
        public static final int THREAD = 4;

        /**
         * The exception class name column index of the table.
         * <P>Type: TEXT</P>
         */
        public static final int CLASS = 5;

        /**
         * The stack trace column index of the table.
         * <P>Type: TEXT</P>
         */
        public static final int STACK_TRACE = 6;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public CrashDatabase(Context context) {
            super(context, "crash.db", null, 1);
        }

        /**
         * Returns the number of the crash infos in the table.
         * @return The number of the crash infos.
         */
        public final int getCount() {
            return DatabaseUtils.simpleQueryLong(getWritableDatabase(), "SELECT COUNT(_date) FROM crashes", (Object[])null).intValue();
        }

        /**
         * Returns the crash infos from table which the crash time before the specified <em>date</em>.
         * @param date The date to query in milliseconds.
         * @return The {@link Cursor}.
         */
        public final Cursor query(long date) {
            return getWritableDatabase().rawQuery("SELECT * FROM crashes WHERE _date < " + date, null);
        }

        /**
         * Deletes the crash infos from table which the crash time before the specified <em>date</em>.
         * @param date The date to query in milliseconds.
         * @return The number of rows to delete.
         */
        public final int delete(long date) {
            return DatabaseUtils.executeUpdateDelete(getWritableDatabase(), "DELETE FROM crashes WHERE _date < " + date, (Object[])null);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS crashes (" +
                       "_date INTEGER PRIMARY KEY," +
                       "vcode INTEGER," +
                       "vname TEXT," +
                       "process TEXT," +
                       "thread TEXT," +
                       "class TEXT," +
                       "stack TEXT)");

            db.execSQL("CREATE TRIGGER IF NOT EXISTS insert_trigger AFTER INSERT ON crashes" +
                       " WHEN (SELECT COUNT(_date) FROM crashes) > 200" +
                       " BEGIN" +
                           " DELETE FROM crashes WHERE _date = (SELECT MIN(_date) FROM crashes);" +
                       " END;");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        /**
         * Writes the specified crash infos to the <em>writer</em>.
         * <p>The crash infos such as the following:</p><pre>
         * [{
         *     "_date": 1537852558991,
         *     "vcode": 1,
         *     "vname": "1.0",
         *     "process": "com.xxxx",
         *     "thread": "main",
         *     "class": "java.lang.NullPointerException",
         *     "stack": "java.lang.NullPointerException: This is test! ... ..."
         *   }, ... ]</pre>
         * @param writer The {@link JsonWriter} to write to.
         * @param cursor The {@link Cursor} from which to get the crash data.
         * May be returned earlier by {@link CrashDatabase#query(long)}.
         * @return The <em>writer</em>.
         * @throws IOException if an error occurs while writing to the <em>writer</em>.
         * @see #writeDeviceInfo(JsonWriter, String)
         * @see DatabaseUtils#writeCursor(JsonWriter, Cursor, String[])
         */
        public static JsonWriter writeTo(JsonWriter writer, Cursor cursor) throws IOException {
            return DatabaseUtils.writeCursor(writer, cursor, cursor.getColumnNames());
        }

        /**
         * Writes the device info (e.g. mode, brand, version, abis and package name)
         * to the <em>writer</em>.<p>The device info such as the following:</p><pre>
         * "model": "MODEL",
         * "brand": "BRAND",
         * "sdk": 19,
         * "version": "4.4.4",
         * "abis": "armeabi-v7a, armeabi",
         * "package": "com.xxxx"</pre>
         * @param writer The {@link JsonWriter} to write to.
         * @param packageName The application's package name.
         * @return The <em>writer</em>.
         * @throws IOException if an error occurs while writing to the <em>writer</em>.
         * @see #writeTo(JsonWriter, Cursor)
         */
        public static JsonWriter writeDeviceInfo(JsonWriter writer, String packageName) throws IOException {
            return writer.name("model").value(Build.MODEL)
                .name("brand").value(Build.BRAND)
                .name("sdk").value(Build.VERSION.SDK_INT)
                .name("version").value(Build.VERSION.RELEASE)
                .name("abis").value(getSupportedABIs())
                .name("package").value(packageName);
        }

        private static String getSupportedABIs() {
            final String[] abis = DeviceUtils.getSupportedABIs();
            final StringBuilder result = new StringBuilder(48).append(abis[0]);
            for (int i = 1; i < abis.length; ++i) {
                result.append(", ").append(abis[i]);
            }

            return result.toString();
        }
    }

    /**
     * Class <tt>CrashHandler</tt> is an implementation of an {@link UncaughtExceptionHandler}.
     */
    private static final class CrashHandler implements UncaughtExceptionHandler {
        private final Context mContext;
        private final UncaughtExceptionHandler mDefaultHandler;

        public CrashHandler(Context context) {
            mContext = context.getApplicationContext();
            mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
        }

        @Override
        public void uncaughtException(Thread thread, Throwable e) {
            try {
                final PackageInfo pi = PackageUtils.myPackageInfo(mContext, 0);
                if (pi.versionName == null) {
                    pi.versionName = "";
                }

                String processName = myProcessName(mContext);
                if (processName == null) {
                    processName = pi.applicationInfo.processName;
                }

                this.writeUncaughtException(pi, processName, thread, e);
            } catch (Throwable ex) {
                Log.e(ProcessUtils.class.getName(), "Couldn't write crash infos", ex);
            } finally {
                // Dispatches to the default handler.
                if (mDefaultHandler != null) {
                    mDefaultHandler.uncaughtException(thread, e);
                }
            }
        }

        /**
         * Writes the crash infos to the "crash.db.crashes" table.
         */
        @SuppressWarnings("unused")
        private void storeUncaughtException(PackageInfo pi, String processName, Thread thread, Throwable e) {
            final CrashDatabase db = new CrashDatabase(mContext);
            try {
                final StringWriter stackTrace = new StringWriter(2048);
                e.printStackTrace(new PrintWriter(stackTrace));
                DatabaseUtils.executeInsert(db.getWritableDatabase(), "INSERT INTO crashes VALUES(?,?,?,?,?,?,?)", System.currentTimeMillis(), pi.versionCode, pi.versionName, processName, thread.getName(), e.getClass().getName(), stackTrace.toString());
            } finally {
                db.close();
            }
        }

        /**
         * Writes the crash infos to "/storage/emulated/0/Android/data/<em>packagename</em>/files/crashes.log"
         */
        private void writeUncaughtException(PackageInfo pi, String processName, Thread thread, Throwable e) throws FileNotFoundException {
            // Open the log file.
            final PrintStream ps = new PrintStream(new FileOutputStream(new File(mContext.getExternalFilesDir(null), "crashes.log"), true));
            try {
                // Writes the uncaught exception to log file.
                final long now = System.currentTimeMillis();
                ps.println("========================================================================================================================");
                ps.format("Model : %s (brand = %s, sdk = %d, version = %s, cpu abis = %s)\n", Build.MODEL, Build.BRAND, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, Arrays.toString(DeviceUtils.getSupportedABIs()));
                ps.format("Date : %s.%03d\n", DateFormat.format("yyyy-MM-dd kk:mm:ss", now).toString(), now % 1000);
                ps.format("Package : %s\nVersionCode : %d\nVersionName : %s\n", pi.packageName, pi.versionCode, pi.versionName);
                ps.format("Process : %s (pid = %d, uid = %d)\nThread : %s\n", processName, Process.myPid(), Process.myUid(), thread.getName());
                e.printStackTrace(ps);
                ps.println();
            } finally {
                ps.close();
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ProcessUtils() {
    }
}