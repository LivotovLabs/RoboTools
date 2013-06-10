package eu.livotov.labs.android.robotools.imaging;

import android.graphics.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 31.10.12
 * Time: 1:35
 * To change this template use File | Settings | File Templates.
 */
public class RTBitmaps
{

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqSize)
    {
        int scale = 1;
        if (options.outHeight > reqSize || options.outWidth > reqSize)
        {
            scale = (int) Math.pow(2, (int) Math.round(Math.log(reqSize / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
        }
        return scale;
    }

    public static Bitmap loadBitmapFromFile(File file, int reqSize)
    {
        try
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            FileInputStream fis = new FileInputStream(file);
            BitmapFactory.decodeStream(fis, null, options);
            fis.close();

            int inSampleSize = calculateInSampleSize(options, reqSize);
            options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;
            fis = new FileInputStream(file);
            final Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
            fis.close();
            return bitmap;
        } catch (IOException err)
        {
            return null;
        }
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}