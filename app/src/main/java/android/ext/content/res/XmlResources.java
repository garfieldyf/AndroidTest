package android.ext.content.res;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.ext.image.binder.RoundedImageBinder;
import android.ext.image.binder.TransitionBinder;
import android.ext.image.params.Parameters;
import android.ext.image.params.ScaleParameters;
import android.ext.image.params.SizeParameters;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.util.AttributeSet;
import android.util.Xml;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class XmlResources
 * @author Garfield
 */
public final class XmlResources {
    /**
     * Loads an new object from a xml resource. The returns value may be
     * one of {@link Parameters} or {@link Binder} object.
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the object to load.
     * @throws NotFoundException if the given <em>id</em> cannot be load.
     */
    @SuppressWarnings("unchecked")
    public static <T> T load(Context context, int id) throws NotFoundException {
        return (T)load(context, id, XmlResources::inflate);
    }

    /**
     * Loads an new object from a xml resource.
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the object to load.
     * @param inflater The {@link XmlResourceInflater} to inflating XML data.
     * @return The newly object.
     * @throws NotFoundException if the given <em>id</em> cannot be load.
     * @see XmlResourceInflater#inflate(Context, XmlPullParser)
     */
    public static <T> T load(Context context, int id, XmlResourceInflater<T> inflater) throws NotFoundException {
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
            return inflater.inflate(context, parser);
        } catch (Exception e) {
            throw (NotFoundException)new NotFoundException("Couldn't load resources - ID #0x" + Integer.toHexString(id)).initCause(e);
        } finally {
            parser.close();
        }
    }

    /**
     * Equivalent to calling <tt>loadCornerRadii(res, attrs, new float[8])</tt>.
     * @param res The {@link Resources} object containing the data.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @return The corner radii, array of 8 values, 4 pairs of [X,Y] radii.
     * @see #loadCornerRadii(Resources, AttributeSet, float[])
     */
    public static float[] loadCornerRadii(Resources res, AttributeSet attrs) {
        return loadCornerRadii(res, attrs, new float[8]);
    }

    /**
     * Returns the corner radii associated with <em>attrs</em>. Each corner receives two
     * radius values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
     * <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @param res The {@link Resources} object containing the data.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @param outRadii Array of 8 values to store the radii, 4 pairs of [X,Y] radii.
     * @return The <em>outRadii</em>.
     * @see #loadCornerRadii(Resources, AttributeSet)
     */
    @SuppressLint("ResourceType")
    public static float[] loadCornerRadii(Resources res, AttributeSet attrs, float[] outRadii) {
        DebugUtils.__checkError(outRadii == null || outRadii.length < 8, "outRadii == null || outRadii.length < 8");
        final TypedArray a = res.obtainAttributes(attrs, DRAWABLE_CORNERS_ATTRS);
        final float radius = a.getDimension(0 /* android.R.attr.radius */, Float.NaN);

        if (Float.compare(radius, Float.NaN) != 0) {
            Arrays.fill(outRadii, radius);
        } else {
            outRadii[0] = outRadii[1] = a.getDimension(1 /* android.R.attr.topLeftRadius */, 0);
            outRadii[2] = outRadii[3] = a.getDimension(2 /* android.R.attr.topRightRadius */, 0);
            outRadii[6] = outRadii[7] = a.getDimension(3 /* android.R.attr.bottomLeftRadius */, 0);
            outRadii[4] = outRadii[5] = a.getDimension(4 /* android.R.attr.bottomRightRadius */, 0);
        }

        a.recycle();
        return outRadii;
    }

    /**
     * Inflates a new object from the XML data.
     * @param context The <tt>Context</tt>.
     * @param parser The {@link XmlPullParser} to parsing the XML data.
     * The parser is must be move to the first start tag position.
     * @return The newly object.
     * @throws XmlPullParserException if the XML data cannot be parsed.
     * @throws ReflectiveOperationException if the constructor cannot be invoked.
     */
    public static Object inflate(Context context, XmlPullParser parser) throws XmlPullParserException, ReflectiveOperationException {
        String name = parser.getName();
        if (name.equals("binder") || name.equals("parameters")) {
            final String tagName = name;
            if ((name = parser.getAttributeValue(null, "class")) == null) {
                throw new XmlPullParserException(parser.getPositionDescription() + ": The <" + tagName + "> tag requires a valid 'class' attribute");
            }
        }

        final AttributeSet attrs = Xml.asAttributeSet(parser);
        switch (name) {
        /* --------------- parameters --------------- */
        case "Parameters":
            return new Parameters(context, attrs);

        case "SizeParameters":
            return new SizeParameters(context, attrs);

        case "ScaleParameters":
            return new ScaleParameters(context, attrs);

        /* ---------------- binders ----------------- */
        case "TransitionBinder":
            return new TransitionBinder(context, attrs);

        case "RoundedImageBinder":
            return new RoundedImageBinder(context, attrs);

        default:
            return ClassUtils.newInstance(name, new Class[] { Context.class, AttributeSet.class }, context, attrs);
        }
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
         * @return The newly object.
         * @throws XmlPullParserException if the XML data cannot be parsed.
         * @throws ReflectiveOperationException if the constructor cannot be invoked.
         */
        T inflate(Context context, XmlPullParser parser) throws XmlPullParserException, ReflectiveOperationException;
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
