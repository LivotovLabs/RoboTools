package eu.livotov.labs.android.robotools.crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTCryptUtil {

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
}
