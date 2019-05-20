package android.ext.widget;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * Class BarcodeCameraView
 * @author Garfield
 */
@SuppressWarnings("deprecation")
public class BarcodeCameraView extends SurfaceView implements Callback, Runnable, AutoFocusCallback, Comparator<Size> {
    /**
     * The <tt>Camera</tt> device open failed.
     */
    public static final int CAMERA_ERROR_OPEN_FAILED = 0x10000000;

    /**
     * Represents an invalid zoom value.
     */
    public static final int INVALID_ZOOM = -1;

    private Camera mCamera;
    private int mZoom;
    private int mMaxZoom;
    private String mFlashMode;

    private Rect mClipBounds;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private OnBarcodeCameraListener mListener;

    public BarcodeCameraView(Context context) {
        super(context);
        initView();
    }

    public BarcodeCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BarcodeCameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    /**
     * Gives the next preview frame in addition to displaying it on the
     * screen. This method can be called any time, even when preview is live.
     */
    public void requestPreview() {
        if (mCamera != null) {
            mCamera.setOneShotPreviewCallback(mListener);
        }
    }

    /**
     * Sets current zoom value of the camera device. If the zoom is not supported
     * invoke this method has no effect.
     * @param zoom The zoom value. The valid range is 0 to {@link #getCameraMaxZoom}.
     * @see #getCameraZoom()
     * @see #getCameraMaxZoom()
     */
    public void setCameraZoom(int zoom) {
        if (mCamera == null || mMaxZoom == INVALID_ZOOM) {
            // The camera device uninitialize, We will set the
            // zoom value after the camera device initialize.
            mZoom = zoom;
        } else {
            zoom = ArrayUtils.rangeOf(zoom, 0, mMaxZoom);
            if (mZoom != zoom) {
                final Parameters params = mCamera.getParameters();
                params.setZoom(mZoom = zoom);
                mCamera.setParameters(params);
            }
        }
    }

    /**
     * Returns current zoom value of the camera device.
     * @return The current zoom value. The value range
     * is 0 to {@link #getCameraMaxZoom}.
     * @see #setCameraZoom(int)
     * @see #getCameraMaxZoom()
     */
    public final int getCameraZoom() {
        return mZoom;
    }

    /**
     * Returns the maximum zoom value of the camera device.
     * @return The maximum zoom value supported by the camera
     * device, or {@link #INVALID_ZOOM} if the zoom is not supported.
     * @see #getCameraZoom()
     * @see #setCameraZoom(int)
     */
    public final int getCameraMaxZoom() {
        return mMaxZoom;
    }

    /**
     * Sets the flash mode of the camera device.
     * @param mode The flash mode. May be one of
     * <tt>Parameters.FLASH_MODE_XXX</tt> constants.
     * @see #getCameraFlashMode()
     */
    public void setCameraFlashMode(String mode) {
        DebugUtils.__checkError(mode == null, "mode == null");
        if (mCamera == null) {
            // The camera device uninitialize, We will set the
            // flash mode after the camera device initialize.
            mFlashMode = mode;
        } else if (!mode.equals(mFlashMode)) {
            final Parameters params = mCamera.getParameters();
            params.setFlashMode(mFlashMode = mode);
            mCamera.setParameters(params);
        }
    }

    /**
     * Returns the current flash mode of the camera device.
     * @return The current flash mode. <tt>null</tt> if
     * flash mode is not supported.
     * @see #setCameraFlashMode(String)
     */
    public final String getCameraFlashMode() {
        return mFlashMode;
    }

    /**
     * Returns the camera preview width. <p>Note: The returned
     * width relative to the camera device orientation (The
     * default camera device orientation is landscape).</p>
     * @return The preview width in pixels.
     * @see #getPreviewHeight()
     */
    public final int getPreviewWidth() {
        return mPreviewWidth;
    }

    /**
     * Returns the camera preview height. <p>Note: The returned
     * height relative to the camera device orientation (The
     * default camera device orientation is landscape).</p>
     * @return The preview height in pixels.
     * @see #getPreviewWidth()
     */
    public final int getPreviewHeight() {
        return mPreviewHeight;
    }

    /**
     * Returns the bounds of the barcode clip area. The returned rectangle is
     * not a copy, you should not change the rectangle. <p>Note: The returned
     * rectangle's coordinates relative to the camera device orientation (The
     * default camera device orientation is landscape).</p>
     * @return The barcode clip bounds in pixels.
     * @see #computeBarcodeClipBounds(Rect)
     */
    public final Rect getBarcodeClipBounds() {
        return mClipBounds;
    }

    /**
     * Computes the bounds of the barcode clip area. <p>Note: This method recommended call
     * in the {@link OnBarcodeCameraListener#onPreviewSizeChanged(Camera, int, int)}.</p>
     * @param scanningBounds The coordinates of the scanning bounds on the screen in pixels.
     * @see #getBarcodeClipBounds()
     */
    public void computeBarcodeClipBounds(Rect scanningBounds) {
        // Computes the bounds of the barcode clip area.
        final Point screenSize = getScreenSize();
        mClipBounds.left   = scanningBounds.top    * mPreviewWidth  / screenSize.x;
        mClipBounds.top    = scanningBounds.left   * mPreviewHeight / screenSize.y;
        mClipBounds.right  = scanningBounds.bottom * mPreviewWidth  / screenSize.x;
        mClipBounds.bottom = scanningBounds.right  * mPreviewHeight / screenSize.y;
    }

    public final void setOnBarcodeCameraListener(OnBarcodeCameraListener listener) {
        mListener = listener;
    }

    @Override
    public void run() {
        if (mCamera != null) {
            mCamera.autoFocus(this);
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        postDelayed(this, 2000);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
            if (mCamera == null) {
                throw new SecurityException("Unable to open the camera, Check whether the camera is enabled.");
            }

            final boolean autoFocus = setCameraParameters();
            holder.setFixedSize(mPreviewHeight, mPreviewWidth);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            if (autoFocus) {
                mCamera.autoFocus(this);
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getMessage());
            if (mListener != null) {
                mListener.onError(CAMERA_ERROR_OPEN_FAILED, mCamera);
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setErrorCallback(null);
                mCamera.cancelAutoFocus();
                mCamera.stopPreview();
                mCamera.release();
            } catch (Exception e) {
                Log.e(getClass().getName(), Log.getStackTraceString(e));
            } finally {
                mCamera = null;
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public int compare(Size one, Size another) {
        return (another.width * another.height - one.width * one.height);
    }

    private void initView() {
        mMaxZoom = INVALID_ZOOM;
        mClipBounds = new Rect();
        setKeepScreenOn(true);

        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.NV21);
    }

    private Point getScreenSize() {
        final Point screenSize = new Point();
        getDisplay().getRealSize(screenSize);
        screenSize.set(screenSize.y, screenSize.x); // Swap width and height.
        return screenSize;
    }

    private boolean setCameraParameters() {
        // Sets the camera zoom value.
        final Parameters params = mCamera.getParameters();
        if (params.isZoomSupported()) {
            mMaxZoom = params.getMaxZoom();
            mZoom = ArrayUtils.rangeOf(mZoom, 0, mMaxZoom);
            params.setZoom(mZoom);
        }

        // Sets the camera flash mode.
        if (mFlashMode != null) {
            params.setFlashMode(mFlashMode);
        } else {
            mFlashMode = params.getFlashMode();
        }

        // Computes the camera preview size.
        final Size previewSize = computePreviewSize(params);
        mPreviewWidth  = previewSize.width;
        mPreviewHeight = previewSize.height;
        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewSize(mPreviewWidth, mPreviewHeight);
        if (mListener != null) {
            mListener.onPreviewSizeChanged(mCamera, mPreviewWidth, mPreviewHeight);
        }

        // Sets the camera focus mode and scene mode.
        final boolean autoFocus = !contains(params.getSupportedFocusModes(), Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setFocusMode(autoFocus ? Parameters.FOCUS_MODE_AUTO : Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        if (contains(params.getSupportedSceneModes(), Parameters.SCENE_MODE_BARCODE)) {
            params.setSceneMode(Parameters.SCENE_MODE_BARCODE);
        }

        // Gets the camera device orientation.
        final CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info);
        this.__checkDumpCameraInfo(params, info);

        // Sets the camera parameters.
        mCamera.setParameters(params);
        mCamera.setErrorCallback(mListener);
        mCamera.setOneShotPreviewCallback(mListener);
        mCamera.setDisplayOrientation(info.orientation);

        return autoFocus;
    }

    private Size computePreviewSize(Parameters params) {
        final Point screenSize = getScreenSize();
        final Size defaultSize = params.getPreviewSize();
        if (defaultSize.width >= screenSize.x * 2 / 3 && defaultSize.height >= screenSize.y * 2 / 3) {
            return defaultSize;
        }

        final List<Size> supportedSizes = params.getSupportedPreviewSizes();
        final int count = ArrayUtils.getSize(supportedSizes);
        if (count == 0) {
            return defaultSize;
        }

        Collections.sort(supportedSizes, this);
        final int minWidth  = screenSize.x / 3;
        final int minHeight = screenSize.y / 3;
        final float screenRatio = (float)screenSize.x / screenSize.y;

        Size previewSize = null;
        float minDiff = Float.POSITIVE_INFINITY;
        for (int i = 0; i < count; ++i) {
            final Size size = supportedSizes.get(i);
            if ((size.width < minWidth && size.height < minHeight) || size.width > screenSize.x || size.height > screenSize.y) {
                continue;
            }

            if (size.width == screenSize.x && size.height == screenSize.y) {
                previewSize = size;
                break;
            }

            final float newDiff = Math.abs((float)size.width / size.height - screenRatio);
            if (minDiff > newDiff) {
                minDiff = newDiff;
                previewSize = size;
            }
        }

        return (previewSize != null ? previewSize : defaultSize);
    }

    private void __checkDumpCameraInfo(Parameters params, CameraInfo info) {
        final Point screenSize = getScreenSize();
        final List<Size> supportedSizes = params.getSupportedPreviewSizes();
        final StringBuilder result = new StringBuilder(512).append("BarcodeCameraView\n{")
            .append("\n  screenSize  = ").append(screenSize.x).append('x').append(screenSize.y).append(" (").append((float)screenSize.x / screenSize.y).append(')')
            .append("\n  previewSize = ").append(mPreviewWidth).append('x').append(mPreviewHeight).append(" (").append((float)mPreviewWidth / mPreviewHeight).append(')')
            .append("\n  orientation = ").append(info.orientation).append("\n  zoom = ").append(mZoom).append("\n  maxZoom = ").append(mMaxZoom).append("\n  flashMode = ").append(mFlashMode)
            .append("\n  clipBounds = (").append(mClipBounds.left).append(", ").append(mClipBounds.top).append(" - ").append(mClipBounds.right).append(", ").append(mClipBounds.bottom).append(')')
            .append("\n  supportedPreviewSizes = [ ");

        for (int i = 0, count = ArrayUtils.getSize(supportedSizes); i < count; ++i) {
            final Size size = supportedSizes.get(i);
            if (i != 0) {
                result.append(", ");
            }

            result.append(size.width).append('x').append(size.height).append(" (").append((float)size.width / size.height).append(')');
        }

        Log.d(getClass().getSimpleName(), result.append(" ]\n}").toString());
    }

    private static boolean contains(List<String> supportedModes, String mode) {
        return (supportedModes != null && supportedModes.contains(mode));
    }

    /**
     * Used for being notified the barcode camera events.
     */
    public static interface OnBarcodeCameraListener extends PreviewCallback, ErrorCallback {
        /**
         * Callback method to be invoked when the camera preview size has been changed.
         * <p>Note: The preview size relative to the camera device orientation (The
         * default camera device orientation is landscape).</p>
         * @param camera The <tt>Camera</tt> device.
         * @param previewWidth The camera preview width in pixels.
         * @param previewHeight The camera preview height in pixels.
         */
        void onPreviewSizeChanged(Camera camera, int previewWidth, int previewHeight);
    }
}
