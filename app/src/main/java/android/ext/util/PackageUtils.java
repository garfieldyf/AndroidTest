package android.ext.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.ext.util.ArrayUtils.Filter;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
     * Returns the package name associated with the specified <em>info</em>.
     * @param info The {@link ResolveInfo}.
     * @return The package name.
     */
    public static String getPackageName(ResolveInfo info) {
        return (info.resolvePackageName != null ? info.resolvePackageName : getComponentInfo(info).packageName);
    }

    /**
     * Returns the {@link ComponentInfo} associated with the specified <em>info</em>.
     * @param info The {@link ResolveInfo}.
     * @return The <tt>ComponentInfo</tt>.
     */
    public static ComponentInfo getComponentInfo(ResolveInfo info) {
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

    /**
     * Tests if the application is installed in the device's system image.
     * @param ai The {@link ApplicationInfo}.
     */
    public static boolean isSystemApp(ApplicationInfo ai) {
        return ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    /**
     * Tests if the application has been install as an update to a built-in system application.
     * @param ai The {@link ApplicationInfo}.
     */
    public static boolean isUpdatedSystemApp(ApplicationInfo ai) {
        return ((ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    /**
     * Install a package with the specified <em>packageFile</em>.
     * @param context The <tt>Context</tt>.
     * @param authority The authority of a {@link FileProvider} defined
     * in a <tt>&lt;provider&gt;</tt> element in your app's manifest.
     * @param packageFile The location of the package file to install.
     */
    public static void installPackage(Context context, String authority, File packageFile) {
        DebugUtils.__checkError(packageFile == null, "packageFile == null");
        final Uri packageUri;
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT < 24) {
            packageUri = Uri.fromFile(packageFile);
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            packageUri = FileProvider.getUriForFile(context, authority, packageFile);
        }

        DebugUtils.__checkDebug(true, "PackageUtils", "path = " + packageFile + ", uri = " + packageUri);
        intent.setDataAndType(packageUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Retrieve the application's {@link Resources} from a package archive file.
     * <p>Note: The returned <tt>Resources</tt> internal asset manager should be
     * close. For example: res.getAssets().close()</p>
     * @param pm The <tt>PackageManager</tt>.
     * @param archiveFile The full path to the archive file.
     * @return The <tt>Resources</tt> or <tt>null</tt> if the archive file could
     * not be loaded.
     */
    public static Resources getResourcesForArchiveFile(PackageManager pm, String archiveFile) {
        try {
            final PackageInfo pi = pm.getPackageArchiveInfo(archiveFile, 0);
            pi.applicationInfo.publicSourceDir = archiveFile;
            return pm.getResourcesForApplication(pi.applicationInfo);
        } catch (Exception e) {
            Log.e(PackageUtils.class.getName(), Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Returns a <tt>List</tt> of all packages that are installed on the device.
     * @param pm The <tt>PackageManager</tt>.
     * @param flags Additional option flags. See {@link PackageManager#getInstalledPackages(int)}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the packages.
     * @return A <tt>List</tt> of {@link PackageInfo} objects.
     */
    public static List<PackageInfo> getInstalledPackages(PackageManager pm, int flags, Filter<PackageInfo> filter) {
        final List<PackageInfo> result = pm.getInstalledPackages(flags);
        return (filter != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Returns a <tt>List</tt> of all application packages that are installed on the device.
     * @param pm The <tt>PackageManager</tt>.
     * @param flags Additional option flags. See {@link PackageManager#getInstalledApplications(int)}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the applications.
     * @return A <tt>List</tt> of {@link ApplicationInfo} objects.
     */
    public static List<ApplicationInfo> getInstalledApplications(PackageManager pm, int flags, Filter<ApplicationInfo> filter) {
        final List<ApplicationInfo> result = pm.getInstalledApplications(flags);
        return (filter != null ? ArrayUtils.filter(result, filter) : result);
    }

    /**
     * Retrieve all top-level launcher activities.
     * @param pm The <tt>PackageManager</tt>.
     * @param flags Additional option flags. See {@link PackageManager#queryIntentActivities}.
     * @param filter May be <tt>null</tt>. The {@link Filter} to filtering the activities.
     * @return A <tt>List</tt> of {@link ResolveInfo} containing one entry for each matching Activity.
     */
    public static List<ResolveInfo> queryLauncherActivities(PackageManager pm, int flags, Filter<ResolveInfo> filter) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> result = pm.queryIntentActivities(intent, flags);
        return (filter != null ? ArrayUtils.filter(result, filter) : result);
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
        return (filter != null ? ArrayUtils.filter(result, filter) : result);
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
                    .append(", system = ").append(isSystemApp(info.applicationInfo))
                    .append(", updatedSystem = ").append(isUpdatedSystemApp(info.applicationInfo))
                    .append(", sourceDir = ").append(info.applicationInfo.publicSourceDir)
                    .toString());
            }
        }
    }

    /**
     * Class <tt>PackageItemIcon</tt> used to store the package item's icon and label.
     */
    public static class PackageItemIcon {
        /**
         * The package item's icon.
         */
        public Drawable icon;

        /**
         * The package item's label.
         */
        public CharSequence label;

        /**
         * Constructor
         */
        public PackageItemIcon() {
        }

        /**
         * Constructor
         */
        public PackageItemIcon(Drawable icon, CharSequence label) {
            this.icon  = icon;
            this.label = label;
        }

        /**
         * Constructor
         * @param pm The <tt>PackageManager</tt>.
         * @param info The <tt>ResolveInfo</tt>.
         */
        public PackageItemIcon(PackageManager pm, ResolveInfo info) {
            this.icon  = info.loadIcon(pm);
            this.label = info.loadLabel(pm);
        }

        /**
         * Constructor
         * @param pm The <tt>PackageManager</tt>.
         * @param info The package archive file's {@link ApplicationInfo} and the
         * {@link ApplicationInfo#publicSourceDir ApplicationInfo's publicSourceDir}
         * must be contains the archive file full path.
         * @throws NameNotFoundException if the resources for the given application
         * could not be loaded.
         * @see PackageManager#getPackageArchiveInfo(String, int)
         */
        public PackageItemIcon(PackageManager pm, ApplicationInfo info) throws NameNotFoundException {
            DebugUtils.__checkError(TextUtils.isEmpty(info.publicSourceDir), "The info.publicSourceDir is empty");
            final Resources res = pm.getResourcesForApplication(info);
            initialize(pm, res, info);

            // Close the assets to avoid ProcessKiller
            // kill my process after unmounting usb disk.
            res.getAssets().close();
        }

        /**
         * Initializes this object with the specified <em>info</em>.
         * @param pm The <tt>PackageManager</tt>.
         * @param res The package archive file's <tt>Resources</tt>.
         * @param info The package archive file's <tt>ApplicationInfo</tt>.
         */
        @SuppressWarnings("deprecation")
        protected void initialize(PackageManager pm, Resources res, ApplicationInfo info) {
            this.icon  = (info.icon != 0 ? res.getDrawable(info.icon) : pm.getDefaultActivityIcon());
            this.label = (info.nonLocalizedLabel != null ? info.nonLocalizedLabel : StringUtils.trim(res.getText(info.labelRes, info.packageName)));
        }
    }

    /**
     * Class <tt>PackageParser</tt> used to parse the package archive files.
     * <h3>Usage</h3>
     * <p>Here is an example:</p><pre>
     * final List&lt;PackageInfo&gt; result = new PackageParser(context)
     *     .addParseFlags(PackageManager.GET_ACTIVITIES)
     *     .addScanFlags(FileUtils.FLAG_SCAN_FOR_DESCENDENTS)
     *     .setCancelable(cancelable)
     *     .parse(dirPath1, dirPath2, ...);</pre>
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
         * @param cancelable A {@link Cancelable}. If the operation is cancelled
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
         * @see #parse(Collection, String[])
         */
        public final List<PackageInfo> parse(String... dirPaths) {
            final List<PackageInfo> result = new ArrayList<PackageInfo>();
            return (parse(result, dirPaths) == 0 ? result : null);
        }

        /**
         * Parses the package archive files with the specified <em>dirPaths</em>.
         * @param dirPaths An array of the directory paths, must be absolute file path.
         * @param outInfos A <tt>Collection</tt> to store the {@link PackageInfo} objects.
         * @return Returns <tt>0</tt> if the operation succeeded, Otherwise returns an
         * error code. See {@link ErrnoException}.
         * @see #parse(String[])
         */
        public final int parse(Collection<PackageInfo> outInfos, String... dirPaths) {
            DebugUtils.__checkError(dirPaths == null, "Invalid parameter - The dirPaths is null");
            this.__checkParseStatus();
            int result  = 0;
            mCancelable = Cancelable.ofNullable(mCancelable);
            for (int i = 0; i < dirPaths.length; ++i) {
                if ((result = FileUtils.scanFiles(dirPaths[i], this, mScanFlags, outInfos)) != 0) {
                    break;
                }
            }

            return result;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public int onScanFile(String path, int type, Object cookie) {
            if (isArchiveFile(path, type)) {
                final PackageInfo packageInfo = mPackageManager.getPackageArchiveInfo(path, mParseFlags);
                if (packageInfo != null) {
                    packageInfo.applicationInfo.publicSourceDir = path;
                    ((Collection)cookie).add(packageInfo);
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
                throw new AssertionError("Cannot parse: the PackageParser has already been executed (a PackageParser can be executed only once)");
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private PackageUtils() {
    }
}
