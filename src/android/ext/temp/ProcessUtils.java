package android.ext.temp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Pattern;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.ext.database.DatabaseUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.ArrayUtils.Filter;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
import android.ext.util.JsonUtils;
import android.ext.util.PackageUtils;
import android.os.Build;
import android.os.Debug.MemoryInfo;
import android.os.Process;
import android.text.format.DateFormat;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Printer;

/**
 * Class ProcessUtils
 * @author Garfield
 */
public final class ProcessUtils {
    /**
     * This flag use with {@link #getRunningAppProcesses(Context, Filter, int)}.
     * If set the <tt>getRunningAppProcesses</tt> will retrieve the native processes.
     */
    public static final int GET_NATIVE_PROCESSES = 0x01;

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
     * Returns the current process name.
     * @param context The <tt>Context</tt>.
     * @return The current process name.
     */
    public static String myProcessName(Context context) {
        return getProcessName(context, Process.myPid());
    }

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
     * Returns the process name with the specified <em>pid</em>.
     * @param context The <tt>Context</tt>.
     * @param pid The id of the process.
     * @return The process name of the <em>pid</em> or <tt>null</tt>.
     */
    public static String getProcessName(Context context, int pid) {
        final List<RunningAppProcessInfo> infos = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        for (int i = 0, size = ArrayUtils.getSize(infos); i < size; ++i) {
            final RunningAppProcessInfo info = infos.get(i);
            if (info.pid == pid) {
                return info.processName;
            }
        }

        return null;
    }

    /**
     * Returns A <tt>List</tt> of application processes that are running on the device.
     * @param context The <tt>Context</tt>.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the processes.
     * @param flags The flags. Pass 0 or {@link #GET_NATIVE_PROCESSES}.
     * @return A <tt>List</tt> of {@link RunningAppProcessInfo} records.
     */
    public static List<RunningAppProcessInfo> getRunningAppProcesses(Context context, Filter<RunningAppProcessInfo> filter, int flags) {
        List<RunningAppProcessInfo> results = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        if (results == null) {
            results = new ArrayList<RunningAppProcessInfo>();
        }

        if (filter != null) {
            ArrayUtils.filter(results, filter);
        }

        return ((flags & GET_NATIVE_PROCESSES) != 0 ? getRunningNativeProcesses(results, filter) : results);
    }

    /**
     * Returns information about the memory usage of the current process.
     * @param context The <tt>Context</tt>.
     * @return The {@link MemoryInfo}.
     * @see #getProcessMemoryInfo(Context, int[])
     */
    public static MemoryInfo getProcessMemoryInfo(Context context) {
        return ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getProcessMemoryInfo(new int[] { Process.myPid() })[0];
    }

    /**
     * Returns information about the memory usage of one or more processes.
     * @param context The <tt>Context</tt>.
     * @param pids An array of the ids of the processes to be retrieved.
     * @return An array of memory information.
     * @see #getProcessMemoryInfo(Context)
     */
    public static MemoryInfo[] getProcessMemoryInfo(Context context, int... pids) {
        return ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getProcessMemoryInfo(pids);
    }

    /**
     * Install the uncaught exception handler. This handler is invoked
     * in case any thread dies due to an unhandled exception.
     * @param context The <tt>Context</tt>.
     */
    public static void installUncaughtExceptionHandler(Context context) {
        new UncaughtHandler(context);
    }

    public static void dumpProcessInfos(Printer printer, List<RunningAppProcessInfo> infos) {
        final StringBuilder result = new StringBuilder(256);
        final Formatter formatter  = new Formatter(result);
        final int size = ArrayUtils.getSize(infos);

        DebugUtils.dumpSummary(printer, result, 130, " Dumping Running Process Info [ size = %d ] ", size);
        for (int i = 0; i < size; ++i) {
            final RunningAppProcessInfo info = infos.get(i);
            result.setLength(0);
            printer.println(formatter.format("  %s@%x [ pid = %d, uid = %d, name = %s, packages = %s ]\n", info.getClass().getSimpleName(), info.hashCode(), info.pid, info.uid, info.processName, Arrays.toString(info.pkgList)).toString());
        }

        formatter.close();
    }

    /**
     * Returns A <tt>List</tt> of the native processes that are running on the device.
     */
    private static List<RunningAppProcessInfo> getRunningNativeProcesses(List<RunningAppProcessInfo> results, Filter<RunningAppProcessInfo> filter) {
        java.lang.Process process = null;
        BufferedReader reader = null;

        try {
            process = Runtime.getRuntime().exec("ps");
            reader  = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final int size = results.size();
            final Pattern pattern = Pattern.compile("\\s+");
            final RunningNativeProcessInfo info = new RunningNativeProcessInfo();

            // Reads the running processes info, skip the title.
            for (String processInfo = reader.readLine(); (processInfo = reader.readLine()) != null; ) {
                info.addTo(results, size, pattern.split(processInfo), filter);
            }
        } catch (Exception e) {
            Log.e(ProcessUtils.class.getName(), "Couldn't get running processes info", e);
        } finally {
            FileUtils.close(reader);
            if (process != null) {
                process.destroy();
            }
        }

        return results;
    }

    /**
     * A class for filtering {@link RunningAppProcessInfo} objects based on their uid.
     * (uid >= {@link Process#FIRST_APPLICATION_UID && uid <= {@link Process#LAST_APPLICATION_UID})
     */
    public static final class ProcessUidFilter implements Filter<RunningAppProcessInfo> {
        /**
         * A {@link Filter} to filters the {@link RunningAppProcessInfo} objects based on their uid.
         */
        public static final Filter<RunningAppProcessInfo> sInstance = new ProcessUidFilter();

        /**
         * This class cannot be instantiated.
         */
        private ProcessUidFilter() {
        }

        @Override
        public boolean accept(RunningAppProcessInfo info) {
            return (info.uid >= Process.FIRST_APPLICATION_UID && info.uid <= Process.LAST_APPLICATION_UID);
        }
    }

    /**
     * Information you can retrieve about a native running process.
     */
    public static final class RunningNativeProcessInfo extends RunningAppProcessInfo implements Cloneable, Filter<RunningAppProcessInfo> {
        /**
         * The parent process id of this process; 0 if none
         */
        public int ppid;

        @Override
        public boolean accept(RunningAppProcessInfo info) {
            return (this.uid == info.uid && this.pid == info.pid);
        }

        @Override
        protected RunningNativeProcessInfo clone() {
            try {
                return (RunningNativeProcessInfo)super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }

        /* package */ RunningNativeProcessInfo() {
            this.importance = IMPORTANCE_BACKGROUND;
        }

        /* package */ final void addTo(List<RunningAppProcessInfo> results, int size, String[] processInfo, Filter<RunningAppProcessInfo> filter) {
            if (ArrayUtils.getSize(processInfo) == 9 && !"ps".equals(processInfo[8])) {
                // The processInfo such as:
                // USER     PID   PPID  VSIZE  RSS     WCHAN    PC         NAME
                // root      1     0     608   444    ffffffff 00000000 S /init
                this.processName = processInfo[8];
                this.uid  = Process.getUidForName(processInfo[0]);
                this.pid  = Integer.parseInt(processInfo[1]);
                this.ppid = Integer.parseInt(processInfo[2]);

                if (ArrayUtils.indexOf(results, 0, size, this) == -1 && (filter == null || filter.accept(this))) {
                    results.add(clone());
                }
            }
        }
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
         * The exception stack trace column index of the table.
         * <P>Type: TEXT</P>
         */
        public static final int STACK = 6;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public CrashDatabase(Context context) {
            super(context, "crash.db", null, 1);
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
         * {
         *   "brand": "BRAND",
         *   "model": "MODEL",
         *   "sdk": 19,
         *   "version": "4.4.4",
         *   "abis": [
         *     "armeabi-v7a",
         *     "armeabi"
         *   ],
         *   "package": "com.xxxx",
         *   "crashes": [{
         *     "_date": 1537852558991,
         *     "vcode": 1,
         *     "vname": "1.0",
         *     "process": "com.xxxx",
         *     "thread": "main",
         *     "class": "java.lang.NullPointerException",
         *     "stack": "java.lang.NullPointerException: This is test! ... ..."
         *   }, ... ]
         * }</pre>
         * @param context The <tt>Context</tt>.
         * @param writer The {@link JsonWriter} to write to.
         * @param cursor The {@link Cursor} from which to get the crash data.
         * May be returned earlier by {@link CrashDatabase#query(long)}.
         * @return The <em>writer</em>.
         * @throws IOException if an error occurs while writing to the <em>writer</em>.
         * @see #writeDeviceInfo(Context, JsonWriter)
         * @see DatabaseUtils#writeCursor(JsonWriter, Cursor)
         */
        public static JsonWriter writeTo(Context context, JsonWriter writer, Cursor cursor) throws IOException {
            return DatabaseUtils.writeCursor(writeDeviceInfo(context, writer.beginObject()).name("crashes"), cursor).endObject();
        }

        /**
         * Writes the device info (e.g. brand, mode, version, abis and package name)
         * to the <em>writer</em>.
         * @param context The <tt>Context</tt>.
         * @param writer The {@link JsonWriter} to write to.
         * @return The <em>writer</em>.
         * @throws IOException if an error occurs while writing to the <em>writer</em>.
         * @see #writeTo(Context, JsonWriter, Cursor)
         */
        public static JsonWriter writeDeviceInfo(Context context, JsonWriter writer) throws IOException {
            return JsonUtils.writeObject(writer.name("brand").value(Build.BRAND)
                .name("model").value(Build.MODEL)
                .name("sdk").value(Build.VERSION.SDK_INT)
                .name("version").value(Build.VERSION.RELEASE)
                .name("abis"), DeviceUtils.getSupportedABIs())
                .name("package").value(context.getPackageName());
        }
    }

    /**
     * Class <tt>UncaughtHandler</tt> is an implementation of an {@link UncaughtExceptionHandler}.
     */
    @SuppressWarnings("unused")
    private static final class UncaughtHandler implements UncaughtExceptionHandler {
        private final Context mContext;
        private final UncaughtExceptionHandler mDefaultHandler;

        public UncaughtHandler(Context context) {
            mContext = context;
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
         * Writes the crash infos to the "crash.crashes" table.
         */
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
         * Writes the crash infos to "/storage/emulated/0/Android/data/packagename/files/crashes.log"
         */
        private void writeUncaughtException(PackageInfo pi, String processName, Thread thread, Throwable e) throws FileNotFoundException {
            Formatter formatter = null;
            try {
                // Creates the log file.
                final PrintStream ps = new PrintStream(new FileOutputStream(FileUtils.getFilesDir(mContext, null).getPath() + "/crashes.log", true));
                formatter = new Formatter(ps);

                // Writes the uncaught exception to log file.
                ps.println("========================================================================================================================");
                final long now = System.currentTimeMillis();
                formatter.format("Model : %s %s (sdk = %d, version = %s, cpu abis = %s)\n", Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, Arrays.toString(DeviceUtils.getSupportedABIs()));
                formatter.format("Date : %s.%03d\n", DateFormat.format("yyyy-MM-dd kk:mm:ss", now).toString(), now % 1000);
                formatter.format("Package : %s\nVersionCode : %d\nVersionName : %s\n", pi.packageName, pi.versionCode, pi.versionName);
                formatter.format("Process : %s (pid = %d, uid = %d)\nThread : %s\n", processName, Process.myPid(), Process.myUid(), thread.getName());
                e.printStackTrace(ps);
                ps.println();
            } finally {
                FileUtils.close(formatter);
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private ProcessUtils() {
    }
}