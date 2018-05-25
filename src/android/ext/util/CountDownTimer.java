package android.ext.util;

import android.os.Handler;
import android.os.SystemClock;

/**
 * Class CountDownTimer
 * @author Garfield
 * @version 1.0
 */
public abstract class CountDownTimer implements Runnable {
    private long mCountDownTime;
    private final Handler mHandler;
    private final long mIntervalMillis;
    private final long mCountDownMillis;

    /**
     * Constructor
     * @param countDownMillis The number of millis in the future from the call
     * to {@link #start()} until the countdown is done and {@link #onFinish()} is called.
     * @param intervalMillis The interval millis to receive {@link #onTick(long)} callback.
     * @see #CountDownTimer(long, long, Handler)
     */
    public CountDownTimer(long countDownMillis, long intervalMillis) {
        this(countDownMillis, intervalMillis, null);
    }

    /**
     * Constructor
     * @param countDownMillis The number of millis in the future from the call
     * to {@link #start()} until the countdown is done and {@link #onFinish()} is called.
     * @param intervalMillis The interval millis to receive {@link #onTick(long)} callback.
     * @param handler The Handler to run {@link #onTick(long)} on, or <tt>null</tt> to run
     * on UI thread.
     * @see #CountDownTimer(long, long)
     */
    public CountDownTimer(long countDownMillis, long intervalMillis, Handler handler) {
        mIntervalMillis  = intervalMillis;
        mCountDownMillis = countDownMillis;
        mHandler = (handler == null ? UIHandler.sInstance : handler);
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

        mHandler.post(this);
        return this;
    }

    /**
     * Cancel this countdown.
     * @see #start()
     */
    public final void cancel() {
        mHandler.removeCallbacks(this);
    }

    @Override
    public void run() {
        final long remainingMillis = mCountDownTime - SystemClock.elapsedRealtime();
        if (remainingMillis <= 0) {
            onFinish();
        } else if (mIntervalMillis <= 0 || remainingMillis < mIntervalMillis) {
            // No tick, just delay until done.
            mHandler.postDelayed(this, remainingMillis);
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

            mHandler.postDelayed(this, delayMillis);
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
