package eu.livotov.labs.android.robotools.crypt;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.KeySpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PSource;
import javax.security.auth.x500.X500Principal;

import eu.livotov.labs.android.robotools.hardware.RTDevice;
import eu.livotov.labs.android.robotools.text.RTBase64;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTCryptUtil {

    public static final String PKCS12_DERIVATION_ALGORITHM = "PBEWITHSHA256AND256BITAES-CBC-BC";
    private static final String TAG = RTCryptUtil.class.getSimpleName();
    private static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int PKCS5_SALT_LENGTH = 8;
    private static String DELIMITER = "]";
    private static int KEY_LENGTH = 256;
    private static int KEY_LENGTH_AES = 192;
    private static int ITERATION_COUNT = 1000;
    private static SecureRandom random = new SecureRandom();


    public static String encrypt(final String plaintext, final String password) {
        final byte[] salt = generateSalt();
        SecretKey key = null;

        try {
            key = createKeyPbkdf2(salt, password);
        } catch (Throwable err) {
        }

        if (key == null) {
            key = createKeyPkcs12(salt, password);
        }

        try {
            return encrypt(plaintext.getBytes("UTF-8"), key, salt);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(final byte[] plaintext, final String password) {
        final byte[] salt = generateSalt();
        SecretKey key = null;

        try {
            key = createKeyPbkdf2(salt, password);
        } catch (Throwable err) {
        }

        if (key == null) {
            key = createKeyPkcs12(salt, password);
        }

        return encrypt(plaintext, key, salt);
    }

    protected static String encrypt(byte[] plaintext, SecretKey key, byte[] salt) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            byte[] iv = generateIv(cipher.getBlockSize());
            Log.d(TAG, "IV: " + toHex(iv));
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: " + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
            byte[] cipherText = cipher.doFinal(plaintext);

            if (salt != null) {
                return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER, toBase64(iv), DELIMITER, toBase64(cipherText));
            }

            return String.format("%s%s%s", toBase64(iv), DELIMITER, toBase64(cipherText));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


    public static byte[] decryptAsBytes(final String ciphertext, final String password) {
        String[] fields = ciphertext.split(DELIMITER);
        if (fields.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted text format");
        }

        final byte[] salt = fromBase64(fields[0]);
        final byte[] iv = fromBase64(fields[1]);
        final byte[] cipherBytes = fromBase64(fields[2]);
        SecretKey key = null;
        try {
            key = createKeyPbkdf2(salt, password);
        } catch (Throwable err) {
        }

        if (key == null) {
            key = createKeyPkcs12(salt, password);
        }

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

        SecretKey key = null;
        try {
            key = createKeyPbkdf2(salt, password);
        } catch (Throwable err) {
        }

        if (key == null) {
            key = createKeyPkcs12(salt, password);
        }

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

    /*AES*/

    public static String encryptAesCbc(String plaintext, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            byte[] iv = generateIv(cipher.getBlockSize());
            Log.d(TAG, "IV: " + toHex(iv));
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: " + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

            return String.format("%s%s%s", toBase64(iv), DELIMITER, toBase64(cipherText));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptAesCbc(String ciphertext, SecretKey key) {
        try {
            String[] fields = ciphertext.split(DELIMITER);
            if (fields.length != 2) {
                throw new IllegalArgumentException("Invalid encypted text format");
            }

            byte[] iv = fromBase64(fields[0]);
            byte[] cipherBytes = fromBase64(fields[1]);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
            byte[] plaintext = cipher.doFinal(cipherBytes);

            return new String(plaintext, "UTF-8");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /*--  Keys --*/

    protected static SecretKey createKeyPbkdf2(byte[] salt, String password) {
        return createKeyPkcs12(salt, password);
    }

    protected static SecretKey createKeyPkcs12(byte[] salt, String password) {
        try {
            long start = System.currentTimeMillis();
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PKCS12_DERIVATION_ALGORITHM);
            SecretKey result = keyFactory.generateSecret(keySpec);
            long elapsed = System.currentTimeMillis() - start;
            Log.d(TAG, String.format("PKCS#12 key derivation took %d [ms].", elapsed));

            return result;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecretKey createAesKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(KEY_LENGTH_AES);
            return kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("NewApi")
    public static KeyPair createRsaKey(Context ctx, String alais) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException {
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, 20);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec
                .Builder(ctx)
                .setAlias(alais)
                .setSubject(new X500Principal(String.format("CN=%s, OU=%s", alais, ctx.getPackageName())))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(notBefore.getTime())
                .setEndDate(notAfter.getTime()).build();

        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        kpGenerator.initialize(spec);
        return kpGenerator.generateKeyPair();
    }

    /* --- Other methods --- */

    public static String md5(String input) {
        String res = "";

        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes());
            byte[] md5 = algorithm.digest();
            final int len = md5.length;
            StringBuilder buf = new StringBuilder(len * 2);
            for (int i = 0; i < len; i++) {
                String tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1) {
                    buf.append('0');
                }
                buf.append(tmp);
            }
            res = buf.toString();
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

    protected static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);
        return b;
    }

    protected static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }

    public static String toHex(byte[] bytes) {
        StringBuffer buff = new StringBuffer();
        for (byte b : bytes) {
            buff.append(String.format("%02X", b));
        }

        return buff.toString();
    }

    private static String toBase64(byte[] bytes) {
        return RTBase64.encodeToString(bytes, RTBase64.NO_WRAP);
    }

    private static byte[] fromBase64(String base64) {
        return RTBase64.decode(base64, RTBase64.NO_WRAP);
    }


    public static void listAlgorithms(String algFilter) {
        Provider[] providers = Security.getProviders();
        for (Provider p : providers) {
            String providerStr = String.format("%s/%s/%f\n", p.getName(), p.getInfo(), p.getVersion());
            Log.d(TAG, providerStr);
            Set<Provider.Service> services = p.getServices();
            List<String> algs = new ArrayList<String>();
            for (Provider.Service s : services) {
                boolean match = true;
                if (algFilter != null) {
                    match = s.getAlgorithm().toLowerCase().contains(algFilter.toLowerCase());
                }

                if (match) {
                    String algStr = String.format("\t%s/%s/%s", s.getType(), s.getAlgorithm(), s.getClassName());
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

}
