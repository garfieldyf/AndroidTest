package com.tencent.test;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

public final class BlackList {
    public static final Uri CONTENT_URI = Uri.parse("content://tv.fun.children.blacklist/blacklist");

    @SuppressWarnings("deprecation")
    public static Uri insert(Context context, String mediaId, String action, String name, String still) {
        final ContentProviderClient client = context.getContentResolver().acquireUnstableContentProviderClient(CONTENT_URI);
        if (client != null) {
            try {
                final ContentValues values = new ContentValues();
                values.put("media_id", mediaId);
                values.put("name", name);
                values.put("still", still);
                values.put("action", action);
                return client.insert(CONTENT_URI, values);
            } catch (RemoteException e) {
                return null;
            } finally {
                client.release();
            }
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static boolean isInBlackList(Context context, String mediaId, String action) {
        final ContentProviderClient client = context.getContentResolver().acquireUnstableContentProviderClient(CONTENT_URI);
        if (client != null) {
            try {
                final Bundle extra = new Bundle();
                extra.putString("action", action);
                final Bundle result = client.call("isInBlackList", mediaId, extra);
                return (result != null && result.getBoolean("result", false));
            } catch (RemoteException e) {
                return false;
            } finally {
                client.release();
            }
        }

        return false;
    }
}
