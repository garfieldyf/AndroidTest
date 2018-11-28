package android.ext.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Abstract class SyncService
 * @author Garfield
 */
public abstract class SyncService extends Service {
    private Messenger mMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        mMessenger = new Messenger(createHandler());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     * Returns the {@link Messenger} associated with this service.
     * @return The <tt>Messenger</tt>.
     */
    public final Messenger getMessenger() {
        return mMessenger;
    }

    /**
     * Create the service {@link Handler}.
     * @return The service <tt>Handler</tt>.
     */
    protected abstract Handler createHandler();

    /**
     * Sets the result when the receive <tt>Message</tt> was handled.
     * @param replyTo The receive <tt>Message's</tt> <em>replyTo</em>.
     * @param result The result to send to client.
     * @see #setResult(Messenger, Message)
     */
    public static void setResult(Messenger replyTo, int result) {
        setResult(replyTo, Message.obtain(null, result));
    }

    /**
     * Sets the result when the receive <tt>Message</tt> was handled.
     * @param replyTo The receive <tt>Message's</tt> <em>replyTo</em>.
     * @param result The result to send to client.
     * @see #setResult(Messenger, int)
     */
    public static void setResult(Messenger replyTo, Message result) {
        try {
            replyTo.send(result);
        } catch (RemoteException e) {
            Log.e(SyncService.class.getName(), new StringBuilder("Couldn't set result - ").append(result).toString(), e);
        }
    }
}
