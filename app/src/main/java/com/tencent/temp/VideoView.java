package com.tencent.temp;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Map;

public class VideoView extends SurfaceView implements SurfaceHolder.Callback, OnErrorListener, OnPreparedListener, OnSeekCompleteListener, OnVideoSizeChangedListener, OnCompletionListener, Handler.Callback {
    private static final String TAG = "VideoView";

    /**
     * The video mode constants, See {@link #setVideoMode(int)}
     */
    public static final int VIDEO_MODE_AUTO = 0;
    public static final int VIDEO_MODE_16_9 = 1;
    public static final int VIDEO_MODE_4_3  = 2;

    // The media player state.
    private static final int STATE_ERROR     = -1;
    private static final int STATE_IDLE      = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED  = 2;
    private static final int STATE_PLAYING   = 3;
    private static final int STATE_PAUSED    = 4;
    private static final int STATE_COMPLETED = 5;

    // The media player messages.
    private static final int MESSAGE_OPEN_VIDEO  = 0;
    private static final int MESSAGE_STOP_VIDEO  = 1;
    private static final int MESSAGE_SEEK_VIDEO  = 2;
    private static final int MESSAGE_PLAY_VIDEO  = 3;
    private static final int MESSAGE_PAUSE_VIDEO = 4;

    // The callback messages.
    private static final int MESSAGE_VIDEO_ERROR     = 1;
    private static final int MESSAGE_SIZE_CHANGED    = 2;
    private static final int MESSAGE_SEEK_COMPLETED  = 3;
    private static final int MESSAGE_VIDEO_PREPARED  = 4;
    private static final int MESSAGE_VIDEO_COMPLETED = 5;

    private int mVideoMode;
    private int mVideoWidth;
    private int mVideoHeight;

    private Uri mUri;
    private static final Looper sLooper;
    private Map<String, String> mHeaders;

    private OnVideoListener mListener;
    private final Handler mUIHandler;
    private final Handler mVideoHandler;

    private volatile int mCurrentState;
    private volatile int mSeekToPosition;
    private volatile MediaPlayer mMediaPlayer;

    public VideoView(Context context) {
        this(context, null, 0);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setKeepScreenOn(true);
        getHolder().addCallback(this);
        mVideoHandler = new Handler(sLooper, this);
        mUIHandler = new Handler(Looper.getMainLooper(), this::handleUIMessage);
    }

    public final void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekToPosition = -1;
        mVideoHandler.sendEmptyMessage(MESSAGE_OPEN_VIDEO);
    }

    public void start() {
        if (isPlaybackState()) {
            mVideoHandler.sendEmptyMessage(MESSAGE_PLAY_VIDEO);
        }
    }

    public void pause() {
        if (isPlaybackState()) {
            mVideoHandler.sendEmptyMessage(MESSAGE_PAUSE_VIDEO);
        }
    }

    public void stop() {
        mVideoHandler.sendEmptyMessage(MESSAGE_STOP_VIDEO);
    }

    public void seekTo(int msec) {
        if (msec != -1 && isPlaybackState()) {
            mVideoHandler.sendMessage(Message.obtain(mVideoHandler, MESSAGE_SEEK_VIDEO, msec, 0));
            mSeekToPosition = -1;
        } else {
            mSeekToPosition = msec;
        }
    }

    public void setVideoMode(int mode) {
        if (mVideoMode != mode) {
            mVideoMode = mode;
            if (isPlaybackState()) {
                requestLayout();
            }
        }
    }

    public void setLooping(boolean looping) {
        if (isPlaybackState()) {
            mMediaPlayer.setLooping(looping);
        }
    }

    public boolean isPlaying() {
        return (isPlaybackState() && mMediaPlayer.isPlaying());
    }

    public int getDuration() {
        return (isPlaybackState() ? mMediaPlayer.getDuration() : -1);
    }

    public int getCurrentPosition() {
        return (isPlaybackState() ? mMediaPlayer.getCurrentPosition() : 0);
    }

    public void setOnVideoListener(OnVideoListener listener) {
        mListener = listener;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_OPEN_VIDEO:
            openVideo();
            break;

        case MESSAGE_STOP_VIDEO:
            stopVideo();
            break;

        case MESSAGE_SEEK_VIDEO:
            mMediaPlayer.seekTo(msg.arg1);
            break;

        case MESSAGE_PLAY_VIDEO:
            mCurrentState = STATE_PLAYING;
            mMediaPlayer.start();
            break;

        case MESSAGE_PAUSE_VIDEO:
            if (mMediaPlayer.isPlaying()) {
                mCurrentState = STATE_PAUSED;
                mMediaPlayer.pause();
            }
            break;
        }

        return true;
    }

    public boolean handleUIMessage(Message msg) {
        if (mMediaPlayer == null) {
            return true;
        }

        switch (msg.what) {
        case MESSAGE_VIDEO_ERROR:
            if (mListener != null) {
                mListener.onError(mMediaPlayer, msg.arg1, msg.arg2);
            }
            break;

        case MESSAGE_SIZE_CHANGED:
            onVideoSizeChanged(msg.arg1, msg.arg2);
            break;

        case MESSAGE_SEEK_COMPLETED:
            if (mListener != null) {
                mListener.onSeekComplete(mMediaPlayer);
            }
            break;

        case MESSAGE_VIDEO_PREPARED:
            requestLayout();
            if (mListener != null) {
                mListener.onPrepared(mMediaPlayer);
            }
            break;

        case MESSAGE_VIDEO_COMPLETED:
            if (mListener != null) {
                mListener.onCompletion(mMediaPlayer);
            }
            break;
        }

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mCurrentState = STATE_PREPARED;
        mMediaPlayer.start();
        mUIHandler.sendEmptyMessage(MESSAGE_VIDEO_PREPARED);

        if (mSeekToPosition != -1) {
            mMediaPlayer.seekTo(mSeekToPosition);
            mSeekToPosition = -1;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentState = STATE_COMPLETED;
        mUIHandler.sendEmptyMessage(MESSAGE_VIDEO_COMPLETED);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        mUIHandler.sendEmptyMessage(MESSAGE_SEEK_COMPLETED);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mCurrentState = STATE_ERROR;
        mUIHandler.sendMessage(Message.obtain(mUIHandler, MESSAGE_VIDEO_ERROR, what, extra));
        return true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mUIHandler.sendMessage(Message.obtain(mUIHandler, MESSAGE_SIZE_CHANGED, width, height));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mVideoHandler.sendEmptyMessage(MESSAGE_STOP_VIDEO);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measureWidth  = getDefaultSize(mVideoWidth, widthMeasureSpec);
        final int measureHeight = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            measureVideo(measureWidth, measureHeight);
        } else {
            setMeasuredDimension(measureWidth, measureHeight);
        }
    }

    private void openVideo() {
        if (mUri == null) {
            return;
        }

        final Context context = getContext();
        final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        stopVideo();
        Log.i(TAG, "openVideo");
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);

            mMediaPlayer.setDisplay(getHolder());
            mMediaPlayer.setDataSource(context, mUri, mHeaders);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mCurrentState = STATE_PREPARING;
            mMediaPlayer.prepareAsync();
        } catch (IOException | IllegalArgumentException e) {
            mCurrentState = STATE_ERROR;
            onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    private void stopVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer  = null;
            mCurrentState = STATE_IDLE;
            Log.i(TAG, "stopVideo");
        }
    }

    private void measureVideo(int measureWidth, int measureHeight) {
        final int width, height;
        switch (mVideoMode) {
        case VIDEO_MODE_4_3:
            width  = (int)(measureHeight * 4.0f / 3.0f);
            height = measureHeight;
            break;

        case VIDEO_MODE_16_9:
            width  = measureWidth;
            height = measureHeight;
            break;

        default:
            final int computeWidth = (int)((float)mVideoWidth * measureHeight / mVideoHeight);
            if (computeWidth <= measureWidth) {
                width  = computeWidth;
                height = measureHeight;
            } else {
                width  = measureWidth;
                height = (int)((float)mVideoHeight * measureWidth / mVideoWidth);
            }
            break;
        }

        setMeasuredDimension(width, height);
    }

    private void onVideoSizeChanged(int width, int height) {
        mVideoWidth  = width;
        mVideoHeight = height;
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();
        }
    }

    private boolean isPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    static {
        final HandlerThread thread = new HandlerThread("VideoThread");
        thread.start();
        sLooper = thread.getLooper();
    }

    public static interface OnVideoListener extends OnPreparedListener, OnErrorListener, OnCompletionListener, OnSeekCompleteListener {
    }
}
