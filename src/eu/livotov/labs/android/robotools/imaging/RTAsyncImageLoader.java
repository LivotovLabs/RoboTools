package eu.livotov.labs.android.robotools.imaging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import eu.livotov.labs.android.robotools.async.RTQueueExecutor;
import eu.livotov.labs.android.robotools.device.RTDevice;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;


public class RTAsyncImageLoader
{

    private static RTAsyncImageLoader singleton = null;
    private final ExecutorService executor;
    private LruCache<String, Bitmap> cache;

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

    private int loadingStub;
    private int failoverStub;

    private ImageView.ScaleType finalImageScaleType = ImageView.ScaleType.CENTER_CROP;
    private ImageView.ScaleType loadingImageScaleType = ImageView.ScaleType.CENTER_INSIDE;
    private ImageView.ScaleType failoverImageScaleType = ImageView.ScaleType.CENTER_INSIDE;

    protected Context context;

    private static final int MAX_ENTRIES = 150;


    /**
     * Constructs image loader and starts a loading thread. No post-scaling will be applied
     */
    public RTAsyncImageLoader(Context ctx)
    {
        this(ctx, 128, 0, 0);
    }

    /**
     * Constructs image loader and starts a loading thread. Sets the post-scaling parameters to apply
     * to all loaded images.
     *
     * @param explicitWidth  desired image width in pixels, you want to scale any loaded image to. Set to 0 to disable scaling on this axis.
     * @param explicitHeight desired image height in pixels, you want to scale any loaded image to. Set to 0 to disable scaling on this axis.
     */
    public RTAsyncImageLoader(Context ctx, int maxCacheSizeKb, int explicitWidth, int explicitHeight)
    {
        this.explicitWidth = explicitWidth;
        this.explicitHeight = explicitHeight;
        this.cacheDir = ctx.getCacheDir();
        this.context = ctx;
        this.cache = new LruCache<String, Bitmap>(maxCacheSizeKb);
        executor = RTQueueExecutor.create(RTDevice.getCpuCoresCount() * 2);
    }

    public static synchronized RTAsyncImageLoader getDefaultImagesLoader(Context ctx)
    {
        if (singleton == null)
        {
            singleton = new RTAsyncImageLoader(ctx);
        }

        return singleton;
    }

    public static synchronized RTAsyncImageLoader getDefaultImagesLoader(Context ctx, int defaultImgResId)
    {
        if (singleton == null)
        {
            singleton = new RTAsyncImageLoader(ctx);
            singleton.cacheForEmptyUrl(defaultImgResId);
        }

        return singleton;
    }

    public ImageView.ScaleType getFinalImageScaleType()
    {
        return finalImageScaleType;
    }

    public void setFinalImageScaleType(final ImageView.ScaleType finalImageScaleType)
    {
        this.finalImageScaleType = finalImageScaleType;
    }

    public ImageView.ScaleType getLoadingImageScaleType()
    {
        return loadingImageScaleType;
    }

    public void setLoadingImageScaleType(final ImageView.ScaleType loadingImageScaleType)
    {
        this.loadingImageScaleType = loadingImageScaleType;
    }

    public ImageView.ScaleType getFailoverImageScaleType()
    {
        return failoverImageScaleType;
    }

    public void setFailoverImageScaleType(final ImageView.ScaleType failoverImageScaleType)
    {
        this.failoverImageScaleType = failoverImageScaleType;
    }

    public void loadImage(ImageView view, String url, int width, int height)
    {
        loadImage(view, url, width, height, null);
    }

    public void loadImage(ImageView view, String url)
    {
        loadImage(view, url, explicitWidth, explicitHeight, null);
    }

    /**
     * Posts a request to load an image. Requests will be put into a queue and processed in background.
     *
     * @param view TouchImageView instance you need to load and set image to
     * @param url  image url
     */
    public void loadImage(ImageView view, String url, int width, int height, ImageLoadListener onLoadListener)
    {

        String tag;
        tag = UUID.randomUUID().toString();
        view.setTag(tag);
        if (hasCachedImage(url))
        {
            loadCachedImage(view, url, width, height); // RTAsyncImageLoader.getDefaultImagesLoader(context).loadCachedImage(view,url, width, height);

            if (onLoadListener != null)
            {
                onLoadListener.onImageLoaded(view);
            }
        } else
        {
            if (loadingStub > 0)
            {
                if (loadingImageScaleType != null)
                {
                    view.setScaleType(loadingImageScaleType);
                }

                view.setImageResource(loadingStub);
            }

            ImageLoadRequest request = new ImageLoadRequest(view, url, onLoadListener, width, height);
            request.tag = tag;
            executor.execute(new SingleImageLoadTask(request));
        }
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
    public Bitmap getCachedImage(String url, int width, int height)
    {
        try
        {
            Bitmap bitmap = cache.get(url);

            if (bitmap == null)
            {
                bitmap = decodeFile(generateLocalCacheFileName(url), width, height);
                cache.put(url, bitmap);
            }

            return bitmap;
        } catch (Throwable e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Immideately loads the cahced image. If image is not cached, image view will receive a null bitmap. This method is a
     * equivalent of ImageView.setImageBitmap ( ImageLoader.getCachedImage ( url ));
     *
     * @param view TouchImageView to set cached image to
     * @param url  image remote url.
     */
    public void loadCachedImage(ImageView view, String url, int widht, int height)
    {
        Bitmap bitmap = getCachedImage(url, widht, height);

        if (finalImageScaleType != null)
        {
            view.setScaleType(finalImageScaleType);
        }

        disposeOldImageState(view);
        view.setImageBitmap(bitmap);
    }

    /**
     * Terminates the image loader background thread. Usually this method is called automatically on application closure,
     * but you may call it manually. Note, that once thread is terminated, it cannot be resumed and you'll need to
     * create a new instance of the ImageLoader then.
     */
    public void terminateLoader()
    {
        executor.shutdown();
    }

    public void cacheForEmptyUrl(int imgResId)
    {
        Bitmap image = BitmapFactory.decodeResource(context.getResources(), imgResId);
        FileOutputStream fos = null;
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


    protected Bitmap decodeBitmapStream(InputStream is)
    {
        return BitmapFactory.decodeStream(is);
    }

    private void loadToView(final ImageLoadRequest request, final Bitmap image)
    {
        if (request.getView() != null && request.tag != null && request.tag.equals(request.view.getTag()))
        {
            request.getHandler().post(new Runnable()
            {

                public void run()
                {
                    if (finalImageScaleType != null)
                    {
                        request.getView().setScaleType(finalImageScaleType);
                    }

                    disposeOldImageState(request.getView());
                    request.getView().setImageBitmap(image);

                    if (request.imageLoadListener != null)
                    {
                        request.imageLoadListener.onImageLoaded(request.getView());
                    }
                }
            });

        }
    }

    private void loadToView(final ImageLoadRequest request, final int res)
    {
        if (request.getView() != null && request.tag != null && request.tag.equals(request.view.getTag()))
        {
            request.getHandler().post(new Runnable()
            {

                public void run()
                {
                    if (finalImageScaleType != null)
                    {
                        request.getView().setScaleType(finalImageScaleType);
                    }

                    request.getView().setImageResource(res);

                    if (request.imageLoadListener != null)
                    {
                        request.imageLoadListener.onImageLoaded(request.getView());
                    }
                }
            });

        }
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
            byte[] messageDigest = md.digest((url).getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32)
            {
                md5 = "0" + md5;
            }

            return new File(cacheDir, "ILCACHE-" + md5);
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

        cache.evictAll();
    }

    public void setLoadingImage(final int res)
    {
        this.loadingStub = res;
    }

    public void setFailoverImage(final int res)
    {
        this.failoverStub = res;
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

        private long lastModifiedDate;

        private ImageLoadListener imageLoadListener;
        /**
         * Handler object for UI thread synchronization when setting a newly loaded image to an ImageView
         */
        private Handler handler;

        private int width;

        private int height;

        private int retryCount = 0;

        /**
         * Creates the load request instance
         *
         * @param view view to load image for
         * @param url  remote image url
         */
//        public ImageLoadRequest(TouchImageView view, String url, ImageLoadListener imageLoadListener)
//        {
//            this.view = view;
//            this.url = url;
//            this.handler = new Handler();
//            this.imageLoadListener = imageLoadListener;
//        }
        public ImageLoadRequest(ImageView view, String url, ImageLoadListener imageLoadListener, int width, int height)
        {
            this.view = view;
            this.url = url;
            this.handler = new Handler();
            this.imageLoadListener = imageLoadListener;
            this.width = width;
            this.height = height;
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

        public int getRetryCount()
        {
            return retryCount;
        }

        public int incrementAndGetRetryCount()
        {
            retryCount++;
            return retryCount;
        }
    }

    public static Bitmap decodeFile(File file, int width, int height)
            throws IllegalArgumentException, FileNotFoundException
    {
        return RTBitmaps.loadBitmapFromFile(file.getPath(), width, height);
    }

    private void disposeOldImageState(ImageView view)
    {
//        try
//        {
//            final Drawable drawable = view.getDrawable();
//            if (drawable instanceof BitmapDrawable)
//            {
//                final BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
//                final Bitmap bitmap = bitmapDrawable.getBitmap();
//                bitmap.recycle();
//            }
//        } catch (Throwable err)
//        {
//        }
    }

    public interface ImageLoadListener
    {

        void onImageLoaded(ImageView view);
    }

    public class SingleImageLoadTask implements Runnable
    {

        ImageLoadRequest request;

        public SingleImageLoadTask(final ImageLoadRequest request)
        {
            this.request = request;
        }

        public void run()
        {
            Log.d("SingleImageLoadTask", "Loading image " + request.getUrl());

            try
            {
                if (!hasCachedImage(request.url))
                {
                    InputStream is = new BufferedHttpEntity(executeHttpRequest(request.getUrl())).getContent();
                    FileOutputStream fos = new FileOutputStream(generateLocalCacheFileName(request.url));

                    final byte buffer[] = new byte[1024];
                    int read = 1;

                    while (read > 0)
                    {
                        read = is.read(buffer);
                        if (read > 0)
                        {
                            fos.write(buffer, 0, read);
                        }
                    }

                    is.close();
                    fos.flush();
                    fos.close();
                }

                loadToView(request, getCachedImage(request.url, request.width, request.height));
            } catch (Throwable err)
            {
                if (request.incrementAndGetRetryCount() < 3)
                {
                    Log.e("SingleImageLoadTask", "Retrying task due to error for " + request.getUrl(), err);
                    executor.execute(new SingleImageLoadTask(request));
                } else
                {
                    Log.e("SingleImageLoadTask", "Error loading task for " + request.getUrl(), err);
                }
            } finally
            {
                Log.d("SingleImageLoadTask", "Loading task finished for " + request.getUrl());
            }
        }

        private HttpEntity executeHttpRequest(String url) throws IOException
        {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            return httpClient.execute(get).getEntity();
        }


    }


}