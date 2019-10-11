package android.ext.temp;

import android.ext.page.Page;
import android.ext.page.PagedArray;
import android.ext.page.Pages;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

public abstract class PagedArrayAdapter<E, VH extends ViewHolder> extends Adapter<VH> {
    private final PagedArray<E> mPages;

    public PagedArrayAdapter(int capacity) {
        mPages = new PagedArray<E>(capacity);
    }

    @Override
    public int getItemCount() {
        return mPages.getItemCount();
    }

    public void addPage(Page<? extends E> page) {
//        final int itemCount = Pages.getCount(page);
//        if (itemCount > 0) {
//            final int positionStart = mPages.getItemCount();
//            mPages.addPage(page);
//            notifyItemRangeInserted(positionStart, itemCount);
//        }

        addPage(mPages.getItemCount(), page);
    }

    public void addPage(int pageIndex, Page<? extends E> page) {
        final int itemCount = Pages.getCount(page);
        if (itemCount > 0) {
            final int positionStart = mPages.getPositionForPage(pageIndex);
            mPages.addPage(pageIndex, page);
            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void setPage(int pageIndex, Page<? extends E> page) {
        final int itemCount = Pages.getCount(page);
        if (itemCount > 0) {
            final int positionStart = mPages.getPositionForPage(pageIndex);
            final Page<E> oldPage = mPages.setPage(pageIndex, page);
            if (oldPage.getCount() != itemCount) {
                throw new AssertionError("Error!!! - This adapter's item count has been changed.");
            }

            notifyItemRangeChanged(positionStart, itemCount);
        }
    }

    public void removePage(int pageIndex) {
        final Page<E> oldPage = mPages.getPage(pageIndex);
        final int positionStart = mPages.removePage(pageIndex);
        notifyItemRangeRemoved(positionStart, oldPage.getCount());
    }
}
