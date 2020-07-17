package com.tencent.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.ext.barcode.BarcodeBuilder;
import android.ext.barcode.BarcodeEncoder;
import android.ext.barcode.BarcodeEncoder.OnEncodeListener;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.Map;

public class EncodeActivity extends Activity implements OnClickListener, OnEncodeListener {
    private CheckBox mCheckBox;
    private EditText mInputText;

    private ImageView mBarcodeView;
    private BarcodeEncoder mEncoder;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encode);

        mEncoder = new BarcodeEncoder().setMargin(0)
            .setCharset("UTF-8")
            .setErrorCorrection(ErrorCorrectionLevel.H);

        mCheckBox = (CheckBox)findViewById(R.id.add_logo);
        mInputText = (EditText)findViewById(R.id.input);
        mBarcodeView = (ImageView)findViewById(R.id.barcode_image);
        findViewById(R.id.create_barcode).setOnClickListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View view) {
        int size = BarcodeEncoder.computeQRCodeSize(this, 0.55f);
        // String contents = mInputText.getText().toString();
        String contents = "http://phapp.tv.funshion.com/download?action=setlock&version=3.1.0_2017-04-12_11-57&mac=28:76:CD:01:D9:EA&sid=funshion";
        mEncoder.startEncode(MainApplication.sThreadPool, contents, BarcodeFormat.QR_CODE, size, size, this);
//        String contents = "33456798";
//        mEncoder.startEncode(contents, BarcodeFormat.CODE_128, size, size / 4, MainApplication.sInstance.getExecutor(), this);

//        DebugUtils.startMethodTracing();
//        final BitMatrix bitMatrix = mEncoder.encode(contents, BarcodeFormat.QR_CODE, size, size);
//        if (bitMatrix != null) {
//            Drawable logo = null;
//            if (mCheckBox.isChecked()) {
//                logo = getResources().getDrawable(R.drawable.ic_launcher);
//            }
//
//            final Bitmap bitmap = new BarcodeBuilder(bitMatrix)
//                .logo(logo)
//                .margins(30)
//                .size(128, 128)
//                .build();
//            mBarcodeView.setImageBitmap(bitmap);
//        }
//
//        DebugUtils.stopMethodTracing("yf", "encode", 'm');
    }

    @Override
    @SuppressWarnings("deprecation")
    public Bitmap convertToBitmap(BitMatrix bitMatrix, Map<EncodeHintType, ?> hints) {
        Drawable logo = null;
        if (mCheckBox.isChecked()) {
            logo = getResources().getDrawable(R.drawable.ic_launcher);
        }

        DebugUtils.startMethodTracing();
        final Bitmap bitmap = new BarcodeBuilder(bitMatrix)
            .logo(logo, 128, 128)
            .margins(30)
            .build();
        DebugUtils.stopMethodTracing("yf", "encode", 'm');

        return bitmap;
    }

    @Override
    @SuppressLint("NewApi")
    public void onEncodeComplete(BitMatrix bitMatrix, Bitmap bitmap) {
        BitmapUtils.dumpBitmap(this, "yf", bitmap);
        if (bitmap != null && !isDestroyed()) {
            mBarcodeView.setImageBitmap(bitmap);
        }
    }
}
