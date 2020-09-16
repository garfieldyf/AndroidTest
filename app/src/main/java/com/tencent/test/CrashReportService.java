package com.tencent.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.zip.GZIPOutputStream;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.ext.json.JSONObject;
import android.ext.json.JSONUtils;
import android.ext.net.DownloadPostRequest;
import android.ext.net.DownloadPostRequest.PostCallback;
import android.ext.util.ProcessUtils.CrashDatabase;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;

public final class CrashReportService extends Service {
    private static final String EXTRA_URL = "url";
    private Executor mSerialExecutor;

    public static void startReport(Context context, String url) {
        final Intent service = new Intent(context, CrashReportService.class);
        service.putExtra(EXTRA_URL, url);
        context.startService(service);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSerialExecutor = MainApplication.sThreadPool.createSerialExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String url = intent.getStringExtra(EXTRA_URL);
        if (!TextUtils.isEmpty(url)) {
            mSerialExecutor.execute(new ReportAction(url, startId));
        }

        return START_NOT_STICKY;
    }

    private final class ReportAction implements Runnable, PostCallback {
        private final String url;
        private final int startId;

        public ReportAction(String url, int startId) {
            this.url = url;
            this.startId = startId;
        }

        @Override
        public void run() {
            final long now = System.currentTimeMillis();
            final CrashDatabase db = new CrashDatabase(CrashReportService.this);
            final Cursor cursor = db.query(now);
            try {
                final int count = cursor.getCount();
                if (count > 0) {
                    final DownloadPostRequest request = new DownloadPostRequest(url);
                    if (count >= 10) {
                        request.post(this, cursor, true).contentEncoding("gzip");
                    } else {
                        request.post(this, cursor, false);
                    }

                    final JSONObject result = request.connectTimeout(60000).readTimeout(60000).download(null);
                    if (JSONUtils.optInt(result, "retCode", 0) == 200) {
                        db.delete(now);
                    }
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), "Couldn't report crash infos", e);
            } finally {
                db.close();
                cursor.close();
            }

            stopSelfResult(startId);
        }

        @Override
        public void onPostData(URLConnection conn, Object[] params) throws IOException {
            OutputStream os = conn.getOutputStream();
            if ((boolean)params[1]) {
                os = new GZIPOutputStream(os);
            }

            try (final JsonWriter writer = new JsonWriter(new OutputStreamWriter(os))) {
                CrashDatabase.write(writer, (Cursor)params[0], getPackageName());
            }
        }
    }
}
