package android.ext.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.ext.util.UIHandler;
import android.net.Uri;
import android.os.Handler;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class <tt>CursorObserver</tt> is an implementation of a {@link ContentObserver}.
 * @author Garfield
 */
public class CursorObserver extends ContentObserver {
    private static final int STATE_REGISTERED   = 1;
    private static final int STATE_UNREGISTERED = 0;

    private final AtomicInteger mState;
    private final CursorObserverClient mClient;

    /**
     * Constructor
     * @param client The {@link CursorObserverClient} for being
     * notified when the content is changed.
     * @see #CursorObserver(CursorObserverClient, Handler)
     */
    public CursorObserver(CursorObserverClient client) {
        this(client, UIHandler.sInstance);
    }

    /**
     * Constructor
     * @param client The {@link CursorObserverClient} for being
     * notified when the content is changed.
     * @param handler The {@link Handler} for being notified
     * when the content is changed, or <tt>null</tt> if none.
     * @see #CursorObserver(CursorObserverClient)
     */
    public CursorObserver(CursorObserverClient client, Handler handler) {
        super(handler);
        mClient = client;
        mState  = new AtomicInteger(STATE_UNREGISTERED);
    }

    /**
     * Register this observer that is called when changes happen to the
     * content backing the cursor. If this observer already registered
     * then invoking this method has no effect.
     * @param cursor The <tt>Cursor</tt> will be register to.
     * @see #unregister(Cursor)
     */
    public void register(Cursor cursor) {
        if (mState.compareAndSet(STATE_UNREGISTERED, STATE_REGISTERED)) {
            cursor.registerContentObserver(this);
        }
    }

    /**
     * Unregister this observer that has previously been registered. If this
     * observer already unregistered then invoking this method has no effect.
     * @param cursor The <tt>Cursor</tt> will be unregister to.
     * @see #register(Cursor)
     */
    public void unregister(Cursor cursor) {
        if (mState.compareAndSet(STATE_REGISTERED, STATE_UNREGISTERED)) {
            cursor.unregisterContentObserver(this);
        }
    }

    /**
     * Register this observer that gets callbacks when data identified by a given
     * content URI changes. If this observer already registered then invoking this
     * method has no effect.
     * @param context The <tt>Context</tt>.
     * @param uri The URI to watch for changes. This can be a specific row URI, or
     * a base URI for a whole class of content.
     * @param notifyForDescendents If <tt>true</tt> changes to URIs beginning with
     * uri will also cause notifications to be sent. If <tt>false</tt> only changes
     * to the exact URI specified by uri will cause notifications to be sent.
     * @see #unregister(Context)
     */
    public void register(Context context, Uri uri, boolean notifyForDescendents) {
        if (mState.compareAndSet(STATE_UNREGISTERED, STATE_REGISTERED)) {
            context.getContentResolver().registerContentObserver(uri, notifyForDescendents, this);
        }
    }

    /**
     * Unregister this observer that has previously been registered. If this
     * observer already unregistered then invoking this method has no effect.
     * @param context The <tt>Context</tt>.
     * @see #register(Context, Uri, boolean)
     */
    public void unregister(Context context) {
        if (mState.compareAndSet(STATE_REGISTERED, STATE_UNREGISTERED)) {
            context.getContentResolver().unregisterContentObserver(this);
        }
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public final void onChange(boolean selfChange) {
        onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        mClient.onContentChanged(selfChange, uri);
    }

    /**
     * Used for being notified when the content is changed.
     */
    public static interface CursorObserverClient {
        /**
         * Called when the {@link CursorObserver} receives a change notification.
         * @param selfChange <tt>true</tt> if this is a self-change notification.
         * @param uri The Uri of the changed content, or <tt>null</tt> if unknown.
         * @see ContentObserver#onChange(boolean, Uri)
         */
        void onContentChanged(boolean selfChange, Uri uri);
    }
}
