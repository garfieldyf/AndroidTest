package com.tencent.test;

import android.app.Activity;
import android.ext.temp.BarcodeCameraView;
import android.os.Bundle;
import android.view.View;

public class AndroidTestActivity extends Activity {
    private BarcodeCameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_test);

        mCameraView = (BarcodeCameraView)findViewById(R.id.camera2_view);
    }

    public void onRequestPreview(View view) {
    }
}
