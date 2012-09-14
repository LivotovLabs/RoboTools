package eu.livotov.labs.android.robotools.io;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTStreamUtil {

    public static String streamToString(InputStream stream, String encoding, boolean keepLineBreaks)
    {
        try
        {
            BufferedReader rd = new BufferedReader(new InputStreamReader(stream, encoding), 65535);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = rd.readLine()) != null)
            {
                sb.append(line);
                if (keepLineBreaks)
                {
                    sb.append("\n");
                }
            }

            rd.close();
            return sb.toString();
        } catch (Throwable err)
        {
            throw new RuntimeException(err.getMessage(), err);
        }
    }
}
