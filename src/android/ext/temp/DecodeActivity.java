package android.ext.temp;

import java.util.Map;
import android.app.Activity;
import android.ext.barcode.BarcodeDecoder;
import android.ext.barcode.BarcodeDecoder.Builder;
import android.ext.barcode.BarcodeDecoder.OnDecodeListener;
import android.ext.temp.BarcodeCameraView.OnBarcodeCameraListener;
import android.ext.widget.BarcodeDecorView;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.tencent.test.MainApplication;
import com.tencent.test.R;

public class DecodeActivity extends Activity implements OnDecodeListener, OnBarcodeCameraListener, OnClickListener {
    private View mScanView;
    private TextView mResultView;

    private BarcodeDecoder mDecoder;
    private BarcodeDecorView mDecorView;
    private BarcodeCameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decode2);

        mDecoder = new BarcodeDecoder();
        final Map<DecodeHintType, Object> hints = new Builder()
            .charset("UTF-8")
            .formats(BarcodeFormat.QR_CODE)
            .build();
        mDecoder.setHints(hints);

        mResultView = (TextView)findViewById(R.id.barcode_result);
        mDecorView = (BarcodeDecorView)findViewById(R.id.barcode_frame_view);
        mDecorView.animate();

        mScanView = findViewById(R.id.scan);
        mScanView.setOnClickListener(this);

        mCameraView = (BarcodeCameraView)findViewById(R.id.camera_view);
        mCameraView.setOnBarcodeCameraListener(this);
    }

    public Rect getBarcodeClipBounds() {
        return mCameraView.getBarcodeClipBounds();
    }

    @Override
    public void onClick(View view) {
        showResultView(false);
        mCameraView.requestPreview();
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        if (isDestroyed()) {
            return;
        }

        // if (error == BarcodeCameraView.CAMERA_ERROR_OPEN_FAILED) {
        // mScanView.setEnabled(false);
        // mResultView.setText(R.string.camera_error_uninitialized);
        // mResultView.setVisibility(View.VISIBLE);
        // }
    }

    @Override
    public void onPreviewFrame(CameraDevice camera, byte[] data) {
        if (!isDestroyed()) {
            final Point size = mCameraView.getPreviewSize();
            final Rect clipBounds = mCameraView.getBarcodeClipBounds();
            mDecoder.startDecode(data, size.x, size.y, clipBounds, MainApplication.sInstance.getExecutor(), this);
        }
    }

    @Override
    public void onPreviewSizeChanged(Point previewSize) {
        // mDecorView.computeScanningBounds(0.6f, -30);
        // mCameraView.computeBarcodeClipBounds(mDecorView.getScanningBounds());
    }

    @Override
    public void onDecodeComplete(LuminanceSource source, Result result) {
        if (isDestroyed()) {
            return;
        }

        if (result != null) {
            final StringBuilder text = new StringBuilder().append("内容：").append(result.getText()).append('\n').append("类型：").append(result.getBarcodeFormat().name()).append('\n').append("时间：").append(DateFormat.format("yyyy-MM-dd kk:mm:ss", result.getTimestamp())).append('\n');

            final Map<ResultMetadataType, Object> metadata = result.getResultMetadata();
            if (metadata != null) {
                text.append("Meta data：\n").append(metadata.toString());
            }

            final PlanarYUVLuminanceSource yuvSource = (PlanarYUVLuminanceSource)source;
            final ImageView thumbView = (ImageView)findViewById(R.id.thumbnial);
            thumbView.setImageBitmap(Bitmap.createBitmap(yuvSource.renderThumbnail(), yuvSource.getThumbnailWidth(), yuvSource.getThumbnailHeight(), Config.RGB_565));

            mResultView.setText(text);
            showResultView(true);
        } else {
            showResultView(false);
            mCameraView.requestPreview();
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
