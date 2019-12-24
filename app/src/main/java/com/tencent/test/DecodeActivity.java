package com.tencent.test;

import android.app.Activity;
import android.ext.barcode.BarcodeDecoder;
import android.ext.barcode.BarcodeDecoder.Builder;
import android.ext.barcode.BarcodeDecoder.OnDecodeListener;
import android.ext.renderscript.RenderScriptYuvToRGB;
import android.ext.util.FileUtils;
import android.ext.widget.BarcodeCameraView;
import android.ext.widget.BarcodeCameraView.CameraCallback;
import android.ext.widget.BarcodeDecorView;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

@SuppressWarnings("deprecation")
public class DecodeActivity extends Activity implements OnDecodeListener, CameraCallback, OnClickListener, ResultPointCallback {
    private View mScanView;
    private TextView mResultView;

    private BarcodeDecoder mDecoder;
    private BarcodeDecorView mDecorView;
    private MediaActionSound mActionSound;
    private BarcodeCameraView mCameraView;
    private RenderScriptYuvToRGB mYuvToRGB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decode);

        mYuvToRGB = new RenderScriptYuvToRGB(this);
        mActionSound = new MediaActionSound();
        mDecoder = new BarcodeDecoder(new Builder()
            .charset("UTF-8")
            .formats(BarcodeFormat.QR_CODE)
            .build());

        mResultView = (TextView)findViewById(R.id.barcode_result);
        mDecorView = (BarcodeDecorView)findViewById(R.id.barcode_frame_view);

        mScanView = findViewById(R.id.scan);
        mScanView.setOnClickListener(this);
        findViewById(R.id.zoom_in).setOnClickListener(this);

        mCameraView = (BarcodeCameraView)findViewById(R.id.camera_view);
//        mCameraView.setCameraZoom(30);
//        mCameraView.setCameraFlashMode(Parameters.FLASH_MODE_TORCH);
        mCameraView.setCameraCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mYuvToRGB.close();
        mActionSound.release();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.zoom_in:
//            final int zoom = mCameraView.getCameraZoom();
//            Log.i("yf", "oldZoom = " + zoom + ", newZoom = " + (zoom + 1));
//            mCameraView.setCameraZoom(zoom + 1);

//            final String mode = mCameraView.getCameraFlashMode();
//            Log.i("yf", "FlashMode = " + mode);
//            if (Parameters.FLASH_MODE_TORCH.equals(mode)) {
//                mCameraView.setCameraFlashMode(Parameters.FLASH_MODE_OFF);
//            } else {
//                mCameraView.setCameraFlashMode(Parameters.FLASH_MODE_TORCH);
//            }
            break;

        default:
            showResultView(false);
            mCameraView.requestPreview();
            break;
        }
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint point) {
        if (point != null) {
            Log.i("yf", point.toString());
        }
    }

    @Override
    public void onPreviewSizeChanged(Camera camera, int previewWidth, int previewHeight) {
        Toast.makeText(this, "previewWidth = " + previewWidth + ", previewHeight = " + previewHeight, Toast.LENGTH_LONG).show();
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        }

        mCameraView.computeBarcodeClipBounds(mDecorView.computeScanningBounds(0.6f, -30));
    }

    private volatile byte[] mData;
    private Bitmap mBitmap;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!isDestroyed()) {
            mData = data;
//            Toast.makeText(this, "data length = " + data.length, Toast.LENGTH_LONG).show();
            mDecoder.startDecode(data, mCameraView.getPreviewWidth(), mCameraView.getPreviewHeight(),
                    mCameraView.getBarcodeClipBounds(), MainApplication.sInstance.getExecutor(), this);
        }
    }

    @Override
    public void onError(int error, Camera camera) {
        if (isDestroyed()) {
            return;
        }

        if (error == BarcodeCameraView.CAMERA_ERROR_OPEN_FAILED) {
            mScanView.setEnabled(false);
            mResultView.setText(R.string.camera_error_uninitialized);
            mResultView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDecodeComplete(LuminanceSource source, Result result) {
        if (isDestroyed()) {
            return;
        }

        if (result != null) {
            //mActionSound.play(MediaActionSound.SHUTTER_CLICK);
            final StringBuilder text = new StringBuilder()
                .append("内容：").append(result.getText()).append('\n')
                .append("类型：").append(result.getBarcodeFormat().name()).append('\n')
                .append("时间：").append(DateFormat.format("yyyy-MM-dd kk:mm:ss", result.getTimestamp())).append('\n');

            final Map<ResultMetadataType, Object> metadata = result.getResultMetadata();
            if (metadata != null) {
                text.append("Meta data：\n").append(metadata.toString());
            }

            final ImageView thumbView = (ImageView)findViewById(R.id.thumbnial);
//            thumbView.setImageBitmap(BarcodeDecoder.createLuminanceBitmap(source));
            mYuvToRGB.convert(mData, mBitmap, false);
            thumbView.setImageBitmap(mBitmap);
//            thumbView.setImageBitmap(mYuvToRGB.convert(mData, mCameraView.getPreviewWidth(), mCameraView.getPreviewHeight()));

            mResultView.setText(text);
            showResultView(true);
            //saveImage();
        } else {
            showResultView(false);
            mCameraView.requestPreview();
        }
    }

    private void saveImage() {
        OutputStream os = null;
        try {
            os = new FileOutputStream("/sdcard/image.jpg");
            final int width  = mCameraView.getPreviewWidth();
            final int height = mCameraView.getPreviewHeight();
            new YuvImage(mData, ImageFormat.NV21, width, height, null).compressToJpeg(new Rect(0, 0, width, height), 100, os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.close(os);
        }
    }

    private void showResultView(boolean show) {
        mScanView.setEnabled(show);
        if (show) {
            findViewById(R.id.thumbnial).setVisibility(View.VISIBLE);
            mResultView.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.thumbnial).setVisibility(View.GONE);
            mResultView.setVisibility(View.GONE);
        }
    }
}
