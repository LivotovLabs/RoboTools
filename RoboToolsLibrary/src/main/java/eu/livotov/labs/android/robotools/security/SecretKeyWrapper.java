package eu.livotov.labs.android.robotools.security;

import android.content.Context;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;

/**
 * Wraps {@link javax.crypto.SecretKey} instances using a public/private key pair stored in
 * the platform {@link java.security.KeyStore}. This allows us to protect symmetric keys with
 * hardware-backed crypto, if provided by the device.
 * <p/>
 * See <a href="http://en.wikipedia.org/wiki/Key_Wrap">key wrapping</a> for more
 * details.
 * <p/>
 * Not inherently thread safe.
 */
public class SecretKeyWrapper {

    private final Cipher mCipher;
    private final KeyPair mPair;

    /**
     * Create a wrapper using the public/private key pair with the given alias.
     * If no pair with that alias exists, it will be generated.
     */
    public SecretKeyWrapper(Context context, String alias)
            throws GeneralSecurityException, IOException {
        mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(alias)) {
            CryptUtil.generateRsaPairWithGenerator(context, alias);
        }

        // Even if we just generated the key, always read it back to ensure we
        // can read it successfully.
        final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                alias, null);
        mPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }


    /**
     * Wrap a {@link javax.crypto.SecretKey} using the public key assigned to this wrapper.
     * Use {@link #unwrap(byte[])} to later recover the original
     * {@link javax.crypto.SecretKey}.
     *
     * @return a wrapped version of the given {@link javax.crypto.SecretKey} that can be
     * safely stored on untrusted storage.
     */
    public byte[] wrap(SecretKey key) throws GeneralSecurityException {
        mCipher.init(Cipher.WRAP_MODE, mPair.getPublic());
        return mCipher.wrap(key);
    }

    /**
     * Unwrap a {@link javax.crypto.SecretKey} using the private key assigned to this
     * wrapper.
     *
     * @param blob a wrapped {@link javax.crypto.SecretKey} as previously returned by
     *             {@link #wrap(javax.crypto.SecretKey)}.
     */
    public SecretKey unwrap(byte[] blob) throws GeneralSecurityException {
        mCipher.init(Cipher.UNWRAP_MODE, mPair.getPrivate());
        return (SecretKey) mCipher.unwrap(blob, "AES", Cipher.SECRET_KEY);
    }
}
