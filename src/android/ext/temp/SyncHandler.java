package android.ext.temp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

/**
 * Class SyncHandler
 * @author Garfield
 * @version 1.0
 */
public class SyncHandler extends Handler {
    /**
     * Constructor
     */
    public SyncHandler() {
    }

    /**
     * Constructor
     * @param looper The looper, must not be <tt>null</tt>.
     */
    public SyncHandler(Looper looper) {
        super(looper);
    }

    /**
     * Constructor
     * @param The {@link Callback} interface in which to
     * handle messages, or <tt>null</tt>.
     */
    public SyncHandler(Callback callback) {
        super(callback);
    }

    /**
     * Constructor
     * @param looper The looper, must not be <tt>null</tt>.
     * @param The {@link Callback} interface in which to
     * handle messages, or <tt>null</tt>.
     */
    public SyncHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }

    /**
     * Sends a <tt>Message</tt> synchronously. <p><em>This method
     * will block the calling thread until the message was handled
     * or timeout.</em></p>
     * @param msg The <tt>Message</tt> to send.
     * @param timeout The maximum time to wait in milliseconds.
     * @return <tt>true</tt> if the message was successfully handled.
     * <tt>false</tt> on failure, usually because the looper processing
     * the message queue is exiting or timeout.
     */
    public final boolean sendMessageSync(Message msg, long timeout) {
        return sendMessageSync(msg, SystemClock.uptimeMillis(), timeout);
    }

    /**
     * Sends a <tt>Message</tt> at the front of the message queue synchronously.
     * <p><em>This method will block the calling thread until the message was
     * handled or timeout.</em></p>
     * @param msg The <tt>Message</tt>.
     * @param timeout The maximum time to wait in milliseconds.
     * @return <tt>true</tt> if the message was successfully handled. <tt>false</tt>
     * on failure, usually because the looper processing the message queue is exiting
     * or timeout.
     */
    public final boolean sendMessageAtFrontSync(Message msg, long timeout) {
        return sendMessageSync(msg, 0, timeout);
    }

    /**
     * Sends a <tt>Message</tt> containing only the <em>callback</em>
     * value synchronously. <p><em>This method will block the calling
     * thread until the message was handled or timeout.</em></p>
     * @param callback The <tt>Runnable</tt>.
     * @param timeout The maximum time to wait in milliseconds.
     * @return <tt>true</tt> if the message was successfully handled.
     * <tt>false</tt> on failure, usually because the looper processing
     * the message queue is exiting or timeout.
     */
    public final boolean sendMessageSync(Runnable callback, long timeout) {
        return sendMessageSync(Message.obtain(this, callback), SystemClock.uptimeMillis(), timeout);
    }

    /**
     * Sends a <tt>Message</tt> containing only the <em>what</em> value synchronously.
     * <p><em>This method will block the calling thread until the message was handled
     * or timeout.</em></p>
     * @param what The user-defined message code.
     * @param timeout The maximum time to wait in milliseconds.
     * @return <tt>true</tt> if the message was successfully handled. <tt>false</tt>
     * on failure, usually because the looper processing the message queue is exiting
     * or timeout.
     */
    public final boolean sendMessageSync(int what, long timeout) {
        return sendMessageSync(Message.obtain(this, what), SystemClock.uptimeMillis(), timeout);
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, arg1, arg2</em> values
     * synchronously. <p><em>This method will block the calling thread until the
     * message was handled or timeout.</em></p>
     * @param what The user-defined message code.
     * @param arg1 The <em>arg1</em> value.
     * @param arg2 The <em>arg2</em> value.
     * @param timeout The maximum time to wait in milliseconds.
     * @return <tt>true</tt> if the message was successfully handled. <tt>false</tt>
     * on failure, usually because the looper processing the message queue is exiting
     * or timeout.
     */
    public final boolean sendMessageSync(int what, int arg1, int arg2, long timeout) {
        return sendMessageSync(Message.obtain(this, what, arg1, arg2), SystemClock.uptimeMillis(), timeout);
    }

    /**
     * Sends a <tt>Message</tt> containing the <em>what, arg1, arg2, obj</em>
     * values synchronously. <p><em>This method will block the calling thread
     * until the message was handled or timeout.</em></p>
     * @param what The user-defined message code.
     * @param arg1 The <em>arg1</em> value.
     * @param arg2 The <em>arg2</em> value.
     * @param obj The <em>obj</em> value.
     * @param timeout The maximum time to wait in milliseconds.
     * @return <tt>true</tt> if the message was successfully handled. <tt>false</tt>
     * on failure, usually because the looper processing the message queue is exiting
     * or timeout.
     */
    public final boolean sendMessageSync(int what, int arg1, int arg2, Object obj, long timeout) {
        return sendMessageSync(Message.obtain(this, what, arg1, arg2, obj), SystemClock.uptimeMillis(), timeout);
    }

    @Override
    public void dispatchMessage(Message msg) {
        super.dispatchMessage(msg);
        synchronized (msg) {
            msg.notify();
        }
    }

    private boolean sendMessageSync(Message msg, long uptimeMillis, long timeout) {
        if (getLooper() == Looper.myLooper()) {
            throw new RuntimeException("This method can NOT be called from the self thread");
        }

        final boolean successful = sendMessageAtTime(msg, uptimeMillis);
        if (successful) {
            synchronized (msg) {
                try {
                    msg.wait(timeout);
                } catch (InterruptedException e) {
                    // Ignored.
                }
            }
        }

        return successful;
    }
}
