package com.tencent.test;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.ext.annotation.CursorField;
import android.ext.content.ResourceLoader;
import android.ext.content.res.XmlResources.XmlResourceInflater;
import android.ext.database.AsyncQueryHandler;
import android.ext.database.AsyncSQLiteHandler;
import android.ext.database.DatabaseReceiver;
import android.ext.database.DatabaseUtils;
import android.ext.graphics.BitmapUtils;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.json.JSONArray;
import android.ext.json.JSONObject;
import android.ext.json.JSONUtils;
import android.ext.net.NetworkUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.ByteArrayBuffer;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.ext.util.MessageDigests;
import android.ext.util.MessageDigests.Algorithm;
import android.ext.util.PackageUtils;
import android.ext.util.PackageUtils.PackageArchiveInfo;
import android.ext.util.PackageUtils.PackageParser;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.ext.util.ProcessUtils.CrashDatabase;
import android.ext.util.StringUtils;
import android.ext.util.UriUtils;
import android.ext.util.ZipUtils;
import android.ext.widget.AsyncViewStub;
import android.ext.widget.AsyncViewStub.OnInflateListener;
import android.ext.widget.CountDownTimer;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.JsonWriter;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;
import android.util.TypedValue;
import android.util.Xml;
import android.view.InflateException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.tencent.temp.BaseListAdapter;
import com.tencent.temp.DexLoader;
import com.tencent.temp.JsonLoader;
import com.tencent.temp.JsonLoader.JsonLoadParams;
import com.tencent.test.JSONTest.Permission;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ImageActivity extends Activity implements OnScrollListener, OnItemClickListener {
    private ListView mListView;
    private ImageView mImageView;
    private ImageAdapter mAdapter;
    private UserDatabase mDatabase;
    private UserBroadcastReceiver mReceiver;
    PackageUtils.PackageParser mParser;
    private NoteDatabase mDatabase2;

    @Override
    @SuppressLint({ "NewApi", "SdCardPath" })
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image);

        // new LoadingTask(this).execute();
        // startService(new Intent(this, TestService.class));

        mAdapter = new ImageAdapter();
        mImageView = (ImageView)findViewById(R.id.blur);
        mListView = (ListView)findViewById(R.id.images);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);
        mListView.setOnItemClickListener(this);

        final Printer printer = new LogPrinter(Log.DEBUG, "aaaa");
        DeviceUtils.dumpSystemInfo(this, printer);
        mDatabase2 = new NoteDatabase(getApplicationContext());
        // testReport();
        // Log.i("yf", NetworkUtils.getActiveNetworkInfo(this).toString());
//         testScanFiles();
        // JSONTest.download(this);
//         JSONTest.queryCrashInfos();
        // testReflect();
//         testDatabase();
//        mParser = new PackageUtils.PackageParser<AbsPackageInfo>(this, AppPackageInfo.FACTORY);
        // testNetworkUtils();
        // testJson(printer);
        // testRegex();
        // testDex();
        // testColor();
        // testUncompress();
        // testLogo();
        // testDBHandler();
//         testDate();
        // testGray();
        // testForkJoin();
        // testComponents();
//         testDirent();
        // Stat stat = FileUtils.stat("/sdcard/resource.zip");
        // Log.i("yf", stat.toString());
        // testDimension();
//         testSort();
//         testIndexOf();
//        testPackageParser();
//        testFile();
//        testDrawableDensity();
//        testSemaphore();
//        testUri(UriUtils.getFileUri("/sdcard/applog"));
//        testUri(UriUtils.getAssetUri("docs/abc.xml"));
//        testUri(UriUtils.getResourceUri("com.tencent.test", R.raw.abc));
//        testUri("/sdcard/applog");
//        testUri(new File("/sdcard/applog"));
//        testUri(Uri.parse(UriUtils.getFileUri("/sdcard/applog")));
//        testUri(Uri.parse(UriUtils.getAssetUri("abc.xml")));
//        testUri(Uri.parse(UriUtils.getResourceUri("com.tencent.test", R.raw.abc)));
        //testArraySectionIndexer();
        //testListSectionIndexer();
//        testPagedList();
//        testFileProvider();
//        testScaleParameters();
//        testComputeFileSizes();
//        testJSONArray();
        //XmlResources.loadParameters(this, R.xml.size_params).dump(new LogPrinter(Log.DEBUG, "yf"), "");
        //testFileCopy();
//        TestSectionList.testList();

        testJsonLoader();
    }

    @TargetApi(26)
    private void decode() {
        final Options opts = new Options();
        opts.inMutable = true;
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image, opts);
        BitmapUtils.dumpBitmap(this, "ImageModule", bitmap);

        opts.inMutable = false;
        //opts.inBitmap = bitmap;
        opts.inPreferredConfig = Config.HARDWARE;
        final Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_image, opts);
        BitmapUtils.dumpBitmap(this, "ImageModule", bitmap2);
    }

    private void testJSONArray() {
        JSONArray array = makeArray(4, "item");
//        JsonUtils.addAll(array, makeArray(3, "add"));
//        JsonUtils.addAll(array, 3, makeArray(3, "insert"));
        Log.i("abcd", array.toString());
        Object a = array.clone();

        JSONObject object = new JSONObject()
            .put("name", "tom")
            .put("age", 24)
            .put("sex", true);
        Log.d("abcd", object.toString());

        JSONObject o = new JSONObject(object);
        JSONArray array1 = new JSONArray(array);

        final Object clone = object.clone();
        Log.d("abcd", "equals = " + clone.equals(object));
        Log.d("abcd", "equals = " + array.equals(a));
        Log.d("abcd", clone.getClass().getName());
        Log.d("abcd", a.getClass().getName());
        Log.d("abcd", JSONUtils.emptyJSONObject().clone().getClass().getName());
//        JSONUtils.emptyJSONArray().add(1);
        //JSONUtils.emptyJSONObject().put("aaa", 2);
    }

    private void testFileCopy() {
        String srcPath = "/sdcard/jdk.zip";
        String dstPath = "/data/data/com.tencent.test/files/1.rar";

        byte[] digest = MessageDigests.computeFile(srcPath, Algorithm.SHA1);
        Log.i("yf", "srcSHA = " + StringUtils.toHexString(digest));

        try {
            FileUtils.copyFile(this, UriUtils.getFileUri(srcPath), dstPath, null);
            digest = MessageDigests.computeFile(dstPath, Algorithm.SHA1);
            Log.i("yf", "dstSHA = " + StringUtils.toHexString(digest));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONArray makeArray(int length, String prefix) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < length; ++i) {
            array.add(prefix + (i + 1));
        }

        return array;
    }

    static final class Task1 implements Runnable {
        @Override
        public void run() {
            Log.i("yf", "Task1 is Begin");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            Log.i("yf", "Task1 is Done");
        }
    }

    static final class Task2 implements Runnable {
        @Override
        public void run() {
            Log.i("yf", "Task2 is Begin");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            Log.i("yf", "Task2 is Done");
        }
    }

    private void testComputeFileSizes() {
        final File dir = new File("/sdcard/Android/data/com.tencent.test");
        final int size = 1;

        for (int i = 0; i < size; ++i) {
            DebugUtils.startMethodTracing();
            long l = computeFiles(dir);
            DebugUtils.stopMethodTracing("abcd", "java   computeFileBytes = " + l);
        }

        for (int i = 0; i < size; ++i) {
            DebugUtils.startMethodTracing();
            long l = FileUtils.computeFileSizes(dir.getPath());
            DebugUtils.stopMethodTracing("abcd", "native computeFileBytes = " + l);
        }
    }

    private static long computeFiles(File dir) {
        long result = 0;
        final String[] filenames = dir.list();
        for (int i = 0, size = ArrayUtils.getSize(filenames); i < size; ++i) {
            final File file = new File(dir, filenames[i]);
            result += (file.isDirectory() ? computeFiles(file) : file.length());
        }

        return result;
    }

    private void testScaleParameters() {
//        Options opts = new Options();
//        opts.inDensity = DisplayMetrics.DENSITY_HIGH;
//        opts.inTargetDensity = DisplayMetrics.DENSITY_HIGH;
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.personal_card, opts);
//        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
//        BitmapUtils.dumpBitmap(this, "yf", bitmap);
//        Log.i("yf", drawable + ", W = " + drawable.getIntrinsicWidth() + ", H = " + drawable.getIntrinsicHeight());
//
//        opts.inTargetDensity = DisplayMetrics.DENSITY_MEDIUM;
//        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.personal_card, opts);
//        drawable = new BitmapDrawable(getResources(), bitmap);
//        drawable.setTargetDensity(DisplayMetrics.DENSITY_MEDIUM);
//        BitmapUtils.dumpBitmap(this, "yf", bitmap);
//        Log.i("yf", drawable + ", W = " + drawable.getIntrinsicWidth() + ", H = " + drawable.getIntrinsicHeight());

//        ScaleParameters parameters = new ScaleParameters(this, Config.ARGB_8888, 0, false);
//        Bitmap bitmap = BitmapUtils.decodeBitmap(this, UriUtils.getResourceUri(this, R.drawable.personal_card), parameters);
//        BitmapUtils.dumpBitmap(this, "yf", bitmap);
//
//        parameters = new ScaleParameters(this, Config.ARGB_8888, 0.666f, false);
//        bitmap = BitmapUtils.decodeBitmap(this, UriUtils.getResourceUri(this, R.drawable.personal_card), parameters);
//        BitmapUtils.dumpBitmap(this, "yf", bitmap);
    }

    private void testSemaphore() {
        // 只允许3个线程同时访问
        final Semaphore semp = new Semaphore(3);

        // 模拟10个客户端访问
        for (int i = 0; i < 10; ++i) {
            Runnable run = new Runnable() {
                public void run() {
                    try {
                        semp.acquire();
                        // 获取许可
                        System.out.println("线程" + Thread.currentThread().getName() + "获得许可");

                        // 模拟耗时的任务
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        // 释放许可
                        semp.release();
                        System.out.println("线程" + Thread.currentThread().getName() + "释放许可, 当前允许进入的任务个数：" + semp.availablePermits());
                    }
                }
            };

            MainApplication.sThreadPool.execute(run);
        }
    }

    private void testFile() {
        String path = "/sdcard/Android/data/com.tencent.test/cache/.image_cache";
        final FileScanCallback callback = new FileScanCallback();
        FileUtils.scanFiles(path, callback, FileUtils.FLAG_SCAN_FOR_DESCENDENTS, null);
        Log.i("yf", "scan files = " + callback.fileCount);
    }

    private void testDirent() {
        String path = Environment.getExternalStorageDirectory().getPath();
        int flags = 0;//FileUtils.FLAG_SCAN_FOR_DESCENDENTS FileUtils.FLAG_SCAN_SYMLINK_NOFOLLOW;
        List<Dirent> dirents = new ArrayList<>();
        DebugUtils.startMethodTracing();
        int errno = FileUtils.listFiles(path, flags, dirents);
        DebugUtils.stopMethodTracing("yf", "listFiles");
        if (dirents != null) {
            Log.i("yf", "errno = " + errno + ", dirent count = " + dirents.size());
        }

//        Stat stat = FileUtils.stat(path);
//        stat.dump(printer);
//
//        printer.println("isDirectory = " + new Dirent(path).isDirectory());
//        printer.println("isDirectory = " + new File(path).isDirectory());
    }

    private void testJsonLoader() {
        final String url1 = "http://jo.funtv.bestv.com.cn/config/channel/index/v3?block_id=620&ispreview=1&version=3.3.4.1&sid=FD4351A-LU&mac=28%3A76%3ACD%3A01%3AD9%3AEA&chiptype=638";
//        final String url2 = "http://jo.funtv.bestv.com.cn/config/mretrievetabs/v2?block_id=288&ispreview=1&version=3.3.4.1&sid=FD4351A-LU&mac=28%3A76%3ACD%3A01%3AD9%3AEA&chiptype=638";
//        final String url3 = "http://appv2.funtv.bestv.com.cn/frontpage/all/tomato/v3";
        final JsonLoadParams params = new JsonLoadParams("content");
//        final URLLoadParams params = new URLLoadParams();

        final ResourceLoader<String, JSONObject> loader = new ResourceLoader<String, JSONObject>(this, MainApplication.sThreadPool);
        loader.load(url1, params, JsonLoader.sListener, null);

        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                final AsyncViewStub view = (AsyncViewStub)findViewById(R.id.btn_cancel_stub);
                view.inflate(MainApplication.sThreadPool, new OnInflateListener() {
                    @Override
                    public void onFinishInflate(AsyncViewStub stub, View view, int layoutId) {
                        //Log.i("abc", view.toString());
                    }
                });
            }
        }, 1000);

//        PackageInfo pi = PackageUtils.myPackageInfo(this, PackageManager.GET_SHARED_LIBRARY_FILES);
//        pi.applicationInfo.dump(new LogPrinter(Log.DEBUG, "abc"), "");
//        Log.i("abc", "nativeLibraryDir = " + pi.applicationInfo.nativeLibraryDir);
//        Log.i("abc", "sharedLibraryFiles = " + Arrays.toString(pi.applicationInfo.sharedLibraryFiles));
        //Toast.makeText(this, "nativeLibraryDir = " + pi.applicationInfo.nativeLibraryDir, Toast.LENGTH_LONG).show();
    }

    static final class ArrayListEx<E> extends ArrayList<E> {
        private static final long serialVersionUID = 2009803799392307841L;
    }

    private void testSort() {
        final Integer[] numbers = new Integer[10];
        final List<Integer> list = new ArrayListEx<Integer>();
//        final List<Integer> list = new LinkedList<Integer>();
        final Random random = new Random();
        for (int i = 0; i < numbers.length; ++i) {
            list.add(numbers[i] = random.nextInt(10000));
        }

        int size = list.size();
        Log.i("abc", "org   = " + list.toString());

        ArrayUtils.sort(list, 2, size, null);
        Log.i("abc", "sort1 = " + list.toString());

        ArrayUtils.sort(Arrays.asList(numbers), 2, size, Collections.reverseOrder());
        Log.i("abc", "sort2 = " + Arrays.toString(numbers));
    }

    private void testPackageParser() {
        final List<PackageInfo> result = new PackageParser(this)
            //.addParseFlags(PackageManager.GET_ACTIVITIES)
            .addScanFlags(FileUtils.FLAG_SCAN_FOR_DESCENDENTS)
            .parse("/mnt/usb/sda1");
        PackageUtils.dumpPackageInfos(new LogPrinter(Log.INFO, "yf"), result);
        for (PackageInfo pi : result) {
            Drawable icon = getPackageManager().getApplicationIcon(pi.applicationInfo);
            CharSequence label = getPackageManager().getApplicationLabel(pi.applicationInfo);
            Log.i("yf", "label = " + label + ", icon = " + icon);
        }

        //////////////////////////////////

        List<PackageInfo> infos = new PackageParser(this).parse("/sdcard/apks");
        PackageUtils.dumpPackageInfos(new LogPrinter(Log.INFO, "yf"), infos);
    }
    
    private void testDimension() {
        final Resources res = getResources();
        final DisplayMetrics dm = res.getDisplayMetrics();
        // dm.density = 1.5f;
        // dm.densityDpi = DisplayMetrics.DENSITY_HIGH;
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, dm);
        Log.i("yf", "50dp = " + v);

        int w = res.getDimensionPixelOffset(R.dimen.normal_icon_width);
        int h = res.getDimensionPixelOffset(R.dimen.normal_icon_height);
        Log.i("yf", "w = " + w + ", h = " + h);

        // float i = (DisplayMetrics.DENSITY_HIGH / DisplayMetrics.DENSITY_MEDIUM);
    }

    private void testBlackList() {
        Uri u = BlackList.insert(this, "323773", "mplay", "战地枪王", "http://img.funtv.bestv.com.cn/sdw?oid=d00650d3624d1ae572f40b3c8dfc4bf8&w=530&h=298");
        Log.i("yf", u.toString());

        boolean in = BlackList.isInBlackList(this, "323773", "mplay");
        Log.i("yf", "isInBlackList = " + in);

        in = BlackList.isInBlackList(this, "323783", "mplay");
        Log.i("yf", "isInBlackList = " + in);
    }

    private void testDrawableDensity() {
        BitmapDrawable drawable = (BitmapDrawable)getResources().getDrawable(R.drawable.indicator_area_green);
        BitmapUtils.dumpBitmap(this, "yf", drawable.getBitmap());
        Log.i("yf", "bd w = " + drawable.getIntrinsicWidth() + ", h = " + drawable.getIntrinsicHeight());

        OvalBitmapDrawable od = new OvalBitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.indicator_area_green));
        BitmapUtils.dumpBitmap(this, "yf", od.getBitmap());
        Log.i("yf", "od w = " + od.getIntrinsicWidth() + ", h = " + od.getIntrinsicHeight());

//        drawable.setTargetDensity(DisplayMetrics.DENSITY_MEDIUM);
//        Log.i("yf", "w = " + drawable.getIntrinsicWidth() + ", h = " + drawable.getIntrinsicHeight());
//        drawable.setTargetDensity(DisplayMetrics.DENSITY_XHIGH);
//        Log.i("yf", "w = " + drawable.getIntrinsicWidth() + ", h = " + drawable.getIntrinsicHeight());
    }

    private void testComponents() {
        ContentResolver resolver = getContentResolver();
        Log.i("yf", resolver.toString());
        ContentResolver cr = getContentResolver();
        Log.i("yf", cr.toString());
        Log.i("yf", "resolver equals = " + cr.equals(resolver));

        AssetManager am = getAssets();
        Log.i("yf", am.toString());
        AssetManager assets = getAssets();
        Log.i("yf", assets.toString());
        AssetManager a = getResources().getAssets();
        Log.i("yf", a.toString());
        Log.i("yf", "assets equals = " + assets.equals(am));
    }

    private void testGray() {
        final Options opts = new Options();
        opts.inMutable = true;
        opts.inPreferredConfig = Config.ARGB_8888;
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.abc, opts);
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        BitmapUtils.grayBitmap(bitmap);

        opts.inMutable = false;
        final Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.abc, opts);
        final byte[] grays = new byte[width * height];
        // BitmapUtils.grayBitmap(bmp, grays);

        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; ++i) {
            int color = pixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            int gray = grays[i] & 0xFF;
            if (!(r == g && g == b && r == gray)) {
                throw new IllegalStateException("i = " + i + ", r = " + r + ", g = " + g + ", b = " + b + ", gray = " + grays[i]);
            }
        }

        Log.i("yf", "gray colors == bitmap colors");
    }

    private void testDate() {
        final Calendar calendar = Calendar.getInstance();
        Log.i("yf", "UTC hour = " + calendar.get(Calendar.HOUR_OF_DAY));
        Log.i("yf", "UTC minute = " + calendar.get(Calendar.MINUTE));
        Log.i("yf", "UTC second = " + calendar.get(Calendar.SECOND));
        Log.i("yf", "UTC time = " + calendar.getTimeInMillis());
        Log.i("yf", "UTC time = " + DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar).toString());

        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.setTimeInMillis(System.currentTimeMillis());
        Log.i("yf", "GMT hour = " + calendar.get(Calendar.HOUR_OF_DAY));
        Log.i("yf", "GMT minute = " + calendar.get(Calendar.MINUTE));
        Log.i("yf", "GMT second = " + calendar.get(Calendar.SECOND));
        Log.i("yf", "GMT time = " + calendar.getTimeInMillis());
        Log.i("yf", "GMT time = " + DateFormat.format("yyyy-MM-dd kk:mm:ss", calendar).toString());
    }

    private void testUri(Object uri) {
        String scheme = UriUtils.parseScheme(uri);
        Log.i("yf", "scheme = " + scheme + ", uri = " + uri);

        InputStream is = null;
        try {
            is = UriUtils.openInputStream(this, uri);
            Log.i("yf", is.toString());
        } catch (Exception e) {
            Log.i("yf", "UriUtils.openInputStream open failed");
        } finally {
            FileUtils.close(is);
        }
    }

    private void testCompress() {
        try {
            DebugUtils.startMethodTracing();
            ZipUtils.compress("/sdcard/aaa.zip", Deflater.DEFAULT_COMPRESSION, null, "/sdcard/apks");
            DebugUtils.stopMethodTracing("yf", "ZipUtils.compress");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testUncompress() {
        try {
            DebugUtils.startMethodTracing();
            ZipUtils.uncompress("/sdcard/aaa.zip", "/sdcard/apks1", null);
            DebugUtils.stopMethodTracing("yf", "ZipUtils.uncompress");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testColor() {
        final Options opts = new Options();
        opts.inMutable = true;
        opts.inPreferredConfig = Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_image, opts);
        BitmapUtils.grayBitmap(bitmap);
    }

    private void testLoadDex() {
        final String apkFile = new File(getFilesDir(), "tvmaster.apk.tmp").getPath();
        try {
            final PackageManager pm = getPackageManager();
            final PackageArchiveInfo info = PackageUtils.getPackageArchiveInfo(pm, apkFile);
            final Resources res = info.resources;
            final String packageName = info.packageInfo.packageName;
            int id;
            Drawable drawable;
            String s;
            id = res.getIdentifier("barcolor", "drawable", packageName);
            drawable = res.getDrawable(id);
            Log.i("abcd", "drawable = " + drawable);

            id = res.getIdentifier("icon", "drawable", packageName);
            drawable = res.getDrawable(id);
            Log.i("abcd", "drawable = " + drawable);

            id = res.getIdentifier("speed_test_retry", "string", packageName);
            s = res.getString(id);
            Log.i("abcd", "string = " + s);

            id = res.getIdentifier("connect_web_fail", "string", packageName);
            s = res.getString(id);
            Log.i("abcd", "string = " + s);

            info.close();
        } catch (Exception e) {
            Log.e("abcd", Log.getStackTraceString(e));
        }
    }

    private void testDex() {
        String librarySearchPath = "/data/data/com.tencent.test/cache/dex";
        String dexPath = "/data/data/com.tencent.test/cache/dex/TVSecurityTest.apk";
        //String dexOutputDir = DexLoader.getCodeCacheDir(this, "optDex");

        try {
            final DexLoader factory = new DexLoader(this, dexPath, librarySearchPath, new String[] { "funshion" });
            int error = (int)factory.loadClass("com.funshion.util.FileUtils").getDeclaredMethod("access", String.class, int.class).invoke(null, dexPath + "1", 0);
            Log.i("yf", "error = " + error);

            final Intent intent = new Intent();
            intent.setClassName("com.fun.mytest", "com.fun.mytest.MainActivity");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class SQLiteHandler extends AsyncSQLiteHandler {
        public SQLiteHandler(SQLiteDatabase db) {
            super(MainApplication.sThreadPool.createSerialExecutor(), db);
        }
    }

    private static final class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(Context context) {
            super(context, MainApplication.sThreadPool.createSerialExecutor());
        }
    }

    private void testDBHandler() {
        SQLiteHandler handler = new SQLiteHandler(null);
        Log.i("yf", "SQLiteHandler = " + handler.toString());

        QueryHandler h = new QueryHandler(this);
        Log.i("yf", "QueryHandler = " + h.toString());
    }

    private void testRegex() {
        String mac = "aa:bb:cc:dd:ee:ff";
        final Pattern pattern = Pattern.compile("([A-Fa-f0-9]{2}[-:\\s]){5}[A-Fa-f0-9]{2}");
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "aa:bb-cc:dd:ee:ff";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "za:bb-cc:dd:ee:ff";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A:BB-CC:DD:EE:FF";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A-BB-CC-DD-EE-FF";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A-BB-CC-DDa-EE-FF";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A-ab-Ce-Da-E2-Fd";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A\nab\nCe\nDa\nE2\nFd";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A\tab\tCe\tDa\tE2\tFd";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A ab Ce Da E2 Fd";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());

        mac = "9A \tab \tCe \tDa \tE2 \tFd";
        Log.i("yf", mac + " is mac address = " + pattern.matcher(mac).matches());
    }

    private void testNetworkUtils() {
        String macAddress = NetworkUtils.getMacAddress(NetworkUtils.ETHERNET, null);
        Log.i("yf", "mac  = " + macAddress);
        byte[] addr = NetworkUtils.toMacAddress(macAddress);
        Log.i("yf", "addr = " + NetworkUtils.formatMacAddress(addr));
    }

    private void testDatabase() {
        mReceiver = new UserBroadcastReceiver();
        mReceiver.register(this, UserDatabase.TABLE_NAME, 3);

        mDatabase = new UserDatabase(this);
        mDatabase.insert(this, "garfield", "11111111");
        mDatabase.insert(this, "yuanfeng", "22222222");
        mDatabase.insert(this, "xt", "123456");

        /*
         * [
         *   User [id = 1, name = garfield, password = 11111111],
         *   User [id = 2, name = yuanfeng, password = 22222222],
         *   User [id = 3, name = xt, password = 123456]
         * ]
         */
    }

    private static final class UserDatabase extends SQLiteOpenHelper {
        public static final String TABLE_NAME = "users";

        public UserDatabase(Context context) {
            super(context, null, null, 1);
        }

        public final long insert(Context context, String name, String password) {
            final long id = DatabaseUtils.executeInsert(getWritableDatabase(), "INSERT INTO " + TABLE_NAME + "(name, password) VALUES(?,?)", name, password);
            if (id != -1) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(DatabaseReceiver.resolveIntent(UserDatabase.TABLE_NAME, id));
            }

            return id;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name TEXT," + "password TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private static final class User {
        @CursorField("_id")
        private long id;

        @CursorField("name")
        private String name;

        @CursorField("password")
        private String password;

        private boolean sex = true;

        @Override
        public String toString() {
            return "User [id = " + id + ", name = " + name + ", sex = " + (sex ? "man" : "woman") + ", password = " + password + "]";
        }
    }

    /* package */ final class UserBroadcastReceiver extends DatabaseReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            dump("yf", intent);
            Log.i("yf", "id = " + intent.getData().getLastPathSegment());

            final Cursor cursor = mDatabase.getWritableDatabase().rawQuery("SELECT * FROM " + UserDatabase.TABLE_NAME, null);
            try {
                final List<User> users = DatabaseUtils.parse(cursor, User.class);
                if (users != null) {
                    Log.i("yf", users.toString());
                }
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
    }

    private void testJSON() {
        // final String path = Environment.getExternalStorageDirectory().getPath();
        // final Result result = JSONTest.load(path + "/school.json", Result.class);
        // if (result != null) {
        // Log.i("yf", result.toString());
        // }

        // final Item[] items = (Item[])JSONTest.loadArray(path + "/items.json", Item.class);
        // if (items != null) {
        // Log.i("yf", "length = " + items.length + ", type = " + items.toString());
        // }
        //
        // final Integer[] nums = JSONTest.loadArray(path + "/num.json", Integer.class);
        // if (nums != null) {
        // Log.i("yf", "length = " + nums.length + ", type = " + nums.toString());
        // }
        //
        // final Long[] lnums = JSONTest.loadArray(path + "/num.json", Long.class);
        // if (lnums != null) {
        // Log.i("yf", "length = " + lnums.length + ", type = " + lnums.toString());
        // }

        // JSONTest.loadUserInfos();

        Permission[] permissions = JSONTest.loadPermissions(this);
        if (permissions != null) {
            Log.i("yf", "length = " + permissions.length + ", type = " + permissions.toString());
            try {
                JSONUtils.writeObject("/sdcard/permissions.json", permissions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String[] packages = (String[])JSONTest.loadPackages(this, CharSequence.class);
        if (packages != null) {
            Log.i("yf", "length = " + packages.length + ", packages = " + Arrays.toString(packages));
        }

        String[] pkgs = (String[])JSONTest.loadPackages(this, String.class);
        if (pkgs != null) {
            Log.i("yf", "length = " + pkgs.length + ", packages = " + Arrays.toString(pkgs));
        }
    }

    @SuppressWarnings("deprecation")
    private void testRoundedDrawable() {
        try {
            final Drawable drawable = getResources().getDrawable(R.drawable.rounded_drawable);
            if (drawable != null) {
                Log.i("yf", drawable.toString());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void testInvokeAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Callable<Object>> tasks = new ArrayList<Callable<Object>>();
                for (int i = 0; i < 20; ++i) {
                    tasks.add(Executors.callable(new TaskRunnable()));
                }

                try {
                    MainApplication.sThreadPool.invokeAll(tasks);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static final class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(int countDownTime, long intervalMillis) {
            super(countDownTime, intervalMillis);
        }

        @Override
        protected void onFinish() {
            Log.i("yf", "onFinish");
        }

        @Override
        protected void onTick(int countDown) {
            Log.i("yf", "onTick - " + countDown);
        }
    }

    private final AtomicInteger mCount = new AtomicInteger();

    private class TaskRunnable implements Runnable {
        @Override
        public void run() {
            Log.i("yf", "count = " + mCount.incrementAndGet());
        }
    }

    public static void testReport() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                report(MainApplication.sInstance);
                Log.i("yf", "report complete");
            }
        }).start();
    }

    public static void report(Context context) {
        final ByteArrayBuffer crashInfos = new ByteArrayBuffer();
        final JsonWriter writer = new JsonWriter(new OutputStreamWriter(crashInfos));
        writer.setIndent("    ");

        final long now = System.currentTimeMillis();
        final CrashDatabase db = new CrashDatabase(context);
        final Cursor cursor = db.query(now);

        try {
            DebugUtils.startMethodTracing();
            CrashDatabase.writeTo(writer, cursor);
            FileUtils.close(writer);
            DebugUtils.stopMethodTracing("yf", "queryCrashInfos count = " + cursor.getCount());

            if (crashInfos != null && report(crashInfos)) {
                crashInfos.dump(new LogPrinter(Log.DEBUG, "yf"));
                db.delete(now);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            db.close();
            FileUtils.close(cursor);
        }
    }

    private static boolean report(ByteArrayBuffer crashInfos) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream("/sdcard/crash.json");
            crashInfos.writeTo(os);
            return true;
        } finally {
            FileUtils.close(os);
        }
    }

    public void testGif() {
        final Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.bbb, opts);
        Log.i("yf", "width = " + opts.outWidth + ", height = " + opts.outHeight + ", mimeType = " + opts.outMimeType);
        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bbb, opts);
        BitmapUtils.dumpBitmap(this, "yf", bitmap);
    }

    public void testPools() {
        Pool<Rect> pool = Pools.newPool(new Factory<Rect>() {
            @Override
            public Rect newInstance() {
                return new Rect();
            }
        }, 8);

        Log.i("yf", pool.toString());
        final Rect rect = pool.obtain();
        pool.recycle(rect);
        Log.i("yf", pool.toString());

        Pool<Matrix> pm = Pools.synchronizedPool(Pools.newPool(new Factory<Matrix>() {
            @Override
            public Matrix newInstance() {
                return new Matrix();
            }
        }, 6));
        Log.i("yf", pm.toString());
        final Matrix m = pm.obtain();
        pm.recycle(m);
        Log.i("yf", pm.toString());
    }

    public static String initHmacSHA1Key() throws Exception {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA1");
        final SecretKey secretKey = keyGenerator.generateKey();
        Log.i("yf", "format = " + secretKey.getFormat());

        return StringUtils.toHexString(secretKey.getEncoded());
    }

    // public static String getChipType(Context context) {
    // final MemoryInfo info = new MemoryInfo();
    // ((ActivityManager)context.getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(info);
    // return (info.totalMem < 966367641 ? "338_cvte" : "638_cvte");
    // }

    private static void dumpClass(Class<?> clazz, int step) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < step; ++i) {
            builder.append("  ");
        }

        Log.i("yf", builder.toString() + clazz.getName());
        final Class<?>[] classes = clazz.getDeclaredClasses();
        for (int i = 0, size = ArrayUtils.getSize(classes); i < size; ++i) {
            dumpClass(classes[i], step + 1);
        }
    }

    private static void testLinkedList() {
        LinkedList<Integer> list = new LinkedList<>();

        add(list, 5);
        add(list, 6);
        add(list, 7);
        add(list, 7);
        add(list, 1);
        add(list, 4);
        add(list, 0);

        Random random = new Random();
        for (int i = 0; i < 20; ++i) {
            add(list, random.nextInt(1000));
        }
    }

    private static void add(LinkedList<Integer> list, int key) {
        final Comparator<Integer> comparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer one, Integer another) {
                return (one - another);
            }
        };

//        ArrayUtils.insert(list, key, comparator);
        Log.i("yf", list.toString());

        for (int i = 1; i < list.size(); ++i) {
            final Integer curr = list.get(i);
            final Integer prev = list.get(i - 1);
            if (curr < prev) {
                throw new IllegalStateException("current = " + curr + ", prev = " + prev);
            }
        }
    }

    private void testDrawable() {
        // indicator_code_lock_point_area_red
        // indicator_code_lock_point_area_green
        @SuppressWarnings("deprecation")
        BitmapDrawable drawable = (BitmapDrawable)getResources().getDrawable(R.drawable.indicator_code_lock_point_area_red);
        Bitmap bitmap = drawable.getBitmap();
        BitmapUtils.dumpBitmap(this, "yf", bitmap);
        Log.i("yf", "width = " + drawable.getIntrinsicWidth() + ", height = " + drawable.getIntrinsicHeight());

        // final Resources res = getResources();
        // Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.indicator_code_lock_point_area_green);
        // RoundedBitmapDrawable drawable = new RoundedBitmapDrawable(bitmap);
        // drawable.setCornerRadius(20.0f);
        // BitmapUtils.dumpBitmap(this, "yf", bitmap);
        // Log.i("yf", "width = " + drawable.getIntrinsicWidth() + ", height = " + drawable.getIntrinsicHeight());
        //
        // GIFDrawable gd = GIFDrawable.decodeResource(res, R.drawable.movie);
        // GIFImage gi = gd.getImage();
        // Log.i("yf", "width = " + gi.getWidth() + ", height = " + gi.getHeight());
        // Log.i("yf", "width = " + gd.getIntrinsicWidth() + ", height = " + gd.getIntrinsicHeight());
    }

    private void testmkdirs(String path) {
        DebugUtils.startMethodTracing();
        FileUtils.mkdirs(path, FileUtils.FLAG_IGNORE_FILENAME);
        DebugUtils.stopMethodTracing("yf", "FileUtils.mkdirs", 'u');
    }

    private void testmkdirs(File file) {
        DebugUtils.startMethodTracing();
        final File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        DebugUtils.stopMethodTracing("yf", "File.mkdirs", 'u');
    }

    private static void testScanFiles() {
        MainApplication.sThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                DebugUtils.startMethodTracing();
                final FileScanCallback callback = new FileScanCallback();
                int errno = FileUtils.scanFiles(Environment.getExternalStorageDirectory().getPath(), callback, FileUtils.FLAG_SCAN_FOR_DESCENDENTS, null);
                Log.i("yf", "scanFiles errno = " + errno + ", fileCount = " + callback.fileCount);
                DebugUtils.stopMethodTracing("yf", "scanFiles");
            }
        });
    }

    static final class FileScanCallback implements ScanCallback {
        public int fileCount;

        @Override
        public int onScanFile(String path, int type, Object userData) {
            if (type == Dirent.DT_REG) {
                if (compareName(path, ".nodata")) {
                    return SC_STOP;
                }

                ++fileCount;
            }

            return SC_CONTINUE;
        }

        private static boolean compareName(String path, String name) {
            final int index = FileUtils.findFileName(path);
            return (index >= 0 && name.regionMatches(0, path, index, name.length()));
        }
    }

    private static final XmlResourceInflater<Drawable> sDrawableInflater = new XmlResourceInflater<Drawable>() {
        @Override
        public Drawable inflate(Context context, XmlPullParser parser) throws XmlPullParserException, ReflectiveOperationException {
            String name = parser.getName();
            if (name.equals("drawable") && (name = parser.getAttributeValue(null, "class")) == null) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": The <drawable> tag requires a valid 'class' attribute");
            }

            final Drawable drawable = (Drawable)Class.forName(name).newInstance();
            try {
                drawable.inflate(context.getResources(), parser, Xml.asAttributeSet(parser));
            } catch (IOException e) {
                throw new InflateException("Error inflating class - " + name, e);
            }

            return drawable;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabase2.close();
        if (mReceiver != null) {
            mReceiver.unregister(this);
            mDatabase.close();
        }
    }

    @Override
    public void onBackPressed() {
        if (mImageView.getVisibility() == View.GONE) {
            super.onBackPressed();
        } else {
            mImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            MainApplication.sInstance.resume(R.xml.image_loader);
        } else {
            MainApplication.sInstance.pause(R.xml.image_loader);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
        case 0:
            final Intent intent = new Intent(this, RecyclerActivity.class);
            ActivityOptionsCompat opts = 
                    ActivityOptionsCompat.makeScaleUpAnimation(mListView, mListView.getWidth() / 2, mListView.getHeight() / 2, 0, 0);
//                    ActivityOptionsCompat.makeThumbnailScaleUpAnimation(mListView, BitmapFactory.decodeResource(getResources(), R.drawable.tool_local), mListView.getWidth() / 2, mListView.getHeight() / 2);
            ActivityCompat.startActivity(this, intent, opts.toBundle());
//            startActivity(intent);
            break;

        case 1:
//             throw new NullPointerException("This is test!");
            startActivity(new Intent(this, RecyclerViewActivity.class));
//            PackageUtils.installPackage(this, new File("/sdcard/apks/vst_4.0.5_dangbei.apk"), "com.tencent.test.fileprovider");
            break;

        case 2:
            startActivity(new Intent(this, PackageActivity.class));
            break;

        case 3:
            startActivity(new Intent(this, PrinterActivity.class));
            break;

        case 4:
            TestService.stopService(this, 101);
            break;

        case 5:
            TestService.stopService(this, 1);
            break;
        }

        view.findViewById(R.id.text).setSelected(true);
    }

    private final class ImageAdapter extends BaseListAdapter<String> {
        public ImageAdapter() {
            super(Arrays.asList(MainApplication.obtainUrls()));
        }

        private void setCornerRadius(int index, int cornerRadius) {
            final View view = mListView.getChildAt(index);
            if (view != null) {
                final ImageView image = (ImageView)view.findViewById(R.id.image);
                final Drawable drawable = image.getDrawable();
                if (drawable instanceof RoundedBitmapDrawable) {
                    ((RoundedBitmapDrawable)drawable).setCornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius);
                }
            }
        }

        private void setCornerRadius(int index) {
            final View view = mListView.getChildAt(index);
            if (view != null) {
                final ImageView image = (ImageView)view.findViewById(R.id.image);
                final Drawable drawable = image.getDrawable();
                if (drawable instanceof RoundedBitmapDrawable) {
                    ((RoundedBitmapDrawable)drawable).setCornerRadii(null);
                }
            }
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return View.inflate(ImageActivity.this, R.layout.image_item, null);
        }

        @Override
        protected void bindView(String itemData, int position, View view) {
            final TextView text = (TextView)view.findViewById(R.id.text);
            text.setText(itemData);

            final ImageView image = (ImageView)view.findViewById(R.id.image);
//            if (position == 1) {
//                final String uri = UriUtils.getResourceUri(ImageActivity.this, "drawable/more_bg");
//                mImageLoader.load(uri)
//                    .skipMemory()
//                    .placeholder(R.drawable.ic_placeholder)
//                    .into(image);
//            } else if (position == 2) {
//                final String uri = UriUtils.getResourceUri(ImageActivity.this, R.drawable.more_bg);
//                mImageLoader.load(uri)
//                    .skipMemory()
//                    .placeholder(R.drawable.ic_placeholder)
//                    .into(image);
//            } else {
                MainApplication.sInstance.load(R.xml.image_loader, itemData)
//                    .flags(ImageLoader.FLAG_DUMP_OPTIONS)
//                    .parameters(R.xml.size_params)
//                    .binder(R.xml.ring_bitmap_binder)
                    .binder(R.xml.oval_transition_binder)
                    .placeholder(R.drawable.ic_placeholder)
//                    .binder(R.xml.image_binder)
//                    .preload();
                    .into(image);
//            }
        }
    }

    private static final class LoadingDialog extends Dialog implements OnCancelListener {
        public LoadingDialog(Activity activity) {
            super(activity, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
            setOwnerActivity(activity);
            setOnCancelListener(this);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.loading);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            getOwnerActivity().finish();
        }
    }

    private static final class LoadingTask extends AsyncTask<Object, Object, Object> {
        private final WeakReference<ImageActivity> mActivity;

        public LoadingTask(ImageActivity activity) {
            mActivity = new WeakReference<ImageActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Object result) {
            final ImageActivity activity = mActivity.get();
            if (activity != null && !activity.isDestroyed()) {
            }
        }

        @Override
        protected Object doInBackground(Object... params) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                final Thread thread = Thread.currentThread();
                final String name = thread.getName();
                Log.e("yf", name + " - interrupted = " + Thread.interrupted() + ", isInterrupted = " + thread.isInterrupted(), e);
            }

            return null;
        }
    }

    @SuppressLint("NewApi")
    private static void testForkJoin() {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        // 生成一个计算任务，负责计算1 .... 1000
        CountTask task = new CountTask(1, 1000);

        // 执行一个任务
        Future<Integer> result = forkJoinPool.submit(task);
        try {
            Log.i("yf", "result = " + result.get());
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
    }

    @SuppressLint("NewApi")
    public static final class CountTask extends RecursiveTask<Integer> {
        private final int startNumber;
        private final int endNumber;

        public CountTask(int startNumber, int endNumber) {
            this.startNumber = startNumber;
            this.endNumber = endNumber;
        }

        @Override
        protected Integer compute() {
            int sum = 0;
            // 如果任务足够小就计算任务
            boolean canCompute = (endNumber - startNumber) <= 500;
            if (canCompute) {
                for (int i = startNumber; i <= endNumber; i++) {
                    sum += i;
                }
            } else {
                int middleNumber = (startNumber + endNumber) / 2;
                CountTask leftTask = new CountTask(startNumber, middleNumber);
                CountTask rightTask = new CountTask(middleNumber + 1, endNumber);

                // 执行子任务
                leftTask.fork();
                rightTask.fork();

                // 等待子任务执行完，并得到其结果
                int leftResult = leftTask.join();
                int rightResult = rightTask.join();

                // 合并子任务
                sum = leftResult + rightResult;
            }

            return sum;
        }
    }

    static final class JsonObject extends JSONObject {
        @Override
        public String toString() {
            return super.toString();
        }
    }
}
