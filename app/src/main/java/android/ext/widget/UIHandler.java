package android.ext.widget;

import android.ext.content.Loader.Task;
import android.ext.database.DatabaseHandler.AbstractSQLiteTask;
import android.ext.widget.BaseAdapter.NotificationRunnable;
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
     * Runs the specified <em>action</em> on the UI thread. If the
     * current thread is the UI thread, then the action is executed
     * immediately. If the current thread is not the UI thread, the
     * action is posted to the event queue of the UI thread.
     * @param action The action to run on the UI thread.
     */
    public static void runOnUIThread(Runnable action) {
        sInstance.execute(action);
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
        switch (msg.what) {
        // Dispatch the Task messages.
        case MESSAGE_PROGRESS:
            ((Task)msg.getCallback()).onProgress(msg.obj);
            break;

        case MESSAGE_FINISHED:
            ((Task)msg.getCallback()).onPostExecute(msg.obj);
            break;

        // Dispatch the BaseAdapter messages.
        case MESSAGE_ADAPTER:
            ((NotificationRunnable)msg.getCallback()).handleMessage(msg);
            break;

        // Dispatch the DatabaseHandler messages.
        case MESSAGE_DATABASE_HANDLER:
            ((AbstractSQLiteTask)msg.getCallback()).handleMessage(msg.arg1, msg.arg2, msg.obj);
            break;

        default:
            super.dispatchMessage(msg);
        }
    }

    /**
     * Called on the {@link Task} internal, do not call this method directly.
     * @hide
     */
    public final void finish(Task task, Object result) {
        final Message msg = Message.obtain(this, task);
        msg.what = MESSAGE_FINISHED;
        msg.obj  = result;
        sendMessage(msg);
    }

    /**
     * Called on the {@link Task} internal, do not call this method directly.
     * @hide
     */
    public final void setProgress(Task task, Object value) {
        final Message msg = Message.obtain(this, task);
        msg.what = MESSAGE_PROGRESS;
        msg.obj  = value;
        sendMessage(msg);
    }

    /**
     * Called on the {@link BaseAdapter} internal, do not call this method directly.
     * @hide
     */
    public final void sendMessage(Message msg, int message, int positionStart) {
        msg.what = MESSAGE_ADAPTER;
        msg.arg1 = message;
        msg.arg2 = positionStart;
        sendMessage(msg);
    }

    /**
     * Called on the {@link DatabaseHandler} internal, do not call this method directly.
     * @hide
     */
    public final void sendMessage(Runnable callback, int message, int token, Object result) {
        final Message msg = Message.obtain(this, callback);
        msg.what = MESSAGE_DATABASE_HANDLER;
        msg.arg1 = message;
        msg.arg2 = token;
        msg.obj  = result;
        sendMessage(msg);
    }

    // The Task messages.
    private static final int MESSAGE_PROGRESS = 0xDEDEDEDE;
    private static final int MESSAGE_FINISHED = 0xDFDFDFDF;

    // The BaseAdapter messages.
    private static final int MESSAGE_ADAPTER = 0xEFEFEFEF;

    // The DatabaseHandler messages.
    private static final int MESSAGE_DATABASE_HANDLER = 0xFEFEFEFE;

    /**
     * This class cannot be instantiated.
     */
    private UIHandler() {
        super(Looper.getMainLooper());
    }
}
