package com.tencent.test;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class CrashService extends Service implements OnDismissListener {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("title")
            .setOnDismissListener(this)
            .setMessage("Crash!!!!")
            .create();

        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //stopSelf();
        //Process.killProcess(Process.myPid());
    }
}
