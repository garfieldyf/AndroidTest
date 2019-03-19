package android.ext.widget;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.widget.Filter;

/**
 * Class Filters
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public final class Filters {
    /**
     * Class <tt>CursorFilter</tt> is an implementation of a {@link Filter}.
     */
    public static final class CursorFilter extends Filter {
        private final CursorFilterClient mClient;

        /**
         * Constructor
         * @param client The {@link CursorFilterClient}.
         */
        public CursorFilter(CursorFilterClient client) {
            mClient = client;
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return mClient.convertToString((Cursor)resultValue);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final Cursor cursor = mClient.onPerformFiltering(constraint);
            final FilterResults results = new FilterResults();
            if (cursor != null) {
                results.values = cursor;
                results.count  = cursor.getCount();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mClient.changeCursor((Cursor)results.values);
        }
    }

    /**
     * Used for being notified when the {@link CursorFilter}
     * filters constrains the content of the adapter.
     */
    public static interface CursorFilterClient {
        /**
         * Changes the underlying cursor to a new cursor.
         * If there is an existing cursor it will be closed.
         * @param cursor The new cursor to be used.
         */
        void changeCursor(Cursor cursor);

        /**
         * Converts the cursor into a {@link CharSequence}.
         * @param cursor The cursor to convert to a <tt>CharSequence</tt>.
         * @return A CharSequence representing the value.
         * @see #onPerformFiltering(CharSequence)
         * @see Filter#convertResultToString(Object)
         */
        CharSequence convertToString(Cursor cursor);

        /**
         * Invoked in a worker thread to filter the data according to the constraint.
         * @param constraint The constraint with which the query must be filtered.
         * @return A <tt>Cursor</tt> representing the results of the new query.
         * @see #convertToString(Cursor)
         */
        Cursor onPerformFiltering(CharSequence constraint);
    }

    /**
     * Class <tt>ListFilter</tt> is an implementation of a {@link Filter}.
     */
    public static final class ListFilter<T> extends Filter {
        private final List<T> mData;
        private final ListFilterClient<T> mClient;

        /**
         * Constructor
         * @param data The data.
         * @param client The {@link CursorFilterClient}.
         */
        public ListFilter(List<T> data, ListFilterClient<T> client) {
            mClient = client;
            mData = new ArrayList<T>(data);
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return mClient.convertToString((T)resultValue);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final List<T> newData = mClient.onPerformFiltering(constraint, mData);
            final FilterResults results = new FilterResults();
            if (newData != null) {
                results.values = newData;
                results.count  = newData.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mClient.changeData((List<T>)results.values);
        }
    }

    /**
     * Used for being notified when the {@link ListFilter}
     * filters constrains the content of the adapter.
     */
    public static interface ListFilterClient<T> {
        /**
         * Changes the underlying data to a new data.
         * @param newData The new data to be used or
         * <tt>null</tt> to clear the underlying data.
         */
        void changeData(List<T> newData);

        /**
         * Converts the item into a {@link CharSequence}.
         * @param item The item to convert to a <tt>CharSequence</tt>.
         * @return A CharSequence representing the value.
         * @see Filter#convertResultToString(Object)
         * @see #onPerformFiltering(CharSequence, List)
         */
        CharSequence convertToString(T item);

        /**
         * Invoked in a worker thread to filter the data according to the constraint.
         * @param constraint The constraint used to filter.
         * @param originalData The original data used to filter.
         * @return A <tt>List</tt> representing the results of the filtering.
         * @see #convertToString(T)
         */
        List<T> onPerformFiltering(CharSequence constraint, List<T> originalData);
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Filters() {
    }
}
