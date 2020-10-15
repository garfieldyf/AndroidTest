package com.tencent.temp;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.ext.util.DebugUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

public class NetworkStateLiveData extends LiveData<State> {
    private static NetworkStateLiveData sInstance;

    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    public static NetworkStateLiveData getInstance(Context context) {
        DebugUtils.__checkUIThread("getInstance");
        if (sInstance == null) {
            sInstance = new NetworkStateLiveData(context);
        }

        return sInstance;
    }

    private NetworkStateLiveData(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    protected void onActive() {
        mContext.registerReceiver(mReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    }

    @Override
    protected void onInactive() {
        mContext.unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final NetworkInfo info = ((ConnectivityManager)mContext.getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            setValue(info != null ? info.getState() : State.UNKNOWN);
        }
    };
}
