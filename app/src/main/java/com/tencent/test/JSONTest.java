package com.tencent.test;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.ext.annotation.CursorField;
import android.ext.database.DatabaseUtils;
import android.ext.net.AsyncDownloadTask;
import android.ext.net.DownloadRequest;
import android.ext.util.ByteArrayBuffer;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.ProcessUtils.CrashDatabase;
import android.net.Uri;
import android.util.JsonWriter;
import android.util.Log;
import android.util.LogPrinter;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;
import java.util.RandomAccess;

public final class JSONTest {
    public static void testDownload() {
        new DownloadTask().executeOnExecutor(MainApplication.sThreadPool, "http://111.170.234.234/app.znds.com/down/20190902/dptvb_1.0_dangbei.apk");
    }

    /* package */ static final class DownloadTask extends AsyncDownloadTask<String, Object, ByteArrayBuffer> {
        @Override
        protected DownloadRequest newDownloadRequest(String[] urls) throws Exception {
            return new DownloadRequest(urls[0]).readTimeout(30000).connectTimeout(30000);
        }

        @Override
        public ByteArrayBuffer onDownload(URLConnection conn, int statusCode, String[] urls) throws Exception {
            ByteArrayBuffer result = null;
            Log.i("abcd", "begin download");
            if (statusCode == HttpURLConnection.HTTP_OK) {
                result = download();
            }

            if (result != null) {
                result.dump(new LogPrinter(Log.INFO, "abcd"));
            } else {
                Log.i("abcd", "download failed!");
            }

            return result;
        }
    }

    public static class Permission implements RandomAccess {
        @CursorField("_id")
        private long mId;

        @CursorField("uid")
        private int mUid;

        @CursorField("package")
        private CharSequence mPackage;

        @CursorField("op_code")
        private int mOpCode;

        private final int a = 1;
        private static int b = 2;
        private static final int c = 3;

        @Override
        public String toString() {
            return "Permission [id=" + mId + ", uid=" + mUid + ", package=" + mPackage + ", op_code=" + mOpCode + "]";
        }
    }

    public static class PermissionEx extends Permission implements Cloneable {
        @CursorField("permission")
        private String permission;

        @Override
        public String toString() {
            return "PermissionEx [" + super.toString() + ", permission=" + permission + "]";
        }
    }

    public static Permission[] loadPermissions(Context context) {
        final Uri uri = Uri.parse("content://tv.fun.ottsecurity.ottpermission");
        Cursor cursor = null;
        JsonWriter writer = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            writer = new JsonWriter(new FileWriter("/sdcard/perms.json"));
            DatabaseUtils.writeCursor(writer, cursor, cursor.getColumnNames());
            List<Permission> p = DatabaseUtils.<Permission>parse(cursor, PermissionEx.class);
            return p.toArray(new Permission[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            FileUtils.close(cursor);
            FileUtils.close(writer);
        }
    }

    public static Object loadPackages(Context context, Class<?> clazz) {
        final Uri uri = Uri.parse("content://tv.fun.ottsecurity.ottpermission");
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[] { "package" }, null, null, null);
            return DatabaseUtils.parse(cursor, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            FileUtils.close(cursor);
        }
    }

    private static final String KEY_CHANNEL = "channelId";
    private static final String KEY_DEVICE_CODE = "deviceCode";
    private static final String KEY_REQUEST_ID = "requestId";
    private static final String KEY_CPU_TYPE = "cpuType";
    private static final String KEY_MAC = "mac";
    private static final String KEY_VERSION = "version";
    private static final String KEY_ROM_VERSION = "romVersion";

    public static void download(Activity activity) {
    }

    public static final class CrashInfo {
        @CursorField("_date")
        public long date;

        @CursorField("vcode")
        public int versionCode;

        @CursorField("vname")
        public String versionName;

        @CursorField("process")
        public String process;

//        @CursorField("thread")
//        public String thread;
//
//        @CursorField("class")
//        public String className;
//
//        @CursorField("stack")
//        public String stack;

        @Override
        public String toString() {
            return "CrashInfo [date=" + date + ", versionCode=" + versionCode + ", versionName=" + versionName + "]";
        }
    }

    public static CrashInfo[] queryCrashInfos() {
        CrashDatabase db = new CrashDatabase(MainApplication.sInstance);
        try {
            DebugUtils.startMethodTracing();
            final Cursor cursor = db.getWritableDatabase().rawQuery("SELECT * FROM crashes", null);
            List<CrashInfo> infos = DatabaseUtils.parse(cursor, CrashInfo.class);
            DebugUtils.stopMethodTracing("yf", "queryCrashInfos");
            return infos.toArray(new CrashInfo[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            db.close();
        }
    }

    private JSONTest() {
    }
}
