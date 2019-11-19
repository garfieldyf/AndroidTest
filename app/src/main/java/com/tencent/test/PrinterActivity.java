package com.tencent.test;

import android.app.Activity;
import android.ext.util.DeviceUtils;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import com.tencent.temp.PrinterView;

public class PrinterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        dump((PrinterView)findViewById(R.id.device_info));
    }

    protected void dump(PrinterView printer) {
        DeviceUtils.dumpSystemInfo(this, printer);
        final int index = TextUtils.indexOf(printer.getText(), '\n');
        if (index != -1) {
            printer.setSpan(new ForegroundColorSpan(Color.RED), 0, index, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            printer.setSpan(new BackgroundColorSpan(Color.BLUE), 0, index, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
    }
}
