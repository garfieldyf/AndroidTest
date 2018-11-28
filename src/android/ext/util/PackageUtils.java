package android.ext.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.ext.util.ArrayUtils.Filter;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.ext.util.Pools.Factory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Printer;

/**
 * Class PackageUtils
 * @author Garfield
 */
public final class PackageUtils {
    /**
     * Returns the {@link PackageInfo} object of the current application.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any
     * combination of <tt>PackageManager.GET_XXX</tt> constants.
     * @return The {@link PackageInfo} object.
     */
    public static PackageInfo myPackageInfo(Context context, int flags) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), flags);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Equivalent to calling <tt>getInstalledPackages(context, flags, AppPackageInfo.FACTORY, filter)</tt>.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination of
     * <tt>PackageManager.GET_XXX</tt> constants.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link AppPackageInfo} objects.
     * @see #getInstalledPackages(Context, int, Factory, Filter)
     * @see InstalledPackageFilter
     */
    public static List<AppPackageInfo> getInstalledPackages(Context context, int flags, Filter<PackageInfo> filter) {
        return getInstalledPackages(context, flags, AppPackageInfo.FACTORY, filter);
    }

    /**
     * Return a <tt>List</tt> of all packages that are installed on the device.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @param factory The {@link Factory} to create the {@link AppPackageInfo} or
     * subclass object.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link AppPackageInfo} objects.
     * @see #getInstalledPackages(Context, int, Filter)
     * @see InstalledPackageFilter
     */
    public static <T extends AppPackageInfo> List<T> getInstalledPackages(Context context, int flags, Factory<T> factory, Filter<PackageInfo> filter) {
        final List<PackageInfo> infos = context.getPackageManager().getInstalledPackages(flags);
        final int size = ArrayUtils.getSize(infos);
        final List<T> result = new ArrayList<T>(size);
        final Resources res  = context.getResources();
        if (filter != null) {
            for (int i = 0; i < size; ++i) {
                final PackageInfo info = infos.get(i);
                if (filter.accept(info)) {
                    result.add(newPackageInfo(context, res, info, factory));
                }
            }
        } else {
            for (int i = 0; i < size; ++i) {
                result.add(newPackageInfo(context, res, infos.get(i), factory));
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
            final AssetManager assets = new AssetManager();
            try {
                // Adds an additional archive file to the assets.
                assets.addAssetPath(archiveFile);
                packageInfo.applicationInfo.sourceDir = archiveFile;

                // Creates an AppPackageInfo or subclass object.
                return newPackageInfo(context, new Resources(assets, context.getResources().getDisplayMetrics(), null), packageInfo, factory);
            } finally {
                // Close the assets to avoid ProcessKiller
                // kill my process after unmounting usb disk.
                assets.close();
            }
        }

        return null;
    }

    public static void dump(Printer printer, Collection<? extends AppPackageInfo> infos) {
        final StringBuilder result = new StringBuilder(256);
        final int size = ArrayUtils.getSize(infos);
        DebugUtils.dumpSummary(printer, result, 140, " Dumping AppPackageInfos [ size = %d ] ", size);

        if (size > 0) {
            for (AppPackageInfo info : infos) {
                result.setLength(0);
                printer.println(info.dump(result.append("  ")).append(" }").toString());
            }
        }
    }

    /**
     * Create a {@link AppPackageInfo} or subclass object with the specified <em>packageInfo</em>.
     */
    private static <T extends AppPackageInfo> T newPackageInfo(Context context, Resources res, PackageInfo packageInfo, Factory<T> factory) {
        final T result = factory.newInstance();
        result.initialize(context, res, packageInfo);
        return result;
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

        public final void dump(Printer printer) {
            printer.println(dump(new StringBuilder(256)).append(" }").toString());
        }

        @Override
        public int compareTo(AppPackageInfo another) {
            return label.toString().compareTo(another.label.toString());
        }

        @Override
        public String toString() {
            return new StringBuilder(64).append(getClass().getSimpleName())
                .append('{').append(Integer.toHexString(hashCode())).append(' ')
                .append(packageInfo != null ? packageInfo.packageName : "N/A")
                .append('}').toString();
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
            this.label = StringUtils.trim(res.getText(packageInfo.applicationInfo.labelRes, packageInfo.packageName));
        }

        protected StringBuilder dump(StringBuilder out) {
            return out.append(getClass().getSimpleName())
                .append(" { package = ").append(packageInfo.packageName)
                .append(", version = ").append(packageInfo.versionName)
                .append(", label = ").append(label)
                .append(", flags = ").append(String.format("0x%08x", packageInfo.applicationInfo.flags))
                .append(", sourceDir = ").append(packageInfo.applicationInfo.sourceDir);
        }

        @SuppressWarnings("deprecation")
        private Drawable loadIcon(Context context, Resources res) {
            Drawable icon = null;
            if (packageInfo.applicationInfo.icon != 0) {
                icon = res.getDrawable(packageInfo.applicationInfo.icon);
            }

            return (icon != null ? icon : context.getPackageManager().getDefaultActivityIcon());
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
         * Equivalent to calling <tt>parsePackages(dirPath, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, 0, cancelable)</tt>.
         * @param dirPath The path of directory, must be absolute file path.
         * @param cancelable A {@link Cancelable} can be check the parse is cancelled, or <tt>null</tt> if none.
         * If the parse was cancelled before it completed normally then the returned value is undefined.
         * @return If the parse succeeded return a {@link List} of {@link AppPackageInfo} or subclass objects, <tt>null</tt> otherwise.
         * @see #parsePackages(String, int, int, Cancelable)
         * @see #parsePackages(String, int, int, Cancelable, List)
         */
        public final List<T> parsePackages(String dirPath, Cancelable cancelable) {
            final ParsedResult<T> parsedResult = new ParsedResult<T>(new ArrayList<T>(), 0, cancelable);
            return (FileUtils.scanFiles(dirPath, this, FileUtils.FLAG_IGNORE_HIDDEN_FILE | FileUtils.FLAG_SCAN_FOR_DESCENDENTS, parsedResult) == 0 ? parsedResult.result : null);
        }

        /**
         * Equivalent to calling <tt>parsePackages(dirPath, scanFlags, parseFlags, cancelable, new ArrayList())</tt>.
         * @param dirPath The path of directory, must be absolute file path.
         * @param scanFlags The scan flags. May be <tt>0</tt> or any combination of {@link #FLAG_IGNORE_HIDDEN_FILE},
         * {@link #FLAG_SCAN_FOR_DESCENDENTS}. See {@link FileUtils#scanFiles}.
         * @param parseFlags The parse flags. May be <tt>0</tt> or any combination of <tt>PackageManager.GET_XXX</tt> constants.
         * @param cancelable A {@link Cancelable} can be check the parse is cancelled, or <tt>null</tt> if none.
         * If the parse was cancelled before it completed normally then the returned value is undefined.
         * @return If the parse succeeded return a {@link List} of {@link AppPackageInfo} or subclass objects, <tt>null</tt> otherwise.
         * @see #parsePackages(String, Cancelable)
         * @see #parsePackages(String, int, int, Cancelable, List)
         */
        public final List<T> parsePackages(String dirPath, int scanFlags, int parseFlags, Cancelable cancelable) {
            final ParsedResult<T> parsedResult = new ParsedResult<T>(new ArrayList<T>(), parseFlags, cancelable);
            return (FileUtils.scanFiles(dirPath, this, scanFlags, parsedResult) == 0 ? parsedResult.result : null);
        }

        /**
         * Parses the package archive files in the specified <em>dirPath</em>.
         * @param dirPath The path of directory, must be absolute file path.
         * @param scanFlags The scan flags. May be <tt>0</tt> or any combination of {@link #FLAG_IGNORE_HIDDEN_FILE},
         * {@link #FLAG_SCAN_FOR_DESCENDENTS}. See {@link FileUtils#scanFiles}.
         * @param parseFlags The parse flags. May be <tt>0</tt> or any combination of <tt>PackageManager.GET_XXX</tt> constants.
         * @param cancelable A {@link Cancelable} can be check the parse is cancelled, or <tt>null</tt> if none.
         * If the parse was cancelled before it completed normally then the <em>outResults's</em> contents are undefined.
         * @param outResults A <tt>List</tt> to store the {@link AppPackageInfo} or subclass objects.
         * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an error code. See {@link ErrnoException}.
         * @see #parsePackages(String, Cancelable)
         * @see #parsePackages(String, int, int, Cancelable)
         */
        public final int parsePackages(String dirPath, int scanFlags, int parseFlags, Cancelable cancelable, List<? super T> outResults) {
            return FileUtils.scanFiles(dirPath, this, scanFlags, new ParsedResult<T>(outResults, parseFlags, cancelable));
        }

        @Override
        @SuppressWarnings("unchecked")
        public int onScanFile(String path, int type, Object cookie) {
            final ParsedResult<T> parsedResult = (ParsedResult<T>)cookie;
            if (isArchiveFile(path, type)) {
                final T result = parsePackage(mContext, path, parsedResult.parseFlags, mFactory);
                if (result != null) {
                    parsedResult.result.add(result);
                }
            }

            return (parsedResult.cancelable.isCancelled() ? SC_STOP : SC_CONTINUE);
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
     * Class <tt>InstalledPackageFilter</tt> filtering the installed application
     * {@link PackageInfo} objects (excluding the system and the updated system application).
     */
    public static final class InstalledPackageFilter implements Filter<PackageInfo> {
        private final List<String> packages;

        /**
         * Constructor
         * @see #InstalledPackageFilter(List)
         * @see #InstalledPackageFilter(String)
         */
        public InstalledPackageFilter() {
            this.packages = Collections.<String>emptyList();
        }

        /**
         * Constructor
         * @param excludingPackage The package name to excluding.
         * @see #InstalledPackageFilter()
         * @see #InstalledPackageFilter(List)
         */
        public InstalledPackageFilter(String excludingPackage) {
            this.packages = (TextUtils.isEmpty(excludingPackage) ? Collections.<String>emptyList() : Collections.singletonList(excludingPackage));
        }

        /**
         * Constructor
         * @param excludingPackages A <tt>List</tt> of package names to excluding.
         * @see #InstalledPackageFilter()
         * @see #InstalledPackageFilter(String)
         */
        public InstalledPackageFilter(List<String> excludingPackages) {
            this.packages = (excludingPackages != null ? excludingPackages : Collections.<String>emptyList());
        }

        @Override
        public boolean accept(PackageInfo packageInfo) {
            return (!packages.contains(packageInfo.packageName) && (packageInfo.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0);
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
     * Class <tt>ParsedResult</tt> store the {@link PackageParser} parsed result.
     */
    private static final class ParsedResult<T> {
        public final List<T> result;
        public final int parseFlags;
        public final Cancelable cancelable;

        @SuppressWarnings("unchecked")
        public ParsedResult(List<? super T> result, int parseFlags, Cancelable cancelable) {
            this.result = (List<T>)result;
            this.parseFlags = parseFlags;
            this.cancelable = CancelableWrapper.wrap(cancelable);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private PackageUtils() {
    }
}
