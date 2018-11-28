package android.ext.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Class CountDownTimer
 * @author Garfield
 */
public abstract class CountDownTimer implements Runnable {
    private int mCurCountDown;
    private int mCountDownTime;
    private long mIntervalMillis;
    private final Handler mHandler;

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
        mHandler = (handler == null ? UIHandler.sInstance : handler);
    }

    /**
     * Start the countdown.
     * @return This <tt>CountDownTimer</tt>.
     * @see #restart(int, long)
     * @see #cancel()
     */
    public final CountDownTimer start() {
        if (mCurCountDown <= 0) {
            mCurCountDown = mCountDownTime;
            if (Looper.myLooper() == mHandler.getLooper()) {
                run();
            } else {
                mHandler.post(this);
            }
        }

        return this;
    }

    /**
     * Restart the countdown.
     * @return This <tt>CountDownTimer</tt>.
     * @see #start()
     * @see #cancel()
     */
    public final CountDownTimer restart(int countDownTime, long intervalMillis) {
        DebugUtils.__checkError(countDownTime < 0 || intervalMillis < 0, "countDownTime < 0 || intervalMillis < 0");
        mCountDownTime  = countDownTime;
        mIntervalMillis = intervalMillis;
        return start();
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
        if (mCurCountDown == 0) {
            onFinish();
        } else if (mCurCountDown > 0) {
            onTick(mCurCountDown--);
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
