package android.ext.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.ext.util.ArrayUtils.Filter;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
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
     * @see InstalledApplicationFilter
     */
    public static List<PackageInfo> getInstalledPackages(PackageManager pm, int flags, Filter<ApplicationInfo> filter) {
        final List<PackageInfo> result = pm.getInstalledPackages(flags);
        if (filter != null && result != null) {
            final Iterator<PackageInfo> itor = result.iterator();
            while (itor.hasNext()) {
                if (!filter.accept(itor.next().applicationInfo)) {
                    itor.remove();
                }
            }
        }

        return result;
    }

    /**
     * Returns a <tt>List</tt> of all application packages that are installed on the device.
     * @param pm The <tt>PackageManager</tt>.
     * @param flags Additional option flags. See {@link PackageManager#getInstalledApplications(int)}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the applications.
     * @return A <tt>List</tt> of {@link ApplicationInfo} objects.
     * @see InstalledApplicationFilter
     */
    public static List<ApplicationInfo> getInstalledApplications(PackageManager pm, int flags, Filter<ApplicationInfo> filter) {
        final List<ApplicationInfo> result = pm.getInstalledApplications(flags);
        return (filter != null && result != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Retrieve all activities that can be performed for the given intent.
     * @param pm The <tt>PackageManager</tt>.
     * @param intent The desired intent as per resolveActivity().
     * @param flags Additional option flags. See {@link PackageManager#queryIntentActivities}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the activities.
     * @return A <tt>List</tt> of {@link ResolveInfo} containing one entry for each matching Activity.
     */
    public static List<ResolveInfo> queryIntentActivities(PackageManager pm, Intent intent, int flags, Filter<ResolveInfo> filter) {
        final List<ResolveInfo> result = pm.queryIntentActivities(intent, flags);
        return (filter != null && result != null ? ArrayUtils.filter(result, filter) : result);
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

    public static void dumpPackageInfos(Printer printer, Collection<PackageInfo> infos) {
        final StringBuilder result = new StringBuilder(384);
        final int size = ArrayUtils.getSize(infos);
        DebugUtils.dumpSummary(printer, result, 140, " Dumping PackageInfos [ size = %d ] ", size);
        if (size > 0) {
            for (PackageInfo info : infos) {
                result.setLength(0);
                printer.println(result.append("  package = ").append(info.packageName)
                    .append(", version = ").append(info.versionName)
                    .append(", system = ").append((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                    .append(", updatedSystem = ").append((info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0)
                    .append(", sourceDir = ").append(info.applicationInfo.sourceDir)
                    .toString());
            }
        }
    }

    /**
     * Class <tt>PackageParser</tt> used to parse the package archive files.
     * <h2>Usage</h2>
     * <p>Here is an example:</p><pre>
     * final List&lt;PackageInfo&gt; result = new PackageParser(context)
     *     .addParseFlags(PackageManager.GET_ACTIVITIES)
     *     .addScanFlags(FileUtils.FLAG_SCAN_FOR_DESCENDENTS)
     *     .setCancelable(cancelable)
     *     .parse(dirPath);</pre>
     */
    public static class PackageParser implements ScanCallback {
        private boolean __checkParseStatus;
        private int mScanFlags;
        private int mParseFlags;
        private Cancelable mCancelable;
        private final PackageManager mPackageManager;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public PackageParser(Context context) {
            mPackageManager = context.getPackageManager();
        }

        /**
         * Adds the scan flags to scan the package archive files.
         * @param flags The scan flags. May be <tt>0</tt> or any combination of
         * {@link #FLAG_IGNORE_HIDDEN_FILE}, {@link #FLAG_SCAN_FOR_DESCENDENTS}.
         * @return This parser.
         * @see FileUtils#scanFiles(String, ScanCallback, int, Object)
         */
        public final PackageParser addScanFlags(int flags) {
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
        public final PackageParser addParseFlags(int flags) {
            mParseFlags |= flags;
            return this;
        }

        /**
         * Sets a {@link Cancelable} to be check the operation is cancelled.
         * @param cancelable A {@link Cancelable}. If the operation was cancelled
         * before it completed normally the parsed result is undefined.
         * @return This parser.
         */
        public final PackageParser setCancelable(Cancelable cancelable) {
            mCancelable = cancelable;
            return this;
        }

        /**
         * Parses the package archive files with the specified <em>dirPaths</em>.
         * @param dirPaths An array of the directory paths, must be absolute file path.
         * @return If the parse succeeded return a {@link List} of {@link PackageInfo}
         * objects, <tt>null</tt> otherwise.
         * @see #parse(List, String[])
         */
        public final List<PackageInfo> parse(String... dirPaths) {
            final List<PackageInfo> result = new ArrayList<PackageInfo>();
            return (parse(result, dirPaths) == 0 ? result : null);
        }

        /**
         * Parses the package archive files with the specified <em>dirPaths</em>.
         * @param dirPaths An array of the directory paths, must be absolute file path.
         * @param outResult A <tt>List</tt> to store the {@link PackageInfo} objects.
         * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an
         * error code. See {@link ErrnoException}.
         * @see #parse(String[])
         */
        public final int parse(List<PackageInfo> outResult, String... dirPaths) {
            DebugUtils.__checkError(dirPaths == null, "Invalid parameter - The dirPaths is null");
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
        @SuppressWarnings("unchecked")
        public int onScanFile(String path, int type, Object cookie) {
            if (isArchiveFile(path, type)) {
                final PackageInfo packageInfo = mPackageManager.getPackageArchiveInfo(path, mParseFlags);
                if (packageInfo != null) {
                    packageInfo.applicationInfo.sourceDir = path;
                    ((List<PackageInfo>)cookie).add(packageInfo);
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
     * Class <tt>InstalledApplicationFilter</tt> filtering the installed applications (excluding the system application).
     */
    public static final class InstalledApplicationFilter implements Filter<ApplicationInfo> {
        private final Collection<String> mPackages;

        /**
         * Constructor
         * @param excludingPackages An array of package names to filter.
         * @see #InstalledApplicationFilter(Collection)
         */
        public InstalledApplicationFilter(String... excludingPackages) {
            mPackages = (excludingPackages != null ? Arrays.asList(excludingPackages) : Collections.<String>emptyList());
        }

        /**
         * Constructor
         * @param excludingPackages The <tt>Collection</tt> of package names to filter.
         * @see #InstalledApplicationFilter(String[])
         */
        public InstalledApplicationFilter(Collection<String> excludingPackages) {
            mPackages = (excludingPackages != null ? excludingPackages : Collections.<String>emptyList());
        }

        @Override
        public boolean accept(ApplicationInfo applicationInfo) {
            return (!mPackages.contains(applicationInfo.packageName) && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private PackageUtils() {
    }
}
