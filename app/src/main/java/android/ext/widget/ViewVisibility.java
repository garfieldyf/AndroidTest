package android.ext.widget;

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
    public final void show(long delayMillis) {
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
     * Hide the view.
     * @param remove Whether to remove the view from it's parent.
     * If <tt>true</tt> the view can no longer visible.
     */
    public final void hide(boolean remove) {
        final ViewGroup parent = (ViewGroup)mView.getParent();
        if (parent == null) {
            DebugUtils.__checkDebug(parent == null, "ViewVisibility", DeviceUtils.toString(mView, new StringBuilder("HIDE - The ")).append(" was removed from the parent").toString());
            return;
        }

        DebugUtils.__checkDebug(true, "ViewVisibility", DeviceUtils.toString(mView, new StringBuilder("HIDE - view = ")).toString());
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

    @Override
    public void run() {
        if (mVisible) {
            show();
        }
    }

    private void show() {
        if (mView instanceof ViewStub) {
            final ViewStub stub = (ViewStub)mView;
            DebugUtils.__checkDebug(true, "ViewVisibility", "Inflates the layout resource - ID #0x" + Integer.toHexString(stub.getLayoutResource()));
            mView = stub.inflate();
        }

        mView.setVisibility(View.VISIBLE);
    }
}
