package com.tencent.temp;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.ViewGroup.LayoutParams;
import java.io.IOException;
import java.util.Map;

public class VideoView extends TextureView implements SurfaceTextureListener, OnErrorListener, OnPreparedListener, OnCompletionListener, OnVideoSizeChangedListener {
    public static final int VIDEO_MODE_AUTO = 0;
    public static final int VIDEO_MODE_16_9 = 1;
    public static final int VIDEO_MODE_4_3 = 2;

    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private Uri mUri;
    private Map<String, String> mHeaders;

    private int mVideoWidth;
    private int mVideoHeight;

    private int mVideoMode;
    private int mCurrentState;
    private int mSeekWhenPrepared;
    private MediaPlayer mMediaPlayer;
    private LayoutParams mLayoutParams;

    public VideoView(Context context) {
        super(context);
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView();
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            mLayoutParams = getLayoutParams();
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            setLayoutParams(mLayoutParams);
        }
    }

    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
    }

    public void pause() {
        if (isInPlaybackState() && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
        }
    }

    public void stop() {
        release(true);
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public int getDuration() {
        return (isInPlaybackState() ? mMediaPlayer.getDuration() : -1);
    }

    public int getCurrentPosition() {
        return (isInPlaybackState() ? mMediaPlayer.getCurrentPosition() : 0);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mCurrentState = STATE_PREPARED;
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();

        // mSeekWhenPrepared may be changed after seekTo() call.
        int seekToPosition = mSeekWhenPrepared;
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentState = STATE_PLAYBACK_COMPLETED;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mCurrentState = STATE_ERROR;
        return true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            final SurfaceTexture surface = getSurfaceTexture();
            if (surface != null) {
                surface.setDefaultBufferSize(mVideoWidth, mVideoHeight);
                requestLayout();
            }
        }
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        release(true);
        return true;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        openVideo();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measureWidth = getDefaultSize(mVideoWidth, widthMeasureSpec);
        final int measureHeight = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            measureVideo(measureWidth, measureHeight);
        } else {
            setMeasuredDimension(measureWidth, measureHeight);
        }
    }

    private void initVideoView() {
        setSurfaceTextureListener(this);
    }

    private void openVideo() {
        final SurfaceTexture surface = getSurfaceTexture();
        if (mUri == null || surface == null) {
            return;
        }

        final Context context = getContext();
        final AudioManager am = (AudioManager)context.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // We shouldn't clear the target state, because
        // somebody might have called start() previously
        release(false);

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setDataSource(context, mUri, mHeaders);
            mMediaPlayer.setSurface(new Surface(surface));
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
        } catch (IOException | IllegalArgumentException e) {
            onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    private void release(boolean clearTargetState) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
    }

    private void measureVideo(int measureWidth, int measureHeight) {
        final int width, height;
        switch (mVideoMode) {
        case VIDEO_MODE_16_9:
            width = measureWidth;
            height = measureHeight;
            break;

        case VIDEO_MODE_4_3:
            width = (int)Math.floor(measureHeight * 4.0f / 3.0f);
            height = measureHeight;
            break;

        default:
            final int computeWidth = (int)Math.floor((float)mVideoWidth * measureHeight / mVideoHeight);
            if (computeWidth <= measureWidth) {
                width = computeWidth;
                height = measureHeight;
            } else {
                width = measureWidth;
                height = (int)Math.floor((float)mVideoHeight * measureWidth / mVideoWidth);
            }
            break;
        }

        setMeasuredDimension(width, height);
    }
}
