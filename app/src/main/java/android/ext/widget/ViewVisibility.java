package android.ext.widget;

import android.annotation.UiThread;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

/**
 * Class ViewVisibility
 * @author Garfield
 */
public final class ViewVisibility implements Runnable {
    private View mView;
    private boolean mVisible;

    /**
     * Constructor
     * @param view The {@link View} or {@link ViewStub}.
     */
    public ViewVisibility(View view) {
        mView = view;
    }

    /**
     * Show the view with the specified <em>delayMillis</em>.
     * @param delayMillis The delay in milliseconds until the view will be show.
     */
    @UiThread
    public final void show(long delayMillis) {
        DebugUtils.__checkUIThread("show");
        final ViewGroup parent = (ViewGroup)mView.getParent();
        if (parent == null) {
            DebugUtils.__checkDebug(true, "ViewVisibility", DeviceUtils.toString(mView, new StringBuilder("SHOW - The ")).append(" was removed from the parent").toString());
            return;
        }

        if (delayMillis <= 0) {
            show();
        } else {
            mVisible = true;
            mView.postDelayed(this, delayMillis);
        }
    }

    /**
     * Hide the view, but do not dismiss it.
     * @see #dismiss()
     */
    @UiThread
    public final void hide() {
        DebugUtils.__checkUIThread("hide");
        hide(false);
    }

    /**
     * Dismiss the view, removing it from the parent.
     * @see #hide()
     */
    @UiThread
    public final void dismiss() {
        DebugUtils.__checkUIThread("dismiss");
        hide(true);
    }

    @Override
    public final void run() {
        if (mVisible) {
            show();
        }
    }

    private void show() {
        if (mView instanceof ViewStub) {
            DebugUtils.__checkDebug(true, "ViewVisibility", "Inflates the layout resource - ID #0x" + Integer.toHexString(((ViewStub)mView).getLayoutResource()));
            mView = ((ViewStub)mView).inflate();
        }

        mView.setVisibility(View.VISIBLE);
    }

    private void hide(boolean remove) {
        final ViewGroup parent = (ViewGroup)mView.getParent();
        if (parent == null) {
            DebugUtils.__checkDebug(parent == null, "ViewVisibility", DeviceUtils.toString(mView, new StringBuilder("HIDE - The ")).append(" was removed from the parent").toString());
            return;
        }

        if (mVisible) {
            mVisible = false;
            mView.removeCallbacks(this);
        }

        if (remove) {
            parent.removeView(mView);
        } else {
            mView.setVisibility(View.GONE);
        }
    }
}
