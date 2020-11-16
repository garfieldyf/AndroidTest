package com.tencent.temp;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Class Downloads
 * @author beckyuan
 */
public final class Downloads implements BaseColumns {
    /**
     * The content:// style URL for this table.
     */
    public static final Uri CONTENT_URI = Uri.parse("content://AUTHORITY/downloads");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of downloads.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.qqbrowsertv.downloads";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single download.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.qqbrowsertv.downloads";

    /**
     * The uri of the table.
     * <P>Type: TEXT</P>
     */
    public static final String URI = "uri";

    /**
     * The title of the table.
     * <P>Type: TEXT</P>
     */
    public static final String TITLE = "title";

    /**
     * The description of the table.
     * <P>Type: TEXT</P>
     */
    public static final String DESCRIPTION = "description";

    /**
     * The filename of the table.
     * <P>Type: TEXT</P>
     */
    public static final String FILENAME = "filename";

    /**
     * The MIME type of the table.
     * <P>Type: TEXT</P>
     */
    public static final String MIME_TYPE = "mime_type";

    /**
     * The status of the table.
     * <P>Type: INTEGER</P>
     */
    public static final String STATUS = "status";

    /**
     * The download speed of the table.
     * <P>Type: INTEGER (bytes/sec)</P>
     */
    public static final String SPEED = "speed";

    /**
     * The download finished time of the table.
     * <P>Type: INTEGER (System.currentTimeMillis())</P>
     */
    public static final String FINISHED_TIME = "finished_time";

    /**
     * The total size of the table.
     * <P>Type: INTEGER</P>
     */
    public static final String TOTAL_SIZE = "total_size";

    /**
     * The downloaded size of the table.
     * <P>Type: INTEGER</P>
     */
    public static final String DOWNLOADED_SIZE = "downloaded_size";

    /**
     * The value of {@link #STATUS} when the download has failed.
     */
    public static final int STATUS_FAILED = 0;

    /**
     * The value of {@link #STATUS} when the download has paused.
     */
    public static final int STATUS_PAUSED = 1;

    /**
     * The value of {@link #STATUS} when the download is waiting to start.
     */
    public static final int STATUS_PENDING = 2;

    /**
     * The value of {@link #STATUS} when the download is running.
     */
    public static final int STATUS_RUNNING = 3;

    /**
     * The value of {@link #STATUS} when the download has successfully completed.
     */
    public static final int STATUS_SUCCESSFUL = 4;

    /**
     * The method name for <tt>ContentProvider.call()</tt>.
     */
    public static final String QUERY_DOWNLOADING_COUNT = "queryDownloadingCount";

    /**
     * This class cannot be instantiated.
     */
    private Downloads() {
    }
}
