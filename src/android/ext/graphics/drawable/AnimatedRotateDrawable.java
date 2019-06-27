package android.ext.graphics.drawable;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;

/**
 * Class AnimatedRotateDrawable
 * @author Garfield
 */
public class AnimatedRotateDrawable extends RotateDrawable {
    private static final int DEFAULT_INCREMENT = 100;
    private int mIncrement;

    /**
     * Constructor
     */
    public AnimatedRotateDrawable() {
        mIncrement = DEFAULT_INCREMENT;
    }

    /**
     * Constructor
     * @param drawable The drawable to rotate.
     */
    public AnimatedRotateDrawable(Drawable drawable) {
        this(drawable, DEFAULT_INCREMENT);
    }

    /**
     * Constructor
     * @param drawable The drawable to rotate.
     * @param increment The level increment.
     */
    @SuppressLint("NewApi")
    public AnimatedRotateDrawable(Drawable drawable, int increment) {
        setDrawable(drawable);
        mIncrement = increment;
    }

    /**
     * Constructor
     * @param res The <tt>Resources</tt>.
     * @param id The resource id of drawable.
     */
    @SuppressWarnings("deprecation")
    public AnimatedRotateDrawable(Resources res, int id) {
        this(res.getDrawable(id), DEFAULT_INCREMENT);
    }

    /**
     * Constructor
     * @param res The <tt>Resources</tt>.
     * @param id The resource id of drawable.
     * @param increment The level increment.
     */
    @SuppressWarnings("deprecation")
    public AnimatedRotateDrawable(Resources res, int id, int increment) {
        this(res.getDrawable(id), increment);
    }

    /**
     * Returns the current level increment.
     * @return The level increment.
     * @see #setLevelIncrement(int)
     */
    public int getLevelIncrement() {
        return mIncrement;
    }

    /**
     * Sets the level increment for this drawable.
     * @param increment The level increment.
     * @see #getLevelIncrement()
     */
    public void setLevelIncrement(int increment) {
        mIncrement = increment;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        setLevel((getLevel() + mIncrement) % 10000);
    }
}
