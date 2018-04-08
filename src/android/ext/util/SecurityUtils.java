package android.ext.util;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import android.util.Log;

/**
 * Class SecurityUtils
 * @author Garfield
 * @version 1.0
 */
public final class SecurityUtils {
    /**
     * Uses the RSA encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param publicKey The public key.
     * @param data The byte array to encrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array if the encryption succeeded, <tt>null</tt> otherwise.
     * @see #RSAEncrypt(BigInteger, BigInteger, byte[])
     * @see #RSAEncrypt(BigInteger, BigInteger, byte[], int, int)
     */
    public static byte[] RSAEncrypt(Key publicKey, byte[] data, int offset, int length) {
        try {
            return transfer("RSA", Cipher.ENCRYPT_MODE, publicKey, data, offset, length);
        } catch (Exception e) {
            Log.e(SecurityUtils.class.getName(), "RSA encryption failed", e);
            return null;
        }
    }

    /**
     * Uses the RSA encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param modulus The modulus <tt>n</tt>.
     * @param publicExponent The public exponent <tt>d</tt>.
     * @param data The byte array to encrypt.
     * @return The byte array if the encryption succeeded, <tt>null</tt> otherwise.
     * @see #RSAEncrypt(Key, byte[], int, int)
     * @see #RSAEncrypt(BigInteger, BigInteger, byte[], int, int)
     */
    public static byte[] RSAEncrypt(BigInteger modulus, BigInteger publicExponent, byte[] data) {
        return RSAEncrypt(modulus, publicExponent, data, 0, data.length);
    }

    /**
     * Uses the RSA encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param modulus The modulus <tt>n</tt>.
     * @param publicExponent The public exponent <tt>d</tt>.
     * @param data The byte array to encrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array if the encryption succeeded, <tt>null</tt> otherwise.
     * @see #RSAEncrypt(Key, byte[], int, int)
     * @see #RSAEncrypt(BigInteger, BigInteger, byte[])
     */
    public static byte[] RSAEncrypt(BigInteger modulus, BigInteger publicExponent, byte[] data, int offset, int length) {
        try {
            final Key key = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
            return transfer("RSA", Cipher.ENCRYPT_MODE, key, data, 0, data.length);
        } catch (Exception e) {
            Log.e(SecurityUtils.class.getName(), "RSA encryption failed", e);
            return null;
        }
    }

    /**
     * Uses the DES encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param password The password to encrypt.
     * @param data The byte array to encrypt.
     * @return The byte array if the encryption succeeded, <tt>null</tt> otherwise.
     * @see #DESEncrypt(byte[], byte[], int, int)
     * @see #DESEncrypt(byte[], String, String)
     */
    public static byte[] DESEncrypt(byte[] password, byte[] data) {
        return DESTransfer(Cipher.ENCRYPT_MODE, password, data, 0, data.length);
    }

    /**
     * Uses the DES encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param password The password to encrypt.
     * @param data The byte array to encrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array if the encryption succeeded, <tt>null</tt> otherwise.
     * @see #DESEncrypt(byte[], byte[])
     * @see #DESEncrypt(byte[], String, String)
     */
    public static byte[] DESEncrypt(byte[] password, byte[] data, int offset, int length) {
        return DESTransfer(Cipher.ENCRYPT_MODE, password, data, offset, length);
    }

    /**
     * Uses the DES encryption algorithm encrypts the specified string <em>data</em>.
     * @param password The password to encrypt.
     * @param data The string to encrypt.
     * @param charsetName May be <tt>null</tt>. The charset name to encoded the <em>data</em>.
     * @return The byte array if the encryption succeeded, <tt>null</tt> otherwise.
     * @see #DESEncrypt(byte[], byte[])
     * @see #DESEncrypt(byte[], byte[], int, int)
     */
    public static byte[] DESEncrypt(byte[] password, String data, String charsetName) {
        final byte[] codePoints = data.getBytes(charsetName != null ? Charset.forName(charsetName) : Charset.defaultCharset());
        return DESTransfer(Cipher.ENCRYPT_MODE, password, codePoints, 0, codePoints.length);
    }

    /**
     * Uses the DES encryption algorithm decrypts the specified byte array <em>data</em>.
     * @param password The password to decrypt.
     * @param data The byte array to decrypt.
     * @return The byte array if the decryption succeeded, <tt>null</tt> otherwise.
     * @see #DESDecrypt(byte[], byte[], int, int)
     */
    public static byte[] DESDecrypt(byte[] password, byte[] data) {
        return DESTransfer(Cipher.DECRYPT_MODE, password, data, 0, data.length);
    }

    /**
     * Uses the DES encryption algorithm decrypts the specified byte array <em>data</em>.
     * @param password The password to decrypt.
     * @param data The byte array to decrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array if the decryption succeeded, <tt>null</tt> otherwise.
     * @see #DESDecrypt(byte[], byte[])
     */
    public static byte[] DESDecrypt(byte[] password, byte[] data, int offset, int length) {
        return DESTransfer(Cipher.DECRYPT_MODE, password, data, offset, length);
    }

    private static byte[] DESTransfer(int opmode, byte[] password, byte[] data, int offset, int length) {
        try {
            final Key key = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(password));
            return transfer("DES", opmode, key, data, offset, length);
        } catch (Exception e) {
            Log.e(SecurityUtils.class.getName(), "DES encryption failed", e);
            return null;
        }
    }

    private static byte[] transfer(String transformation, int opmode, Key key, byte[] data, int offset, int length) throws Exception {
        final Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(opmode, key);
        return cipher.doFinal(data, offset, length);
    }

    /**
     * This utility class cannot be instantiated.
     */
    private SecurityUtils() {
    }
}
