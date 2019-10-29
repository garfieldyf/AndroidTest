package android.ext.temp;

import java.lang.ref.WeakReference;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.ext.util.DebugUtils;
import android.ext.util.UIHandler;
import android.os.AsyncTask;
import android.os.Bundle;

/**
 * Class <tt>AsyncDialogTask</tt> like as {@link AsyncTask}, But this task allows to show
 * a {@link Dialog} on the UI thread when it perform a computation on a background thread.
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public abstract class AsyncDialogTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements Runnable, OnCancelListener, ActivityLifecycleCallbacks {
    private Dialog mDialog;
    private final long mDelayMillis;
    private volatile boolean mShowDialog;

    public final Application mApplication;
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
        DebugUtils.__checkMemoryLeaks(getClass());
        mActivity = new WeakReference<Activity>(activity);
        mShowDialog  = true;
        mDelayMillis = showDelayMillis;
        mApplication = activity.getApplication();
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
     * returned earlier by {@link #onCreateDialog(Activity)}.
     * @return The <tt>Dialog</tt>, or <tt>null</tt> if this
     * task was finished or cancelled.
     */
    protected final Dialog getDialog() {
        return mDialog;
    }

    /**
     * Closes the owner activity associated with this task.
     */
    protected final void finishActivity() {
        final Activity activity = mActivity.get();
        if (activity != null && !activity.isDestroyed()) {
            activity.finish();
        }
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
        if (mShowDialog) {
            final Activity activity = mActivity.get();
            if (activity != null && !activity.isDestroyed()) {
                mDialog = onCreateDialog(activity);
                mDialog.setOnCancelListener(this);
                mDialog.show();
                mApplication.registerActivityLifecycleCallbacks(this);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        if (mDelayMillis <= 0) {
            run();
        } else {
            UIHandler.sInstance.postDelayed(this, mDelayMillis);
        }
    }

    @Override
    protected Result doInBackground(Params... params) {
        final Result result = doInBackground(mApplication, params);
        if (mDelayMillis > 0) {
            mShowDialog = false;
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
     * @param activity The <tt>Activity</tt> to create.
     * @return The dialog.
     */
    protected abstract Dialog onCreateDialog(Activity activity);

    /**
     * Performs a computation on a background thread. The specified parameters are
     * the parameters passed to {@link #execute(Params[])} by the caller of this task.
     * @param context The application <tt>Context</tt>.
     * @param params The parameters of this task.
     * @return A result, defined by the subclass of this task.
     */
    protected abstract Result doInBackground(Context context, Params[] params);

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(false);
        dismissDialog();
    }

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
        if (mDialog != null && mActivity.get() == activity) {
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
