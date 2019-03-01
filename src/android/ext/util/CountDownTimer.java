package android.ext.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Class CountDownTimer
 * @author Garfield
 */
public abstract class CountDownTimer implements Runnable {
    private int mCountDown;
    private final Handler mHandler;
    private final int mCountDownTime;
    private final long mIntervalMillis;

    /**
     * Constructor
     * @param countDownTime The number of times in the future from the call to {@link #start()}
     * until the countdown is done and {@link #onFinish()} is called.
     * @param intervalMillis The interval millis to receive {@link #onTick(int)} callback.
     * @see #CountDownTimer(int, long, Handler)
     */
    public CountDownTimer(int countDownTime, long intervalMillis) {
        this(countDownTime, intervalMillis, null);
    }

    /**
     * Constructor
     * @param countDownTime The number of times in the future from the call to {@link #start()} until
     * the countdown is done and {@link #onFinish()} is called.
     * @param intervalMillis The interval millis to receive {@link #onTick(int)} callback.
     * @param handler The Handler to run {@link #onTick(int)} on, or <tt>null</tt> to run on UI thread.
     * @see #CountDownTimer(int, long)
     */
    public CountDownTimer(int countDownTime, long intervalMillis, Handler handler) {
        DebugUtils.__checkError(countDownTime < 0 || intervalMillis < 0, "countDownTime < 0 || intervalMillis < 0");
        mCountDownTime  = countDownTime;
        mIntervalMillis = intervalMillis;
        mHandler = (handler != null ? handler : UIHandler.sInstance);
    }

    /**
     * Start the countdown.
     * @return This <tt>CountDownTimer</tt>.
     * @see #cancel()
     */
    public final CountDownTimer start() {
        if (mCountDown <= 0) {
            mCountDown = mCountDownTime;
            if (Looper.myLooper() == mHandler.getLooper()) {
                run();
            } else {
                mHandler.post(this);
            }
        }

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
        if (mCountDown == 0) {
            onFinish();
        } else if (mCountDown > 0) {
            onTick(mCountDown--);
            mHandler.postDelayed(this, mIntervalMillis);
        }
    }

    /**
     * Runs on the UI thread when the time is up.
     * @see #onTick(int)
     */
    protected abstract void onFinish();

    /**
     * Runs on the UI thread to fired on regular interval.
     * @param countDown The countdown until finished.
     * @see #onFinish()
     */
    protected abstract void onTick(int countDown);
}
