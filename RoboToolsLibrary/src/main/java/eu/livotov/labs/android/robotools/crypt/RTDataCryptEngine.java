package eu.livotov.labs.android.robotools.crypt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

/**
 * Author: alex askerov
 * Date: 16/06/14
 * Time: 15:16
 */
public class RTDataCryptEngine
{

    private static final String TAG = RTDataCryptEngine.class.getCanonicalName();
    private static final boolean IS_JB43 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    private static final String WRAPPED_KEY = "wrapped_key";

    private SharedPreferences privatePrefs;

    // for JB+ api
    private RTSecretKeyWrapper kw;

    // for old api
    private String keychainKey;


    public RTDataCryptEngine(Context ctx)
    {
        privatePrefs = ctx.getSharedPreferences("RTDataCryptEnginePrefs", Context.MODE_PRIVATE);
        if (IS_JB43)
        {
            try
            {
                String keyName = ctx.getPackageName() + ".secure_key";
                kw = new RTSecretKeyWrapper(ctx, keyName);
            } catch (Exception e)
            {
                keychainKey = generateDefaultKeychainKeyPassword();
                Log.e(TAG, e.getMessage(), e);
            }
        } else
        {
            keychainKey = generateDefaultKeychainKeyPassword();
        }

    }

    private SecretKey getKey(boolean generateIfNeeded) throws GeneralSecurityException
    {
        SecretKey key = null;
        String wrappedkey = getWrappedKey();
        if (TextUtils.isEmpty(wrappedkey) && generateIfNeeded)
        {
            key = RTCryptUtil.generateAesKey();
            wrappedkey = Base64.encodeToString(kw.wrap(key), Base64.NO_WRAP);
            saveWrappedKey(wrappedkey);
        } else if (!TextUtils.isEmpty(wrappedkey))
        {
            key = kw.unwrap(Base64.decode(wrappedkey, Base64.NO_WRAP));
        }
        return key;
    }

    private String getWrappedKey()
    {
        return privatePrefs.getString(WRAPPED_KEY, "");
    }

    private void saveWrappedKey(String wrappedkey)
    {
        SharedPreferences.Editor editor = privatePrefs.edit();
        editor.putString(WRAPPED_KEY, wrappedkey);
        editor.apply();
    }

    public boolean isKWInit()
    {
        return kw != null;
    }


    protected String generateDefaultKeychainKeyPassword()
    {
        return RTDataCryptEngine.class.getSimpleName();
    }

    public String decryptString(String encrypted)
    {
        return isKWInit() ? decryptWithSecretKey(encrypted) : RTCryptUtil.decryptAsText(encrypted, keychainKey);
    }

    public String encryptString(String value)
    {

        return isKWInit() ? encryptWithSecretKey(value) : RTCryptUtil.encrypt(value, keychainKey);
    }

    private String decryptWithSecretKey(String ciphertext)
    {
        try
        {
            return RTCryptUtil.decryptAesCbc(ciphertext, getKey(false));
        } catch (Exception e)
        {
            Log.d(TAG, e.getMessage(), e);
            return "";
        }
    }

    private String encryptWithSecretKey(String plaintext)
    {
        try
        {
            return RTCryptUtil.encryptAesCbc(plaintext, getKey(true));
        } catch (Exception e)
        {
            Log.d(TAG, e.getMessage(), e);
            return "";
        }
    }
}
