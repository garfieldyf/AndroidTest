package android.ext.temp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Class SyncMessenger
 * @author Garfield
 */
public class SyncMessenger implements ServiceConnection {
    private Messenger mService;
    private final Intent mIntent;
    private final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param service The service <tt>Intent</tt>.
     * @see #SyncMessenger(Context, String, String)
     */
    public SyncMessenger(Context context, Intent service) {
        mIntent  = service;
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param packageName The service package name.
     * @param className The service fully qualified class name.
     * @see #SyncMessenger(Context, Intent)
     */
    public SyncMessenger(Context context, String packageName, String className) {
        this(context, makeService(packageName, className));
    }

    /**
     * Sends a <tt>Message</tt> to the service.
     * @param msg The <tt>Message</tt> to send.
     * @return <tt>true</tt> if the message was successfully
     * send to the service, <tt>false</tt> otherwise.
     */
    public boolean sendMessage(Message msg) {
        if (bindService()) {
            try {
                mService.send(msg);
                return true;
            } catch (RemoteException e) {
                Log.e(getClass().getName(), new StringBuilder("Couldn't send message - ").append(msg).toString(), e);
            }
        }

        return false;
    }

    /**
     * Sends a <tt>Message</tt> containing only the <em>what</em> value
     * to the service.
     * @param what The user-defined message code.
     * @return <tt>true</tt> if the message was successfully send to the
     * service, <tt>false</tt> otherwise.
     */
    public final boolean sendMessage(int what) {
        return sendMessage(Message.obtain(null, what));
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, obj</em>
     * values to the service.
     * @param what The user-defined message code.
     * @param obj The <em>obj</em> value.
     * @return <tt>true</tt> if the message was successfully send
     * to the service, <tt>false</tt> otherwise.
     */
    public final boolean sendMessage(int what, Object obj) {
        return sendMessage(Message.obtain(null, what, obj));
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, arg1, arg2</em>
     * values to the service.
     * @param what The user-defined message code.
     * @param arg1 The <em>arg1</em> value.
     * @param arg2 The <em>arg2</em> value.
     * @return <tt>true</tt> if the message was successfully send to the
     * service, <tt>false</tt> otherwise.
     */
    public final boolean sendMessage(int what, int arg1, int arg2) {
        return sendMessage(Message.obtain(null, what, arg1, arg2));
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, arg1, arg2, obj</em>
     * values to the service.
     * @param what The user-defined message code.
     * @param arg1 The <em>arg1</em> value.
     * @param arg2 The <em>arg2</em> value.
     * @param obj The <em>obj</em> value.
     * @return <tt>true</tt> if the message was successfully send to the service,
     * <tt>false</tt> otherwise.
     */
    public final boolean sendMessage(int what, int arg1, int arg2, Object obj) {
        return sendMessage(Message.obtain(null, what, arg1, arg2, obj));
    }

    /**
     * Sends a <tt>Message</tt> to the service synchronously. <p><em>This method will
     * block the calling thread until the message was handled or timeout.</em></p>
     * @param msg The <tt>Message</tt> to send.
     * @param timeout The maximum time to wait in milliseconds.
     * @return The reply message if succeeded, or <tt>null</tt> if an error or timeout.
     */
    public Message sendMessageSync(Message msg, long timeout) {
        return (bindService() ? SyncHandler.POOL.obtain().sendMessage(mService, msg, timeout) : null);
    }

    /**
     * Sends a <tt>Message</tt> containing only the <em>what</em> value to the service
     * synchronously. <p><em>This method will block the calling thread until the message
     * was handled or timeout.</em></p>
     * @param what The user-defined message code.
     * @param timeout The maximum time to wait in milliseconds.
     * @return The reply message if succeeded, or <tt>null</tt> if an error or timeout.
     */
    public final Message sendMessageSync(int what, long timeout) {
        return sendMessageSync(Message.obtain(null, what), timeout);
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, obj</em> values to the service
     * synchronously. <p><em>This method will block the calling thread until the message
     * was handled or timeout.</em></p>
     * @param what The user-defined message code.
     * @param obj The <em>obj</em> value.
     * @param timeout The maximum time to wait in milliseconds.
     * @return The reply message if succeeded, or <tt>null</tt> if an error or timeout.
     */
    public final Message sendMessageSync(int what, Object obj, long timeout) {
        return sendMessageSync(Message.obtain(null, what, obj), timeout);
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, arg1, arg2</em> values to the
     * service synchronously. <p><em>This method will block the calling thread until
     * the message was handled or timeout.</em></p>
     * @param what The user-defined message code.
     * @param arg1 The <em>arg1</em> value.
     * @param arg2 The <em>arg2</em> value.
     * @param timeout The maximum time to wait in milliseconds.
     * @return The reply message if succeeded, or <tt>null</tt> if an error or timeout.
     */
    public final Message sendMessageSync(int what, int arg1, int arg2, long timeout) {
        return sendMessageSync(Message.obtain(null, what, arg1, arg2), timeout);
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, arg1, arg2, obj</em> values to
     * the service synchronously. <p><em>This method will block the calling thread until
     * the message was handled or timeout.</em></p>
     * @param what The user-defined message code.
     * @param arg1 The <em>arg1</em> value.
     * @param arg2 The <em>arg2</em> value.
     * @param obj The <em>obj</em> value.
     * @param timeout The maximum time to wait in milliseconds.
     * @return The reply message if succeeded, or <tt>null</tt> if an error or timeout.
     */
    public final Message sendMessageSync(int what, int arg1, int arg2, Object obj, long timeout) {
        return sendMessageSync(Message.obtain(null, what, arg1, arg2, obj), timeout);
    }

    /**
     * Disconnects the service.
     */
    public synchronized void disconnect() {
        if (mService != null) {
            mContext.unbindService(this);
            mService = null;
        }
    }

    @Override
    public synchronized void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    @Override
    public synchronized void onServiceConnected(ComponentName name, IBinder service) {
        mService = new Messenger(service);
        notifyAll();
    }

    private synchronized boolean bindService() {
        DebugUtils.__checkError(Looper.myLooper() == Looper.getMainLooper(), "This method can NOT be called from the UI thread");
        if (mService != null) {
            return true;
        }

        final boolean successful = mContext.bindService(mIntent, this, Context.BIND_AUTO_CREATE);
        while (successful && mService == null) {
            try {
                // If the service has been started, wait
                // until the Messenger has been created.
                wait();
            } catch (InterruptedException e) {
                // Ignored.
            }
        }

        return successful;
    }

    private static Intent makeService(String packageName, String className) {
        final Intent service = new Intent();
        service.setClassName(packageName, className);
        return service;
    }

    /**
     * Nested class <tt>SyncHandler</tt> is an implementation of a {@link Handler}.
     */
    private static final class SyncHandler extends Handler {
        private static final Looper sLooper;
        private volatile Message mResult;
        private final Messenger mReplier;

        public SyncHandler() {
            super(sLooper);
            mReplier = new Messenger(this);
        }

        @Override
        public void handleMessage(Message msg) {
            mResult = Message.obtain(msg);
            synchronized (mReplier) {
                mReplier.notify();
            }
        }

        public final Message sendMessage(Messenger service, Message msg, long timeout) {
            final Message result;
            try {
                msg.replyTo = mReplier;
                synchronized (mReplier) {
                    service.send(msg);
                    mReplier.wait(timeout);
                }
            } catch (Throwable e) {
                Log.e(SyncMessenger.class.getName(), new StringBuilder("Couldn't send message synchronously - ").append(msg).toString(), e);
            } finally {
                result  = mResult;
                mResult = null;
                POOL.recycle(this);
            }

            return result;
        }

        public static final Pool<SyncHandler> POOL = Pools.synchronizedPool(Pools.newPool(new Factory<SyncHandler>() {
            @Override
            public SyncHandler newInstance() {
                return new SyncHandler();
            }
        }, 2));

        static {
            final HandlerThread thread = new HandlerThread("SyncMessenger");
            thread.start();
            sLooper = thread.getLooper();
        }
    }
}
