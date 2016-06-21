package eu.livotov.labs.android.robotools.crypt;

import android.annotation.TargetApi;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * Created by Unreal Mojo
 *
 * @author Grishko Nikita
 *         on 21.06.2016.
 */
public class RTFingerprintCryptEngine {

    /**
     * Encrypt text with RSA key
     *
     * @param alias       - alias of the key in {@link KeyStore}
     * @param initialText - text to be encrypted
     */
    public static String encrypt(final String alias, final String initialText) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            PublicKey publicKey = keyStore.getCertificate(alias).getPublicKey();
            PublicKey unrestrictedPublicKey =
                    KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(
                            new X509EncodedKeySpec(publicKey.getEncoded()));

            Cipher input = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            input.init(Cipher.ENCRYPT_MODE, unrestrictedPublicKey, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);
            cipherOutputStream.write(initialText.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            return Base64.encodeToString(vals, Base64.DEFAULT);

        } catch (Exception e) {
            Log.e(RTFingerprintCryptEngine.class.getSimpleName(), Log.getStackTraceString(e));
            return null;
        }
    }

    /**
     * Prepare instance of {@link Cipher} to be authorized by user/
     *
     * @param alias - alias of the key in {@link KeyStore}
     */
    public static Cipher initDecryptionRSACipher(String alias) throws IOException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
        return cipher;
    }

    /**
     * Decrypt with authorized instance of {@link Cipher}
     *
     * @param cipher     - authorized by user instance of {@link Cipher}
     * @param cipherText - text to be encrypted
     */
    public static String decrypt(Cipher cipher, final String cipherText) {
        try {
            CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), cipher);
            ArrayList<Byte> values = new ArrayList<>();

            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            final String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            return finalText;
        } catch (Exception e) {
            Log.e(RTFingerprintCryptEngine.class.getSimpleName(), Log.getStackTraceString(e));
            return null;
        }
    }

    @TargetApi(23)
    public static KeyPair createRSAKeyUserAuthenticationRequired(String alias) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT)
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP).build());
            return keyPairGenerator.generateKeyPair();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
