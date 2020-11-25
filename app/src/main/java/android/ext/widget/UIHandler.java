package android.ext.widget;

import android.annotation.UiThread;
import android.ext.util.DebugUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.Executor;

/**
 * Class UIHandler
 * @author Garfield
 */
public final class UIHandler extends Handler implements Executor {
    /**
     * The {@link Handler} associated with the UI thread's message queue.
     */
    public static final UIHandler sInstance = new UIHandler();

    /**
     * Runs the specified <em>action</em> on the UI thread. If the current thread is the UI thread, then the action is executed
     * immediately. If the current thread is not the UI thread, the action is posted to the event queue of the UI thread.
     * @param action The action to run on the UI thread.
     */
    public static void runOnUIThread(Runnable action) {
        sInstance.execute(action);
    }

    /**
     * Equivalent to calling <tt>sendMessage(obtianMessage(callback, 0, obj))</tt>.
     * @param callback The {@link MessageRunnable} that will call {@link MessageRunnable#handleMessage(Message)} when the message is handled.
     * @param obj The value to assign to the {@link Message#obj} field.
     * @see #obtianMessage(MessageRunnable, int, Object)
     */
    public final void post(MessageRunnable callback, Object obj) {
        DebugUtils.__checkError(callback == null, "Invalid parameter - callback == null");
        final Message msg = Message.obtain(this, callback);
        msg.obj = obj;
        sendMessage(msg);
    }

    /**
     * Same as {@link Message#obtain(Handler, Runnable)}, except that it also sets the what and obj members of the returned {@link Message}.
     * @param callback The {@link MessageRunnable} that will call {@link MessageRunnable#handleMessage(Message)} when the message is handled.
     * @param what The value to assign to the returned {@link Message#what} field.
     * @param obj The value to assign to the returned {@link Message#obj} field.
     * @return A <tt>Message</tt> from the global message pool.
     * @see #post(MessageRunnable, Object)
     */
    public final Message obtianMessage(MessageRunnable callback, int what, Object obj) {
        DebugUtils.__checkError(callback == null, "Invalid parameter - callback == null");
        final Message msg = Message.obtain(this, callback);
        msg.what = what;
        msg.obj  = obj;
        return msg;
    }

    @Override
    public void execute(Runnable command) {
        if (getLooper() == Looper.myLooper()) {
            command.run();
        } else {
            post(command);
        }
    }

    @Override
    public void dispatchMessage(Message msg) {
        final Runnable callback = msg.getCallback();
        if (callback instanceof MessageRunnable) {
            ((MessageRunnable)callback).handleMessage(msg);
        } else {
            super.dispatchMessage(msg);
        }
    }

    /**
     * This class cannot be instantiated.
     */
    private UIHandler() {
        super(Looper.getMainLooper());
    }

    /**
     * The <tt>MessageRunnable</tt> class allows us to perform operations
     * on a background thread and handle message on the UI thread.
     * <h3>Usage</h3>
     * <p>Here is an example of subclassing:</p><pre>
     * public static class DownloadTask implements MessageRunnable {
     *    {@code @Override}
     *     public void run() {
     *         // Performs download on the background thread
     *         // ... ...
     *
     *         final Message msg = Message.obtain(UIHandler.sInstance, this);
     *         msg.what = what;
     *         msg.arg1 = arg1;
     *         ... ...
     *         UIHandler.sInstance.sendMessage(msg);
     *     }
     *
     *    {@code @Override}
     *     public void handleMessage(Message msg) {
     *         // Handle message on the UI thread
     *         // ... ...
     *     }
     * }
     *
     * AsyncTask.execute(new DownloadTask());</pre>
     */
    public static interface MessageRunnable extends Runnable {
        /**
         * Called on the UI thread to receive messages.
         * @param msg The <tt>Message</tt> to handle.
         */
        @UiThread
        void handleMessage(Message msg);
    }
}
