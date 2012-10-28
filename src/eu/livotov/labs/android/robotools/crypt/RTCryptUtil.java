package eu.livotov.labs.android.robotools.crypt;

import android.content.Context;
import eu.livotov.labs.android.robotools.device.RTDevice;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTCryptUtil
{

    private final static String HEX = "0123456789ABCDEF";

    public static String md5(String input)
    {
        String res = "";

        try
        {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes());
            byte[] md5 = algorithm.digest();
            String tmp = "";
            for (int i = 0; i < md5.length; i++)
            {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1)
                {
                    res += "0" + tmp;
                } else
                {
                    res += tmp;
                }
            }
        } catch (NoSuchAlgorithmException ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        return res;
    }

    public static byte[] md5bytes(String input)
    {
        try
        {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes("utf-8"));
            return algorithm.digest();
        } catch (Throwable ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static String encrypt(final String password, final String text)
    {
        try
        {
            return bytesToHexString(encrypt(generateDeviceBoundEncryptionKeyForPassword(password), text.getBytes("utf-8")));
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }

    public static String encrypt(byte[] key, String text)
    {
        try
        {
            return bytesToHexString(encrypt(key, text.getBytes("utf-8")));
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }

    private static byte[] encrypt(byte[] raw, byte[] clear)
    {
        try
        {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(clear);
            return encrypted;
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }

    public static String decrypt(byte[] key, String encrypted)
    {
        try
        {
            return new String(decrypt(key, hexStringToBytes(encrypted)));
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
    {
        try
        {
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return decrypted;
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }


    private static byte[] decrypt(String key, byte[] data)
    {
        try
        {
            return decrypt(generateDeviceBoundEncryptionKeyForPassword(key), data);
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }

    public static byte[] generateDeviceBoundEncryptionKeyForPassword(final Context ctx,
                                                                     String password,
                                                                     boolean lockToWifi,
                                                                     boolean lockToTelephony,
                                                                     boolean lockToSIM)
    {
        try
        {
            StringBuffer finalPassword = new StringBuffer(password);
            finalPassword.append(RTDevice.getDeviceUID(ctx, lockToWifi, lockToTelephony, lockToSIM));
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(finalPassword.toString().getBytes("UTF-8"));
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
            return raw;
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }

    public static byte[] generateDeviceBoundEncryptionKeyForPassword(String password)
    {
        try
        {
            StringBuffer finalPassword = new StringBuffer(password);
            finalPassword.append(RTDevice.getDeviceUID(null, false, false, false));
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(password.getBytes());
            kgen.init(128, sr); // 192 and 256 bits may not be available
            SecretKey skey = kgen.generateKey();
            byte[] raw = skey.getEncoded();
            return raw;
        } catch (Throwable err)
        {
            throw new RTCryptoError(err);
        }
    }


    public static byte[] hexStringToBytes(String hexString)
    {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
        {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    public static String bytesToHexString(byte[] buf)
    {
        if (buf == null)
        {
            return "";
        }
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++)
        {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b)
    {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

}
