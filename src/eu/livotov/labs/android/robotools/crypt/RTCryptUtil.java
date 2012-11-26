package eu.livotov.labs.android.robotools.crypt;

import android.content.Context;
import android.util.Log;
import eu.livotov.labs.android.robotools.device.RTDevice;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTCryptUtil {

    private static final String TAG = RTCryptUtil.class.getSimpleName();

    private static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static String DELIMITER = "]";
    private static int KEY_LENGTH = 256;
    private static int ITERATION_COUNT = 1000;
    private static final int PKCS5_SALT_LENGTH = 8;

    private static SecureRandom random = new SecureRandom();


    public static String md5(String input) {
        String res = "";

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes());
            byte[] md5 = algorithm.digest();
            String tmp = "";
            for (int i = 0; i < md5.length; i++) {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1) {
                    res += "0" + tmp;
                } else {
                    res += tmp;
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        return res;
    }

    public static byte[] md5bytes(String input) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes("utf-8"));
            return algorithm.digest();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }


    public static void listAlgorithms(String algFilter) {
        Provider[] providers = Security.getProviders();
        for (Provider p : providers) {
            String providerStr = String.format("%s/%s/%f\n", p.getName(),
                    p.getInfo(), p.getVersion());
            Log.d(TAG, providerStr);
            Set<Provider.Service> services = p.getServices();
            List<String> algs = new ArrayList<String>();
            for (Provider.Service s : services) {
                boolean match = true;
                if (algFilter != null) {
                    match = s.getAlgorithm().toLowerCase()
                            .contains(algFilter.toLowerCase());
                }

                if (match) {
                    String algStr = String.format("\t%s/%s/%s", s.getType(),
                            s.getAlgorithm(), s.getClassName());
                    algs.add(algStr);
                }
            }

            Collections.sort(algs);
            for (String alg : algs) {
                Log.d(TAG, "\t" + alg);
            }
            Log.d(TAG, "");
        }
    }

    public static String encrypt(final String plaintext, final String password) {
        final byte[] salt = generateSalt();
        final SecretKey key = deriveKeyPbkdf2(salt, password);
        try {
            return encrypt(plaintext.getBytes("UTF-8"), key, salt);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(final byte[] plaintext, final String password) {
        final byte[] salt = generateSalt();
        final SecretKey key = deriveKeyPbkdf2(salt, password);
        return encrypt(plaintext, key, salt);
    }

    public static String toHex(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }

    public static byte[] decryptAsBytes(final String ciphertext, final String password) {
        String[] fields = ciphertext.split(DELIMITER);
        if (fields.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted text format");
        }

        final byte[] salt = fromBase64(fields[0]);
        final byte[] iv = fromBase64(fields[1]);
        final byte[] cipherBytes = fromBase64(fields[2]);
        final SecretKey key = deriveKeyPbkdf2(salt, password);

        return decrypt(cipherBytes, key, iv);
    }

    public static String decryptAsText(final String ciphertext, final String password) {
        String[] fields = ciphertext.split(DELIMITER);
        if (fields.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted text format");
        }

        final byte[] salt = fromBase64(fields[0]);
        final byte[] iv = fromBase64(fields[1]);
        final byte[] cipherBytes = fromBase64(fields[2]);
        final SecretKey key = deriveKeyPbkdf2(salt, password);

        try {
            return new String(decrypt(cipherBytes, key, iv), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    protected static byte[] decrypt(byte[] cipherBytes, SecretKey key, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
            return cipher.doFinal(cipherBytes);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String encrypt(byte[] plaintext, SecretKey key, byte[] salt) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            byte[] iv = generateIv(cipher.getBlockSize());
            Log.d(TAG, "IV: " + toHex(iv));
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: "
                    + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
            byte[] cipherText = cipher.doFinal(plaintext);

            if (salt != null) {
                return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER,
                        toBase64(iv), DELIMITER, toBase64(cipherText));
            }

            return String.format("%s%s%s", toBase64(iv), DELIMITER,
                    toBase64(cipherText));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected static SecretKey deriveKeyPbkdf2(byte[] salt, String password) {
        try {
            long start = System.currentTimeMillis();
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                    ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory
                    .getInstance(PBKDF2_DERIVATION_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            Log.d(TAG, "key bytes: " + toHex(keyBytes));

            SecretKey result = new SecretKeySpec(keyBytes, "AES");
            long elapsed = System.currentTimeMillis() - start;
            Log.d(TAG, String.format("PBKDF2 key derivation took %d [ms].",
                    elapsed));

            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }

    protected static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);
        return b;
    }

    private static String toBase64(byte[] bytes) {
        return RTBase64.encodeToString(bytes, RTBase64.NO_WRAP);
    }

    private static byte[] fromBase64(String base64) {
        return RTBase64.decode(base64, RTBase64.NO_WRAP);
    }


    public static String generateDeviceBoundEncryptionKeyForPassword(final Context ctx,
                                                                     String password,
                                                                     boolean lockToWifi,
                                                                     boolean lockToTelephony,
                                                                     boolean lockToSIM) {
        StringBuffer finalPassword = new StringBuffer(password);
        finalPassword.append(RTDevice.getDeviceUID(ctx, lockToWifi, lockToTelephony, lockToSIM));
        return finalPassword.toString();
    }

}
