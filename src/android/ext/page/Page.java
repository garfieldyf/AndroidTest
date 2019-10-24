package android.ext.page;

/**
 * A <tt>Page</tt> is a collection used to adds the page data to the adapter.
 * @author Garfield
 */
public interface Page<E> {
    /**
     * Returns the total number of items in this page.
     * @return The total number of items in this page.
     */
    int getCount();

    /**
     * Returns the item at the specified <em>position</em> in this page.
     * @param position The position of the item.
     * @return The item at the specified <em>position</em>.
     * @see #setItem(int, E)
     */
    E getItem(int position);

    /**
     * Sets the item at the specified <em>position</em> in this page with
     * the specified <em>value</em>.
     * @param position The position of the item.
     * @param value The value to set.
     * @return The previous item at the specified <em>position</em>.
     * @see #getItem(int)
     */
    E setItem(int position, E value);
}
