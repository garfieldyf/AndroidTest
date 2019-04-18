package android.ext.page;

import android.ext.cache.ArrayMapCache;
import android.ext.cache.Cache;

/**
 * Class <tt>ResourcePageCache</tt> is an implementation of a {@link Cache}.
 * This cache is the <b>unlimited-size</b> and <b>not</b> thread-safely, but
 * it can be release the page resources when the page no longer be used.
 * @see ResourcePage
 * @author Garfield
 */
public final class ResourcePageCache<E> extends ArrayMapCache<Integer, ResourcePage<E>> {
    @Override
    public void clear() {
        final int size = map.size();
        if (size > 0) {
            for (int i = 0; i < size; ++i) {
                map.valueAt(i).close();
            }

            map.clear();
        }
    }

    @Override
    public ResourcePage<E> remove(Integer key) {
        final ResourcePage<E> result = map.remove(key);
        if (result != null) {
            result.close();
        }

        return result;
    }

    @Override
    public ResourcePage<E> put(Integer key, ResourcePage<E> value) {
        final ResourcePage<E> result = map.put(key, value);
        if (result != null) {
            result.close();
        }

        return result;
    }
}
