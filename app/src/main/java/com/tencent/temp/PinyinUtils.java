package com.tencent.temp;

import android.content.res.AssetManager;
import java.util.Locale;
import java.util.Properties;

/**
 * Class PinyinUtils
 * @author Garfield
 */
public final class PinyinUtils {
    /**
     * Define the output case of Hanyu Pinyin string.
     */
    public static enum CaseType {
        /**
         * The type indicates that the output of upper case.
         */
        UPPER_CASE,

        /**
         * The type indicates that the output of lower case.
         */
        LOWER_CASE,
    }

    /**
     * Define the output format of character 'ü'.
     * <p>'ü' is a special character of Hanyu Pinyin, which can not be simply
     * represented by English letters. In Hanyu Pinyin, such characters include
     * 'ü', 'üe', 'üan', and 'ün'.</p> This class provides several options for
     * output of 'ü', which are listed below.
     * <table><tr><th>Options</th><th>Output</th></tr>
     * <tr><td>U_AND_COLON</td><td align = "center">u:</td></tr>
     * <tr><td>V</td><td align = "center">v</td></tr>
     * <tr><td>U_UNICODE</td><td align = "center">ü</td></tr></table>
     */
    public static enum VCharType {
        /**
         * The type indicates that the output of 'ü' is 'v'.
         */
        V,

        /**
         * The type indicates that the output of 'ü' is 'u:'.
         */
        U_AND_COLON,

        /**
         * The type indicates that the output of 'ü' is 'ü' in Unicode form.
         */
        U_UNICODE,
    }

    /**
     * Define the output format of Hanyu Pinyin tones.
     * <p>Chinese has four pitched tones and a "toneless" tone. They are called Píng(平, flat),
     * Shǎng(上, rise), Qù(去, high drop), Rù(入, drop) and Qing(轻, toneless). Usually, we use 1,
     * 2, 3, 4 and 5 to represent them.<p>
     * This class provides several options for output of Chinese tones, which are listed below.
     * <br/>For example, Chinese character '打'.
     * <table><tr><th>Options</th><th>Output</th></tr>
     * <tr><td>TONE_NUMBER</td><td align = "center">da3</td></tr>
     * <tr><td>TONE_NONE</td><td align = "center">da</td></tr>
     * <tr><td>TONE_MARK</td><td align = "center">dǎ</td></tr></table>
     */
    public static enum ToneType {
        /**
         * The type indicates that Hanyu Pinyin is outputted without tone numbers or tone marks.
         */
        TONE_NONE,

        /**
         * The type indicates that Hanyu Pinyin is outputted with tone marks.
         */
        TONE_MARK,

        /**
         * The type indicates that Hanyu Pinyin is outputted with tone numbers.
         */
        TONE_NUMBER,
    }

    public static final class PinyinOutputFormat {
        public CaseType caseType;
        public ToneType toneType;
        public VCharType vcharType;

        public PinyinOutputFormat() {
            this(CaseType.LOWER_CASE, ToneType.TONE_NONE, VCharType.U_UNICODE);
        }

        public PinyinOutputFormat(CaseType caseType, ToneType toneType, VCharType vcharType) {
            this.caseType = caseType;
            this.toneType = toneType;
            this.vcharType = vcharType;
        }
    }

    public static void loadPinyinTable(String filename) {
//        synchronized (PinyinUtils.class) {
//            if (sPinyinTable == null && (sPinyinTable = FileUtils.loadProperties(filename)) == null) {
//                throw new IllegalStateException("Couldn't load pinyin table - " + filename);
//            }
//        }
    }

    public static void loadPinyinTable(AssetManager assetManager, String filename) {
//        synchronized (PinyinUtils.class) {
//            if (sPinyinTable == null && (sPinyinTable = FileUtils.loadProperties(assetManager, filename)) == null) {
//                throw new IllegalStateException("Couldn't load pinyin table from AssetManager - " + filename);
//            }
//        }
    }

    /**
     * Gets all unformmatted Hanyu Pinyin presentations of a single Chinese character.
     * <p>For example, if the input is '间', the return will be an array with two Hanyu
     * Pinyin strings: <br/>"jian1"<br/>"jian4"<br/><br/></p>
     * @param c The given Chinese character.
     * @return A string array contains all unformmatted Hanyu Pinyin presentations with
     * tone numbers or <tt>null</tt> for non-Chinese character.
     * @see #toPinyin(char, PinyinOutputFormat)
     */
    public static String[] toPinyin(char c) {
        final String pinyin = getPinyin(c);
        return (pinyin != null ? pinyin.split(",") : null);
    }

    /**
     * Gets all Hanyu Pinyin presentations of a single Chinese character.
     * @param c The given Chinese character.
     * @param format The desired format of returned Hanyu Pinyin string.
     * @return A string array contains all Hanyu Pinyin presentations with
     * tone or <tt>null</tt> for non-Chinese character.
     * @see #toPinyin(char)
     * @see PinyinOutputFormat
     */
    public static String[] toPinyin(char c, PinyinOutputFormat format) {
        final String[] pinyin = toPinyin(c);
        if (pinyin != null) {
            for (int i = 0; i < pinyin.length; ++i) {
                pinyin[i] = formatPinyin(pinyin[i], format);
            }
        }

        return pinyin;
    }

    /**
     * Gets all unformmatted Hanyu Pinyin presentations of a Chinese string.
     * @param s The given Chinese string.
     * @return A <tt>CharSequence</tt> contains all unformmatted Hanyu Pinyin
     * presentations with tone numbers.
     * @see #toPinyin(CharSequence, PinyinOutputFormat)
     */
    public static CharSequence toPinyin(CharSequence s) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0, length = s.length(); i < length; ++i) {
            final char c = s.charAt(i);
            final String[] pinyin = toPinyin(c);
            if (pinyin != null) {
                result.append(pinyin[0]);
            } else {
                result.append(c);
            }
        }

        return result;
    }

    /**
     * Gets all Hanyu Pinyin presentations of a Chinese string.
     * @param s The given Chinese string.
     * @param format The desired format of returned Hanyu Pinyin
     * string.
     * @return A <tt>CharSequence</tt> contains all Hanyu Pinyin
     * presentations with tone.
     * @see #toPinyin(CharSequence)
     * @see PinyinOutputFormat
     */
    public static CharSequence toPinyin(CharSequence s, PinyinOutputFormat format) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0, length = s.length(); i < length; ++i) {
            final char c = s.charAt(i);
            final String[] pinyin = toPinyin(c);
            if (pinyin != null) {
                result.append(formatPinyin(pinyin[0], format));
            } else {
                result.append(c);
            }
        }

        return result;
    }

    private static String getPinyin(char c) {
        final String pinyin = sPinyinTable.getProperty(Integer.toHexString(c).toUpperCase(Locale.getDefault()));
        return ("none0".equals(pinyin) ? null : pinyin);
    }

    private static String formatPinyin(String pinyin, PinyinOutputFormat format) {
        if (ToneType.TONE_MARK == format.toneType && ((VCharType.V == format.vcharType) || (VCharType.U_AND_COLON == format.vcharType))) {
            throw new IllegalArgumentException("Tone marks cannot be added to v or u:");
        }

        switch (format.toneType) {
        case TONE_NONE:
            pinyin = pinyin.replaceAll("[1-5]", "");
            break;

        case TONE_MARK:
            pinyin = toToneMark(pinyin.replaceAll("u:", "v"));
            break;

        default:
            break;
        }

        switch (format.vcharType) {
        case V:
            pinyin = pinyin.replaceAll("u:", "v");
            break;

        case U_UNICODE:
            pinyin = pinyin.replaceAll("u:", "ü");
            break;

        default:
            break;
        }

        if (format.caseType == CaseType.UPPER_CASE) {
            pinyin = pinyin.toUpperCase(Locale.getDefault());
        }

        return pinyin;
    }

    private static String toToneMark(String origPinyin) {
        final String pinyin = origPinyin.toLowerCase(Locale.getDefault());
        if (!pinyin.matches("[a-z]*[1-5]?")) {
            // The bad format.
            return pinyin;
        }

        if (!pinyin.matches("[a-z]*[1-5]")) {
            // The input string has no any tune number
            // only replace v with ü (umlat) character.
            return pinyin.replaceAll("v", "ü");
        }

        final String allUnmarkedVowel = "aeiouv";
        final int indexOfA = pinyin.indexOf('a');
        final int indexOfE = pinyin.indexOf('e');
        final int indexOfO = pinyin.indexOf("ou");

        char unmarkedVowel = '$';
        int indexOfUnmarkedVowel = -1;
        if (indexOfA != -1) {
            indexOfUnmarkedVowel = indexOfA;
            unmarkedVowel = 'a';
        } else if (indexOfE != -1) {
            indexOfUnmarkedVowel = indexOfE;
            unmarkedVowel = 'e';
        } else if (indexOfO != -1) {
            indexOfUnmarkedVowel = indexOfO;
            unmarkedVowel = 'o';
        } else {
            for (int i = pinyin.length() - 1; i >= 0; --i) {
                if (String.valueOf(pinyin.charAt(i)).matches("[" + allUnmarkedVowel + "]")) {
                    indexOfUnmarkedVowel = i;
                    unmarkedVowel = pinyin.charAt(i);
                    break;
                }
            }
        }

        if (unmarkedVowel != '$' && indexOfUnmarkedVowel != -1) {
            final String allMarkedVowel = "āáăàaēéĕèeīíĭìiōóŏòoūúŭùuǖǘǚǜü";
            final int row = allUnmarkedVowel.indexOf(unmarkedVowel);
            final int col = Character.getNumericValue(pinyin.charAt(pinyin.length() - 1)) - 1;
            final char markedVowel = allMarkedVowel.charAt(row * 5 + col);

            return new StringBuilder(pinyin.substring(0, indexOfUnmarkedVowel).replaceAll("v", "ü")).append(markedVowel)
                .append(pinyin.substring(indexOfUnmarkedVowel + 1, pinyin.length() - 1).replaceAll("v", "ü")).toString();
        }

        return pinyin;
    }

    private static Properties sPinyinTable;

    /**
     * This utility class cannot be instantiated.
     */
    private PinyinUtils() {
    }
}