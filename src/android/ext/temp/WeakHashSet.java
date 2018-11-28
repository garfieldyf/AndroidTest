package android.ext.temp;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Class WeakHashSet
 * @author Garfield
 */
public class WeakHashSet<E> extends AbstractSet<E> {
    private final Map<E, Object> map;

    /**
     * Constructs a new empty instance of <tt>WeakHashSet</tt>.
     * @see #WeakHashSet(int)
     * @see #WeakHashSet(int, float)
     * @see #WeakHashSet(Collection)
     */
    public WeakHashSet() {
        map = new WeakHashMap<E, Object>();
    }

    /**
     * Constructs a new instance of <tt>WeakHashSet</tt> with
     * the specified <em>capacity</em>.
     * @param capacity The initial capacity.
     * @see #WeakHashSet()
     * @see #WeakHashSet(int, float)
     * @see #WeakHashSet(Collection)
     */
    public WeakHashSet(int capacity) {
        map = new WeakHashMap<E, Object>(capacity);
    }

    /**
     * Constructs a new instance of <tt>WeakHashSet</tt> with
     * the specified <em>capacity</em> and <em>loadFactor</em>.
     * @param capacity The initial capacity.
     * @param loadFactor The initial load factor.
     * @see #WeakHashSet()
     * @see #WeakHashSet(int)
     * @see #WeakHashSet(Collection)
     */
    public WeakHashSet(int capacity, float loadFactor) {
        map = new WeakHashMap<E, Object>(capacity, loadFactor);
    }

    /**
     * Constructs a new instance of <tt>WeakHashSet</tt> with
     * the specified <em>collection</em>.
     * @param collection The collection to add.
     * @see #WeakHashSet()
     * @see #WeakHashSet(int)
     * @see #WeakHashSet(int, float)
     */
    public WeakHashSet(Collection<? extends E> collection) {
        map = new WeakHashMap<E, Object>();
        for (E element : collection) {
            map.put(element, this);
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean isEmpty() {
        return (map.size() == 0);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean add(E object) {
        return (map.put(object, this) == null);
    }

    @Override
    public boolean remove(Object object) {
        return (map.remove(object) != null);
    }

    @Override
    public boolean contains(Object object) {
        return map.containsKey(object);
    }

    @Override
    public Object[] toArray() {
        return copyTo(new Object[map.size()]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] contents) {
        final int size = map.size();
        if (contents.length < size) {
            contents = (T[])Array.newInstance(contents.getClass().getComponentType(), size);
        }

        if (copyTo(contents).length > size) {
            contents[size] = null;
        }

        return contents;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] copyTo(T[] contents) {
        final Iterator<E> itor = map.keySet().iterator();
        for (int i = 0; itor.hasNext(); ++i) {
            contents[i] = (T)itor.next();
        }

        return contents;
    }
}
