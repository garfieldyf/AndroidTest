package com.tencent.temp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.tencent.temp.LockPatternUtils.Cell;
import com.tencent.test.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 *
 * Is also capable of displaying a static pattern in "in progress", "wrong" or
 * "correct" states.
 */
public class LockPatternView extends View {
    // Aspect to use when rendering this view
    private static final int ASPECT_SQUARE = 0; // View will be the minimum of width/height
    private static final int ASPECT_LOCK_WIDTH = 1; // Fixed width; height will be minimum of (w,h)
    private static final int ASPECT_LOCK_HEIGHT = 2; // Fixed height; width will be minimum of (w,h)

    /**
     * How many milliseconds we spend animating each circle of a lock pattern
     * if the animating mode is set.  The entire animation should take this
     * constant * the length of the pattern to complete.
     */
    private static final int CIRCLE_ANIMATING_MILLIS = 700;
    private static final float DIAMETER_FACTOR = 0.10f;

    private OnPatternListener mOnPatternListener;
    private final ArrayList<Cell> mPattern = new ArrayList<Cell>(9);

    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    private final boolean[][] mDrawLookup = new boolean[3][3];

    /**
     * the in progress point:
     * - during interaction: where the user's finger is
     * - during animation: the current tip of the animating line
     */
    private float mInProgressX = -1;
    private float mInProgressY = -1;

    private final Paint mPaint = new Paint();
    private final Paint mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);

    private long mAnimatingStart;

    private static final int FLAG_IN_PROGRESS   = 0x01;
    private static final int FLAG_STEALTH_MODE  = 0x02;
    private static final int FLAG_TOUCH_ENABLED = 0x04;

    private int mFlags = FLAG_TOUCH_ENABLED;

    private DisplayMode mDisplayMode = DisplayMode.CORRECT;

    private float mSquareWidth;
    private float mSquareHeight;

    private Bitmap mBitmapBtnDefault;
    private Bitmap mBitmapBtnTouched;
    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapCircleGreen;
    private Bitmap mBitmapCircleRed;
    private Bitmap mBitmapArrowGreenUp;
    private Bitmap mBitmapArrowRedUp;

    private final Path mPath = new Path();
    private final Rect mInvalidateRect = new Rect();
    private final Rect mTmpInvalidateRect = new Rect();

    private int mCellWidth;
    private int mCellHeight;

    private int mAspect;
    private final Matrix mArrowMatrix = new Matrix();
    private final Matrix mCircleMatrix = new Matrix();

    public LockPatternView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockPatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setClickable(true);
        mPathPaint.setColor(Color.WHITE);
        mPathPaint.setAlpha(128);
        mPathPaint.setStrokeCap(Cap.ROUND);
        mPathPaint.setStrokeJoin(Join.ROUND);
        mPathPaint.setStyle(Paint.Style.STROKE);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView);
        final String aspect = a.getString(R.styleable.LockPatternView_aspect);
        a.recycle();

        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }

        // lot's of bitmaps!
        mBitmapBtnDefault = getBitmapFor(R.drawable.btn_code_lock_default_holo);
        mBitmapBtnTouched = getBitmapFor(R.drawable.btn_code_lock_touched_holo);
        mBitmapCircleDefault = getBitmapFor(R.drawable.indicator_code_lock_point_area_default_holo);
        mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green_holo);
        mBitmapCircleRed = getBitmapFor(R.drawable.indicator_code_lock_point_area_red_holo);

        mBitmapArrowGreenUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_green_up_holo);
        mBitmapArrowRedUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_red_up);

        // bitmaps have the size of the largest bitmap in this group
        final Bitmap bitmaps[] = { mBitmapBtnDefault, mBitmapBtnTouched, mBitmapCircleDefault,
                mBitmapCircleGreen, mBitmapCircleRed };

        for (Bitmap bitmap : bitmaps) {
            mCellWidth = Math.max(mCellWidth, bitmap.getWidth());
            mCellHeight = Math.max(mCellHeight, bitmap.getHeight());
        }
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    /**
     * @return Whether the view is in stealth mode.
     */
    public boolean isStealthMode() {
        return containsFlags(FLAG_STEALTH_MODE);
    }

    /**
     * Set whether the view is in stealth mode.  If true, there will be no
     * visible feedback as the user enters the pattern.
     * @param stealthMode Whether in stealth mode.
     */
    public void setStealthMode(boolean stealthMode) {
        setFlags(FLAG_STEALTH_MODE, stealthMode);
    }

    public final void setOnPatternListener(OnPatternListener listener) {
        mOnPatternListener = listener;
    }

    /**
     * Set the pattern explicitely (rather than waiting for the user to input
     * a pattern).
     * @param displayMode How to display the pattern.
     * @param pattern The pattern.
     */
    public void setPattern(DisplayMode mode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearDrawLookup();

        for (int i = 0, size = pattern.size(); i < size; ++i) {
            final Cell cell = pattern.get(i);
            mDrawLookup[cell.row][cell.col] = true;
        }

        setDisplayMode(mode);
    }

    /**
     * Set the display mode of the current pattern.  This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     * @param mode The display mode.
     */
    public void setDisplayMode(DisplayMode mode) {
        if ((mDisplayMode = mode) == DisplayMode.ANIMATE) {
            mAnimatingStart = SystemClock.elapsedRealtime();
            final Cell cell = mPattern.get(0);
            mInProgressX = getCenterX(cell.col);
            mInProgressY = getCenterY(cell.row);
            clearDrawLookup();
        }

        invalidate();
    }

    private void notifyPatternStarted() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternStarted();
        }
    }

    private void notifyPatternCleared() {
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCleared();
        }
    }

    /**
     * Clear the pattern.
     */
    public void clearPattern() {
        mPattern.clear();
        clearDrawLookup();
        mDisplayMode = DisplayMode.CORRECT;
        invalidate();
    }

    private void clearDrawLookup() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                mDrawLookup[i][j] = false;
            }
        }
    }

//    /**
//     * Disable input (for instance when displaying a message that will
//     * timeout so user doesn't get view into messy state).
//     */
//    public void disableInput() {
//        mTouchEnabled = false;
//    }

    public void setTouchEnabled(boolean enabled) {
        setFlags(FLAG_TOUCH_ENABLED, enabled);
    }

    private void setFlags(int flags, boolean set) {
        if (set) {
            mFlags |= flags;
        } else {
            mFlags &= ~flags;
        }
    }

    private boolean containsFlags(int flags) {
        return ((mFlags & flags) == flags);
    }

//    /**
//     * Enable input.
//     */
//    public void enableInput() {
//        mTouchEnabled = true;
//    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        mSquareWidth  = (width - getPaddingLeft() - getPaddingRight()) / 3.0f;
        mSquareHeight = (height - getPaddingTop() - getPaddingBottom()) / 3.0f;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
        case MeasureSpec.UNSPECIFIED:
            result = desired;
            break;
        case MeasureSpec.AT_MOST:
            result = Math.max(specSize, desired);
            break;
        case MeasureSpec.EXACTLY:
        default:
            result = specSize;
        }
        return result;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mCellWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return 3 * mCellWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        switch (mAspect) {
        case ASPECT_SQUARE:
            viewWidth = viewHeight = Math.min(viewWidth, viewHeight);
            break;
        case ASPECT_LOCK_WIDTH:
            viewHeight = Math.min(viewWidth, viewHeight);
            break;
        case ASPECT_LOCK_HEIGHT:
            viewWidth = Math.min(viewWidth, viewHeight);
            break;
        }

        setMeasuredDimension(viewWidth, viewHeight);
    }

    private Cell addDetectCellHit(float x, float y) {
        final Cell cell = checkNewHit(x, y);
        if (cell == null) {
            return null;
        }

        Cell fillCell = null;
        if (!mPattern.isEmpty()) {
            final Cell lastCell = mPattern.get(mPattern.size() - 1);
            final int row = cell.row - lastCell.row;
            final int col = cell.col - lastCell.col;

            int fillRow = lastCell.row;
            int fillCol = lastCell.col;

            if (Math.abs(row) == 2 && Math.abs(col) != 1) {
                fillRow = lastCell.row + (row > 0 ? 1 : -1);
            }

            if (Math.abs(col) == 2 && Math.abs(row) != 1) {
                fillCol = lastCell.col + (col > 0 ? 1 : -1);
            }

            fillCell = Cell.obtain(fillRow, fillCol);
        }

        if (fillCell != null && !mDrawLookup[fillCell.row][fillCell.col]) {
            addCellToPattern(fillCell);
        }

        addCellToPattern(cell);
        if (isHapticFeedbackEnabled()) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }

        return cell;
    }

    private void addCellToPattern(Cell newCell) {
        mDrawLookup[newCell.row][newCell.col] = true;
        mPattern.add(newCell);

        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
    }

    private Cell checkNewHit(float x, float y) {
        final int row = getRowHit(y);
        if (row < 0) {
            return null;
        }

        final int col = getColumnHit(x);
        if (col < 0) {
            return null;
        }

        return (mDrawLookup[row][col] ? null : Cell.obtain(row, col));
    }

    private int getRowHit(float y) {
        final float hitSize = mSquareHeight * 0.6f;
        final float offset  = getPaddingTop() + (mSquareHeight - hitSize) / 2.0f;
        for (int i = 0; i < 3; ++i) {
            final float hitTop = offset + mSquareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }

        return -1;
    }

    private int getColumnHit(float x) {
        final float hitSize = mSquareWidth * 0.6f;
        final float offset  = getPaddingLeft() + (mSquareWidth - hitSize) / 2.0f;
        for (int i = 0; i < 3; ++i) {
            final float hitLeft = offset + mSquareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        final AccessibilityManager am = (AccessibilityManager)getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am.isTouchExplorationEnabled()) {
            final int action = event.getAction();
            switch (action) {
            case MotionEvent.ACTION_HOVER_ENTER:
                event.setAction(MotionEvent.ACTION_DOWN);
                break;

            case MotionEvent.ACTION_HOVER_MOVE:
                event.setAction(MotionEvent.ACTION_MOVE);
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                event.setAction(MotionEvent.ACTION_UP);
                break;
            }

            onTouchEvent(event);
            event.setAction(action);
        }

        return super.onHoverEvent(event);
    }

	@Override
	@SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if (!containsFlags(FLAG_TOUCH_ENABLED) || !isEnabled()) {
            return false;
        }

        boolean handled = true;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            handleActionDown(event);
            break;

        case MotionEvent.ACTION_MOVE:
            handleActionMove(event);
            break;

        case MotionEvent.ACTION_UP:
            handleActionUp(event);
            break;

        case MotionEvent.ACTION_CANCEL:
            if (containsFlags(FLAG_IN_PROGRESS)) {
                setFlags(FLAG_IN_PROGRESS, false);
                clearPattern();
                notifyPatternCleared();
            }
            break;

        default:
            handled = false;
        }

        return handled;
    }

    private void handleActionDown(MotionEvent event) {
        clearPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = addDetectCellHit(x, y);
        if (hitCell != null) {
            setFlags(FLAG_IN_PROGRESS, true);
            mDisplayMode = DisplayMode.CORRECT;
            notifyPatternStarted();
        } else if (containsFlags(FLAG_IN_PROGRESS)) {
            setFlags(FLAG_IN_PROGRESS, false);
            notifyPatternCleared();
        }

        if (hitCell != null) {
            final float startX = getCenterX(hitCell.col);
            final float startY = getCenterY(hitCell.row);
            final float width  = mSquareWidth / 2.0f;
            final float height = mSquareHeight / 2.0f;
            invalidate((int)(startX - width), (int)(startY - height), (int)(startX + width), (int)(startY + height));
        }

        mInProgressX = x;
        mInProgressY = y;
    }

    private void handleActionMove(MotionEvent event) {
        final float radius = (mSquareWidth * DIAMETER_FACTOR * 0.5f);
        final int historySize = event.getHistorySize();
        mTmpInvalidateRect.setEmpty();
        boolean invalidateNow = false;

        Log.i("yf", "historySize = " + historySize);
        for (int i = 0; i < historySize + 1; ++i) {
            final float x = (i < historySize ? event.getHistoricalX(i) : event.getX());
            final float y = (i < historySize ? event.getHistoricalY(i) : event.getY());

            final Cell hitCell = addDetectCellHit(x, y);
            final int size = mPattern.size();
            if (hitCell != null && size == 1) {
                setFlags(FLAG_IN_PROGRESS, true);
                notifyPatternStarted();
            }

            invalidateNow = (Math.abs(x - mInProgressX) > 0 || Math.abs(y - mInProgressY) > 0);
            if (containsFlags(FLAG_IN_PROGRESS) && size > 0) {
                final Cell lastCell = mPattern.get(size - 1);
                final float lastCenterX = getCenterX(lastCell.col);
                final float lastCenterY = getCenterY(lastCell.row);

                float left   = Math.min(lastCenterX, x) - radius;
                float right  = Math.max(lastCenterX, x) + radius;
                float top    = Math.min(lastCenterY, y) - radius;
                float bottom = Math.max(lastCenterY, y) + radius;

                if (hitCell != null) {
                    final float width  = mSquareWidth * 0.5f;
                    final float height = mSquareHeight * 0.5f;
                    final float hitCenterX = getCenterX(hitCell.col);
                    final float hitCenterY = getCenterY(hitCell.row);

                    left   = Math.min(hitCenterX - width, left);
                    right  = Math.max(hitCenterX + width, right);
                    top    = Math.min(hitCenterY - height, top);
                    bottom = Math.max(hitCenterY + height, bottom);
                }

                mTmpInvalidateRect.union((int)(left + 0.5f), (int)(top + 0.5f), (int)(right + 0.5f), (int)(bottom + 0.5f));
            }
        }

        mInProgressX = event.getX();
        mInProgressY = event.getY();

        if (invalidateNow) {
            mInvalidateRect.union(mTmpInvalidateRect);
            invalidate(mInvalidateRect);
            mInvalidateRect.set(mTmpInvalidateRect);
        }
    }

    private void handleActionUp(MotionEvent event) {
        if (!mPattern.isEmpty()) {
            setFlags(FLAG_IN_PROGRESS, false);
            if (mOnPatternListener != null) {
                mOnPatternListener.onPatternDetected(mPattern);
            }

            invalidate();
        }
    }

    private float getCenterX(int col) {
        return getPaddingLeft() + col * mSquareWidth + mSquareWidth / 2.0f;
    }

    private float getCenterY(int row) {
        return getPaddingTop() + row * mSquareHeight + mSquareHeight / 2.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int size = mPattern.size();
        if (mDisplayMode == DisplayMode.ANIMATE) {

            // figure out which circles to draw

            // + 1 so we pause on complete pattern
            final int oneCycle = (size + 1) * CIRCLE_ANIMATING_MILLIS;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() -
                    mAnimatingStart) % oneCycle;
            final int numCircles = spotInCycle / CIRCLE_ANIMATING_MILLIS;

            clearDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final Cell cell = mPattern.get(i);
                mDrawLookup[cell.row][cell.col] = true;
            }

            // figure out in progress portion of ghosting line

            final boolean needToUpdateInProgressPoint = numCircles > 0
                    && numCircles < size;

            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle =
                        ((float) (spotInCycle % CIRCLE_ANIMATING_MILLIS)) /
                        CIRCLE_ANIMATING_MILLIS;

                final Cell currentCell = mPattern.get(numCircles - 1);
                final float centerX = getCenterX(currentCell.col);
                final float centerY = getCenterY(currentCell.row);

                final Cell nextCell = mPattern.get(numCircles);
                final float dx = percentageOfNextCircle *
                        (getCenterX(nextCell.col) - centerX);
                final float dy = percentageOfNextCircle *
                        (getCenterY(nextCell.row) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
            }

            invalidate();
        }

        float radius = (mSquareWidth * DIAMETER_FACTOR * 0.5f);
        mPathPaint.setStrokeWidth(radius);
        mPath.rewind();

        // draw the circles
        final int paddingTop = getPaddingTop();
        final int paddingLeft = getPaddingLeft();

        for (int i = 0; i < 3; ++i) {
            float topY = paddingTop + i * mSquareHeight;
            for (int j = 0; j < 3; ++j) {
                float leftX = paddingLeft + j * mSquareWidth;
                drawCircle(canvas, (int)leftX, (int)topY, mDrawLookup[i][j]);
            }
        }

        // the path should be created and cached every time we hit-detect a cell
        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless the user is in progress, and
        // we are in stealth mode)
        final boolean drawPath = (!containsFlags(FLAG_STEALTH_MODE) || mDisplayMode == DisplayMode.ERROR);

        // draw the arrows associated with the path (unless the user is in progress, and
        // we are in stealth mode)
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        mPaint.setFilterBitmap(true); // draw with higher quality since we render with transforms
        if (drawPath) {
            for (int i = 0; i < size - 1; i++) {
                Cell cell = mPattern.get(i);
                Cell next = mPattern.get(i + 1);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!mDrawLookup[next.row][next.col]) {
                    break;
                }

                float leftX = paddingLeft + cell.col * mSquareWidth;
                float topY  = paddingTop  + cell.row * mSquareHeight;

                drawArrow(canvas, leftX, topY, cell, next);
            }
        }

        if (drawPath) {
            boolean anyCircles = false;
            for (int i = 0; i < size; i++) {
                Cell cell = mPattern.get(i);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!mDrawLookup[cell.row][cell.col]) {
                    break;
                }
                anyCircles = true;

                float centerX = getCenterX(cell.col);
                float centerY = getCenterY(cell.row);
                if (i == 0) {
                    mPath.moveTo(centerX, centerY);
                } else {
                    mPath.lineTo(centerX, centerY);
                }
            }

            // add last in progress section
            if ((containsFlags(FLAG_IN_PROGRESS) || mDisplayMode == DisplayMode.ANIMATE)
                    && anyCircles) {
                mPath.lineTo(mInProgressX, mInProgressY);
            }
            canvas.drawPath(mPath, mPathPaint);
        }

        mPaint.setFilterBitmap(oldFlag); // restore default flag
    }

    private void drawArrow(Canvas canvas, float leftX, float topY, Cell start, Cell end) {
        // offsets for centering the bitmap in the cell
        final int offsetX = ((int)mSquareWidth  - mCellWidth)  / 2;
        final int offsetY = ((int)mSquareHeight - mCellHeight) / 2;

        // compute transform to place arrow bitmaps at correct angle inside circle.
        // This assumes that the arrow image is drawn at 12:00 with it's top edge
        // coincident with the circle bitmap's top edge.
        Bitmap arrow = mDisplayMode != DisplayMode.ERROR ? mBitmapArrowGreenUp : mBitmapArrowRedUp;

        final float angle = (float)Math.toDegrees(Math.atan2((double)(end.row - start.row), (double)(end.col - start.col))) + 90.0f;
        float sx = Math.min(mSquareWidth / mCellWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mCellHeight, 1.0f);
        mArrowMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mArrowMatrix.preTranslate(mCellWidth / 2, mCellHeight / 2);
        mArrowMatrix.preScale(sx, sy);
        mArrowMatrix.preTranslate(-mCellWidth / 2, -mCellHeight / 2);
        mArrowMatrix.preRotate(angle, mCellWidth / 2.0f, mCellHeight / 2.0f);
        mArrowMatrix.preTranslate((mCellWidth - arrow.getWidth()) / 2.0f, 0.0f);
        canvas.drawBitmap(arrow, mArrowMatrix, mPaint);
    }

    private void drawCircle(Canvas canvas, int leftX, int topY, boolean partOfPattern) {
        Bitmap outerCircle;
        Bitmap innerCircle;

        if (!partOfPattern || (containsFlags(FLAG_STEALTH_MODE) && mDisplayMode != DisplayMode.ERROR)) {
            // unselected circle
            outerCircle = mBitmapCircleDefault;
            innerCircle = mBitmapBtnDefault;
        } else if (containsFlags(FLAG_IN_PROGRESS)) {
            // user is in middle of drawing a pattern
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnTouched;
        } else if (mDisplayMode == DisplayMode.ERROR) {
            // the pattern is wrong
            outerCircle = mBitmapCircleRed;
            innerCircle = mBitmapBtnDefault;
        } else if (mDisplayMode == DisplayMode.CORRECT ||
                mDisplayMode == DisplayMode.ANIMATE) {
            // the pattern is correct
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnDefault;
        } else {
            throw new IllegalStateException("unknown display mode " + mDisplayMode);
        }

        int offsetX = (int)((mSquareWidth - mCellWidth) / 2f);
        int offsetY = (int)((mSquareHeight - mCellHeight) / 2f);

        float sx = Math.min(mSquareWidth / mCellWidth, 1.0f);
        float sy = Math.min(mSquareHeight / mCellHeight, 1.0f);

        mCircleMatrix.setTranslate(leftX + offsetX, topY + offsetY);
        mCircleMatrix.preTranslate(mCellWidth / 2, mCellHeight / 2);
        mCircleMatrix.preScale(sx, sy);
        mCircleMatrix.preTranslate(-mCellWidth / 2, -mCellHeight / 2);

        canvas.drawBitmap(outerCircle, mCircleMatrix, mPaint);
        canvas.drawBitmap(innerCircle, mCircleMatrix, mPaint);

//        int c = mPaint.getColor();
//        mPaint.setColor(Color.RED);
//        canvas.drawRect(0, 0, mSquareWidth, mSquareHeight, mPaint);
//        mPaint.setColor(Color.GREEN);
//        canvas.drawRect(0, 0, mCellWidth, mCellHeight, mPaint);
//        mPaint.setColor(c);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), LockPatternUtils.flatten(mPattern),
                mDisplayMode, mFlags, isHapticFeedbackEnabled());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState)state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mFlags = savedState.mFlags;
        mDisplayMode = savedState.mDisplayMode;
        setHapticFeedbackEnabled(savedState.mHapticFeedbackEnabled);
        setPattern(mDisplayMode, LockPatternUtils.unflatten(savedState.mPattern));
    }

    /**
     * How to display the current pattern.
     */
    public static enum DisplayMode {
        /**
         * The pattern drawn is error.
         */
        ERROR,

        /**
         * The pattern drawn is correct.
         */
        CORRECT,

        /**
         * The pattern drawn is animate.
         */
        ANIMATE,
    }

    /**
     * The call back interface for detecting patterns entered by the user.
     */
    public static interface OnPatternListener {
        /**
         * A new pattern has started.
         */
        void onPatternStarted();

        /**
         * The pattern was cleared.
         */
        void onPatternCleared();

        /**
         * The user extended the pattern currently being drawn by one cell.
         * @param pattern The pattern with newly added cell.
         */
        void onPatternCellAdded(List<Cell> pattern);

        /**
         * A pattern was detected from the user.
         * @param pattern The pattern.
         */
        void onPatternDetected(List<Cell> pattern);
    }

    /**
     * The parecelable for saving and restoring a lock pattern view.
     */
    private static final class SavedState extends BaseSavedState {
        private final int mFlags;
        private final String mPattern;
        private final DisplayMode mDisplayMode;
        private final boolean mHapticFeedbackEnabled;

        private SavedState(Parcelable superState, String pattern, DisplayMode mode, int flags, boolean hapticFeedbackEnabled) {
            super(superState);
            mFlags   = flags;
            mPattern = pattern;
            mDisplayMode = mode;
            mHapticFeedbackEnabled = hapticFeedbackEnabled;
        }

        private SavedState(Parcel source) {
            super(source);
            mFlags   = source.readInt();
            mPattern = source.readString();
            mDisplayMode = DisplayMode.values()[source.readInt()];
            mHapticFeedbackEnabled = (source.readByte() == 1);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mFlags);
            dest.writeString(mPattern);
            dest.writeInt(mDisplayMode.ordinal());
            dest.writeByte(mHapticFeedbackEnabled ? (byte)1 : 0);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR = new Creator<LockPatternView.SavedState>() {
            public LockPatternView.SavedState createFromParcel(Parcel source) {
                return new LockPatternView.SavedState(source);
            }

            public LockPatternView.SavedState[] newArray(int size) {
                return new LockPatternView.SavedState[size];
            }
        };
    }
}
