package android.ext.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Class CryptoUtils
 * @author Garfield
 */
public final class CryptoUtils {
    /**
     * Uses the RSA encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param publicKey The public key.
     * @param data The byte array to encrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array from the encryption.
     * @throws GeneralSecurityException if an error occurs while encrypting the <em>data</em>.
     * @see #RSAEncrypt(BigInteger, BigInteger, byte[], int, int)
     */
    public static byte[] RSAEncrypt(Key publicKey, byte[] data, int offset, int length) throws GeneralSecurityException {
        return transform("RSA", Cipher.ENCRYPT_MODE, publicKey, data, offset, length);
    }

    /**
     * Uses the RSA encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param modulus The modulus <b>n</b>.
     * @param publicExponent The public exponent <b>d</b>.
     * @param data The byte array to encrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array from the encryption.
     * @throws GeneralSecurityException if an error occurs while encrypting the <em>data</em>.
     * @see #RSAEncrypt(Key, byte[], int, int)
     */
    public static byte[] RSAEncrypt(BigInteger modulus, BigInteger publicExponent, byte[] data, int offset, int length) throws GeneralSecurityException {
        final Key key = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        return transform("RSA", Cipher.ENCRYPT_MODE, key, data, 0, data.length);
    }

    /**
     * Uses the RSA encryption algorithm encrypts the specified <em>source's</em> contents.
     * @param publicKey The public key.
     * @param source The <tt>InputStream's</tt> contents to encrypt.
     * @return The byte array from the encryption.
     * @throws IOException if an error occurs while reading the <em>source's</em> contents.
     * @throws GeneralSecurityException if an error occurs while encrypting the <em>source's</em> contents.
     * @see #RSAEncrypt(BigInteger, BigInteger, InputStream)
     */
    public static byte[] RSAEncrypt(Key publicKey, InputStream source) throws IOException, GeneralSecurityException {
        return transform("RSA", Cipher.ENCRYPT_MODE, publicKey, source);
    }

    /**
     * Uses the RSA encryption algorithm encrypts the specified <em>source's</em> contents.
     * @param modulus The modulus <b>n</b>.
     * @param publicExponent The public exponent <b>d</b>.
     * @param source The <tt>InputStream's</tt> contents to encrypt.
     * @return The byte array from the encryption.
     * @throws IOException if an error occurs while reading the <em>source's</em> contents.
     * @throws GeneralSecurityException if an error occurs while encrypting the <em>source's</em> contents.
     * @see #RSAEncrypt(Key, InputStream)
     */
    public static byte[] RSAEncrypt(BigInteger modulus, BigInteger publicExponent, InputStream source) throws IOException, GeneralSecurityException {
        final Key key = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        return transform("RSA", Cipher.ENCRYPT_MODE, key, source);
    }

    /**
     * Uses the DES encryption algorithm encrypts the specified <em>source's</em> contents.
     * @param password The password to encrypt.
     * @param source The <tt>InputStream's</tt> contents to encrypt.
     * @return The byte array from the encryption.
     * @throws IOException if an error occurs while reading the <em>source's</em> contents.
     * @throws GeneralSecurityException if an error occurs while encrypting the <em>source's</em> contents.
     * @see #DESEncrypt(byte[], String, String)
     * @see #DESEncrypt(byte[], byte[], int, int)
     */
    public static byte[] DESEncrypt(byte[] password, InputStream source) throws IOException, GeneralSecurityException {
        final Key key = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(password));
        return transform("DES", Cipher.ENCRYPT_MODE, key, source);
    }

    /**
     * Uses the DES encryption algorithm encrypts the specified byte array <em>data</em>.
     * @param password The password to encrypt.
     * @param data The byte array to encrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array from the encryption.
     * @throws GeneralSecurityException if an error occurs while encrypting the <em>data</em>.
     * @see #DESEncrypt(byte[], InputStream)
     * @see #DESEncrypt(byte[], String, String)
     */
    public static byte[] DESEncrypt(byte[] password, byte[] data, int offset, int length) throws GeneralSecurityException {
        final Key key = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(password));
        return transform("DES", Cipher.ENCRYPT_MODE, key, data, offset, length);
    }

    /**
     * Uses the DES encryption algorithm encrypts the specified string <em>data</em>.
     * @param password The password to encrypt.
     * @param data The string to encrypt.
     * @param charsetName May be <tt>null</tt>. The charset name to encoded the <em>data</em>.
     * @return The byte array from the encryption.
     * @throws GeneralSecurityException if an error occurs while encrypting the <em>data</em>.
     * @see #DESEncrypt(byte[], InputStream)
     * @see #DESEncrypt(byte[], byte[], int, int)
     */
    public static byte[] DESEncrypt(byte[] password, String data, String charsetName) throws GeneralSecurityException {
        final byte[] codePoints = data.getBytes(charsetName != null ? Charset.forName(charsetName) : Charset.defaultCharset());
        return DESEncrypt(password, codePoints, 0, codePoints.length);
    }

    /**
     * Uses the DES decryption algorithm decrypts the specified byte array <em>data</em>.
     * @param password The password to decrypt.
     * @param data The byte array to decrypt.
     * @param offset The start position in the <em>data</em>.
     * @param length The number of count in the <em>data</em>.
     * @return The byte array from the decryption.
     * @throws GeneralSecurityException if an error occurs while decrypting the <em>data</em>.
     * @see #DESDecrypt(byte[], InputStream)
     */
    public static byte[] DESDecrypt(byte[] password, byte[] data, int offset, int length) throws GeneralSecurityException {
        final Key key = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(password));
        return transform("DES", Cipher.DECRYPT_MODE, key, data, offset, length);
    }

    /**
     * Uses the DES decryption algorithm decrypts the specified <em>source's</em> contents.
     * @param password The password to decrypt.
     * @param source The <tt>InputStream's</tt> contents to decrypt.
     * @return The byte array from the decryption.
     * @throws IOException if an error occurs while reading the <em>source's</em> contents.
     * @throws GeneralSecurityException if an error occurs while decrypting the <em>source's</em> contents.
     * @see #DESDecrypt(byte[], byte[], int, int)
     */
    public static byte[] DESDecrypt(byte[] password, InputStream source) throws IOException, GeneralSecurityException {
        final Key key = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(password));
        return transform("DES", Cipher.DECRYPT_MODE, key, source);
    }

    private static byte[] transform(String transformation, int opmode, Key key, InputStream source) throws IOException, GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(opmode, key);

        final byte[] buffer = Pools.sByteArrayPool.obtain();
        try {
            int readBytes;
            while ((readBytes = source.read(buffer, 0, buffer.length)) != -1) {
                cipher.update(buffer, 0, readBytes);
            }

            return cipher.doFinal();
        } finally {
            Pools.sByteArrayPool.recycle(buffer);
        }
    }

    private static byte[] transform(String transformation, int opmode, Key key, byte[] data, int offset, int length) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(opmode, key);
        return cipher.doFinal(data, offset, length);
    }

    /**
     * This utility class cannot be instantiated.
     */
    private CryptoUtils() {
    }
}
