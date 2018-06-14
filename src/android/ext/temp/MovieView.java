package android.ext.temp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

public class MovieView extends View {
    private Movie mMovie;
    private long mMovieStart;

    public MovieView(Context context) {
        this(context, null, 0);
    }

    public MovieView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

//        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GIFImageView);
//        final int resId = a.getResourceId(R.styleable.GIFImageView_android_src, 0);
//        a.recycle();
//
//        if (resId > 0) {
//            mMovie = getResources().getMovie(resId);
//        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie == null) {
            return;
        }

        final long now = SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }

        int dur = mMovie.duration();
        if (dur == 0) {
            dur = 1000;
        }

        mMovie.setTime((int)((now - mMovieStart) % dur));
        mMovie.draw(canvas, 0, 0);
        postInvalidateDelayed(100);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        final int minimumWidth = super.getSuggestedMinimumWidth();
        return (mMovie != null ? Math.max(minimumWidth, mMovie.width()) : minimumWidth);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        final int minimumHeight = super.getSuggestedMinimumHeight();
        return (mMovie != null ? Math.max(minimumHeight, mMovie.height()) : minimumHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(widthMeasureSpec) : getSuggestedMinimumWidth() + getPaddingLeft() + getPaddingRight(), MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(heightMeasureSpec) : getSuggestedMinimumHeight() + getPaddingTop() + getPaddingBottom());
    }
}
