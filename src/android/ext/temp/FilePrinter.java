package android.ext.temp;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.ext.util.StringUtils;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Pair;
import android.util.Printer;

public final class FilePrinter implements Printer, Closeable, Callback {
    private static final char[] lineSeparator = new char[] { '\n' };
    private static final int MESSAGE_QUIT    = 0;
    private static final int MESSAGE_PRINT   = 1;
    private static final int MESSAGE_FORMAT  = 2;
    private static final int MESSAGE_NEWLINE = 3;

    private final Handler mHandler;
    private final PrintWriter mWriter;
    private final Formatter mFormatter;

    public FilePrinter(String path, boolean append) {
        final HandlerThread thread = new HandlerThread("FilePrinterThread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mHandler = new Handler(thread.getLooper(), this);

        try {
            mWriter = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(path, append)), StandardCharsets.UTF_8), false);
            mFormatter = new Formatter(mWriter, Locale.getDefault());
        } catch (Exception e) {
            mHandler.getLooper().quit();
            throw new RuntimeException(e);
        }
    }

    public final void format(String format, Object... args) {
        if (StringUtils.getLength(format) > 0) {
            Message.obtain(mHandler, MESSAGE_FORMAT, Pair.create(format, args)).sendToTarget();
        }
    }

    public final void println() {
        Message.obtain(mHandler, MESSAGE_NEWLINE).sendToTarget();
    }

    @Override
    public void close() {
        Message.obtain(mHandler, MESSAGE_QUIT).sendToTarget();
    }

    @Override
    public void println(String str) {
        final int length = StringUtils.getLength(str);
        if (length == 0) {
            Message.obtain(mHandler, MESSAGE_NEWLINE).sendToTarget();
        } else {
            int what = MESSAGE_PRINT;
            if (str.charAt(length - 1) != '\n') {
                what += MESSAGE_NEWLINE;
            }

            Message.obtain(mHandler, what, str).sendToTarget();
        }
    }

    @Override
    @SuppressLint("NewApi")
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_QUIT:
            mHandler.getLooper().quitSafely();
            mWriter.close();
            break;

        case MESSAGE_PRINT:
            mWriter.write((String)msg.obj);
            break;

        case MESSAGE_NEWLINE:
            mWriter.write(lineSeparator);
            break;

        case MESSAGE_FORMAT:
            @SuppressWarnings("unchecked")
            final Pair<String, Object[]> args = (Pair<String, Object[]>)msg.obj;
            mFormatter.format(args.first, args.second);
            break;

        default:
            mWriter.write((String)msg.obj);
            mWriter.write(lineSeparator);
            break;
        }

        return true;
    }
}
