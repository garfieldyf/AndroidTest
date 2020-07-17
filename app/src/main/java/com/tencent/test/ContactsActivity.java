package com.tencent.test;

import android.app.Activity;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.ext.database.AsyncQueryHandler;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.TextView;

public class ContactsActivity extends Activity {
    private final String[] mProjection = { Contacts._ID, Contacts.DISPLAY_NAME, };

    private RecyclerView mRecyclerView;
    private ContactsLoader mContactsLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler);

        mRecyclerView = (RecyclerView)findViewById(R.id.images);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mContactsLoader = new ContactsLoader(this);
        mContactsLoader.startQuery(0, Contacts.CONTENT_URI, mProjection, null, null, Contacts._ID + " DESC");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onAdd(View view) {
        final ContentValues values = new ContentValues();
        final long id = ContentUris.parseId(getContentResolver().insert(RawContacts.CONTENT_URI, values));

        values.put(Data.RAW_CONTACT_ID, id);
        values.put(Data.MIMETYPE, "vnd.android.cursor.item/name");
        values.put(Data.DATA2, "aaaa");
        getContentResolver().insert(Data.CONTENT_URI, values);
    }

    private final class ContactsLoader extends AsyncQueryHandler {
        public ContactsLoader(Context context) {
            super(context, MainApplication.sThreadPool.createSerialExecutor());
        }

        @Override
        protected Object onExecute(ContentResolver resolver, int token, Object[] params) {
            return super.onExecute(resolver, token, params);
        }

        @Override
        protected void onCallComplete(int token, Bundle result) {
            super.onCallComplete(token, result);
        }

        @Override
        protected void onInsertComplete(int token, Uri newUri) {
            super.onInsertComplete(token, newUri);
        }

        @Override
        protected void onBulkInsertComplete(int token, int newRows) {
            super.onBulkInsertComplete(token, newRows);
        }

        @Override
        protected void onApplyBatchComplete(int token, ContentProviderResult[] results) {
            super.onApplyBatchComplete(token, results);
        }

        @Override
        protected void onExecuteComplete(int token, Object result) {
            super.onExecuteComplete(token, result);
        }

        @Override
        protected void onQueryComplete(int token, Cursor cursor) {
            super.onQueryComplete(token, cursor);
        }

        @Override
        protected void onUpdateComplete(int token, int rowsAffected) {
            super.onUpdateComplete(token, rowsAffected);
        }

        @Override
        protected void onDeleteComplete(int token, int rowsAffected) {
            super.onDeleteComplete(token, rowsAffected);
        }
    }

    private static final class ContactViewHolder extends ViewHolder {
        private final TextView title;
        private final TextView content;

        public ContactViewHolder(View itemView) {
            super(itemView);

            title = (TextView)itemView.findViewById(android.R.id.text1);
            content = (TextView)itemView.findViewById(android.R.id.text2);
        }

        private void bindValue(Cursor cursor) {
            title.setText(cursor.getString(0));
            content.setText(cursor.getString(1));
        }
    }
}
