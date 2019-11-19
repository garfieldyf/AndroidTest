package com.tencent.temp;

import android.ext.page.Page;
import android.ext.page.PagedList;
import android.ext.page.Pages;
import android.ext.widget.BaseAdapter;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView.ViewHolder;

public abstract class PagedListAdapter<E, VH extends ViewHolder> extends BaseAdapter<VH> {
    /* package */ final PagedList<E> mPagedList;

    public PagedListAdapter() {
        mPagedList = new PagedList<E>(8);
    }

    @Override
    public int getItemCount() {
        return mPagedList.size();
    }

    @Override
    public int getItemViewType(int position) {
        final long combinedPosition = mPagedList.getPageForPosition(position);
        final Page<E> page = mPagedList.getPage(Pages.getOriginalPage(combinedPosition));
        return page.getItemViewType(Pages.getOriginalPosition(combinedPosition));
    }

    public E getItem(int position) {
        return mPagedList.get(position);
    }

    public E setItem(int position, E value) {
        final E previous = mPagedList.set(position, value);
        postNotifyItemRangeChanged(position, 1, null);
        return previous;
    }

    public void addPage(Page<? extends E> page) {
        final int itemCount = Pages.getCount(page);
        if (itemCount > 0) {
            final int positionStart = mPagedList.size();
            mPagedList.addPage(page);
            notifyItemRangeInserted(positionStart, itemCount);
        }

        //addPage(mPagedList.getPageCount(), page);
    }

    public void addPage(int pageIndex, Page<? extends E> page) {
        final int itemCount = Pages.getCount(page);
        if (itemCount > 0) {
            final int positionStart;
            if (pageIndex == mPagedList.getPageCount()) {
                positionStart = mPagedList.size();
            } else {
                positionStart = mPagedList.getPositionForPage(pageIndex);
            }

            mPagedList.addPage(pageIndex, page);
            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void setPage(int pageIndex, Page<? extends E> page) {
        final int itemCount = Pages.getCount(page);
        if (itemCount > 0) {
            final int positionStart = mPagedList.getPositionForPage(pageIndex);
            final Page<E> oldPage = mPagedList.setPage(pageIndex, page);
            if (oldPage.getCount() != itemCount) {
                throw new AssertionError("Error!!! - This adapter's item count has been changed.");
            }

            postNotifyItemRangeChanged(positionStart, itemCount, null);
        }
    }

    public void removePage(int pageIndex) {
        final Page<E> oldPage = mPagedList.getPage(pageIndex);
        final int positionStart = mPagedList.removePage(pageIndex);
        postNotifyItemRangeRemoved(positionStart, oldPage.getCount());
    }

    public final SpanSizeLookup createSpanSizeLookup() {
        return new ItemSpanSizeLookup();
    }

    /* package */ final class ItemSpanSizeLookup extends SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            final long combinedPosition = mPagedList.getPageForPosition(position);
            final Page<E> page = mPagedList.getPage(Pages.getOriginalPage(combinedPosition));
            return page.getItemSpanSize(Pages.getOriginalPosition(combinedPosition));
        }
    }
}
