package android.ext.temp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
 * @version 1.0
 */
public class SyncMessenger implements ServiceConnection {
    private Messenger mService;

    /**
     * Connects the service.
     * @param context The <tt>Context</tt>.
     * @param service The service <tt>Intent</tt> to connect to.
     * @return <tt>true</tt> if you have successfully bound to the service,
     * <tt>false</tt> if the connection is not made so you will not receive
     * the service object.
     * @see #connect(Context, String, String)
     * @see #disconnect(Context)
     * @see #isConnected()
     */
    public boolean connect(Context context, Intent service) {
        return (mService != null || context.getApplicationContext().bindService(service, this, Context.BIND_AUTO_CREATE));
    }

    /**
     * Connects the service.
     * @param context The <tt>Context</tt>.
     * @param packageName The service package name.
     * @param className The service fully qualified class name.
     * @return <tt>true</tt> if you have successfully bound to the service,
     * <tt>false</tt> if the connection is not made so you will not receive
     * the service object.
     * @see #connect(Context, Intent)
     * @see #disconnect(Context)
     * @see #isConnected()
     */
    public final boolean connect(Context context, String packageName, String className) {
        final Intent service = new Intent();
        service.setClassName(packageName, className);
        return connect(context, service);
    }

    /**
     * Connects the service synchronously. <p><em>This method will block the
     * calling thread until the service was connected.</em></p>
     * @param context The <tt>Context</tt>.
     * @param service The service <tt>Intent</tt> to connect to.
     * @return <tt>true</tt> if you have successfully bound to the service,
     * <tt>false</tt> if the connection is not made so you will not receive
     * the service object.
     * @see #connectSync(Context, String, String)
     * @see #disconnect(Context)
     * @see #isConnected()
     */
    public boolean connectSync(Context context, Intent service) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("This method can NOT be called from the main application thread");
        }

        synchronized (this) {
            final boolean successful = connect(context, service);
            while (successful && mService == null) {
                try {
                    // If the service has been started, wait
                    // until the Messenger has been created.
                    wait();
                } catch (InterruptedException e) {
                }
            }

            return successful;
        }
    }

    /**
     * Connects the service synchronously. <p><em>This method will block the
     * calling thread until the service was connected.</em></p>
     * @param context The <tt>Context</tt>.
     * @param packageName The service package name.
     * @param className The service fully qualified class name.
     * @return <tt>true</tt> if you have successfully bound to the service,
     * <tt>false</tt> if the connection is not made so you will not receive
     * the service object.
     * @see #connectSync(Context, Intent)
     * @see #disconnect(Context)
     * @see #isConnected()
     */
    public final boolean connectSync(Context context, String packageName, String className) {
        final Intent service = new Intent();
        service.setClassName(packageName, className);
        return connectSync(context, service);
    }

    /**
     * Returns whether this object connected the service.
     * @return <tt>true</tt> if this object connected the
     * service, <tt>false</tt> otherwise.
     * @see #connect(Context, Intent)
     * @see #connectSync(Context, Intent)
     * @see #disconnect(Context)
     */
    public boolean isConnected() {
        return (mService != null);
    }

    /**
     * Disconnects the service.
     * @param context The <tt>Context</tt>.
     * @see #connect(Context, Intent)
     * @see #connectSync(Context, Intent)
     * @see #isConnected()
     */
    public void disconnect(Context context) {
        if (mService != null) {
            context.getApplicationContext().unbindService(this);
            mService = null;
        }
    }

    /**
     * Sends a <tt>Message</tt> to the service.
     * @param msg The <tt>Message</tt> to send.
     * @return <tt>true</tt> if the message was successfully
     * send to the service, <tt>false</tt> otherwise.
     */
    public boolean sendMessage(Message msg) {
        if (mService != null) {
            try {
                mService.send(msg);
                return true;
            } catch (RemoteException e) {
                Log.e(SyncMessenger.class.getName(), "Couldn't send message - " + msg.toString(), e);
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
        return (mService != null ? SyncHandler.POOL.obtain().sendMessage(mService, msg, timeout) : null);
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

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (this) {
            mService = new Messenger(service);
            notifyAll();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

    /**
     * Nested class SyncHandler
     */
    private static final class SyncHandler extends Handler {
        private static final Looper sLooper;
        private Message mResult;
        private final Messenger mReplier;

        public SyncHandler() {
            super(sLooper);
            mReplier = new Messenger(this);
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
                Log.e(SyncMessenger.class.getName(), "Couldn't send message synchronously - " + msg.toString(), e);
            } finally {
                result  = mResult;
                mResult = null;
                POOL.recycle(this);
            }

            return result;
        }

        @Override
        public void handleMessage(Message msg) {
            mResult = Message.obtain(msg);
            synchronized (mReplier) {
                mReplier.notify();
            }
        }

        /* package */ static final Pool<SyncHandler> POOL = Pools.synchronizedPool(Pools.newPool(new Factory<SyncHandler>() {
            @Override
            public SyncHandler newInstance() {
                return new SyncHandler();
            }
        }, 5));

        static {
            final HandlerThread thread = new HandlerThread("SyncMessenger");
            thread.start();
            sLooper = thread.getLooper();
        }
    }
}
