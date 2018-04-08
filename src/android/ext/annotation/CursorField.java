package android.ext.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type used to indicate the class field is a
 * cursor field. The cursor field has a column name string.
 * <h3>Example</h3><pre>
 * public static final class User {
 *     {@code @CursorField("_id")}
 *     private String mId;
 * 
 *     {@code @CursorField("name")}
 *     private String mName;

 *     // No Cursor fields.
 *     private int mState;
 *     ...
 * }</pre>
 * @author Garfield
 * @version 1.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CursorField {
    /**
     * The name of the column.
     */
    public String value();
}
