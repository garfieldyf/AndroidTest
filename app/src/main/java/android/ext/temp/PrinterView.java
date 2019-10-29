package android.ext.temp;

import android.content.Context;
import android.ext.util.StringUtils;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.TextView;

/**
 * Class PrinterView
 * @author Garfield
 */
public class PrinterView extends TextView implements Printer {
    public PrinterView(Context context) {
        super(context);
    }

    public PrinterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrinterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Adds the specified markup object to the range
     * <tt>start...end</tt> of the text.
     * @param span The span to add.
     * @param start The start position of the text.
     * @param end The end position of the text.
     * @param flags The flags. See {@link Spannable} constants.
     * @see Spannable#setSpan(Object, int, int, int)
     * @see #removeSpan(Object)
     */
    public void setSpan(Object span, int start, int end, int flags) {
        getText().setSpan(span, start, end, flags);
    }

    /**
     * Removes the specified markup object from the range of text.
     * @param span The span to remove.
     * @see Spannable#removeSpan(Object)
     * @see #setSpan(Object, int, int, int)
     */
    public void removeSpan(Object span) {
        getText().removeSpan(span);
    }

    @Override
    public void println(String s) {
        final Editable text = getText();
        final int length = StringUtils.getLength(s);
        if (length == 0) {
            text.append('\n');
        } else {
            text.append(s, 0, length);
            if (s.charAt(length - 1) != '\n') {
                text.append('\n');
            }
        }
    }

    @Override
    public Editable getText() {
        return (Editable)super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }
}
