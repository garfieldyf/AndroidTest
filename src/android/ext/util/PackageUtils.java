package android.ext.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.ext.util.FileUtils.Dirent;
import android.ext.util.FileUtils.ScanCallback;
import android.ext.util.Pools.Factory;
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
     * Equivalent to calling <tt>parsePackage(context, sourceFile, 0, PackageArchiveInfo.FACTORY)</tt>.
     * @param context The <tt>Context</tt>.
     * @param sourceFile The path to the archive file, must be absolute file path.
     * @return A {@link PackageArchiveInfo} object.
     * @throws Exception if the package archive file cannot be parsed.
     * @see #parsePackage(Context, String, int, Factory)
     */
    public static PackageArchiveInfo parsePackage(Context context, String sourceFile) throws Exception {
        return parsePackage(context, sourceFile, 0, PackageArchiveInfo.FACTORY);
    }

    /**
     * Parses a package archive file with the specified <em>sourceFile</em>.
     * @param context The <tt>Context</tt>.
     * @param sourceFile The path to the archive file, must be absolute file path.
     * @param flags Additional option flags. May be <tt>0</tt> or any combination
     * of <tt>PackageManager.GET_XXX</tt> constants.
     * @param factory The {@link Factory} to create the {@link PackageArchiveInfo}
     * or subclass object.
     * @return A {@link PackageArchiveInfo} or subclass object.
     * @throws Exception if the package archive file cannot be parsed.
     * @see #parsePackage(Context, String)
     */
    public static <T extends PackageArchiveInfo> T parsePackage(Context context, String sourceFile, int flags, Factory<T> factory) throws Exception {
        DebugUtils.__checkError(factory == null, "factory == null");
        final Resources res = createResources(context, sourceFile);
        try {
            // Retrieve the package information from the package archive file.
            final PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(sourceFile, flags);
            packageInfo.applicationInfo.sourceDir = sourceFile;

            // Creates a PackageArchiveInfo or subclass object.
            final T result = factory.newInstance();
            result.initialize(context, res, packageInfo);
            return result;
        } finally {
            // Close the underlying AssetManager for the res to avoid
            // ProcessKiller kill my process after unmounting usb disk.
            res.getAssets().close();
        }
    }

    public static void dump(Printer printer, Collection<? extends PackageArchiveInfo> infos) {
        final StringBuilder result = new StringBuilder(256);
        final int size = ArrayUtils.getSize(infos);
        DebugUtils.dumpSummary(printer, result, 140, " Dumping PackageArchiveInfo collection [ size = %d ] ", size);

        for (PackageArchiveInfo info : infos) {
            result.setLength(0);
            printer.println(info.dump(result.append("  ")).append(" }").toString());
        }
    }

    /**
     * Create a new <tt>Resources</tt> object base on an existing <em>sourceFile</em>.
     */
    private static Resources createResources(Context context, String sourceFile) throws Exception {
        final AssetManager assets = AssetManager.class.newInstance();
        sAddAssetPath.invoke(assets, sourceFile);
        return new Resources(assets, context.getResources().getDisplayMetrics(), null);
    }

    /**
     * Class <tt>PackageArchiveInfo</tt> contains an application
     * package information defined in a package archive file.
     */
    public static class PackageArchiveInfo implements Comparable<PackageArchiveInfo> {
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

        @Override
        public String toString() {
            return packageInfo.packageName;
        }

        @Override
        public int compareTo(PackageArchiveInfo another) {
            return label.toString().compareTo(another.label.toString());
        }

        /**
         * Returns the application's label that removing the white
         * spaces and control characters from the start and end.
         */
        public final CharSequence getTrimmedLabel() {
            return StringUtils.trim(label);
        }

        /**
         * Initializes this object with the specified <em>packageInfo</em>.
         * @param context The <tt>Context</tt>.
         * @param res The <tt>Resources</tt> to load the application's label and icon.
         * @param packageInfo The <tt>PackageInfo</tt> to set.
         */
        protected void initialize(Context context, Resources res, PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
            this.icon  = loadIcon(context, res);
            this.label = loadLabel(context, res);
        }

        /**
         * Loads the application's icon with the arguments supplied to the {@link #packageInfo}.
         * @param context The <tt>Context</tt>.
         * @param res The <tt>Resources</tt> to load the icon.
         * @return A <tt>Drawable</tt> containing the application's icon. If the application does
         * not have an icon, the default icon is returned.
         * @see #loadLabel(Context, Resources)
         */
        @SuppressWarnings("deprecation")
        protected Drawable loadIcon(Context context, Resources res) {
            final Drawable icon = res.getDrawable(packageInfo.applicationInfo.icon);
            return (icon != null ? icon : context.getPackageManager().getDefaultActivityIcon());
        }

        /**
         * Loads the application's label with the arguments supplied to the {@link #packageInfo}.
         * @param context The <tt>Context</tt>.
         * @param res The <tt>Resources</tt> to load the label.
         * @return A <tt>CharSequence</tt> containing the application's label. If the application
         * does not have a label, the package name is returned.
         * @see #loadIcon(Context, Resources)
         */
        protected CharSequence loadLabel(Context context, Resources res) {
            final CharSequence label = res.getText(packageInfo.applicationInfo.labelRes);
            return (TextUtils.isEmpty(label) ? packageInfo.packageName : label);
        }

        public final void dump(Printer printer) {
            printer.println(dump(new StringBuilder(256)).append(" }").toString());
        }

        protected StringBuilder dump(StringBuilder out) {
            return out.append(getClass().getSimpleName())
                .append(" { package = ").append(packageInfo.packageName)
                .append(", version = ").append(packageInfo.versionName)
                .append(", label = ").append(label)
                .append(", sourceFile = ").append(packageInfo.applicationInfo.sourceDir);
        }

        public static final Factory<PackageArchiveInfo> FACTORY = new Factory<PackageArchiveInfo>() {
            @Override
            public PackageArchiveInfo newInstance() {
                return new PackageArchiveInfo();
            }
        };
    }

    /**
     * Class <tt>PackageParser</tt> used to parse the package archive files.
     * @param <T> A class that extends {@link PackageArchiveInfo} that will
     * be the parser result type.
     */
    public static final class PackageParser<T extends PackageArchiveInfo> implements ScanCallback {
        private final Context mContext;
        private final Factory<T> mFactory;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         * @param factory The {@link Factory} to create the
         * {@link PackageArchiveInfo} or subclass object.
         */
        public PackageParser(Context context, Factory<T> factory) {
            mFactory = factory;
            mContext = context.getApplicationContext();
        }

        /**
         * Equivalent to calling <tt>parsePackages(FLAG_IGNORE_HIDDEN_FILE | FLAG_SCAN_FOR_DESCENDENTS, 0, dirPaths)</tt>.
         * @param dirPaths An array of directories.
         * @return A <tt>List</tt> of {@link PackageArchiveInfo} or subclass objects.
         * @see #parsePackages(int, int, String[])
         */
        public final List<T> parsePackages(String... dirPaths) {
            return parsePackages(FileUtils.FLAG_IGNORE_HIDDEN_FILE | FileUtils.FLAG_SCAN_FOR_DESCENDENTS, 0, dirPaths);
        }

        /**
         * Parses the package archive files in the specified <em>dirPaths</em>.
         * @param scanFlags The scan flags. May be <tt>0</tt> or any combination
         * of {@link FileUtils#FLAG_IGNORE_HIDDEN_FILE FLAG_IGNORE_HIDDEN_FILE},
         * {@link FileUtils#FLAG_SCAN_FOR_DESCENDENTS FLAG_SCAN_FOR_DESCENDENTS}.
         * @param parseFlags The parse flags. May be <tt>0</tt> or any combination
         * of <tt>PackageManager.GET_XXX</tt> constants.
         * @param dirPaths An array of directory paths.
         * @return A <tt>List</tt> of {@link PackageArchiveInfo} or subclass objects.
         * @see #parsePackages(String[])
         */
        public final List<T> parsePackages(int scanFlags, int parseFlags, String... dirPaths) {
            final Pair<Integer, List<T>> parseResult = new Pair<Integer, List<T>>(parseFlags, new ArrayList<T>());
            for (int i = 0, size = ArrayUtils.getSize(dirPaths); i < size; ++i) {
                FileUtils.scanFiles(dirPaths[i], this, scanFlags, parseResult);
            }

            return parseResult.second;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int onScanFile(String path, int type, Object userData) {
            if (accept(path, type)) {
                try {
                    final Pair<Integer, List<T>> parseResult = (Pair<Integer, List<T>>)userData;
                    parseResult.second.add(parsePackage(mContext, path, parseResult.first, mFactory));
                } catch (Exception e) {
                    Log.e(PackageParser.class.getName(), "Couldn't parse archive file - " + path, e);
                }
            }

            return SC_CONTINUE;
        }

        private static boolean accept(String path, int type) {
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

    private static final Method sAddAssetPath;

    static {
        try {
            sAddAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath", String.class);
            sAddAssetPath.setAccessible(true);
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
