package android.ext.content;

import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.content.image.BitmapDecoder.SizeParameters;
import android.ext.content.image.ImageBinder;
import android.ext.content.image.TransitionBinder;
import android.util.AttributeSet;
import android.util.Xml;

/**
 * Class XmlResources
 * @author Garfield
 * @version 1.5
 */
public final class XmlResources {
    /**
     * Loads a {@link Parameters} object from a xml resource.
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the parameters to load.
     * @return The <tt>Parameters</tt>.
     * @throws NotFoundException if the given <em>id</em> cannot be loaded.
     */
    public static Parameters loadParameters(Context context, int id) throws NotFoundException {
        return load(context, id, XmlParametersInflater.sInstance, (Object[])null);
    }

    /**
     * Loads a {@link Binder} object from a xml resource.
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the binder to load.
     * @return The <tt>Binder</tt>.
     * @throws NotFoundException if the given <em>id</em> cannot be loaded.
     */
    @SuppressWarnings("unchecked")
    public static <URI, Params, Image, T extends Binder<URI, Params, Image>> T loadBinder(Context context, int id) throws NotFoundException {
        return (T)load(context, id, XmlBinderInflater.sInstance, (Object[])null);
    }

    /**
     * Loads an new object from a xml resource.
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the object to load.
     * @param inflater The {@link XmlResourceInflater} to inflating XML data.
     * @param params The parameters to inflate. If no parameters, you can pass
     * <em>(Object[])null</em> instead of allocating an empty array.
     * @return The newly object.
     * @throws NotFoundException if the given <em>id</em> cannot be loaded.
     * @see XmlResourceInflater#inflate(Context, XmlPullParser, Object[])
     */
    public static <T> T load(Context context, int id, XmlResourceInflater<T> inflater, Object... params) throws NotFoundException {
        final XmlResourceParser parser = context.getResources().getXml(id);
        try {
            // Moves to the first start tag position.
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT) {
                // Empty loop
            }

            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            // Inflates the XML data.
            return inflater.inflate(context, parser, params);
        } catch (Exception e) {
            throw (NotFoundException)new NotFoundException("Couldn't load resources - ID #0x" + Integer.toHexString(id)).initCause(e);
        } finally {
            parser.close();
        }
    }

    /**
     * Equivalent to calling <tt>buildResourceUri(context.getPackageName(), resource)</tt>.
     * @param context The <tt>Context</tt>.
     * @param resource Type {@link Integer}, or {@link String} representation of the
     * resource, such as <tt>R.drawable.ic_launcher</tt> or <tt>"drawable/ic_launcher"</tt>.
     * @return The uri string.
     * @see #buildResourceUri(String, Object)
     */
    public static String buildResourceUri(Context context, Object resource) {
        return buildResourceUri(context.getPackageName(), resource);
    }

    /**
     * Constructs a scheme is "android.resource" uri string.
     * @param packageName The application's package name.
     * @param resource Type {@link Integer}, or {@link String} representation of the
     * resource, such as <tt>R.drawable.ic_launcher</tt> or <tt>"drawable/ic_launcher"</tt>.
     * @return The uri string.
     * @see #buildResourceUri(Context, Object)
     */
    public static String buildResourceUri(String packageName, Object resource) {
        final String res = resource.toString();
        return new StringBuilder(ContentResolver.SCHEME_ANDROID_RESOURCE.length() + packageName.length() + res.length() + 4).append(ContentResolver.SCHEME_ANDROID_RESOURCE).append("://").append(packageName).append('/').append(res).toString();
    }

    /**
     * Returns the corner radii associated with <em>attrs</em>.
     * @param res The {@link Resources} object containing the data.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @return The corner radii, array of 8 values, 4 pairs of [X,Y] radii.
     */
    public static float[] loadCornerRadii(Resources res, AttributeSet attrs) {
        final TypedArray a  = res.obtainAttributes(attrs, DRAWABLE_CORNERS_ATTRS);
        final float radius  = a.getDimension(0 /* android.R.attr.radius */, Float.NaN);
        final float[] radii = new float[8];

        if (Float.compare(radius, Float.NaN) != 0) {
            Arrays.fill(radii, radius);
        } else {
            radii[0] = radii[1] = a.getDimension(1 /* android.R.attr.topLeftRadius */, 0);
            radii[2] = radii[3] = a.getDimension(2 /* android.R.attr.topRightRadius */, 0);
            radii[6] = radii[7] = a.getDimension(3 /* android.R.attr.bottomLeftRadius */, 0);
            radii[4] = radii[5] = a.getDimension(4 /* android.R.attr.bottomRightRadius */, 0);
        }

        a.recycle();
        return radii;
    }

    /**
     * The <tt>XmlResourceInflater</tt> interface used to inflate the XML data.
     */
    public static interface XmlResourceInflater<T> {
        /**
         * Inflates a new object from the XML data.
         * @param context The <tt>Context</tt>.
         * @param parser The {@link XmlPullParser} to parsing the XML data.
         * The parser is already moved to the first start tag position.
         * @param params May be <tt>null</tt>. The parameters to inflate,
         * passed earlier by {@link XmlResources#load}.
         * @return The newly object.
         * @throws XmlPullParserException if the XML data cannot be parsed.
         * @throws ReflectiveOperationException if the constructor cannot be invoked.
         * @see XmlResources#load(Context, int, XmlResourceInflater, Object[])
         */
        T inflate(Context context, XmlPullParser parser, Object[] params) throws XmlPullParserException, ReflectiveOperationException;
    }

    /**
     * Class <tt>XmlParametersInflater</tt> is an implementation of a {@link XmlResourceInflater}.
     */
    private static final class XmlParametersInflater implements XmlResourceInflater<Parameters> {
        public static final XmlParametersInflater sInstance = new XmlParametersInflater();

        @Override
        public Parameters inflate(Context context, XmlPullParser parser, Object[] params) throws XmlPullParserException, ReflectiveOperationException {
            String name = parser.getName();
            if (name.equals("parameters") && (name = parser.getAttributeValue(null, "class")) == null) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": The <parameters> tag requires a valid 'class' attribute");
            }

            final AttributeSet attrs = Xml.asAttributeSet(parser);
            switch (name) {
            case "Parameters":
                return new Parameters(context, attrs);

            case "SizeParameters":
                return new SizeParameters(context, attrs);

            default:
                return (Parameters)Class.forName(name).getConstructor(Context.class, AttributeSet.class).newInstance(context, attrs);
            }
        }
    }

    /**
     * Class <tt>XmlBinderInflater</tt> is an implementation of a {@link XmlResourceInflater}.
     */
    private static final class XmlBinderInflater implements XmlResourceInflater<Object> {
        public static final XmlBinderInflater sInstance = new XmlBinderInflater();

        @Override
        public Object inflate(Context context, XmlPullParser parser, Object[] params) throws XmlPullParserException, ReflectiveOperationException {
            String name = parser.getName();
            if (name.equals("binder") && (name = parser.getAttributeValue(null, "class")) == null) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": The <binder> tag requires a valid 'class' attribute");
            }

            final AttributeSet attrs = Xml.asAttributeSet(parser);
            switch (name) {
            case "ImageBinder":
                return new ImageBinder<Object, Object, Object>(context, attrs);

            case "TransitionBinder":
                return new TransitionBinder<Object, Object, Object>(context, attrs);

            default:
                return Class.forName(name).getConstructor(Context.class, AttributeSet.class).newInstance(context, attrs);
            }
        }
    }

    /**
     * The drawable corners attributes.
     */
    private static final int[] DRAWABLE_CORNERS_ATTRS = {
        android.R.attr.radius,
        android.R.attr.topLeftRadius,
        android.R.attr.topRightRadius,
        android.R.attr.bottomLeftRadius,
        android.R.attr.bottomRightRadius,
    };

    /**
     * This utility class cannot be instantiated.
     */
    private XmlResources() {
    }
}
