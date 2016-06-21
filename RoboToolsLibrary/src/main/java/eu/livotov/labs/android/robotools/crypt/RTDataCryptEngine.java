package eu.livotov.labs.android.robotools.crypt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import javax.crypto.SecretKey;

import java.security.GeneralSecurityException;

import eu.livotov.labs.android.robotools.R;

/**
 * Author: alex askerov
 * Date: 16/06/14
 * Time: 15:16
 */
public class RTDataCryptEngine {

    private static final String TAG = RTDataCryptEngine.class.getCanonicalName();
    private static final boolean IS_JB43 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final String WRAPPED_KEY = "wrapped_key";

    private static final String KEY_PART_ONE = "MIIBOAIBAAJAW+XLnvNL99fGmjR7aKxrfQRuu9nLyVfNvQ3f1ugf5EHKzIlCS6G8\n" +
            "ijwdwMxIPPFQ/FaY3i9DbgDCY8oXngdo7QIDAQABAkBHM8MSwvuHan4MmMFNltop\n" +
            "oTeTAOsuc4OLCab3Qh8DoS9YLZxAZ7LHQqGjFh4+c3T9MR4G9CPNUjOfJ1TDxl2B\n" +
            "AiEAqnUqBum+oIulaYTli89syLVPNJUDe9lHCQ4ZpsyJQx0CIQCKA/WwnlsVEAPo\n" +
            "3oXcZgHeQLn1GVsoBL0bbSsAU1uEEQIgDQaG/6A9AOeq7DVLlTN0jKHOO6Znbb9c\n" +
            "vkRlkWlv08ECIENHGN5G42mKDA3ZY3GDvEdmT//do2UHolObTMn02HixAiBX9/jy\n" +
            "talOPaCX9766MhmzhLfciEtuEogZ9gsBs8piug";

    private SharedPreferences privatePrefs;

    // for JB+ api
    private RTSecretKeyWrapper secretKeyWrapper;

    // for old api
    private String keychainKey;
    private Context context;
    private String password;


    public RTDataCryptEngine(Context context) {
        this(context, null);
    }

    public RTDataCryptEngine(Context context, String password) {
        this.context = context;
        this.password = password;
        privatePrefs = context.getSharedPreferences("RTDataCryptEnginePrefs", Context.MODE_PRIVATE);
        init();
    }

    private void init() {
        if (IS_JB43) {
            try {
                secretKeyWrapper = new RTSecretKeyWrapper(context, getSecretKeyAlias());
            } catch (Exception e) {
                keychainKey = generateDefaultKeychainKeyPassword(context, password);
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            keychainKey = generateDefaultKeychainKeyPassword(context, password);
        }
    }

    private String getSecretKeyAlias() {
        return String.format("%s%s", context.getPackageName(), ".secure_key");
    }

    private SecretKey getKey(boolean generateIfNeeded) throws GeneralSecurityException {
        SecretKey key = null;
        String wrappedKey = getWrappedKey();
        if (TextUtils.isEmpty(wrappedKey) && generateIfNeeded) {
            key = RTCryptUtil.createAesKey();
            wrappedKey = Base64.encodeToString(secretKeyWrapper.wrap(key), Base64.NO_WRAP);
            saveWrappedKey(wrappedKey);
        } else if (!TextUtils.isEmpty(wrappedKey)) {
            key = secretKeyWrapper.unwrap(Base64.decode(wrappedKey, Base64.NO_WRAP));
        }
        return key;
    }

    private String getWrappedKey() {
        return privatePrefs.getString(WRAPPED_KEY, "");
    }

    private void saveWrappedKey(String wrappedKey) {
        SharedPreferences.Editor editor = privatePrefs.edit();
        editor.putString(WRAPPED_KEY, wrappedKey);
        editor.apply();
    }

    public boolean isKWInit() {
        return secretKeyWrapper != null;
    }

    public void reset() {
        privatePrefs.edit().clear().apply();
        if (isKWInit()) {
            secretKeyWrapper.removeKey(getSecretKeyAlias());
        }
        init();
    }

    protected String generateDefaultKeychainKeyPassword(Context context, String userPartOfPassword) {
        String keychain = String.format("%s%s%s", RTDataCryptEngine.class.getSimpleName(), KEY_PART_ONE, context.getString(R.string.key_part_two));
        if (!TextUtils.isEmpty(userPartOfPassword)) {
            return String.format("%s%s", keychain, userPartOfPassword);
        }
        return keychain;
    }

    public String decryptString(String encrypted) throws Throwable {
        return isKWInit() ? decryptWithSecretKey(encrypted) : RTCryptUtil.decryptAsText(encrypted, keychainKey);
    }

    public String encryptString(String value) throws Throwable {
        return isKWInit() ? encryptWithSecretKey(value) : RTCryptUtil.encrypt(value, keychainKey);
    }

    private String decryptWithSecretKey(String cipherText) throws Throwable {
        return RTCryptUtil.decryptAesCbc(cipherText, getKey(true));
    }

    private String encryptWithSecretKey(String plaintext) throws Throwable {
        return RTCryptUtil.encryptAesCbc(plaintext, getKey(true));
    }
}
