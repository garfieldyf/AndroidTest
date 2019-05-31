package android.ext.temp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.util.SparseArray;

public class ListSection<T> {
    private int mItemCount;
    //private final List<T> mSections;
    private final List<Integer> mPositions;
    //private final List<T[]> mPageCache;

    public ListSection() {
        //mSections = new ArrayList<T>();
        mPositions = new ArrayList<Integer>();
    }

    public final int getItemCount() {
        return mItemCount;
    }

    public int getPositionForSection(int section) {
        return (section >= 0 && section < mPositions.size() ? mPositions.get(section) : -1);
    }

    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mItemCount) {
            return -1;
        }

        final int index = Collections.binarySearch(mPositions, position);
        return (index >= 0 ? index : -index - 2);
    }

    public int getSectionPositionForPosition(int position) {
        final int section = getSectionForPosition(position);
        if (section >= 0) {
            position -= mPositions.get(section);
        }

        return position;
    }

    public void add(T section, int count) {
        mPositions.add(mItemCount);
        //mSections.add(section);
        mItemCount += count;
    }
}
