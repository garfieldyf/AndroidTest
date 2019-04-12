package android.ext.page;

/**
 * A <tt>Page</tt> is a collection used to adds the page data to the adapter.
 * @author Garfield
 */
public interface Page<E> {
    /**
     * Returns the total number of items in this page.
     * @return The total number of items in this page.
     * @see #getItem(int)
     */
    int getCount();

    /**
     * Returns the item at the specified <em>position</em> in this page.
     * @param position The position of the item.
     * @return The item at the specified <em>position</em>, or <tt>null</tt>
     * if this page has no item at <em>position</em>.
     * @see #getCount()
     */
    E getItem(int position);
}