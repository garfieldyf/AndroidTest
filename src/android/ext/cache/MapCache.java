package android.ext.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.util.Printer;

/**
 * Class <tt>MapCache</tt> is an implementation of a {@link Cache}.
 * This cache is the <b>unlimited-size</b>.
 * @author Garfield
 */
public class MapCache<K, V> implements Cache<K, V> {
    protected final Map<K, V> map;

    /**
     * Constructor
     * @param map The {@link Map}.
     */
    public MapCache(Map<K, V> map) {
        DebugUtils.__checkError(map == null, "map == null");
        this.map = map;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public V remove(K key) {
        return map.remove(key);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public Map<K, V> entries() {
        return Collections.unmodifiableMap(map);
    }

    /* package */ final void dump(Context context, Printer printer) {
        final StringBuilder result = new StringBuilder(256);
        final int size = map.size();
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %sCache [ size = %d ] ", map.getClass().getSimpleName(), size);
        for (Entry<K, V> entry : map.entrySet()) {
            result.setLength(0);
            printer.println(result.append("  ").append(entry.getKey()).append(" ==> ").append(entry.getValue()).toString());
        }
    }
}
