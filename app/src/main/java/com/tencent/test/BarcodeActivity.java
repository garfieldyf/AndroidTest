package com.tencent.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.ext.util.ByteArrayBuffer;
import android.ext.util.FileUtils;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BarcodeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode);
        //computeMessageDigest(this);
        //computeMessageDigest(getPackageName());

        //Log.i("yf", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        //Log.i("yf", getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        //Log.i("yf", Arrays.toString(getExternalFilesDirs(Environment.DIRECTORY_PICTURES)));
        //setMode(this);

//        PinyinUtils.loadPinyinTable(getAssets(), "pinyin/pinyin.db");
//        char c = '中';
//        String[] pinyin = PinyinUtils.toPinyin(c);
//        if (pinyin != null) {
//            System.out.println(Arrays.toString(pinyin));
//        } else {
//            System.out.println("error");
//        }

        long bytes = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024;
        long bytes1 = Runtime.getRuntime().maxMemory();
        Log.i("yf", "bytes = " + bytes + ", byte1 = " + bytes1);
        Log.i("yf", "freeMemory = " + FileUtils.formatFileSize(Runtime.getRuntime().freeMemory()));
        Log.i("yf", "totalMemory = " + FileUtils.formatFileSize(Runtime.getRuntime().totalMemory()));
        Log.i("yf", "maxMemory = " + FileUtils.formatFileSize(Runtime.getRuntime().maxMemory()));
    }

    public void onDecode(View view) {
        startActivity(new Intent(this, DecodeActivity.class));
    }

    public void onEncode(View view) {
        startActivity(new Intent(this, EncodeActivity.class));
    }

    public void onMovie(View view) {
        startActivity(new Intent(this, DrawActivity.class));
    }

    public void onLeanback(View view) {
        startActivity(new Intent(this, MainActivity.class));
//        throw new NullPointerException("NullPointerException");
    }

    public static void computeMessageDigest(Context context) {
        try {
            final ByteArrayBuffer buffer = FileUtils.readFile(context, UriUtils.getAssetUri("docs/zlib.h"), null);
            final byte[] data = buffer.toByteArray();
            Log.i("yf", "MessageDigest  MD5 : " + StringUtils.toHexString(MessageDigest.getInstance("MD5").digest(data)));
            Log.i("yf", "MessageDigests MD5 : " + StringUtils.toHexString(MessageDigests.computeByteArray(data, Algorithm.MD5)));

            Log.i("yf", "MessageDigest  SHA1 : " + StringUtils.toHexString(MessageDigest.getInstance("SHA1").digest(data)));
            Log.i("yf", "MessageDigests SHA1 : " + StringUtils.toHexString(MessageDigests.computeByteArray(data, Algorithm.SHA1)));

            Log.i("yf", "MessageDigest  SHA256 : " + StringUtils.toHexString(MessageDigest.getInstance("SHA256").digest(data)));
            Log.i("yf", "MessageDigests SHA256 : " + StringUtils.toHexString(MessageDigests.computeByteArray(data, Algorithm.SHA256)));

            Log.i("yf", "MessageDigest  SHA384 : " + StringUtils.toHexString(MessageDigest.getInstance("SHA384").digest(data)));
            Log.i("yf", "MessageDigests SHA384 : " + StringUtils.toHexString(MessageDigests.computeByteArray(data, Algorithm.SHA384)));

            Log.i("yf", "MessageDigest  SHA512 : " + StringUtils.toHexString(MessageDigest.getInstance("SHA512").digest(data)));
            Log.i("yf", "MessageDigests SHA512 : " + StringUtils.toHexString(MessageDigests.computeByteArray(data, Algorithm.SHA512)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void computeMessageDigest() {
        final String string = "hello world";
        final byte[] data = string.getBytes();
        final File dir = getExternalFilesDir(null);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        final String filename = dir.getPath() + "/md.txt";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)));
            writer.print("MessageDigest  MD5 : ");
            writer.println(StringUtils.toHexString(MessageDigest.getInstance("MD5").digest(data)));
            writer.print("MessageDigests MD5 : ");
            writer.println(StringUtils.toHexString(MessageDigests.computeString(string, Algorithm.MD5)));

            writer.print("MessageDigest  SHA1 : ");
            writer.println(StringUtils.toHexString(MessageDigest.getInstance("SHA1").digest(data)));
            writer.print("MessageDigests SHA1 : ");
            writer.println(StringUtils.toHexString(MessageDigests.computeString(string, Algorithm.SHA1)));

            writer.print("MessageDigest  SHA256 : ");
            writer.println(StringUtils.toHexString(MessageDigest.getInstance("SHA256").digest(data)));
            writer.print("MessageDigests SHA256 : ");
            writer.println(StringUtils.toHexString(MessageDigests.computeString(string, Algorithm.SHA256)));

            writer.print("MessageDigest  SHA384 : ");
            writer.println(StringUtils.toHexString(MessageDigest.getInstance("SHA384").digest(data)));
            writer.print("MessageDigests SHA384 : ");
            writer.println(StringUtils.toHexString(MessageDigests.computeString(string, Algorithm.SHA384)));

            writer.print("MessageDigest  SHA512 : ");
            writer.println(StringUtils.toHexString(MessageDigest.getInstance("SHA512").digest(data)));
            writer.print("MessageDigests SHA512 : ");
            writer.println(StringUtils.toHexString(MessageDigests.computeString(string, Algorithm.SHA512)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FileUtils.close(writer);
        }
    }
}
