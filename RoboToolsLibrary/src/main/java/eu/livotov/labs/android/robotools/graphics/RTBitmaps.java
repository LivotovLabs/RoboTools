package eu.livotov.labs.android.robotools.graphics;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RTBitmaps
{

    private RTBitmaps()
    {
    }

    public static Bitmap loadBitmapFromUrl(final String link, int downscaleSize) throws IOException
    {
        URL url = new URL(link);

        if (downscaleSize > 0)
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            BitmapFactory.decodeStream(input, null, options);

            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();

            int inSampleSize = calculateInSampleSize(options, downscaleSize);
            options = new BitmapFactory.Options();
            options.inSampleSize = inSampleSize;

            return BitmapFactory.decodeStream(input, null, options);
        }
        else
        {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            return BitmapFactory.decodeStream(input);
        }
    }

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
        if (reqSize > 0)
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
            }
            catch (IOException err)
            {
                return null;
            }
        }
        else
        {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
    }

    public static void saveBitmapToFile(Bitmap bm, int quality, File file) throws IOException
    {
        if (bm != null)
        {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
        }
        else
        {
            file.delete();
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
