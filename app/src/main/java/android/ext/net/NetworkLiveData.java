package android.ext.net;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.ext.util.DebugUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Class <tt>NetworkLiveData</tt> used to listen the network state change.
 * <h3>Usage</h3>
 * <p>Here is an example:</p><pre>
 * NetworkLiveData.getInstance(context).observe(lifecycle, new Observer&lt;NetworkInfo&gt;() {
 *    {@code @Override}
 *     public void onChanged(NetworkInfo info) {
 *         Log.d(TAG, "NetworkInfo = " + info);
 *     }
 * });</pre>
 * @author Garfield
 */
public final class NetworkLiveData extends LiveData<NetworkInfo> {
    private static NetworkLiveData sInstance;
    private final Context mContext;

    /**
     * Returns a singleton {@link NetworkLiveData} associated with this class.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     */
    public static NetworkLiveData getInstance(Context context) {
        DebugUtils.__checkUIThread("getInstance");
        if (sInstance == null) {
            sInstance = new NetworkLiveData(context);
        }

        return sInstance;
    }

    /**
     * This class cannot be instantiated.
     */
    private NetworkLiveData(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActive() {
        DebugUtils.__checkDebug(true, "NetworkLiveData", "onActive - registerReceiver");
        mContext.registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onInactive() {
        DebugUtils.__checkDebug(true, "NetworkLiveData", "onInactive - unregisterReceiver");
        mContext.unregisterReceiver(mReceiver);
    }

    /**
     * A <tt>BroadcastReceiver</tt> used to listen the network state change.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setValue(((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo());
        }
    };
}
