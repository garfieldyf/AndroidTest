package android.ext.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.util.ArrayUtils.Filter;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.ext.util.Pools.Factory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.Printer;

/**
 * Class PackageUtils
 * @author Garfield
 * @version 1.0
 */
public final class PackageUtils {
    /**
     * Return a <tt>List</tt> of all packages that are installed on the device.
     * @param pm The <tt>PackageManager</tt> to retrieve the packages.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination of
     * <tt>PackageManager.GET_XXX</tt> constants.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link AppPackageInfo} objects.
     * @see PackageNameFilter
     */
    public static List<AppPackageInfo> getInstalledPackages(PackageManager pm, int flags, Filter<PackageInfo> filter) {
        final List<PackageInfo> infos = pm.getInstalledPackages(flags);
        final int size = infos.size();
        final List<AppPackageInfo> result = new ArrayList<AppPackageInfo>(size);
        if (filter != null) {
            for (int i = 0; i < size; ++i) {
                final PackageInfo info = infos.get(i);
                if (filter.accept(info)) {
                    result.add(new AppPackageInfo(pm, info));
                }
            }
        } else {
            for (int i = 0; i < size; ++i) {
                result.add(new AppPackageInfo(pm, infos.get(i)));
            }
        }

        return result;
    }

    /**
     * Equivalent to calling <tt>parsePackage(context, archiveFile, 0, AppPackageInfo.FACTORY)</tt>.
     * @param context The <tt>Context</tt>.
     * @param archiveFile The path to the archive file, must be absolute file path.
     * @return A {@link AppPackageInfo} object if the parse succeeded, <tt>null</tt> otherwise.
     * @see #parsePackage(Context, String, int, Factory)
     */
    public static AppPackageInfo parsePackage(Context context, String archiveFile) {
        return parsePackage(context, archiveFile, 0, AppPackageInfo.FACTORY);
    }

    /**
     * Parses a package archive file with the specified <em>archiveFile</em>.
     * @param context The <tt>Context</tt>.
     * @param archiveFile The path to the archive file, must be absolute file path.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination of
     * <tt>PackageManager.GET_XXX</tt> constants.
     * @param factory The {@link Factory} to create the {@link AppPackageInfo} or
     * subclass object.
     * @return A {@link AppPackageInfo} or subclass object if the parse succeeded,
     * <tt>null</tt> otherwise.
     * @see #parsePackage(Context, String)
     */
    public static <T extends AppPackageInfo> T parsePackage(Context context, String archiveFile, int flags, Factory<T> factory) {
        // Retrieve the package information from the package archive file.
        DebugUtils.__checkError(factory == null, "factory == null");
        final PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(archiveFile, flags);
        if (packageInfo != null) {
            final Resources res = createResources(context, archiveFile);
            try {
                // Creates a AppPackageInfo or subclass object.
                packageInfo.applicationInfo.sourceDir = archiveFile;
                final T result = factory.newInstance();
                result.initialize(context, res, packageInfo);
                return result;
            } finally {
                // Close the underlying AssetManager for the res to avoid
                // ProcessKiller kill my process after unmounting usb disk.
                res.getAssets().close();
            }
        }

        return null;
    }

    /**
     * Equivalent to calling <tt>parsePackages(context, dirPath, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, 0, AppPackageInfo.FACTORY)</tt>.
     * @param context The <tt>Context</tt>.
     * @param dirPath The path of directory, must be absolute file path.
     * @return A {@link List} of {@link AppPackageInfo}s.
     * @see #parsePackages(Context, String, int, int, Factory)
     */
    public final List<AppPackageInfo> parsePackages(Context context, String dirPath) {
        return new PackageParser<AppPackageInfo>(context, AppPackageInfo.FACTORY).parsePackages(dirPath, FileUtils.FLAG_IGNORE_HIDDEN_FILE | FileUtils.FLAG_SCAN_FOR_DESCENDENTS, 0);
    }

    /**
     * Parses the package archive files in the specified <em>dirPath</em>.
     * @param context The <tt>Context</tt>.
     * @param dirPath The path of directory, must be absolute file path.
     * @param scanFlags The scan flags. May be <tt>0</tt> or any combination
     * of {@link FileUtils#FLAG_IGNORE_HIDDEN_FILE FLAG_IGNORE_HIDDEN_FILE},
     * {@link FileUtils#FLAG_SCAN_FOR_DESCENDENTS FLAG_SCAN_FOR_DESCENDENTS}.
     * @param parseFlags The parse flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @param factory The {@link Factory} to create the {@link AppPackageInfo}
     * or subclass object.
     * @return A {@link List} of {@link AppPackageInfo} or subclass objects.
     * @see #parsePackages(Context, String)
     */
    public final <T extends AppPackageInfo> List<T> parsePackages(Context context, String dirPath, int scanFlags, int parseFlags, Factory<? extends T> factory) {
        return new PackageParser<T>(context, factory).parsePackages(dirPath, scanFlags, parseFlags);
    }

    public static void dump(Printer printer, Collection<? extends AppPackageInfo> infos) {
        final StringBuilder result = new StringBuilder(256);
        final int size = ArrayUtils.getSize(infos);
        DebugUtils.dumpSummary(printer, result, 140, " Dumping AppPackageInfos [ size = %d ] ", size);

        for (AppPackageInfo info : infos) {
            result.setLength(0);
            printer.println(info.dump(result.append("  ")).append(" }").toString());
        }
    }

    /**
     * Create a new <tt>Resources</tt> object base on an existing <em>sourceFile</em>.
     */
    private static Resources createResources(Context context, String sourceFile) {
        final AssetManager assets = new AssetManager();
        assets.addAssetPath(sourceFile);
        return new Resources(assets, context.getResources().getDisplayMetrics(), null);
    }

    /**
     * Class <tt>AppPackageInfo</tt> contains an application's label, icon and {@link PackageInfo}.
     */
    public static class AppPackageInfo implements Comparable<AppPackageInfo> {
        /**
         * The {@link PackageInfo}, parse from the <tt>AndroidManifest.xml</tt>.
         */
        public PackageInfo packageInfo;

        /**
         * The application's icon, load from the &lt;application&gt
         * tag's "icon" attribute;
         */
        public Drawable icon;

        /**
         * The application's label, load from the &lt;application&gt
         * tag's "label" attribute;
         */
        public CharSequence label;

        /**
         * Constructor
         * @see #AppPackageInfo(PackageManager, PackageInfo)
         */
        public AppPackageInfo() {
        }

        /**
         * Constructor
         * @param pm The <tt>PackageManager</tt> to load the application's label and icon.
         * @param packageInfo The <tt>PackageInfo</tt>.
         * @see #AppPackageInfo()
         */
        public AppPackageInfo(PackageManager pm, PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
            this.icon  = packageInfo.applicationInfo.loadIcon(pm);
            this.label = packageInfo.applicationInfo.loadLabel(pm);
        }

        /**
         * Returns the application's label that removing the white
         * spaces and control characters from the start and end.
         */
        public final CharSequence getTrimmedLabel() {
            return StringUtils.trim(label);
        }

        /**
         * Returns the rounded <tt>Drawable</tt> of the application's {@link icon}.
         * @param cornerRadius The corner radius.
         * @return A <tt>RoundedBitmapDrawable</tt> if the {@link icon} is a <tt>BitmapDrawable</tt>.
         * Otherwise returns the {@link icon}.
         * @see #getRoundedIcon(Resources, int)
         */
        public final Drawable getRoundedIcon(float cornerRadius) {
            return (icon instanceof BitmapDrawable ? new RoundedBitmapDrawable(((BitmapDrawable)icon).getBitmap(), cornerRadius) : icon);
        }

        /**
         * Equivalent to calling <tt>getRoundedIcon(res.getDimension(id))</tt>.
         * @param res The {@link Resources}.
         * @param id The resource id of the corner radius.
         * @return A <tt>RoundedBitmapDrawable</tt> if the {@link icon} is a <tt>BitmapDrawable</tt>.
         * Otherwise returns the {@link icon}.
         * @see #getRoundedIcon(float)
         */
        public final Drawable getRoundedIcon(Resources res, int id) {
            return (icon instanceof BitmapDrawable ? new RoundedBitmapDrawable(((BitmapDrawable)icon).getBitmap(), res.getDimension(id)) : icon);
        }

        public final void dump(Printer printer) {
            printer.println(dump(new StringBuilder(256)).append(" }").toString());
        }

        @Override
        public String toString() {
            return packageInfo.packageName;
        }

        @Override
        public int compareTo(AppPackageInfo another) {
            return label.toString().compareTo(another.label.toString());
        }

        /**
         * Initializes this object with the specified <em>res</em> and <em>packageInfo</em>.
         * @param context The <tt>Context</tt>.
         * @param res The <tt>Resources</tt> to load the application's label and icon.
         * @param packageInfo The <tt>PackageInfo</tt> to set.
         */
        protected void initialize(Context context, Resources res, PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
            this.icon  = loadIcon(context, res);
            this.label = loadLabel(context, res);
        }

        protected StringBuilder dump(StringBuilder out) {
            return out.append(getClass().getSimpleName())
                .append(" { package = ").append(packageInfo.packageName)
                .append(", version = ").append(packageInfo.versionName)
                .append(", label = ").append(label)
                .append(", sourceDir = ").append(packageInfo.applicationInfo.sourceDir);
        }

        @SuppressWarnings("deprecation")
        private Drawable loadIcon(Context context, Resources res) {
            final Drawable icon = res.getDrawable(packageInfo.applicationInfo.icon);
            return (icon != null ? icon : context.getPackageManager().getDefaultActivityIcon());
        }

        private CharSequence loadLabel(Context context, Resources res) {
            final CharSequence label = res.getText(packageInfo.applicationInfo.labelRes);
            return (TextUtils.isEmpty(label) ? packageInfo.packageName : label);
        }

        public static final Factory<AppPackageInfo> FACTORY = new Factory<AppPackageInfo>() {
            @Override
            public AppPackageInfo newInstance() {
                return new AppPackageInfo();
            }
        };
    }

    /**
     * Class <tt>PackageParser</tt> used to parse the package archive files.
     * @param <T> A class that extends {@link AppPackageInfo} that will be
     * the parser result type.
     */
    public static class PackageParser<T extends AppPackageInfo> implements ScanCallback {
        /**
         * The application <tt>Context</tt>.
         */
        public final Context mContext;

        /**
         * The {@link Factory} to create the {@link AppPackageInfo} or subclass object.
         */
        private final Factory<? extends T> mFactory;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param factory The {@link Factory} to create the {@link AppPackageInfo} or subclass object.
         */
        public PackageParser(Context context, Factory<? extends T> factory) {
            mFactory = factory;
            mContext = context.getApplicationContext();
        }

        /**
         * Equivalent to calling <tt>parsePackages(dirPath, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, 0)</tt>.
         * @param dirPath The path of directory, must be absolute file path.
         * @return A {@link List} of {@link AppPackageInfo}s.
         * @see #parsePackages(String, int, int)
         */
        public final List<T> parsePackages(String dirPath) {
            return parsePackages(dirPath, FileUtils.FLAG_IGNORE_HIDDEN_FILE | FileUtils.FLAG_SCAN_FOR_DESCENDENTS, 0);
        }

        /**
         * Parses the package archive files in the specified <em>dirPath</em>.
         * @param dirPath The path of directory, must be absolute file path.
         * @param scanFlags The scan flags. May be <tt>0</tt> or any combination
         * of {@link FileUtils#FLAG_IGNORE_HIDDEN_FILE FLAG_IGNORE_HIDDEN_FILE},
         * {@link FileUtils#FLAG_SCAN_FOR_DESCENDENTS FLAG_SCAN_FOR_DESCENDENTS}.
         * @param parseFlags The parse flags. May be <tt>0</tt> or any combination
         * of <tt>PackageManager.GET_XXX</tt> constants.
         * @return A {@link List} of {@link AppPackageInfo} or subclass objects.
         * @see #parsePackages(String)
         */
        public final List<T> parsePackages(String dirPath, int scanFlags, int parseFlags) {
            final Pair<Integer, List<T>> parseResult = new Pair<Integer, List<T>>(parseFlags, new ArrayList<T>());
            FileUtils.scanFiles(dirPath, this, scanFlags, parseResult);
            return parseResult.second;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int onScanFile(String path, int type, Object userData) {
            if (isArchiveFile(path, type)) {
                try {
                    final Pair<Integer, List<T>> parseResult = (Pair<Integer, List<T>>)userData;
                    final T result = parsePackage(mContext, path, parseResult.first, mFactory);
                    if (result != null) {
                        parseResult.second.add(result);
                    }
                } catch (Exception e) {
                    Log.e(PackageParser.class.getName(), "Couldn't parse a package archive file - " + path, e);
                }
            }

            return SC_CONTINUE;
        }

        /**
         * Tests if the file is a package archive file.
         * @param path The file path, passed earlier by {@link #onScanFile}.
         * @param type The file type, passed earlier by {@link #onScanFile}.
         * @return <tt>true</tt> if the file is a package archive file, <tt>false</tt> otherwise.
         */
        protected boolean isArchiveFile(String path, int type) {
            if (type != Dirent.DT_DIR) {
                final int index = FileUtils.findFileExtension(path);
                return (index >= 0 && "apk".regionMatches(true, 0, path, index, 3));
            }

            return false;
        }
    }

    /**
     * Class <tt>PackageNameFilter</tt> filtering {@link PackageInfo} objects based on their package name.
     */
    public static final class PackageNameFilter implements Filter<PackageInfo> {
        private final List<String> packages;

        /**
         * Constructor
         * @param excludingPackage The package name to excluding.
         * @see #PackageNameFilter(List)
         */
        public PackageNameFilter(String excludingPackage) {
            this.packages = Collections.singletonList(excludingPackage);
        }

        /**
         * Constructor
         * @param excludingPackages A <tt>List</tt> of package names to excluding.
         * @see #PackageNameFilter(String)
         */
        public PackageNameFilter(List<String> excludingPackages) {
            this.packages = excludingPackages;
        }

        @Override
        public boolean accept(PackageInfo packageInfo) {
            return (!packages.contains(packageInfo.packageName));
        }
    }

    /**
     * Class <tt>PackageNameComparator</tt> compares the package name of the {@link AppPackageInfo}.
     */
    public static final class PackageNameComparator implements Comparator<AppPackageInfo> {
        public static final PackageNameComparator sInstance = new PackageNameComparator();

        /**
         * This class cannot be instantiated.
         */
        private PackageNameComparator() {
        }

        @Override
        public int compare(AppPackageInfo one, AppPackageInfo another) {
            return one.packageInfo.packageName.compareTo(another.packageInfo.packageName);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private PackageUtils() {
    }
}
