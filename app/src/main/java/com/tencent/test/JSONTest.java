package com.tencent.test;

import java.io.FileWriter;
import java.util.List;
import java.util.RandomAccess;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.ext.annotation.CursorField;
import android.ext.database.DatabaseUtils;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.ProcessUtils.CrashDatabase;
import android.net.Uri;
import android.util.JsonWriter;

public final class JSONTest {
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
            List<CrashInfo> infos = DatabaseUtils.query(db.getWritableDatabase(), CrashInfo.class, "SELECT * FROM crashes", (String[])null);
            DebugUtils.stopMethodTracing("yf", "queryCrashInfos");
            return infos.toArray(new CrashInfo[0]);
        } finally {
            db.close();
        }
    }

    private JSONTest() {
    }
}
