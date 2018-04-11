package android.ext.concurrent;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Dialog;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.ext.util.UIHandler;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Abstract class AsyncDialogTask
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public abstract class AsyncDialogTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Runnable, ActivityLifecycleCallbacks {
    private Dialog mDialog;
    private final long mDelayMillis;
    private volatile boolean mShowing;

    private final Application mApplication;
    private final WeakReference<Activity> mActivity;

    /**
     * Constructor
     * @param activity The {@link Activity}.
     * @see #AsyncDialogTask(Activity, long)
     */
    public AsyncDialogTask(Activity activity) {
        this(activity, 0);
    }

    /**
     * Constructor
     * @param activity The {@link Activity}.
     * @param showDelayMillis The delay in milliseconds until the dialog will be show.
     * @see #AsyncDialogTask(Activity)
     */
    public AsyncDialogTask(Activity activity, long showDelayMillis) {
        DebugUtils._checkMemoryLeaks(getClass());
        mActivity = new WeakReference<Activity>(activity);
        mDelayMillis = showDelayMillis;
        mApplication = activity.getApplication();
    }

    /**
     * Returns the application associated with this task.
     * @return The <tt>Application</tt>.
     */
    public final <T extends Application> T getApplication() {
        return (T)mApplication;
    }

    /**
     * Returns the activity associated with this task.
     * @return The <tt>Activity</tt>, or <tt>null</tt>
     * if the activity was released by the GC.
     */
    protected final <T extends Activity> T getActivity() {
        return (T)mActivity.get();
    }

    /**
     * Returns the {@link Dialog} associated with this task,
     * returned earlier by {@link #onCreateDialog(Context)}.
     * @return The <tt>Dialog</tt>, or <tt>null</tt> if this
     * task was finished or cancelled.
     */
    protected final Dialog getDialog() {
        return mDialog;
    }

    /**
     * Dismiss the dialog associated with this task.
     */
    protected final void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
            mApplication.unregisterActivityLifecycleCallbacks(this);
        }
    }

    @Override
    public void run() {
        if (mShowing) {
            final Activity activity = mActivity.get();
            if (activity != null && !activity.isDestroyed()) {
                mDialog = onCreateDialog(activity);
                mDialog.show();
                mApplication.registerActivityLifecycleCallbacks(this);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        mShowing = true;
        if (mDelayMillis <= 0) {
            run();
        } else {
            UIHandler.sInstance.postDelayed(this, mDelayMillis);
        }
    }

    @Override
    protected Result doInBackground(Params... params) {
        final Result result = doInBackground(params, mApplication);
        if (mDelayMillis > 0) {
            mShowing = false;
            UIHandler.sInstance.removeCallbacks(this);
        }

        return result;
    }

    @Override
    protected void onCancelled(Result result) {
        dismissDialog();
    }

    @Override
    protected void onPostExecute(Result result) {
        dismissDialog();
    }

    /**
     * Callback for creating {@link Dialog} to show when this task is running.
     * @param context The <tt>Context</tt> to create.
     * @return The dialog.
     */
    protected abstract Dialog onCreateDialog(Context context);

    /**
     * Performs a computation on a background thread. The specified parameters are
     * the parameters passed to {@link #execute(Params[])} by the caller of this task.
     * @param params The parameters of this task.
     * @param application The <tt>Application</tt>.
     * @return A result, defined by the subclass of this task.
     */
    protected abstract Result doInBackground(Params[] params, Application application);

    @Override
    public void onActivityStarted(Activity activity) {
        if (mDialog != null && mActivity.get() == activity) {
            mDialog.show();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (mDialog != null && mActivity.get() == activity) {
            mDialog.hide();
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (mActivity.get() == activity) {
            cancel(false);
            dismissDialog();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedState) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }
}
