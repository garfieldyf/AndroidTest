package android.ext.util;

import android.os.SystemClock;

/**
 * Class CountDownTimer
 * @author Garfield
 * @version 1.0
 */
public abstract class CountDownTimer implements Runnable {
    private boolean mCancelled;
    private long mCountDownTime;
    private final long mIntervalMillis;
    private final long mCountDownMillis;

    /**
     * Constructor
     * @param countDownMillis The number of millis in the future from the call
     * to {@link #start()} until the countdown is done and {@link #onFinish()} is called.
     * @param intervalMillis The interval millis to receive {@link #onTick(long)} callback.
     */
    public CountDownTimer(long countDownMillis, long intervalMillis) {
        mIntervalMillis  = intervalMillis;
        mCountDownMillis = countDownMillis;
    }

    /**
     * Start the countdown.
     * @return This <tt>CountDownTimer</tt>.
     * @see #cancel()
     */
    public final CountDownTimer start() {
        if (mCountDownMillis > 0) {
            mCountDownTime = SystemClock.elapsedRealtime() + mCountDownMillis;
        }

        mCancelled = false;
        UIHandler.sInstance.post(this);
        return this;
    }

    /**
     * Cancel this countdown.
     * @see #start()
     */
    public final void cancel() {
        mCancelled = true;
        UIHandler.sInstance.removeCallbacks(this);
    }

    @Override
    public void run() {
        if (mCancelled) {
            return;
        }

        final long remainingMillis = mCountDownTime - SystemClock.elapsedRealtime();
        if (remainingMillis <= 0) {
            onFinish();
        } else if (mIntervalMillis <= 0 || remainingMillis < mIntervalMillis) {
            // No tick, just delay until done.
            UIHandler.sInstance.postDelayed(this, remainingMillis);
        } else {
            // Take into account user's onTick taking time to execute
            final long lastTickTime = SystemClock.elapsedRealtime();
            onTick(remainingMillis);

            // Special case: user's onTick took more than interval
            // to complete, skip to next interval.
            long delayMillis = lastTickTime + mIntervalMillis - SystemClock.elapsedRealtime();
            while (delayMillis < 0) {
                delayMillis += mIntervalMillis;
            }

            UIHandler.sInstance.postDelayed(this, delayMillis);
        }
    }

    /**
     * Runs on the UI thread when the time is up.
     * @see #onTick(long)
     */
    protected abstract void onFinish();

    /**
     * Runs on the UI thread to fired on regular interval. <p>The
     * default implementation do nothing. If you write your own
     * implementation, do not call <tt>super.onTick()</tt>.</p>
     * @param remainingMillis The amount of time until finished.
     * @see #onFinish()
     */
    protected void onTick(long remainingMillis) {
    }
}
