package com.tencent.test;

import android.view.View;
import android.view.ViewGroup;

public final class ViewVisibility implements Runnable {
    private final View mView;

    public ViewVisibility(View view) {
        mView = view;
    }

    public final void setVisible(boolean visible, long delayMillis) {
        final ViewGroup parent = (ViewGroup)mView.getParent();
        if (parent != null) {
            if (visible) {
                setVisibleDelayed(delayMillis);
            } else {
                parent.removeView(mView);
            }
        }
    }

    @Override
    public void run() {
        if (mView.getParent() != null) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    private void setVisibleDelayed(long delayMillis) {
        if (delayMillis > 0) {
            mView.postDelayed(this, delayMillis);
        } else {
            mView.setVisibility(View.VISIBLE);
        }
    }
}
