package com.tencent.test;

import android.app.Activity;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.image.ImageLoader;
import android.ext.image.ImageLoader.ImageDecoder;
import android.os.Bundle;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.tencent.temp.BaseListAdapter;
import java.util.Arrays;

public class GIFImageActivity extends Activity {
    private ListView mListView;
    private ImageLoader<Object> mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gifimage_activity);

//        mImageLoader = MainApplication.sInstance.createImageLoader()
//                .setBinder(R.xml.gif_image_binder)
//                .create();

        mListView = (ListView)findViewById(R.id.gif_list);
        final GIFImageAdapter adapter = new GIFImageAdapter();
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        final Printer printer = new LogPrinter(Log.DEBUG, "aaaa");
        mImageLoader.dump(printer);
        mImageLoader.shutdown();
    }

    private final class GIFImageAdapter extends BaseListAdapter<String> implements OnItemClickListener {
        public GIFImageAdapter() {
            super(Arrays.asList(URLs));
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.git_item, parent, false);
        }

        @Override
        protected void bindView(String itemData, int position, View view) {
            ((TextView)view.findViewById(R.id.text)).setText(itemData);
            mImageLoader.load(itemData).into(view.findViewById(R.id.image));
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final ImageView imageView = (ImageView)parent.getChildAt(3).findViewById(R.id.image);
            final GIFDrawable drawable = (GIFDrawable)imageView.getDrawable();

            switch (position) {
            case 0:
                if (drawable != null) {
                    drawable.setVisible(true, true);
                }
                break;

            case 1:
                if (drawable != null) {
                    drawable.setVisible(true, false);
                }
                break;

            case 2:
                if (drawable != null) {
                    drawable.setVisible(false, false);
                }
                break;
            }
        }
    }

    public static final class GIFImageDecoder implements ImageDecoder<GIFImage> {
        private static final GIFImageDecoder sInstance = new GIFImageDecoder();

        @SuppressWarnings("unchecked")
        public static <Image> ImageDecoder<Image> getInstance() {
            return (ImageDecoder<Image>)sInstance;
        }

//        @Override
//        public GIFImage decodeImage(Object uri, LoadParams<Object> params, byte[] tempStorage) {
//            return GIFImage.decode(null, uri, tempStorage);
//        }

        @Override
        public GIFImage decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage) {
            return GIFImage.decode(null, uri, tempStorage);
        }
    }

    private static final String[] URLs = {
        "/sdcard/welcome.gif",
        "android.resource://com.tencent.test/raw/wind",
        "android.resource://com.tencent.test/raw/timg1",
        "android.resource://com.tencent.test/raw/timg2",
        "android.resource://com.tencent.test/raw/timg3",
        "android.resource://com.tencent.test/raw/timg",
        "android.resource://com.tencent.test/raw/king",
        "android.resource://com.tencent.test/raw/mis",
        "android.resource://com.tencent.test/raw/king",
        "android.resource://com.tencent.test/raw/aaa",
        "android.resource://com.tencent.test/raw/bbb",
        "android.resource://com.tencent.test/raw/ccc",
        "android.resource://com.tencent.test/raw/ddd",
        "android.resource://com.tencent.test/raw/icrn",
        "android.resource://com.tencent.test/raw/ken",
        "android.resource://com.tencent.test/raw/lei",
        "android.resource://com.tencent.test/raw/virus",
        "android.resource://com.tencent.test/raw/movie",
    };
}
