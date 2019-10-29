package android.ext.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.ext.net.NetworkUtils;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Printer;
import android.view.Display;
import android.view.WindowManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Class DeviceUtils
 * @author Garfield
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
     * @return A formatted string with the <em>freq</em> or <tt>fallback</tt>.
     */
    public static String formatCpuFreq(int freq, String fallback) {
        if (freq <= 0) {
            return fallback;
        }

        float result = freq;
        final StringBuilder format = new StringBuilder(8).append("%.0f KHz");
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
     * Tests the current device in a low memory situation. If the total
     * memory size of the current device less 800 MB return <tt>true</tt>.
     */
    public static boolean isLowMemory() {
        return (Process.getTotalMemory() < 838860800L /* 800 MB */);
    }

    /**
     * Returns an array of ABIs supported by this device.
     */
    public static String[] getSupportedABIs() {
        return (Build.VERSION.SDK_INT > 20 ? Build.SUPPORTED_ABIS : new String[] { Build.CPU_ABI, Build.CPU_ABI2 });
    }

    public static void dumpSystemInfo(Context context, Printer printer) {
        final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        final Display display  = wm.getDefaultDisplay();

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
             .append(", core = ").append(cpuCore)
             .append(", abis = ").append(Arrays.toString(getSupportedABIs()))
             .append(" ]");
        printer.println(infos.toString());

        // Dumps cpu infos.
        infos.setLength(0);
        for (int i = 0; i < cpuCore; ++i) {
            dumpWhiteSpace(infos, i).append("cpu").append(i)
                 .append(" [ curFrequency = ").append(formatCpuFreq(getCpuCurFreq(i), "N/A"))
                 .append(", maxFrequency = ").append(formatCpuFreq(getCpuMaxFreq(i), "N/A"))
                 .append(", minFrequency = ").append(formatCpuFreq(getCpuMinFreq(i), "N/A"))
                 .append(" ]");
        }
        printer.println(infos.toString());

        // Dumps sdk, version and mac infos.
        infos.setLength(0);
        infos.append("  sdk = ").append(Build.VERSION.SDK_INT)
             .append("\n  version = ").append(Build.VERSION.RELEASE)
             .append("\n  wlan = ").append(NetworkUtils.getMacAddress(NetworkUtils.WLAN, "N/A"))
             .append("\n  eth0 = ").append(NetworkUtils.getMacAddress(NetworkUtils.ETHERNET, "N/A"));
        printer.println(infos.toString());

        // Dumps display infos.
        infos.setLength(0);
        infos.append("  realWidth  = ").append(size.x)
             .append("\n  realHeight = ").append(size.y)
             .append("\n  width   = ").append(dm.widthPixels)
             .append("\n  height  = ").append(dm.heightPixels)
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
        infos.append("  totalMemory  = ").append(FileUtils.formatFileSize(info.totalMem))
             .append("\n  usedMemory   = ").append(FileUtils.formatFileSize(info.totalMem - info.availMem))
             .append("\n  availMemory  = ").append(FileUtils.formatFileSize(info.availMem))
             .append("\n  lowMemory    = ").append(info.lowMemory)
             .append("\n  threshold    = ").append(FileUtils.formatFileSize(info.threshold))
             .append("\n  appMaxMemory = ").append(FileUtils.formatFileSize(Runtime.getRuntime().maxMemory()))
             .append("\n  appLargeHeap = ").append(getAppHeapSize("dalvik.vm.heapsize"));
        printer.println(infos.toString());

        // Dumps the system storage infos.
        infos.setLength(0);
        final StatFs statFs = new StatFs(Environment.getRootDirectory().getPath());
        dumpStorageInfo(statFs, infos.append("  system ["));

        // Dumps the data storage infos.
        statFs.restat(Environment.getDataDirectory().getPath());
        printer.println(dumpStorageInfo(statFs, infos.append("\n  data [")).toString());

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

    private static String getAppHeapSize(String key) {
        final long heapSize = SystemProperties.getLong(key, 0);
        return (heapSize != 0 ? FileUtils.formatFileSize(heapSize << 20) : "N/A");
    }

    private static StringBuilder dumpStorageInfo(StatFs statFs, StringBuilder out) {
        return out.append(" total = ").append(FileUtils.formatFileSize(statFs.getTotalBytes()))
                  .append(", used = ").append(FileUtils.formatFileSize((statFs.getBlockCountLong() - statFs.getAvailableBlocksLong()) * statFs.getBlockSizeLong()))
                  .append(", avail = ").append(FileUtils.formatFileSize(statFs.getAvailableBytes()))
                  .append(" ]");
    }

    private static String dumpExternalStorageInfo(Context context, StatFs statFs, StringBuilder out) {
        final StorageVolume[] volumes = ((StorageManager)context.getSystemService(Context.STORAGE_SERVICE)).getVolumeList();
        for (int i = 0, size = ArrayUtils.getSize(volumes); i < size; ++i) {
            final StorageVolume volume = volumes[i];
            final String path  = volume.getPath();
            final String state = volume.getState();

            try {
                statFs.restat(path);
            } catch (Throwable e) {
                continue;
            }

            dumpStorageInfo(statFs, dumpWhiteSpace(out, i)
                .append(getUserLabel(context, volume))
                .append(" [ path = ").append(path)
                .append(", primary = ").append(volume.isPrimary())
                .append(", state = ").append(state != null ? state : "unknown")
                .append(','));
        }

        return out.toString();
    }

    private static StringBuilder dumpWhiteSpace(StringBuilder out, int index) {
        if (index > 0) {
            out.append('\n');
        }

        return out.append("  ");
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
        InputStream is = null;
        try {
            is = new FileInputStream("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/" + filename);
            final byte[] data = new byte[24];
            int byteCount = is.read(data, 0, data.length);
            if (data[byteCount - 1] == '\n') {
                --byteCount;    // skip the '\n'
            }

            return Integer.parseInt(new String(data, 0, byteCount));
        } catch (Exception e) {
            return -1;
        } finally {
            FileUtils.close(is);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private DeviceUtils() {
    }
}
