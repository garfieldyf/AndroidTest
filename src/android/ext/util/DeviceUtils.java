package android.ext.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.ext.net.NetworkUtils;
import android.graphics.Point;
import android.os.Build;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Printer;
import android.view.Display;
import android.view.WindowManager;

/**
 * Class DeviceUtils
 * @author Garfield
 * @version 1.5
 */
@SuppressLint("NewApi")
public final class DeviceUtils {
    /**
     * Returns the cpu maximum frequency on the devices.
     * @param coreIndex The cpu core index.
     * @return The cpu frequency in KHz or <tt>-1</tt>
     * if an error occurs.
     * @see #getCpuCurFreq(int)
     * @see #getCpuMinFreq(int)
     */
    public static int getCpuMaxFreq(int coreIndex) {
        return readCpuFrequency(coreIndex, "cpuinfo_max_freq");
    }

    /**
     * Returns the cpu minimum frequency on the devices.
     * @param coreIndex The cpu core index.
     * @return The cpu frequency in KHz or <tt>-1</tt>
     * if an error occurs.
     * @see #getCpuCurFreq(int)
     * @see #getCpuMaxFreq(int)
     */
    public static int getCpuMinFreq(int coreIndex) {
        return readCpuFrequency(coreIndex, "cpuinfo_min_freq");
    }

    /**
     * Returns the cpu current frequency on the devices.
     * @param coreIndex The cpu core index.
     * @return The cpu frequency in KHz or <tt>-1</tt>
     * if an error occurs.
     * @see #getCpuMaxFreq(int)
     * @see #getCpuMinFreq(int)
     */
    public static int getCpuCurFreq(int coreIndex) {
        return readCpuFrequency(coreIndex, "scaling_cur_freq");
    }

    /**
     * Formats the cpu frequency to be in the form of KHz, MHz, GHz, etc.
     * @param freq The cpu frequency in KHz.
     * @return The formatted string with the <tt>freq</tt>.
     */
    public static String formatCpuFreq(int freq) {
        if (freq <= 0) {
            return "N/A";
        }

        float result = freq;
        final StringBuilder format = new StringBuilder("%.0f KHz");
        if (result >= 1000) {
            result /= 1000;
            format.setCharAt(5, 'M');
        }

        if (result >= 1000) {
            result /= 1000;
            format.setCharAt(2, '2');
            format.setCharAt(5, 'G');
        }

        return String.format(format.toString(), result);
    }

    /**
     * Returns an array of ABIs supported by this device.
     */
    @SuppressWarnings("deprecation")
    public static String[] getSupportedABIs() {
        if (Build.VERSION.SDK_INT > 20) {
            return Build.SUPPORTED_ABIS;
        } else {
            return new String[] { Build.CPU_ABI, Build.CPU_ABI2 };
        }
    }

    /**
     * Reads the specified device file contents from the filesystem.
     * @param deviceFile The device filename, must be absolute file path.
     * @param maxSize The maximum number of byte to allow to read.
     * @return The file contents as a string.
     * @throws IOException if an error occurs while reading the data.
     */
    public static String readDeviceFile(String deviceFile, int maxSize) throws IOException {
        final InputStream is = new FileInputStream(deviceFile);
        try {
            final byte[] data = new byte[maxSize];
            int byteCount = is.read(data, 0, data.length);
            if (data[byteCount - 1] == '\n') {
                --byteCount;    // skip the '\n'
            }

            return new String(data, 0, byteCount);
        } finally {
            is.close();
        }
    }

    public static void dumpSystemInfo(Context context, Printer printer) {
        final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        final Display display = wm.getDefaultDisplay();

        final DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);

        final Point size = new Point();
        display.getRealSize(size);

        // Dumps the system infos.
        final int cpuCore = Runtime.getRuntime().availableProcessors();
        final StringBuilder infos = new StringBuilder(256);
        DebugUtils.dumpSummary(printer, infos, 110, " System Informations ", (Object[])null);

        infos.setLength(0);
        infos.append("  model = ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL)
             .append("\n  cpu info [ ").append("model = ").append(Build.HARDWARE)
             .append(", core = ").append(cpuCore).append(" ]");
        printer.println(infos.toString());

        // Dumps cpu infos.
        infos.setLength(0);
        for (int i = 0; i < cpuCore; ++i) {
            if (i == 0) {
                infos.append("  cpu");
            } else {
                infos.append("\n  cpu");
            }

            infos.append(i)
                 .append(" [ curFrequency = ").append(formatCpuFreq(getCpuCurFreq(i)))
                 .append(", maxFrequency = ").append(formatCpuFreq(getCpuMaxFreq(i)))
                 .append(", minFrequency = ").append(formatCpuFreq(getCpuMinFreq(i)))
                 .append(" ]");
        }
        printer.println(infos.toString());

        // Dumps cpu abis, version etc.
        infos.setLength(0);
        infos.append("  cpu abis = ").append(Arrays.toString(getSupportedABIs()))
             .append("\n  sdk = ").append(Build.VERSION.SDK_INT)
             .append("\n  version = ").append(Build.VERSION.RELEASE)
             .append("\n  wlan = ").append(NetworkUtils.getMacAddress(NetworkUtils.WLAN, "N/A"))
             .append("\n  eth  = ").append(NetworkUtils.getMacAddress(NetworkUtils.ETHERNET, "N/A"));
        printer.println(infos.toString());

        // Dumps display infos.
        infos.setLength(0);
        infos.append("  display = ").append(Build.DISPLAY)
             .append("\n  realWidth  = ").append(size.x)
             .append("\n  realHeight = ").append(size.y)
             .append("\n  width  = ").append(dm.widthPixels)
             .append("\n  height = ").append(dm.heightPixels)
             .append("\n  density = ").append(dm.density)
             .append("\n  densityDpi = [ ").append(toDensity(dm.densityDpi)).append(", ").append(dm.densityDpi).append(" ]")
             .append("\n  scaledDensity = ").append(dm.scaledDensity)
             .append("\n  xdpi = ").append(dm.xdpi)
             .append("\n  ydpi = ").append(dm.ydpi);
        printer.println(infos.toString());

        // Dumps the memory infos.
        final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final MemoryInfo info = new MemoryInfo();
        am.getMemoryInfo(info);

        infos.setLength(0);
        infos.append("  totalMemory = ").append(Formatter.formatFileSize(context, info.totalMem))
             .append("\n  usedMemory  = ").append(Formatter.formatFileSize(context, info.totalMem - info.availMem))
             .append("\n  availMemory = ").append(Formatter.formatFileSize(context, info.availMem))
             .append("\n  availThreshold = ").append(Formatter.formatFileSize(context, info.threshold))
             .append("\n  appMaxMemory = ").append(Formatter.formatFileSize(context, Runtime.getRuntime().maxMemory()));
        printer.println(infos.toString());

        // Dumps the system storage infos.
        infos.setLength(0);
        final StatFs statFs = new StatFs("/system");
        dumpInternalStorageInfo(context, statFs, infos, "  system ");

        // Dumps the data storage infos.
        statFs.restat("/data");
        dumpInternalStorageInfo(context, statFs, infos, "\n  data ");
        printer.println(infos.toString());

        // Dumps the external storage infos.
        infos.setLength(0);
        printer.println(dumpExternalStorageInfo(context, statFs, infos));
    }

    /**
     * Returns a string containing a concise, human-readable description
     * with the specified <em>densityDpi</em>.
     * @param densityDpi The screen density expressed as dots-per-inch.
     * @return A readable representation of the <em>densityDpi</em>.
     */
    public static String toDensity(int densityDpi) {
        switch (densityDpi) {
        case DisplayMetrics.DENSITY_LOW:
            return "DENSITY_LOW";

        case DisplayMetrics.DENSITY_MEDIUM:
            return "DENSITY_MEDIUM";

        case DisplayMetrics.DENSITY_TV:
            return "DENSITY_TV";

        case DisplayMetrics.DENSITY_HIGH:
            return "DENSITY_HIGH";

        case DisplayMetrics.DENSITY_XHIGH:
            return "DENSITY_XHIGH";

        case DisplayMetrics.DENSITY_XXHIGH:
            return "DENSITY_XXHIGH";

        case DisplayMetrics.DENSITY_XXXHIGH:
            return "DENSITY_XXXHIGH";

        default:
            return "DENSITY_" + densityDpi;
        }
    }

    /**
     * Writes the supported ABIs into a {@link JsonWriter}.
     */
    /* package */ static JsonWriter writeABIs(JsonWriter writer) throws IOException {
        final StringBuilder result = new StringBuilder(32);
        final String[] abis = getSupportedABIs();
        if (abis.length > 0) {
            result.append(abis[0]);
            for (int i = 1; i < abis.length; ++i) {
                result.append(',').append(abis[i]);
            }
        }

        return writer.name("abis").value(result.toString());
    }

    private static void dumpInternalStorageInfo(Context context, StatFs statFs, StringBuilder out, String prefix) {
        out.append(prefix)
           .append("[ total = ").append(Formatter.formatFileSize(context, statFs.getTotalBytes()))
           .append(", used = ").append(Formatter.formatFileSize(context, (statFs.getBlockCountLong() - statFs.getAvailableBlocksLong()) * statFs.getBlockSizeLong()))
           .append(", avail = ").append(Formatter.formatFileSize(context, statFs.getAvailableBytes()))
           .append(" ]");
    }

    private static String dumpExternalStorageInfo(Context context, StatFs statFs, StringBuilder out) {
        try {
            final StorageVolume[] volumes = ((StorageManager)context.getSystemService(Context.STORAGE_SERVICE)).getVolumeList();
            for (int i = 0, size = ArrayUtils.getSize(volumes); i < size; ++i) {
                final StorageVolume volume = volumes[i];
                final String path = volume.getPath();
                statFs.restat(path);

                if (out.length() <= 0) {
                    out.append("  ");
                } else {
                    out.append("\n  ");
                }

                out.append(getUserLabel(context, volume))
                   .append(" [ path = ").append(path)
                   .append(", primary = ").append(volume.isPrimary())
                   .append(", state = ").append(getState(volume))
                   .append(", total = ").append(Formatter.formatFileSize(context, statFs.getTotalBytes()))
                   .append(", used = ").append(Formatter.formatFileSize(context, (statFs.getBlockCountLong() - statFs.getAvailableBlocksLong()) * statFs.getBlockSizeLong()))
                   .append(", avail = ").append(Formatter.formatFileSize(context, statFs.getAvailableBytes()))
                   .append(" ]");
            }
        } catch (Exception e) {
            Log.e(DeviceUtils.class.getName(), "Couldn't dump storage info.", e);
        }

        return out.toString();
    }

    private static String getState(StorageVolume volume) {
        final String state = volume.getState();
        return (state != null ? state : "unknown");
    }

    private static String getUserLabel(Context context, StorageVolume volume) {
        String userLabel = volume.getUserLabel();
        if (TextUtils.isEmpty(userLabel)) {
            userLabel = volume.getDescription(context);
            if (TextUtils.isEmpty(userLabel)) {
                userLabel = "storage_" + volume.getStorageId();
            }
        }

        return userLabel;
    }

    private static int readCpuFrequency(int coreIndex, String filename) {
        try {
            return Integer.parseInt(readDeviceFile(new StringBuilder(64).append("/sys/devices/system/cpu/cpu").append(coreIndex).append("/cpufreq/").append(filename).toString(), 24));
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private DeviceUtils() {
    }
}
