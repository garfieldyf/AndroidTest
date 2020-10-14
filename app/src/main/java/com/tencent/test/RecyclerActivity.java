package com.tencent.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.ext.image.ImageModule;
import android.ext.widget.LayoutManagerHelper;
import android.ext.widget.LayoutManagerHelper.MarginItemDecoration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PageGridLayoutManager;
import android.support.v7.widget.PageScroller.OnPageChangeListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.tencent.temp.RecyclerAdapter;
import java.util.Arrays;
import java.util.List;

public class RecyclerActivity extends Activity implements OnPageChangeListener {
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private PageGridLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler);

        mRecyclerView = (RecyclerView)findViewById(R.id.images);
        mLayoutManager = new PageGridLayoutManager(this, 2, LinearLayoutManager.HORIZONTAL, false, 1660);
        mLayoutManager.setOnPageChangeListener(this);

        mAdapter = new ImageAdapter(mRecyclerView);
        mRecyclerView.addItemDecoration(new MarginItemDecoration(20, 20, 20, 20));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        LayoutManagerHelper.setChildDrawingOrderCallback(mRecyclerView);

//        final Intent intent = getIntent();
//        if (intent != null) {
//            final Dirent dirent = intent.getParcelableExtra("dirent");
//            if (dirent != null) {
//                dirent.dump(new LogPrinter(Log.INFO, "yf"));
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onPageChanged(RecyclerView recyclerView, int newPage, int oldPage) {
        Log.i("yf", "oldPage = " + oldPage + ", newPage = " + newPage + ", currentPage = " + mLayoutManager.getCurrentPage());
    }

    public void onScrollToPage(View view) {
//        mLayoutManager.scrollToPage(2, true);
        mLayoutManager.requestItemFocus(24, 12);
    }

    public void onScrollPrevPage(View view) {
//        mLayoutManager.scrollToPrevPage(false);
        mLayoutManager.requestItemFocus(5, 12);
    }

    public void onScrollNextPage(View view) {
//        mLayoutManager.scrollToNextPage(false);
        mLayoutManager.requestItemFocus(20, 12);
    }

    public void onRemove(View view) {
        mLayoutManager.requestItemFocus(12, 12);

//        View focused = mRecyclerView.getFocusedChild();
//        Log.i("yf", "RecyclerView getFocusedChild() focused view = " + (focused != null ? focused.toString() : "null"));
//
//        focused = mRecyclerView.getLayoutManager().getFocusedChild();
//        Log.i("yf", "LayoutManager getFocusedChild() focused view = " + (focused != null ? focused.toString() : "null"));
    }

    private final class ImageAdapter extends RecyclerAdapter<ItemViewHolder> {
        private final List<String> mUrls;

        public ImageAdapter(RecyclerView view) {
            super(view, FLAG_ITEM_FOCUSABLE);
            mUrls = Arrays.asList(MainApplication.obtainUrls());
            Log.d("LruFileCache", "count = " + mUrls.size());
        }

        @Override
        public int getItemCount() {
            return mUrls.size();
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.text.setText(Integer.toString(position));
            ImageModule.with(holder.itemView.getContext()).load(R.xml.image_loader, mUrls.get(position))
                .binder(R.xml.transition_binder)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.image);
        }

        @Override
        @SuppressLint("NewApi")
        protected ItemViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            final ItemViewHolder holder = new ItemViewHolder(inflater.inflate(R.layout.recycler_item, parent, false));
            //holder.image.setClipToOutline(true);
            return holder;
        }
    }

    private static final class ItemViewHolder extends ViewHolder {
        private final TextView text;
        private final ImageView image;

        public ItemViewHolder(View itemView) {
            super(itemView);
            text = (TextView)itemView.findViewById(R.id.text);
            image = (ImageView)itemView.findViewById(R.id.image);
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
        "android.resource://tv.fun.appstore/drawable/pkg_icon_game",
        "android.resource://com.tencent.test/raw/abc",
        "android.resource://com.tencent.test/raw/cover",
        "http://img.funshion.com/pictures/ott/img/34750a/8038da/poster_1A2KAUEJO3EFA.png",
        "http://img.funshion.com/pictures/ott/img/53391f/8f14e4/poster_1A3E0HTJ7CMIK.png",
        "http://img.funshion.com/pictures/ott/img/4e72de/072b03/poster_1AHG0S3HBRTJ9.png",
        "http://img.funshion.com/pictures/ott/img/5249b1/c81e72/poster_1A30T00G2J64M.png",
        "http://img.funshion.com/pictures/ott/img/232571/3c59dc/poster_1A2K23P5L98PV.png",
        "http://img.funshion.com/pictures/ott/img/12b4ec/a342e7/poster_1A5DKTP676EHG.png",
        "http://img.funshion.com/pictures/ott/img/952a5e/c81e72/poster_1A9CJBC0AL7T7.jpg",
        "http://img.funshion.com/pictures/ott/img/e36576/c9f0f8/poster_1A3QQQQBJAUEU.png",
        "http://img.funshion.com/pictures/ott/img/7df4ea/38b3ef/poster_1A2K3A8CCCOA5.png",
        "http://img.funshion.com/pictures/ott/img/d82571/e4da3b/poster_1A9JJNT13RJ20.jpg",
        "http://img.funshion.com/pictures/ott/img/eba4f7/c81e72/poster_1AAG7A7ESPEDL.png",
        "http://img.funshion.com/pictures/ott/img/d5a42b/c81e72/poster_1A365DH4937JC.png",
        "http://img.funshion.com/pictures/ott/img/3ecf3b/f89913/poster_1A2K31HAF3I1G.png",
        "http://img.funshion.com/pictures/ott/img/b20caa/f89913/poster_1A3OAS4VA9FUB.png",
        "http://img.funshion.com/pictures/ott/img/cbab96/82a008/poster_1A9UC9DRJD4IK.png",
        "http://img.funshion.com/pictures/ott/img/a23e27/c4ca42/poster_1A334R5OL6DHA.png",
        "http://img.funshion.com/pictures/ott/img/351587/9cdf26/poster_1A2K2VN1EPJKR.png",
        "http://img.funshion.com/pictures/ott/img/898e35/c81e72/poster_1A35HJPB7IM88.png",
        "http://img0.imgtn.bdimg.com/it/u=1515023394,2581396170&fm=206&gp=0.jpg",
        "http://img4.imgtn.bdimg.com/it/u=2906525411,1514739009&fm=206&gp=0.jpg",
        "http://img2.imgtn.bdimg.com/it/u=1843525712,602110404&fm=206&gp=0.jpg",
        "http://img4.imgtn.bdimg.com/it/u=4073909306,3941810796&fm=206&gp=0.jpg",
    };
}
