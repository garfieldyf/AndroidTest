package android.ext.util;

import java.util.ArrayList;
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
public final class PackageUtils {
    /**
     * Returns the {@link PackageInfo} object of the current application.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any
     * combination of <tt>PackageManager.GET_XXX</tt> constants.
     * @return A {@link PackageInfo} object.
     */
    public static PackageInfo myPackageInfo(Context context, int flags) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), flags);
        } catch (NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Equivalent to calling <tt>getInstalledPackages(context.getPackageManager(), flags, filter)</tt>.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination of
     * <tt>PackageManager.GET_XXX</tt> constants.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link PackageInfo} objects.
     * @see #getInstalledPackages(PackageManager, int, Filter)
     * @see InstalledPackageFilter
     */
    public static List<PackageInfo> getInstalledPackages(Context context, int flags, Filter<PackageInfo> filter) {
        final List<PackageInfo> result = context.getPackageManager().getInstalledPackages(flags);
        return (filter != null && result != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Returns a <tt>List</tt> of all packages that are installed on the device.
     * @param pm The <tt>PackageManager</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination of
     * <tt>PackageManager.GET_XXX</tt> constants.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link PackageInfo} objects.
     * @see #getInstalledPackages(Context, int, Filter)
     * @see InstalledPackageFilter
     */
    public static List<PackageInfo> getInstalledPackages(PackageManager pm, int flags, Filter<PackageInfo> filter) {
        final List<PackageInfo> result = pm.getInstalledPackages(flags);
        return (filter != null && result != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Returns a <tt>List</tt> of all packages that are installed on the device.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @param factory The {@link Factory} to create the {@link AbsPackageInfo} subclass object.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link AbsPackageInfo} subclass objects.
     * @see InstalledPackageFilter
     */
    public static <T extends AbsPackageInfo> List<T> getInstalledPackages(Context context, int flags, Factory<T> factory, Filter<PackageInfo> filter) {
        final List<PackageInfo> infos = context.getPackageManager().getInstalledPackages(flags);
        final int size = ArrayUtils.getSize(infos);
        final List<T> result = new ArrayList<T>(size);
        if (filter != null) {
            for (int i = 0; i < size; ++i) {
                final PackageInfo info = infos.get(i);
                if (filter.accept(info)) {
                    result.add(newPackageInfo(context, info, factory));
                }
            }
        } else {
            for (int i = 0; i < size; ++i) {
                result.add(newPackageInfo(context, infos.get(i), factory));
            }
        }

        return result;
    }

    /**
     * Retrieve all displayed in the top-level launcher activities that can be performed.
     * @param context The <tt>Context</tt>.
     * @param flags Additional option flags. See <tt>queryIntentActivities</tt>.
     * @param factory The {@link Factory} to create the {@link AbsResolveInfo} subclass object.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the activities.
     * @return A <tt>List</tt> of {@link AbsResolveInfo} subclass objects.
     * @see PackageManager#queryIntentActivities(Intent, int)
     */
    public static <T extends AbsResolveInfo> List<T> queryLauncherActivities(Context context, int flags, Factory<T> factory, Filter<ResolveInfo> filter) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return queryIntentActivities(context, intent, flags, factory, filter);
    }

    /**
     * Retrieve all activities that can be performed for the given intent.
     * @param context The <tt>Context</tt>.
     * @param intent The desired intent as per resolveActivity().
     * @param flags Additional option flags. See <tt>queryIntentActivities</tt>.
     * @param factory The {@link Factory} to create the {@link AbsResolveInfo} subclass object.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the activities.
     * @return A <tt>List</tt> of {@link AbsResolveInfo} subclass objects.
     * @see PackageManager#queryIntentActivities(Intent, int)
     */
    public static <T extends AbsResolveInfo> List<T> queryIntentActivities(Context context, Intent intent, int flags, Factory<T> factory, Filter<ResolveInfo> filter) {
        final List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(intent, flags);
        final int size = ArrayUtils.getSize(infos);
        final List<T> result = new ArrayList<T>(size);
        if (filter != null) {
            for (int i = 0; i < size; ++i) {
                final ResolveInfo info = infos.get(i);
                if (filter.accept(info)) {
                    result.add(newResolveInfo(context, info, factory));
                }
            }
        } else {
            for (int i = 0; i < size; ++i) {
                result.add(newResolveInfo(context, infos.get(i), factory));
            }
        }

        return result;
    }

    /**
     * Parses a package archive file with the specified <em>archiveFile</em>.
     * @param context The <tt>Context</tt>.
     * @param archiveFile The path to the archive file, must be absolute file path.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @param factory The {@link Factory} to create the {@link AbsPackageInfo}
     * subclass object.
     * @return A {@link AbsPackageInfo} subclass object if the parse succeeded,
     * <tt>null</tt> otherwise.
     */
    public static <T extends AbsPackageInfo> T parsePackage(Context context, String archiveFile, int flags, Factory<T> factory) {
        DebugUtils.__checkError(factory == null, "factory == null");
        final PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(archiveFile, flags);
        if (packageInfo != null) {
            packageInfo.applicationInfo.sourceDir = archiveFile;
            return newPackageInfo(context, packageInfo, factory);
        }

        return null;
    }

    public static void dumpPackageInfos(Printer printer, Collection<? extends AbsPackageInfo> infos) {
        final StringBuilder result = new StringBuilder(256);
        final int size = ArrayUtils.getSize(infos);
        DebugUtils.dumpSummary(printer, result, 140, " Dumping PackageInfos [ size = %d ] ", size);

        if (size > 0) {
            for (AbsPackageInfo info : infos) {
                result.setLength(0);
                printer.println(info.dumpImpl(result.append("  ")));
            }
        }
    }

    public static void dumpResolveInfos(Printer printer, Collection<? extends AbsResolveInfo> infos) {
        final StringBuilder result = new StringBuilder(256);
        final int size = ArrayUtils.getSize(infos);
        DebugUtils.dumpSummary(printer, result, 140, " Dumping ResolveInfos [ size = %d ] ", size);

        if (size > 0) {
            for (AbsResolveInfo info : infos) {
                result.setLength(0);
                printer.println(info.dumpImpl(result.append("  ")));
            }
        }
    }

    /**
     * Create a {@link AbsPackageInfo} subclass object with the specified <em>packageInfo</em>.
     */
    private static <T extends AbsPackageInfo> T newPackageInfo(Context context, PackageInfo packageInfo, Factory<T> factory) {
        final T result = factory.newInstance();
        result.initialize(context, packageInfo);
        return result;
    }

    /**
     * Create a {@link AbsResolveInfo} subclass object with the specified <em>resolveInfo</em>.
     */
    private static <T extends AbsResolveInfo> T newResolveInfo(Context context, ResolveInfo resolveInfo, Factory<T> factory) {
        final T result = factory.newInstance();
        result.initialize(context, resolveInfo);
        return result;
    }

    /**
     * Class <tt>AbsPackageInfo</tt> contains an application's {@link PackageInfo}.
     */
    public static abstract class AbsPackageInfo {
        /**
         * The {@link PackageInfo}, parse from the <tt>AndroidManifest.xml</tt>.
         */
        public PackageInfo packageInfo;

        /**
         * Initializes this object with the specified <em>packageInfo</em>.
         * @param context The <tt>Context</tt>.
         * @param packageInfo The <tt>PackageInfo</tt> to initialize.
         */
        public void initialize(Context context, PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
        }

        /**
         * Retrieve the application's label and icon associated with the package info.
         * The {@link #packageInfo} must be a package archive file's package info.
         * @param context The <tt>Context</tt>.
         * @return A <tt>Pair</tt> containing the application's icon and label.
         * @see PackageManager#getPackageArchiveInfo(String, int)
         */
        public Pair<CharSequence, Drawable> load(Context context) {
            DebugUtils.__checkError(packageInfo == null, "The packageInfo uninitialized");
            DebugUtils.__checkError(packageInfo.applicationInfo.sourceDir == null, "The packageInfo.applicationInfo.sourceDir uninitialized");
            final AssetManager assets = new AssetManager();
            try {
                // Adds an additional archive file to the assets.
                assets.addAssetPath(packageInfo.applicationInfo.sourceDir);

                // Loads the application's label and icon.
                final Resources res = new Resources(assets, context.getResources().getDisplayMetrics(), null);
                return new Pair<CharSequence, Drawable>(loadLabel(res), loadIcon(context, res));
            } finally {
                // Close the assets to avoid ProcessKiller
                // kill my process after unmounting usb disk.
                assets.close();
            }
        }

        public final void dump(Printer printer) {
            DebugUtils.__checkError(packageInfo == null, "The packageInfo uninitialized");
            printer.println(dumpImpl(new StringBuilder(256)));
        }

        protected StringBuilder dump(StringBuilder out) {
            return out.append(" package = ").append(packageInfo.packageName)
                .append(", version = ").append(packageInfo.versionName)
                .append(", system = ").append((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                .append(", sourceDir = ").append(packageInfo.applicationInfo.sourceDir);
        }

        /* package */ final String dumpImpl(StringBuilder out) {
            return dump(out.append(getClass().getSimpleName()).append(" {")).append(" }").toString();
        }

        @SuppressWarnings("deprecation")
        private Drawable loadIcon(Context context, Resources res) {
            Drawable icon = null;
            if (packageInfo.applicationInfo.icon != 0) {
                icon = res.getDrawable(packageInfo.applicationInfo.icon);
            }

            return (icon != null ? icon : context.getPackageManager().getDefaultActivityIcon());
        }

        private CharSequence loadLabel(Resources res) {
            if (packageInfo.applicationInfo.nonLocalizedLabel != null) {
                return packageInfo.applicationInfo.nonLocalizedLabel;
            }

            return StringUtils.trim(res.getText(packageInfo.applicationInfo.labelRes, packageInfo.packageName));
        }
    }

    /**
     * Class <tt>AbsResolveInfo</tt> contains a component's {@link ResolveInfo}.
     */
    public static abstract class AbsResolveInfo {
        /**
         * The {@link ResolveInfo} that is returned from resolving an intent.
         */
        public ResolveInfo resolveInfo;

        /**
         * Initializes this object with the specified <em>resolveInfo</em>.
         * @param context The <tt>Context</tt>.
         * @param resolveInfo The <tt>ResolveInfo</tt> to initialize.
         */
        public void initialize(Context context, ResolveInfo resolveInfo) {
            this.resolveInfo = resolveInfo;
        }

        /**
         * Returns a {@link ComponentInfo} of this component.
         * @return The <tt>ComponentInfo</tt>.
         */
        public ComponentInfo getComponentInfo() {
            DebugUtils.__checkError(resolveInfo == null, "The resolveInfo uninitialized");
            if (resolveInfo.activityInfo != null) {
                return resolveInfo.activityInfo;
            } else if (resolveInfo.serviceInfo != null) {
                return resolveInfo.serviceInfo;
            } else if (resolveInfo.providerInfo != null) {
                return resolveInfo.providerInfo;
            } else {
                throw new IllegalStateException("Missing ComponentInfo!");
            }
        }

        /**
         * Returns a package name of this component.
         * @return The package name.
         */
        public final String getPackageName() {
            DebugUtils.__checkError(resolveInfo == null, "The resolveInfo uninitialized");
            return (resolveInfo.resolvePackageName != null ? resolveInfo.resolvePackageName : getComponentInfo().packageName);
        }

        public final void dump(Printer printer) {
            DebugUtils.__checkError(resolveInfo == null, "The resolveInfo uninitialized");
            printer.println(dumpImpl(new StringBuilder(196)));
        }

        protected StringBuilder dump(StringBuilder out) {
            final ComponentInfo info = getComponentInfo();
            return out.append(" name = ").append(info.name)
                .append(", packageName = ").append(getPackageName())
                .append(", process = ").append(info.processName)
                .append(", system = ").append((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        }

        /* package */ final String dumpImpl(StringBuilder out) {
            return dump(out.append(getClass().getSimpleName()).append(" {")).append(" }").toString();
        }
    }

    /**
     * Class <tt>PackageParser</tt> used to parse the package archive files.
     * @param <T> A class that extends {@link AbsPackageInfo} that will be
     * the parser result type.
     */
    public static class PackageParser<T extends AbsPackageInfo> implements ScanCallback {
        /**
         * The application <tt>Context</tt>.
         */
        public final Context mContext;

        /**
         * The {@link Factory} to create the {@link AbsPackageInfo} subclass object.
         */
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

        public final Request<T> parsePackages(String dirPath) {
            DebugUtils.__checkError(StringUtils.getLength(dirPath) == 0, "Invalid parameter - The dirPath is null or 0-length");
            return new Request<T>(dirPath, this);
        }

//        /**
//         * Equivalent to calling <tt>parsePackages(dirPath, FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, 0, cancelable)</tt>.
//         * @param dirPath The path of directory, must be absolute file path.
//         * @param cancelable A {@link Cancelable} can be check the parse is cancelled, or <tt>null</tt> if none.
//         * If the parse was cancelled before it completed normally the returned value is undefined.
//         * @return If the parse succeeded return a {@link List} of {@link AbsPackageInfo} subclass objects, <tt>null</tt> otherwise.
//         * @see #parsePackages(String, int, int, Cancelable)
//         * @see #parsePackages(String, int, int, Cancelable, List)
//         */
//        public final List<T> parsePackages(String dirPath, Cancelable cancelable) {
//            final ParsedResult parsedResult = new ParsedResult(new ArrayList<T>(), 0, cancelable);
//            return (FileUtils.scanFiles(dirPath, this, FileUtils.FLAG_IGNORE_HIDDEN_FILE | FileUtils.FLAG_SCAN_FOR_DESCENDENTS, parsedResult) == 0 ? parsedResult.result : null);
//        }
//
//        /**
//         * Equivalent to calling <tt>parsePackages(dirPath, scanFlags, parseFlags, cancelable, new ArrayList())</tt>.
//         * @param dirPath The path of directory, must be absolute file path.
//         * @param scanFlags The scan flags. May be <tt>0</tt> or any combination of {@link #FLAG_IGNORE_HIDDEN_FILE},
//         * {@link #FLAG_SCAN_FOR_DESCENDENTS}. See {@link FileUtils#scanFiles}.
//         * @param parseFlags The parse flags. May be <tt>0</tt> or any combination of <tt>PackageManager.GET_XXX</tt> constants.
//         * @param cancelable A {@link Cancelable} can be check the parse is cancelled, or <tt>null</tt> if none.
//         * If the parse was cancelled before it completed normally the returned value is undefined.
//         * @return If the parse succeeded return a {@link List} of {@link AbsPackageInfo} subclass objects, <tt>null</tt> otherwise.
//         * @see #parsePackages(String, Cancelable)
//         * @see #parsePackages(String, int, int, Cancelable, List)
//         */
//        public final List<T> parsePackages(String dirPath, int scanFlags, int parseFlags, Cancelable cancelable) {
//            final ParsedResult parsedResult = new ParsedResult(new ArrayList<T>(), parseFlags, cancelable);
//            return (FileUtils.scanFiles(dirPath, this, scanFlags, parsedResult) == 0 ? parsedResult.result : null);
//        }
//
//        /**
//         * Parses the package archive files in the specified <em>dirPath</em>.
//         * @param dirPath The path of directory, must be absolute file path.
//         * @param scanFlags The scan flags. May be <tt>0</tt> or any combination of {@link #FLAG_IGNORE_HIDDEN_FILE},
//         * {@link #FLAG_SCAN_FOR_DESCENDENTS}. See {@link FileUtils#scanFiles}.
//         * @param parseFlags The parse flags. May be <tt>0</tt> or any combination of <tt>PackageManager.GET_XXX</tt> constants.
//         * @param cancelable A {@link Cancelable} can be check the parse is cancelled, or <tt>null</tt> if none.
//         * If the parse was cancelled before it completed normally the <em>outResults's</em> contents are undefined.
//         * @param outResults A <tt>List</tt> to store the {@link AbsPackageInfo} subclass objects.
//         * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an error code. See {@link ErrnoException}.
//         * @see #parsePackages(String, Cancelable)
//         * @see #parsePackages(String, int, int, Cancelable)
//         */
//        public final int parsePackages(String dirPath, int scanFlags, int parseFlags, Cancelable cancelable, List<? super T> outResults) {
//            return FileUtils.scanFiles(dirPath, this, scanFlags, new ParsedResult(outResults, parseFlags, cancelable));
//        }

        @Override
        @SuppressWarnings("unchecked")
        public int onScanFile(String path, int type, Object cookie) {
            final Request<T> request = (Request<T>)cookie;
            if (isArchiveFile(path, type)) {
                final T result = parsePackage(mContext, path, request.mParseFlags, mFactory);
                if (result != null) {
                    request.mResult.add(result);
                }
            }

            return (request.mCancelable.isCancelled() ? SC_STOP : SC_CONTINUE);
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
    }

    public static final class Request<T extends AbsPackageInfo> {
        int mScanFlags;
        int mParseFlags;
        Cancelable mCancelable;
        List<? super T> mResult;

        final String mPath;
        final ScanCallback mCallback;

        Request(String dirPath, ScanCallback callback) {
            mPath = dirPath;
            mCallback = callback;
        }

        public final Request<T> addScanFlags(int flags) {
            mScanFlags |= flags;
            return this;
        }

        public final Request<T> addParseFlags(int flags) {
            mParseFlags |= flags;
            return this;
        }

        public final Request<T> setResult(List<? super T> result) {
            mResult = result;
            return this;
        }

        public final Request<T> setCancelable(Cancelable cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public final List<? super T> submit() {
            if (mResult == null) {
                mResult = new ArrayList<T>();
            }

            mCancelable = FileUtils.wrap(mCancelable);
            return (FileUtils.scanFiles(mPath, mCallback, mScanFlags, this) == 0 ? mResult : null);
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
            this.packages = Collections.singletonList(excludingPackage);
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

//    /**
//     * Class <tt>ParsedResult</tt> store the {@link PackageParser} parsed result.
//     */
//    @SuppressWarnings("rawtypes")
//    private static final class ParsedResult {
//        public final List result;
//        public final int parseFlags;
//        public final Cancelable cancelable;
//
//        public ParsedResult(List result, int parseFlags, Cancelable cancelable) {
//            this.result = result;
//            this.parseFlags = parseFlags;
//            this.cancelable = FileUtils.wrap(cancelable);
//        }
//    }

    /**
     * This utility class cannot be instantiated.
     */
    private PackageUtils() {
    }
}
