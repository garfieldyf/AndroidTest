package android.ext.image.transformer;

import android.util.Printer;

/**
 * Class Transformers
 * @author Garfield
 */
public final class Transformers {
    /**
     * Prints the <em>transformer</em> informations.
     */
    public static void dump(Printer printer, StringBuilder result, Transformer<?> transformer) {
        if (transformer instanceof RoundedRectTransformer) {
            ((RoundedRectTransformer)transformer).dump(printer, result);
        } else if (transformer instanceof RoundedGIFTransformer) {
            ((RoundedGIFTransformer)transformer).dump(printer, result);
        } else {
            printer.println(result.append(transformer).toString());
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Transformers() {
    }
}
