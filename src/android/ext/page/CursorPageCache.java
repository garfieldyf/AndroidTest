package android.ext.page;

import java.util.Collections;
import java.util.Map;
import android.ext.cache.Cache;
import android.util.ArrayMap;

/**
 * Class <tt>CursorPageCache</tt> is an implementation of a {@link Cache}.
 * This cache is the <b>unlimited-size</b> and <b>not</b> thread-safely.
 * @author Garfield
 */
public class CursorPageCache implements Cache<Integer, CursorPage> {
    private final ArrayMap<Integer, CursorPage> mCache;

    /**
     * Constructor
     */
    public CursorPageCache() {
        mCache = new ArrayMap<Integer, CursorPage>();
    }

    @Override
    public void clear() {
        final int size = mCache.size();
        if (size > 0) {
            for (int i = 0; i < size; ++i) {
                mCache.valueAt(i).mCursor.close();
            }

            mCache.clear();
        }
    }

    @Override
    public CursorPage remove(Integer page) {
        final CursorPage oldPage = mCache.remove(page);
        if (oldPage != null) {
            oldPage.mCursor.close();
        }

        return oldPage;
    }

    @Override
    public CursorPage get(Integer page) {
        return mCache.get(page);
    }

    @Override
    public CursorPage put(Integer page, CursorPage value) {
        final CursorPage oldPage = mCache.put(page, value);
        if (oldPage != null) {
            oldPage.mCursor.close();
        }

        return oldPage;
    }

    @Override
    public Map<Integer, CursorPage> entries() {
        return Collections.unmodifiableMap(mCache);
    }
}
