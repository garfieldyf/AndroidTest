package android.ext.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;

/**
 * Class PackageUtils
 * @author Garfield
 * @version 1.0
 */
public final class PackageUtils {
    /**
     * Equivalent to calling <tt>parsePackage(context.getPackageManager(), context.getResources(), sourceFile, flags)</tt>.
     * @param context The <tt>Context</tt>.
     * @param sourceFile The path to the archive file, must be absolute file path.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @throws Exception if the package archive file cannot be parsed.
     * @see #parsePackage(PackageManager, Resources, String, int)
     */
    public static PackageArchiveInfo parsePackage(Context context, String sourceFile, int flags) throws Exception {
        return parsePackage(context.getPackageManager(), context.getResources(), sourceFile, flags);
    }

    /**
     * Parses a package archive file with the specified <em>sourceFile</em>.
     * @param pm The {@link PackageManager} to retrieve the package information.
     * @param resources The {@link Resources}.
     * @param sourceFile The path to the archive file, must be absolute file path.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @throws Exception if the package archive file cannot be parsed.
     * @see #parsePackage(Context, String, int)
     */
    public static PackageArchiveInfo parsePackage(PackageManager pm, Resources resources, String sourceFile, int flags) throws Exception {
        final Resources res = getResources(resources, sourceFile);
        try {
            final PackageInfo pi = pm.getPackageArchiveInfo(sourceFile, flags);
            pi.applicationInfo.sourceDir = pi.applicationInfo.publicSourceDir = sourceFile;
            return new PackageArchiveInfo(pi, loadIcon(pm, res, pi), loadLabel(pm, res, pi));
        } finally {
            // Close the AssetManager to avoid ProcessKiller kill my process after unmounting usb disk.
            res.getAssets().close();
        }
    }

    public static void dumpPackageArchiveInfos(Printer printer, Collection<PackageArchiveInfo> infos) {
        final int size = ArrayUtils.getSize(infos);
        final StringBuilder result = new StringBuilder(192);
        DebugUtils.dumpSummary(printer, result, 150, " Dumping PackageArchiveInfos [ size = %d ] ", size);
        for (PackageArchiveInfo info : infos) {
            result.setLength(0);
            info.dump(printer, result, "  ");
        }
    }

    private static Resources getResources(Resources res, String sourceFile) throws Exception {
        final AssetManager am = AssetManager.class.newInstance();
        sMethod.invoke(am, sourceFile);
        return new Resources(am, res.getDisplayMetrics(), res.getConfiguration());
    }

    @SuppressWarnings("deprecation")
    private static Drawable loadIcon(PackageManager pm, Resources res, PackageInfo packageInfo) {
        final Drawable icon = res.getDrawable(packageInfo.applicationInfo.icon);
        return (icon != null ? icon : pm.getDefaultActivityIcon());
    }

    private static CharSequence loadLabel(PackageManager pm, Resources res, PackageInfo packageInfo) {
        final CharSequence label = res.getText(packageInfo.applicationInfo.labelRes);
        return (TextUtils.isEmpty(label) ? packageInfo.packageName : label);
    }

    /**
     * Class <tt>PackageArchiveInfo</tt> contains an application
     * package information defined in a package archive file.
     */
    public static final class PackageArchiveInfo implements Comparable<PackageArchiveInfo> {
        /**
         * The {@link PackageInfo}, parse from the <tt>AndroidManifest.xml</tt>.
         */
        public final PackageInfo packageInfo;

        /**
         * The application icon, load from the &lt;application&gt
         * tag's <em>icon</em> attribute;
         */
        public final Drawable icon;

        /**
         * The application label, load from the &lt;application&gt
         * tag's <em>label</em> attribute;
         */
        public final CharSequence label;

        /**
         * Constructor
         */
        public PackageArchiveInfo(PackageInfo packageInfo, Drawable icon, CharSequence label) {
            this.packageInfo = packageInfo;
            this.icon  = icon;
            this.label = label;
        }

        @Override
        public int compareTo(PackageArchiveInfo another) {
            return label.toString().compareTo(another.label.toString());
        }

        public final void dump(Printer printer) {
            dump(printer, new StringBuilder(192), "");
        }

        /* package */ final void dump(Printer printer, StringBuilder result, String indentSpaces) {
            printer.println(result.append(indentSpaces)
                   .append("PackageArchiveInfo { package = ").append(packageInfo.packageName)
                   .append(", version = ").append(packageInfo.versionName)
                   .append(", label = ").append(label)
                   .append(", source = ").append(packageInfo.applicationInfo.sourceDir)
                   .append(" }").toString());
        }
    }

    /**
     * Class <tt>PackageParser</tt> used to parse the package archive files.
     */
    public static final class PackageParser implements ScanCallback {
        private List<PackageArchiveInfo> mResults;
        private final Resources mResources;
        private final PackageManager mPackageManager;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public PackageParser(Context context) {
            mResources = context.getResources();
            mPackageManager = context.getPackageManager();
        }

        /**
         * Parses the package archive files in the specified <em>dirPaths</em>.
         * @param flags The flags. May be <tt>0</tt> or any combination of
         * {@link FileUtils#FLAG_IGNORE_HIDDEN_FILE FLAG_IGNORE_HIDDEN_FILE},
         * {@link FileUtils#FLAG_SCAN_FOR_DESCENDENTS FLAG_SCAN_FOR_DESCENDENTS}.
         * @param dirPaths An array of directories.
         * @return A <tt>List</tt> of {@link PackageArchiveInfo}.
         */
        public final List<PackageArchiveInfo> parsePackages(int flags, String... dirPaths) {
            mResults = new ArrayList<PackageArchiveInfo>();
            for (int i = 0, size = ArrayUtils.getSize(dirPaths); i < size; ++i) {
                FileUtils.scanFiles(dirPaths[i], this, flags);
            }

            return mResults;
        }

        @Override
        public int onScanFile(String path, int type) {
            if (accept(path, type)) {
                try {
                    mResults.add(parsePackage(mPackageManager, mResources, path, 0));
                } catch (Exception e) {
                    Log.e(PackageParser.class.getName(), "Couldn't parse archive file - " + path, e);
                }
            }

            return SC_CONTINUE;
        }

        private boolean accept(String path, int type) {
            if (type != Dirent.DT_DIR) {
                final int index = FileUtils.findFileExtension(path);
                return (index >= 0 && "apk".regionMatches(true, 0, path, index, 3));
            }

            return false;
        }
    }

    /**
     * Class <tt>PackageNameComparator</tt> compares the package name of the {@link PackageArchiveInfo}.
     */
    public static final class PackageNameComparator implements Comparator<PackageArchiveInfo> {
        public static final PackageNameComparator sInstance = new PackageNameComparator();

        /**
         * This class cannot be instantiated.
         */
        private PackageNameComparator() {
        }

        @Override
        public int compare(PackageArchiveInfo one, PackageArchiveInfo another) {
            return one.packageInfo.packageName.compareTo(another.packageInfo.packageName);
        }
    }

    private static final Method sMethod;

    static {
        try {
            sMethod = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            sMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private PackageUtils() {
    }
}
