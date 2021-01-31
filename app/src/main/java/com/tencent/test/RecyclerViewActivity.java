package com.tencent.test;

import android.app.Activity;
import android.content.Context;
import android.ext.content.AsyncTask;
import android.ext.content.Task;
import android.ext.image.ImageModule;
import android.ext.widget.LayoutManagerHelper;
import android.ext.widget.LayoutManagerHelper.MarginItemDecoration;
import android.ext.widget.PageAdapter2;
import android.ext.widget.ViewUtils;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Printer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Arrays;
import java.util.List;

public class RecyclerViewActivity extends Activity {
    private static final int ITEM_TYPE_TITLE = 0;
    private static final int ITEM_TYPE_IMAGE = 1;
    private static final int ITEM_TYPE_ITEM  = 2;

    private static final int MAX_PAGE_SIZE = 4;
    private static final int PAGE_SIZE = 64;
    private static final int INITIAL_SIZE = 32;
    private static final int PREFETCH_DISTANCE = 6;

    /* package */ List<String> mData;
    /* package */ RecyclerAdapter mAdapter;
    /* package */ RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_recycler_view);

        mData = Arrays.asList(MainApplication.obtainUrls());
        mRecyclerView = (RecyclerView)findViewById(R.id.content);
        final GridLayoutManager layoutManager = new ItemGridLayoutManager(this, 6, GridLayoutManager.VERTICAL);
        layoutManager.setSpanSizeLookup(new ItemSpanSizeLookup());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(null);
        mRecyclerView.addItemDecoration(new MarginItemDecoration(20, 20, 20, 20));
        mRecyclerView.addOnScrollListener(mListener);
        LayoutManagerHelper.setChildDrawingOrderCallback(mRecyclerView);
        mAdapter.setItemCount(mData.size());
//        mAdapter.setItemCount(28);
        //LayoutManagerHelper.requestChildFocus(layoutManager, 3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        final Printer printer = new LogPrinter(Log.ERROR, "yuanfeng");
        mAdapter.dump(printer);
    }

    private final OnScrollListener mListener = new OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                ImageModule.getInstance(RecyclerViewActivity.this).pause(R.xml.image_loader);
            } else {
                ImageModule.getInstance(RecyclerViewActivity.this).resume(R.xml.image_loader);
            }
        }
    };

    private final class RecyclerAdapter extends PageAdapter2<String, BaseHolder> {
        public RecyclerAdapter() {
            super(new Config.Builder()
                .setPageSize(PAGE_SIZE)
                .setInitialSize(INITIAL_SIZE)
                .setMaximumPageCount(MAX_PAGE_SIZE)
                .setPrefetchDistance(PREFETCH_DISTANCE)
                .build());
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return ITEM_TYPE_TITLE;
            } else if (position > 0 && position < 4) {
                return ITEM_TYPE_IMAGE;
            } else {
                return ITEM_TYPE_ITEM;
            }
        }

        @Override
        public BaseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final LayoutInflater inflater = getLayoutInflater();
            final BaseHolder holder;
            switch (viewType) {
            case ITEM_TYPE_TITLE:
                holder = new TitleHolder(inflater.inflate(R.layout.recycler_title_item, parent, false));
                break;

            case ITEM_TYPE_IMAGE:
                holder = new ImageHolder(inflater.inflate(R.layout.recycler_image_item, parent, false));
                break;

            default:
                holder = new ItemHolder(inflater.inflate(R.layout.recycler_item_item, parent, false));
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(BaseHolder holder, int position) {
            holder.bindValue(RecyclerViewActivity.this, position, getItem(position));
        }

        @Override
        public List<String> loadPage(Task task, int page, int startPosition, int loadSize) {
            //Log.i("PageAdapter", "loadPage - page = " + page + ", startPosition = " + startPosition + ", loadSize = " + loadSize);
//            new LoadTask(RecyclerViewActivity.this, page).executeOnExecutor(MainApplication.sThreadPool, startPosition, loadSize);
//            return null;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }

            return mData.subList(startPosition, startPosition + loadSize);
        }
    }

    private static final class LoadTask extends AsyncTask<Integer, Integer, List<String>> {
        private int page;

        public LoadTask(RecyclerViewActivity activity, int page) {
            super(activity);
            this.page = page;
        }

        @Override
        protected List<String> doInBackground(Integer[] params) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }

            final RecyclerViewActivity activity = getOwner();
            return (activity != null ? activity.mData.subList(params[0], params[0] + params[1]) : null);
        }

        @Override
        protected void onPostExecute(Integer[] params, List<String> result) {
            final RecyclerViewActivity activity = getOwner();
            if (activity != null) {
                activity.mAdapter.setPage(page, result);
            }
        }
    }

//    private static final class DataPage implements Page<String> {
//        private final String[] mData;
//        private final int mOffset;
//        private final int mCount;
//
//        public DataPage(String[] data, int offset, int count) {
//            mData = data;
//            mOffset = offset;
//            mCount = count;
//        }
//
//        @Override
//        public int getCount() {
//            return mCount;
//        }
//
//        @Override
//        public String getItem(int position) {
//            return mData[position + mOffset];
//        }
//
//        @Override
//        public String setItem(int position, String value) {
//            return null;
//        }
//    }

    private static class BaseHolder extends ViewHolder implements OnFocusChangeListener {
        public BaseHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
        }

        public void bindValue(RecyclerViewActivity activity, int position, String itemData) {
        }
    }

    private static final class TitleHolder extends BaseHolder {
        public TitleHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bindValue(RecyclerViewActivity activity, int position, String itemData) {
            ((TextView)itemView).setText("我的应用");
        }
    }

    private static final class ImageHolder extends BaseHolder {
        public ImageHolder(View itemView) {
            super(itemView);
            itemView.setOnFocusChangeListener(this);
        }

        @Override
        public void bindValue(RecyclerViewActivity activity, int position, String itemData) {
            ImageModule.getInstance(activity).load(R.xml.image_loader, itemData)
                .binder(R.xml.rounded_transition_binder)
//                .parameters(R.xml.size_params)
                .placeholder(R.drawable.ic_placeholder)
                .into(itemView);
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            ViewUtils.startAnimation(view, (hasFocus ? R.animator.home_item_zoom_in : R.animator.home_item_zoom_out), hasFocus);
        }
    }

    private static final class ItemHolder extends BaseHolder {
        private final TextView title;
        private final ImageView icon;

        public ItemHolder(View itemView) {
            super(itemView);

            itemView.setOnFocusChangeListener(this);
            this.icon  = (ImageView)itemView.findViewById(R.id.icon);
            this.title = (TextView)itemView.findViewById(R.id.title);
        }

        @Override
        public void bindValue(RecyclerViewActivity activity, int position, String itemData) {
//            title.setText(itemData);
            title.setText(Integer.toString(position));
            ImageModule.getInstance(activity)
                .load(R.xml.image_loader, itemData)
                .binder(R.xml.rounded_transition_binder)
                .parameters(R.xml.size_params)
                .placeholder(R.drawable.ic_placeholder)
                .into(icon);
        }

        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            ViewUtils.startAnimation(view, (hasFocus ? R.animator.home_item_zoom_in : R.animator.home_item_zoom_out), false);
        }
    }

    /* package */ final class ItemSpanSizeLookup extends SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            switch (mAdapter.getItemViewType(position)) {
            case ITEM_TYPE_TITLE:
                return 6;

            case ITEM_TYPE_IMAGE:
                return 2;

            default:
                return 1;
            }
        }
    }

    private static final class ItemGridLayoutManager extends GridLayoutManager {
        public ItemGridLayoutManager(Context context, int spanCount, int orientation) {
            super(context, spanCount, orientation, false);
        }

        @Override
        public View onFocusSearchFailed(View focused, int focusDirection, Recycler recycler, State state) {
            switch (focusDirection) {
            case View.FOCUS_LEFT:
                return onFocusSearchFailed(focused, -1);

            case View.FOCUS_RIGHT:
                return onFocusSearchFailed(focused, 1);

            default:
                return focused;
            }
        }

        @Override
        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
            return LayoutManagerHelper.scrollVertically(parent, child, rect, 0, immediate);
        }

        private View onFocusSearchFailed(View focused, int offset) {
            final int position = getPosition(focused);
            if (position != RecyclerView.NO_POSITION) {
                final View child = findViewByPosition(position + offset);
                if (child != null && child.isFocusable()) {
                    child.requestFocus();
                    return child;
                }
            }

            return focused;
        }
    }
}
