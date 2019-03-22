package android.ext.util;

import static android.content.pm.ApplicationInfo.FLAG_SYSTEM;
import static android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.ext.util.ArrayUtils.Filter;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.ext.util.Pools.Factory;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.util.Printer;

/**
 * Class PackageUtils
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class PackageUtils {
    /**
     * Returns the {@link PackageInfo} object of the current application.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any
     * combination of <tt>PackageManager.GET_XXX</tt> constants.
     * @return A <tt>PackageInfo</tt> object.
     */
    public static PackageInfo myPackageInfo(Context context, int flags) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), flags);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a <tt>List</tt> of all packages that are installed on the device.
     * @param pm The <tt>PackageManager</tt>.
     * @param flags Additional option flags. See {@link PackageManager#getInstalledPackages(int)}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link PackageInfo} objects.
     * @see #getInstalledPackages(Context, int, Factory, Filter)
     * @see InstalledPackageFilter
     */
    public static List<PackageInfo> getInstalledPackages(PackageManager pm, int flags, Filter<PackageInfo> filter) {
        final List<PackageInfo> result = pm.getInstalledPackages(flags);
        return (filter != null && result != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Returns a <tt>List</tt> of all packages that are installed on the device.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. See {@link PackageManager#getInstalledPackages(int)}.
     * @param factory The {@link Factory} to create the {@link AbsPackageInfo} subclass object.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of <tt>AbsPackageInfo</tt> subclass objects.
     * @see #getInstalledPackages(PackageManager, int, Filter)
     * @see InstalledPackageFilter
     */
    public static <T extends AbsPackageInfo> List<T> getInstalledPackages(Context context, int flags, Factory<T> factory, Filter<PackageInfo> filter) {
        return filterPackageItemInfos(context, context.getPackageManager().getInstalledPackages(flags), factory, filter);
    }

    /**
     * Returns a <tt>List</tt> of all application packages that are installed on the device.
     * @param pm The <tt>PackageManager</tt>.
     * @param flags Additional option flags. See {@link PackageManager#getInstalledApplications(int)}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the applications.
     * @return A <tt>List</tt> of {@link ApplicationInfo} objects.
     * @see #getInstalledApplications(Context, int, Factory, Filter)
     */
    public static List<ApplicationInfo> getInstalledApplications(PackageManager pm, int flags, Filter<ApplicationInfo> filter) {
        final List<ApplicationInfo> result = pm.getInstalledApplications(flags);
        return (filter != null && result != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Returns a <tt>List</tt> of all application packages that are installed on the device.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. See {@link PackageManager#getInstalledApplications(int)}.
     * @param factory The {@link Factory} to create the {@link AbsApplicationInfo} subclass object.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the applications.
     * @return A <tt>List</tt> of <tt>AbsApplicationInfo</tt> subclass objects.
     * @see #getInstalledApplications(PackageManager, int, Filter)
     */
    public static <T extends AbsApplicationInfo> List<T> getInstalledApplications(Context context, int flags, Factory<T> factory, Filter<ApplicationInfo> filter) {
        return filterPackageItemInfos(context, context.getPackageManager().getInstalledApplications(flags), factory, filter);
    }

    /**
     * Retrieve all activities that can be performed for the given intent.
     * @param pm The <tt>PackageManager</tt>.
     * @param intent The desired intent as per resolveActivity().
     * @param flags Additional option flags. See {@link PackageManager#queryIntentActivities}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the activities.
     * @return A <tt>List</tt> of {@link ResolveInfo} containing one entry for each matching Activity.
     * @see #queryIntentActivities(Context, Intent, int, Factory, Filter)
     */
    public static List<ResolveInfo> queryIntentActivities(PackageManager pm, Intent intent, int flags, Filter<ResolveInfo> filter) {
        final List<ResolveInfo> result = pm.queryIntentActivities(intent, flags);
        return (filter != null && result != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Retrieve all activities that can be performed for the given intent.
     * @param context The <tt>Context</tt>.
     * @param intent The desired intent as per resolveActivity().
     * @param flags Additional option flags. See {@link PackageManager#queryIntentActivities}.
     * @param factory The {@link Factory} to create the {@link AbsResolveInfo} subclass object.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the activities.
     * @return A <tt>List</tt> of <tt>AbsResolveInfo</tt> subclass objects.
     * @see #queryIntentActivities(PackageManager, Intent, int, Filter)
     */
    public static <T extends AbsResolveInfo> List<T> queryIntentActivities(Context context, Intent intent, int flags, Factory<T> factory, Filter<ResolveInfo> filter) {
        return filterPackageItemInfos(context, context.getPackageManager().queryIntentActivities(intent, flags), factory, filter);
    }

    /**
     * Parses a package archive file with the specified <em>archiveFile</em>.
     * @param context The <tt>Context</tt>.
     * @param archiveFile The path to the archive file, must be absolute file path.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @param factory The {@link Factory} to create the {@link AbsPackageInfo}
     * subclass object.
     * @return An <tt>AbsPackageInfo</tt> subclass object if the parse succeeded,
     * <tt>null</tt> otherwise.
     */
    public static <T extends AbsPackageInfo> T parsePackage(Context context, String archiveFile, int flags, Factory<T> factory) {
        DebugUtils.__checkError(factory == null, "factory == null");
        final PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(archiveFile, flags);
        if (packageInfo != null) {
            packageInfo.applicationInfo.sourceDir = archiveFile;
            return newPackageItemInfo(context, packageInfo, factory);
        }

        return null;
    }

    /**
     * Retrieve the application's label and icon associated with the specified <em>info</em>.
     * @param context The <tt>Context</tt>.
     * @param info The {@link PackageInfo} must be a package archive file's package info.
     * @return A <tt>Pair</tt> containing the application's icon and label.
     * @see PackageManager#getPackageArchiveInfo(String, int)
     */
    @SuppressWarnings("deprecation")
    public static Pair<CharSequence, Drawable> loadLabelAndIcon(Context context, PackageInfo info) {
        DebugUtils.__checkError(info.applicationInfo.sourceDir == null, "The info.applicationInfo.sourceDir == null");
        final AssetManager assets = new AssetManager();
        try {
            // Adds an additional archive file to the assets.
            assets.addAssetPath(info.applicationInfo.sourceDir);

            // Loads the application's icon.
            final Resources res = new Resources(assets, context.getResources().getDisplayMetrics(), null);
            final Drawable icon;
            if (info.applicationInfo.icon != 0) {
                icon = res.getDrawable(info.applicationInfo.icon);
            } else {
                icon = context.getPackageManager().getDefaultActivityIcon();
            }

            // Loads the application's label.
            final CharSequence label;
            if (info.applicationInfo.nonLocalizedLabel != null) {
                label = info.applicationInfo.nonLocalizedLabel;
            } else {
                label = res.getText(info.applicationInfo.labelRes, info.packageName);
            }

            return new Pair<CharSequence, Drawable>(StringUtils.trim(label), icon);
        } finally {
            // Close the assets to avoid ProcessKiller
            // kill my process after unmounting usb disk.
            assets.close();
        }
    }

    public static void dumpPackageItemInfos(Printer printer, Collection<? extends PackageItemInfo<?>> infos) {
        final int size = ArrayUtils.getSize(infos);
        if (size > 0) {
            final StringBuilder result = new StringBuilder(256);
            DebugUtils.dumpSummary(printer, result, 140, " Dumping %s collection [ size = %d ] ", infos.iterator().next().getClass().getSimpleName(), size);

            for (PackageItemInfo<?> info : infos) {
                result.setLength(0);
                printer.println(info.dumpImpl(result.append("  ")));
            }
        }
    }

    /**
     * Filter a <tt>List</tt> of package item infos with the specified <em>filter</em>.
     */
    private static List filterPackageItemInfos(Context context, List infos, Factory factory, Filter filter) {
        final List result;
        final int size = ArrayUtils.getSize(infos);
        if (filter != null) {
            result = new ArrayList(size);
            for (int i = 0; i < size; ++i) {
                final Object info = infos.get(i);
                if (filter.accept(info)) {
                    result.add(newPackageItemInfo(context, info, factory));
                }
            }
        } else {
            result = infos;
            for (int i = 0; i < size; ++i) {
                final Object info = infos.get(i);
                infos.set(i, newPackageItemInfo(context, info, factory));
            }
        }

        return result;
    }

    /**
     * Create a <tt>PackageItemInfo</tt> subclass object with the specified <em>info</em>.
     */
    private static <T extends PackageItemInfo> T newPackageItemInfo(Context context, Object info, Factory<T> factory) {
        final T result = factory.newInstance();
        result.initialize(context, info);
        return result;
    }

    /**
     * Base class containing information common to the package item.
     */
    public static abstract class PackageItemInfo<T> {
        /**
         * The package item info.
         */
        public T info;

        /**
         * Returns a package name of this item.
         * @return The package name.
         */
        public abstract String getPackageName();

        /**
         * Returns an {@link ApplicationInfo} of this item.
         * @return The <tt>ApplicationInfo</tt> object.
         */
        public abstract ApplicationInfo getApplicationInfo();

        /**
         * Retrieve the current graphical icon associated with this item.
         * @param pm The <tt>PackageManager</tt>.
         * @return A {@link Drawable} containing this item's icon.
         */
        public abstract Drawable loadIcon(PackageManager pm);

        /**
         * Retrieve the current label associated with this item.
         * @param pm The <tt>PackageManager</tt>.
         * @return A {@link CharSequence} containing the item's label.
         */
        public abstract CharSequence loadLabel(PackageManager pm);

        /**
         * Tests if the application is a system application.
         * @return <tt>true</tt> if the application is a system application, <tt>false</tt> otherwise.
         */
        public final boolean isSystem() {
            DebugUtils.__checkError(info == null, "This " + getClass().getSimpleName() + " uninitialized, did not call initialize()");
            return ((getApplicationInfo().flags & FLAG_SYSTEM) != 0);
        }

        /**
         * Tests if the application is an updated system application.
         * @return <tt>true</tt> if the application is an updated system application, <tt>false</tt> otherwise.
         */
        public final boolean isUpdatedSystem() {
            DebugUtils.__checkError(info == null, "This " + getClass().getSimpleName() + " uninitialized, did not call initialize()");
            return ((getApplicationInfo().flags & FLAG_UPDATED_SYSTEM_APP) != 0);
        }

        public final void dump(Printer printer) {
            printer.println(dumpImpl(new StringBuilder(256)));
        }

        /**
         * Initializes this item with the specified <em>info</em>.
         * @param context The <tt>Context</tt>.
         * @param info The package item info to initialize.
         */
        protected void initialize(Context context, T info) {
            this.info = info;
        }

        protected StringBuilder dump(StringBuilder out) {
            return out.append(" package = ").append(getPackageName())
                .append(", system = ").append(isSystem())
                .append(", updatedSystem = ").append(isUpdatedSystem());
        }

        /* package */ final String dumpImpl(StringBuilder out) {
            DebugUtils.__checkError(info == null, "This " + getClass().getSimpleName() + " uninitialized, did not call initialize()");
            return dump(out.append(getClass().getSimpleName()).append(" {")).append(" }").toString();
        }
    }

    /**
     * Class <tt>AbsPackageInfo</tt> contains an application's {@link PackageInfo}.
     */
    public static abstract class AbsPackageInfo extends PackageItemInfo<PackageInfo> {
        /**
         * Retrieve the application's label and icon associated with this package info.
         * The package {@link #info} must be a package archive file's {@link PackageInfo}.
         * @param context The <tt>Context</tt>.
         * @return A <tt>Pair</tt> containing the application's icon and label.
         * @see PackageManager#getPackageArchiveInfo(String, int)
         */
        public final Pair<CharSequence, Drawable> loadLabelAndIcon(Context context) {
            DebugUtils.__checkError(info == null, "This " + getClass().getSimpleName() + " uninitialized, did not call initialize()");
            return PackageUtils.loadLabelAndIcon(context, info);
        }

        @Override
        public String getPackageName() {
            return info.packageName;
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return info.applicationInfo;
        }

        @Override
        public Drawable loadIcon(PackageManager pm) {
            return info.applicationInfo.loadIcon(pm);
        }

        @Override
        public CharSequence loadLabel(PackageManager pm) {
            return info.applicationInfo.loadLabel(pm);
        }

        @Override
        protected StringBuilder dump(StringBuilder out) {
            return super.dump(out).append(", version = ").append(info.versionName).append(", sourceDir = ").append(info.applicationInfo.sourceDir);
        }
    }

    /**
     * Class <tt>AbsResolveInfo</tt> contains a component's {@link ResolveInfo}.
     */
    public static abstract class AbsResolveInfo extends PackageItemInfo<ResolveInfo> {
        @Override
        public String getPackageName() {
            DebugUtils.__checkError(info == null, "This " + getClass().getSimpleName() + " uninitialized, did not call initialize()");
            return (info.resolvePackageName != null ? info.resolvePackageName : getComponentInfo().packageName);
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return getComponentInfo().applicationInfo;
        }

        @Override
        public Drawable loadIcon(PackageManager pm) {
            return info.loadIcon(pm);
        }

        @Override
        public CharSequence loadLabel(PackageManager pm) {
            return info.loadLabel(pm);
        }

        @Override
        protected StringBuilder dump(StringBuilder out) {
            return super.dump(out).append(", name = ").append(getComponentInfo().name);
        }

        /**
         * Returns a {@link ComponentInfo} of this item.
         * @return The <tt>ComponentInfo</tt>.
         */
        protected ComponentInfo getComponentInfo() {
            DebugUtils.__checkError(info == null, "This " + getClass().getSimpleName() + " uninitialized, did not call initialize()");
            if (info.activityInfo != null) {
                return info.activityInfo;
            } else if (info.serviceInfo != null) {
                return info.serviceInfo;
            } else if (info.providerInfo != null) {
                return info.providerInfo;
            } else {
                throw new IllegalStateException("Missing ComponentInfo!");
            }
        }
    }

    /**
     * Class <tt>AbsApplicationInfo</tt> contains an application's {@link ApplicationInfo}.
     */
    public static abstract class AbsApplicationInfo extends PackageItemInfo<ApplicationInfo> {
        @Override
        public String getPackageName() {
            return info.packageName;
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return info;
        }

        @Override
        public Drawable loadIcon(PackageManager pm) {
            return info.loadIcon(pm);
        }

        @Override
        public CharSequence loadLabel(PackageManager pm) {
            return info.loadLabel(pm);
        }

        @Override
        protected StringBuilder dump(StringBuilder out) {
            return super.dump(out).append(", className = ").append(info.className);
        }
    }

    /**
     * Class <tt>PackageParser</tt> used to parse the package archive files.
     * <h5>PackageParser's generic types</h5></br>
     * <tt>T</tt>, A class that extends {@link AbsPackageInfo} that
     * will be the parser result type.
     * <h2>Usage</h2>
     * <p>Here is an example:</p><pre>
     * private static class MyPackageInfo extends AbsPackageInfo {
     *     {@code @Override}
     *     protected void initialize(Context context, PackageInfo packageInfo) {
     *         super.initialize(context, packageInfo);
     *         // Initialize ... ...
     *     }
     *
     *     public static final Factory&lt;MyPackageInfo&gt; FACTORY = new Factory&lt;MyPackageInfo&gt;() {
     *         {@code @Override}
     *         public MyPackageInfo newInstance() {
     *             return new MyPackageInfo();
     *         }
     *     };
     * }
     *
     * final List&lt;MyPackageInfo&gt; result = new PackageParser&lt;MyPackageInfo&gt;(context, MyPackageInfo.FACTORY)
     *     .addParseFlags(PackageManager.GET_ACTIVITIES)
     *     .addScanFlags(FileUtils.FLAG_SCAN_FOR_DESCENDENTS)
     *     .setCancelable(cancelable)
     *     .parse(dirPath1, dirPath2);</pre>
     */
    public static class PackageParser<T extends AbsPackageInfo> implements ScanCallback {
        private boolean __checkParseStatus;

        /**
         * The application <tt>Context</tt>.
         */
        public final Context mContext;

        private int mScanFlags;
        private int mParseFlags;
        private Cancelable mCancelable;
        private final Factory<? extends T> mFactory;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param factory The {@link Factory} to create the {@link AbsPackageInfo} subclass object.
         */
        public PackageParser(Context context, Factory<? extends T> factory) {
            mFactory = factory;
            mContext = context.getApplicationContext();
        }

        /**
         * Adds the scan flags to scan the package archive files.
         * @param flags The scan flags. May be <tt>0</tt> or any combination
         * of {@link #FLAG_IGNORE_HIDDEN_FILE}, {@link #FLAG_SCAN_FOR_DESCENDENTS}.
         * @return This parser.
         * @see FileUtils#scanFiles(String, ScanCallback, int, Object)
         */
        public final PackageParser<T> addScanFlags(int flags) {
            mScanFlags |= flags;
            return this;
        }

        /**
         * Adds the parse flags to parse the package info.
         * @param flags The parse flags. May be <tt>0</tt> or any
         * combination of <tt>PackageManager.GET_XXX</tt> constants.
         * @return This parser.
         * @see PackageManager#getPackageArchiveInfo(String, int)
         */
        public final PackageParser<T> addParseFlags(int flags) {
            mParseFlags |= flags;
            return this;
        }

        /**
         * Sets a {@link Cancelable} to be check the operation is cancelled.
         * @param cancelable A {@link Cancelable}. If the operation was cancelled
         * before it completed normally the parsed result is undefined.
         * @return This parser.
         */
        public final PackageParser<T> setCancelable(Cancelable cancelable) {
            mCancelable = cancelable;
            return this;
        }

        /**
         * Parses the package archive files with the specified <em>dirPaths</em>.
         * @param dirPaths An array of the directory paths, must be absolute file path.
         * @return If the parse succeeded return a {@link List} of {@link AbsPackageInfo}
         * subclass objects, <tt>null</tt> otherwise.
         * @see #parse(List, String[])
         */
        public final List<T> parse(String... dirPaths) {
            final List<T> result = new ArrayList<T>();
            return (parse(result, dirPaths) == 0 ? result : null);
        }

        /**
         * Parses the package archive files with the specified <em>dirPaths</em>.
         * @param dirPaths An array of the directory paths, must be absolute file path.
         * @param outResult A <tt>List</tt> to store the {@link AbsPackageInfo} subclass objects.
         * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an error code.
         * See {@link ErrnoException}.
         * @see #parse(String[])
         */
        public final int parse(List<? super T> outResult, String... dirPaths) {
            this.__checkParseStatus();
            int result = 0;
            mCancelable = FileUtils.wrap(mCancelable);
            for (int i = 0; i < dirPaths.length; ++i) {
                if ((result = FileUtils.scanFiles(dirPaths[i], this, mScanFlags, outResult)) != 0) {
                    break;
                }
            }

            return result;
        }

        @Override
        public int onScanFile(String path, int type, Object cookie) {
            if (isArchiveFile(path, type)) {
                final T result = parsePackage(mContext, path, mParseFlags, mFactory);
                if (result != null) {
                    ((List<T>)cookie).add(result);
                }
            }

            return (mCancelable.isCancelled() ? SC_STOP : SC_CONTINUE);
        }

        /**
         * Tests if the file is a package archive file.
         * @param path The file path, passed earlier by {@link #onScanFile}.
         * @param type The file type, passed earlier by {@link #onScanFile}.
         * @return <tt>true</tt> if the file is a package archive file, <tt>false</tt> otherwise.
         */
        protected boolean isArchiveFile(String path, int type) {
            if (type == Dirent.DT_REG) {
                final int index = FileUtils.findFileExtension(path);
                return (index >= 0 && "apk".regionMatches(true, 0, path, index, 3));
            }

            return false;
        }

        private void __checkParseStatus() {
            boolean checkParseStatus = false;
            checkParseStatus = this.__checkParseStatus;
            this.__checkParseStatus = true;

            if (checkParseStatus) {
                throw new AssertionError("Cannot parse: the PackageParser has already been parsed (a PackageParser can be parsed only once)");
            }
        }
    }

    /**
     * Class <tt>InstalledPackageFilter</tt> filtering the installed application
     * {@link PackageInfo} objects (excluding the system application).
     */
    public static final class InstalledPackageFilter implements Filter<PackageInfo> {
        private final List<String> mPackages;

        /**
         * Constructor
         * @param excludingPackages An array of package names to filter.
         * @see #InstalledPackageFilter(List)
         */
        public InstalledPackageFilter(String... excludingPackages) {
            mPackages = (excludingPackages != null ? Arrays.asList(excludingPackages) : Collections.<String>emptyList());
        }

        /**
         * Constructor
         * @param excludingPackages A <tt>List</tt> of package names to filter.
         * @see #InstalledPackageFilter(String[])
         */
        public InstalledPackageFilter(List<String> excludingPackages) {
            mPackages = (excludingPackages != null ? excludingPackages : Collections.<String>emptyList());
        }

        @Override
        public boolean accept(PackageInfo packageInfo) {
            return (!mPackages.contains(packageInfo.packageName) && (packageInfo.applicationInfo.flags & FLAG_SYSTEM) == 0);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private PackageUtils() {
    }
}
