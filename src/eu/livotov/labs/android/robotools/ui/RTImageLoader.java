package eu.livotov.labs.android.robotools.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 29.10.12
 * Time: 4:30
 * To change this template use File | Settings | File Templates.
 */
public class RTImageLoader implements Runnable
{

    private static RTImageLoader singleton = null;
    public static int DEFAULT_IMAGE_SIZE = 500;

    /**
     * Image loading requests queue
     */
    private Queue<ImageLoadRequest> loadQueue = new ConcurrentLinkedQueue<ImageLoadRequest>();
    private Map<String, String> requestsMap = new ConcurrentHashMap<String, String>();

    /**
     * Internal flag to indicate background thread that we need to shutdown
     */
    private volatile boolean mustTerminate = false;

    /**
     * If set to >0, means the width in pixels in case we want to post-scale all loaded images.
     */
    private int explicitWidth = 0;

    /**
     * If set to >0, means the height in pixels in case we want to post-scale all loaded images.
     */
    private int explicitHeight = 0;

    private File cacheDir = null;

    private HttpClient httpClient = null;

    private Context context;

    /**
     * Constructs image loader and starts a loading thread. No post-scaling will be applied
     */
    public RTImageLoader(Context ctx)
    {
        this(ctx, 0, 0);
    }

    /**
     * Constructs image loader and starts a loading thread. Sets the post-scaling parameters to apply
     * to all loaded images.
     *
     * @param explicitWidth  desired image width in pixels, you want to scale any loaded image to. Set to 0 to disable scaling on this axis.
     * @param explicitHeight desired image height in pixels, you want to scale any loaded image to. Set to 0 to disable scaling on this axis.
     */
    public RTImageLoader(Context ctx, int explicitWidth, int explicitHeight)
    {
        this.explicitWidth = explicitWidth;
        this.explicitHeight = explicitHeight;
        this.cacheDir = ctx.getApplicationContext().getCacheDir();
        this.context = ctx;
        new Thread(this).start();
    }

    public static synchronized RTImageLoader getDefaultImagesLoader(Context ctx)
    {
        if (singleton == null)
        {
            singleton = new RTImageLoader(ctx, 0, 0);
        }

        return singleton;
    }

    public static synchronized RTImageLoader getDefaultImagesLoader(Context ctx, int defaultImgResId)
    {
        if (singleton == null)
        {
            singleton = new RTImageLoader(ctx, 0, 0);
            singleton.cacheForEmptyUrl(defaultImgResId);
        }

        return singleton;
    }

    /**
     * Posts a request to load an image. Requests will be put into a queue and processed in background.
     *
     * @param view ImageView instance you need to load and set image to
     * @param url  image url
     */
    public synchronized void loadImage(ImageView view, String url)
    {
        if (view != null)
        {
            view.setImageResource(0);
        }

        ImageLoadRequest request = new ImageLoadRequest(view, url);
        String tag = "" + request.view.getTag();

        if (requestsMap.containsKey(tag))
        {
            loadQueue.remove(request);
            requestsMap.remove(tag);
        }

        tag = UUID.randomUUID().toString();
        view.setTag(tag);
        request.tag = tag;
        requestsMap.put(tag, "");
        loadQueue.add(request);

        synchronized (this)
        {
            notifyAll();
        }
    }

    /**
     * Returns local cached file of the image. If image is not loaded it will be loaded in the same thread,
     * so calling method will wait.
     *
     * @param url image to load
     * @return local file of that image.
     */
    public synchronized File getCachedImageFile(String url)
    {
        try
        {
            ImageLoadRequest dummyRequest = new ImageLoadRequest(url);
            loadImage(dummyRequest);

            return generateLocalCacheFileName(url);
        } catch (Throwable err)
        {
            err.printStackTrace();
            return null;
        }
    }

    public synchronized void cacheImage(String url)
    {
        ImageLoadRequest dummyRequest = new ImageLoadRequest(url);
        loadImage(dummyRequest);
    }

    /**
     * Checks if the local cache has the cached instance of the image, specified by url
     *
     * @param url image url to load
     * @return <code>true</code> if the local cache has the image
     */
    public boolean hasCachedImage(String url)
    {
        return generateLocalCacheFileName(url).exists();
    }

    /**
     * Returns cached image immideately
     *
     * @param url url of the remote image
     * @return image instance as a Bitmap. If image is not chached, a null will be returned
     */
    public Bitmap getCachedImage(String url)
    {
        try
        {
            return decodeFile(generateLocalCacheFileName(url), DEFAULT_IMAGE_SIZE);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Immediately loads the cached image. If image is not cached, image view will receive a null bitmap. This method is a
     * equivalent of ImageView.setImageBitmap ( ImageLoader.getCachedImage ( url ));
     *
     * @param view image view to set cached image to
     * @param url  image remote url.
     */
    public void loadCachedImage(ImageView view, String url)
    {
        view.setImageBitmap(getCachedImage(url));
    }

    /**
     * Terminates the image loader background thread. Usually this method is called automatically on application closure,
     * but you may call it manually. Note, that once thread is terminated, it cannot be resumed and you'll need to
     * create a new instance of the ImageLoader then.
     */
    public void terminateLoader()
    {
        mustTerminate = true;
        loadQueue.clear();

        synchronized (this)
        {
            notifyAll();
        }
    }

    /**
     * Body of the image loading thread. NEVER call this method manually, it will be called by the corresponding
     * service thread instance.
     */
    public void run()
    {
        while (!mustTerminate)
        {
            synchronized (this)
            {
                try
                {
                    wait();
                } catch (InterruptedException ignored)
                {
                }
            }

            while (!mustTerminate && loadQueue.peek() != null)
            {
                loadImage(loadQueue.poll());
            }
        }
    }

    public void cacheForEmptyUrl(int imgResId)
    {
        Bitmap image = scaleImage(BitmapFactory.decodeResource(context.getResources(), imgResId));
        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(generateLocalCacheFileName(""));
            image.compress(Bitmap.CompressFormat.PNG, 50, fos);
            fos.close();
        } catch (Throwable err)
        {
            System.err.println("Error loading local img");
            err.printStackTrace();
        }

    }

    /**
     * Physically loads an image from a remote URL, caches it and also sets it into the image view
     *
     * @param request image loadig reques from the queue
     */
    private void loadImage(final ImageLoadRequest request)
    {
        try
        {
            if (!hasCachedImage(request.getUrl()))
            {
                InputStream is = new BufferedHttpEntity(executeHttpRequest(request.getUrl())).getContent();
                Bitmap image = scaleImage(BitmapFactory.decodeStream(is));
                is.close();

                FileOutputStream fos = new FileOutputStream(generateLocalCacheFileName(request.getUrl()));
                image.compress(Bitmap.CompressFormat.PNG, 50, fos);
                fos.close();
            }

            if (request.getView() != null && request.tag != null && request.tag.equals("" + request.view.getTag()))
            {
                request.getHandler().post(new Runnable()
                {

                    public void run()
                    {
                        request.getView().setImageBitmap(getCachedImage(request.getUrl()));
                    }
                });
            }

        } catch (Throwable err)
        {
            System.err.println("Error loading from url: " + request.getUrl());
            err.printStackTrace();
        }
    }

    private HttpEntity executeHttpRequest(String url) throws IOException
    {
        HttpGet get = new HttpGet(url);
        HttpClient httpclient = getHttpClient();
        return httpclient.execute(get).getEntity();
    }

    private HttpClient getHttpClient()
    {
        if (httpClient == null)
        {
            httpClient = new DefaultHttpClient();
        }

        return httpClient;
    }

    /**
     * Scales image if necessary, returning the scaled version of the image. Scaling will be only applied if
     * corresponding explicitWidth and/or explicitHeight fields are set
     *
     * @param original original image to scale when necessary.
     * @return scaled (or original, if no scaling was applied) version of the image
     */
    private Bitmap scaleImage(Bitmap original)
    {
        if (explicitWidth == 0 && explicitHeight == 0)
        {
            return original;
        }


        float scaleWidth = explicitWidth > 0 ? ((float) explicitWidth) / original.getWidth() : original.getWidth();
        float scaleHeight = explicitHeight > 0 ? ((float) explicitHeight) / original.getHeight() : original.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
    }

    @Override
    protected void finalize() throws Throwable
    {
        terminateLoader();
        super.finalize();
    }

    public File getCacheFolder()
    {
        return cacheDir;
    }

    public File generateLocalCacheFileName(String url)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(url.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32)
            {
                md5 = "0" + md5;
            }

            return new File(cacheDir, "ILCACHE-" + explicitWidth + "" + explicitHeight + md5);
        } catch (NoSuchAlgorithmException e)
        {
            Log.e("MD5", e.getMessage());
            return new File("ILCACHE-" + UUID.randomUUID().toString());
        }
    }

    public void clearCache()
    {
        File[] tempFiles = cacheDir.listFiles();

        for (File file : tempFiles)
        {
            if (file.isFile() && file.getName().startsWith("ILCACHE"))
            {
                file.delete();
            }
        }
    }

    /**
     * Container to hold a single image loasing request. Used internally to manage the queue.
     */
    public class ImageLoadRequest
    {

        /**
         * Tag to identify request and view
         */
        private String tag;

        /**
         * View to load image for
         */
        private ImageView view;
        /**
         * Url to load image from
         */
        private String url;
        /**
         * Handler object for UI thread synchronization when setting a newly loaded image to an ImageView
         */
        private Handler handler;

        /**
         * Creates the load request instance
         *
         * @param view view to load image for
         * @param url  remote image url
         */
        public ImageLoadRequest(ImageView view, String url)
        {
            this.view = view;
            this.url = url;
            this.handler = new Handler();
        }

        /**
         * Creates viewless request that can be used just to pre-load an image silently, without
         * interacting then with UI.
         *
         * @param url Image url to preload and cache
         */
        public ImageLoadRequest(String url)
        {
            this.view = null;
            this.url = url;
            this.handler = null;
        }

        public String getUrl()
        {
            return url;
        }

        public ImageView getView()
        {
            return view;
        }

        public Handler getHandler()
        {
            return handler;
        }


    }

    public static Bitmap decodeFile(File file, int requiredSize)
            throws IllegalArgumentException, FileNotFoundException
    {
        if (requiredSize <= 0)
        {
            throw new IllegalArgumentException(
                                                      "RequiredSize can't be less to zero");
        }

        // decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(new FileInputStream(file), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true)
        {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
            {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale++;
        }

        // decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(new FileInputStream(file), null, o2);
    }


}
