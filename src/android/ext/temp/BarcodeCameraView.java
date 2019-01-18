package android.ext.temp;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.ext.util.ArrayUtils;
import android.ext.util.FileUtils;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

@SuppressLint("NewApi")
public class BarcodeCameraView extends SurfaceView implements Callback, Comparator<Size>, OnImageAvailableListener {
    private Point mPreviewSize;
    private CameraDevice mCamera;
    private final Rect mClipBounds;

    private Builder mPreviewRequest;
    private ImageReader mImageReader;
    private CameraCaptureSession mSession;
    private OnBarcodeCameraListener mListener;

    private Handler mHandler;

    public BarcodeCameraView(Context context) {
        this(context, null, 0);
    }

    public BarcodeCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarcodeCameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mClipBounds = new Rect();
        setKeepScreenOn(true);

        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.YUV_420_888);
    }

    public final void requestPreview() {
        requestPreview(true);
    }

    public final Point getPreviewSize() {
        return mPreviewSize;
    }

    public final Rect getBarcodeClipBounds() {
        return mClipBounds;
    }

    public void computeBarcodeClipBounds(Rect scanningBounds) {
        // Gets the size of the screen, in pixels.
        final Point screenSize = new Point();
        getDisplay().getSize(screenSize);

        // Computes the coordinates of this view on the screen.
        final int[] location = new int[2];
        getLocationOnScreen(location);

        // Computes the bounds of the barcode clip area on the screen.
        mClipBounds.left   = (scanningBounds.left   + location[0]) * mPreviewSize.y / screenSize.x;
        mClipBounds.top    = (scanningBounds.top    + location[1]) * mPreviewSize.x  / screenSize.y;
        mClipBounds.right  = (scanningBounds.right  + location[0]) * mPreviewSize.y / screenSize.x;
        mClipBounds.bottom = (scanningBounds.bottom + location[1]) * mPreviewSize.x  / screenSize.y;
    }

    public final void setOnBarcodeCameraListener(OnBarcodeCameraListener listener) {
        mListener = listener;
    }

    private void requestPreview(boolean requestImage) {
        if (mSession == null) {
            return;
        }

        try {
            final Surface surface = mImageReader.getSurface();
            if (requestImage) {
                mPreviewRequest.addTarget(surface);
            } else {
                mPreviewRequest.removeTarget(surface);
            }

            mSession.setRepeatingRequest(mPreviewRequest.build(), null, mHandler);
        } catch (CameraAccessException e) {
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mHandler != null) {
            mHandler.getLooper().quitSafely();
            mHandler = null;
        }

        if (mCamera != null) {
            FileUtils.close(mCamera);
            FileUtils.close(mSession);
            FileUtils.close(mImageReader);
            mCamera = null;
        }
    }

    @Override
    public int compare(Size one, Size another) {
        return (another.getWidth() * another.getHeight() - one.getWidth() * one.getHeight());
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        requestPreview(false);
        if (mListener != null) {
            post(new Runnable() {
                @Override
                public void run() {
                    final Image image = mImageReader.acquireNextImage();
                    final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    final byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    image.close();

                    mListener.onPreviewFrame(mCamera, data);
                }
            });
        }
    }

    private void openCamera(SurfaceHolder holder) {
        final HandlerThread thread = new HandlerThread("BarcodeCameraThread");
        thread.start();
        mHandler = new Handler(thread.getLooper());

        final Context context  = getContext();
        final CameraManager cm = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        final String cameraId  = Integer.toString(CameraMetadata.LENS_FACING_BACK);

        try {
            final CameraCharacteristics characteristics = cm.getCameraCharacteristics(cameraId);
            
//            characteristics.get(CameraCharacteristics.LENS_FACING);
            
            final StreamConfigurationMap configurations = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//            //int[] modes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
//            characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
//            characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
//            characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);

//            mPreviewSize = computePreviewSize(context, configurations.getOutputSizes(ImageFormat.YUV_420_888));
//            holder.setFixedSize(mPreviewSize.x, mPreviewSize.y);
//            if (mListener != null) {
//                mListener.onPreviewSizeChanged(mPreviewSize);
//            }
//
//            mImageReader = ImageReader.newInstance(mPreviewSize.x, mPreviewSize.y, ImageFormat.YUV_420_888, 1);
//            mImageReader.setOnImageAvailableListener(this, mHandler);
//            cm.openCamera(cameraId, mStateCallback, mHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Point computePreviewSize(Context context, Size[] supportedSizes) {
        // Gets the size of the screen, in pixels.
        final Point screenSize = new Point();
        getDisplay().getSize(screenSize);

        final int count = ArrayUtils.getSize(supportedSizes);
        if (count <= 0) {
            return screenSize;
        }

        Arrays.sort(supportedSizes, this);
        Log.i("yf", "supportedSizes = " + Arrays.toString(supportedSizes));
        final int minWidth  = screenSize.y / 3;
        final int minHeight = screenSize.x / 3;
        final float screenRatio = (float)screenSize.y / screenSize.x;

        Size previewSize = null;
        float minDiff = Float.POSITIVE_INFINITY;
        for (int i = 0; i < count; ++i) {
            final Size size = supportedSizes[i];
            final int width = size.getWidth(), height = size.getHeight();
            if (width < minWidth && height < minHeight) {
                continue;
            }

            if (width == screenSize.y && height == screenSize.x) {
                previewSize = size;
                break;
            }

            final float newDiff = Math.abs((float)width / height - screenRatio);
            if (minDiff > newDiff) {
                minDiff = newDiff;
                previewSize = size;
            }
        }

        return (previewSize != null ? new Point(previewSize.getWidth(), previewSize.getHeight()) : screenSize);
    }

//    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(CameraDevice camera) {
//            try {
//                final Surface surface = getHolder().getSurface();
//                mCamera = camera;
//                mPreviewRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
////              就是在这里，通过这个set(key,value)方法，设置曝光啊，自动聚焦等参数！！ 如下举例：
////              mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE,CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                mPreviewRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                mPreviewRequest.set(CaptureRequest.CONTROL_SCENE_MODE, CaptureRequest.CONTROL_SCENE_MODE_BARCODE);
//                mPreviewRequest.get(CaptureRequest.CONTROL_AF_MODE);
//                mPreviewRequest.set(CaptureRequest.FLASH_MODE, 1);
//                //mPreviewRequest.get(key)
//                mPreviewRequest.addTarget(surface);
//                camera.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), mSessionCallback, mHandler);
//            } catch (CameraAccessException e) {
//            }
//        }
//
//        @Override
//        public void onClosed(CameraDevice camera) {
//            Log.i("yf", "onClosed");
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//        }
//
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//        }
//    };

    private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                mSession = session;
                mSession.setRepeatingRequest(mPreviewRequest.build(), null, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
        }
    };

    /**
     * Used for being notified the barcode camera events.
     */
    public static interface OnBarcodeCameraListener {
        void onError(CameraDevice camera, int error);
        void onPreviewSizeChanged(Point previewSize);
        void onPreviewFrame(CameraDevice camera, byte[] data);
    }
}
