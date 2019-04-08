package android.ext.page;

import java.util.Collections;
import java.util.Map;
import android.ext.cache.Cache;
import android.util.ArrayMap;

/**
 * Class <tt>ResourcePageCache</tt> is an implementation of a {@link Cache}.
 * This cache is the <b>unlimited-size</b> and <b>not</b> thread-safely, but
 * it can be release the page resources when the page no longer be used.
 * @author Garfield
 */
public class ResourcePageCache<E> implements Cache<Integer, ResourcePage<E>> {
    private final ArrayMap<Integer, ResourcePage<E>> mCache;

    /**
     * Constructor
     */
    public ResourcePageCache() {
        mCache = new ArrayMap<Integer, ResourcePage<E>>();
    }

    @Override
    public ResourcePage<E> get(Integer key) {
        return mCache.get(key);
    }

    @Override
    public Map<Integer, ResourcePage<E>> entries() {
        return Collections.unmodifiableMap(mCache);
    }

    @Override
    public void clear() {
        final int size = mCache.size();
        if (size > 0) {
            for (int i = 0; i < size; ++i) {
                mCache.valueAt(i).close();
            }

            mCache.clear();
        }
    }

    @Override
    public ResourcePage<E> remove(Integer key) {
        final ResourcePage<E> result = mCache.remove(key);
        if (result != null) {
            result.close();
        }

        return result;
    }

    @Override
    public ResourcePage<E> put(Integer key, ResourcePage<E> value) {
        final ResourcePage<E> result = mCache.put(key, value);
        if (result != null) {
            result.close();
        }

        return result;
    }
}
