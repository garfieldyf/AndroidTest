package com.tencent.temp;

import android.arch.lifecycle.LiveData;
import com.tencent.temp.DatabaseLiveData.Result;

public final class DatabaseLiveData extends LiveData<Result> {
    public static final DatabaseLiveData sInstance = new DatabaseLiveData();

    /**
     * This class cannot be instantiated.
     */
    private DatabaseLiveData() {
    }

    @Override
    public void postValue(Result result) {
        super.postValue(result);
    }

    /**
     * @see #setValue(String, int, int)
     * @see #setValue(String, int, long)
     */
    @Override
    public void setValue(Result result) {
        super.setValue(result);
    }

    /**
     * Equivalent to calling <tt>setValue(new Result(table, statement, rowID))</tt>.
     * @param statement
     * @param rowID
     * @see #setValue(Result)
     * @see #setValue(String, int, int)
     */
    public void setValue(String table, int statement, long rowID) {
        super.setValue(new Result(table, statement, rowID));
    }

    /**
     * Equivalent to calling <tt>setValue(new Result(table, statement, rowsAffected))</tt>.
     * @param statement
     * @param rowsAffected
     * @see #setValue(Result)
     * @see #setValue(String, int, long)
     */
    public void setValue(String table, int statement, int rowsAffected) {
        super.setValue(new Result(table, statement, rowsAffected));
    }

    public static class Result {
        /**
         * The type of the SQL statement INSERT.
         */
        public static final int STATEMENT_INSERT = 1;

        /**
         * The type of the SQL statement UPDATE.
         */
        public static final int STATEMENT_UPDATE = 2;

        /**
         * The type of the SQL statement DELETE.
         */
        public static final int STATEMENT_DELETE = 3;

        /**
         * The type of the SQL statement REPLACE.
         */
        public static final int STATEMENT_REPLACE = 4;

        public final String table;
        public final int statement;
        protected final Object value;

        public Result(String table, int statement, long rowID) {
            this(table, statement, (Long)rowID);
        }

        public Result(String table, int statement, int rowsAffected) {
            this(table, statement, (Integer)rowsAffected);
        }

        protected Result(String table, int statement, Object value) {
            this.table = table;
            this.value = value;
            this.statement = statement;
        }

        public final long getID() {
            return (long)value;
        }

        public final int getRowsAffected() {
            return (int)value;
        }
    }
}
