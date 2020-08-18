package android.ext.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type used to indicate the class field is a
 * cursor field. The cursor field has a column name string.
 * <h3>Example</h3><pre>
 * public final class User {
 *     {@code @CursorField("_id")}
 *     [ <em>access modifier</em> ] long mId;
 *
 *     {@code @CursorField("name")}
 *     [ <em>access modifier</em> ] String mName;
 *
 *     // No Cursor fields.
 *     private int mState;
 *     ...
 * }
 *
 * final List&lt;User&gt; users = DatabaseUtils.parse(cursor, User.class);</pre>
 * @see DatabaseUtils#parse(Cursor, Class)
 * @author Garfield
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CursorField {
    /**
     * The name of the column.
     */
    public String value();
}
