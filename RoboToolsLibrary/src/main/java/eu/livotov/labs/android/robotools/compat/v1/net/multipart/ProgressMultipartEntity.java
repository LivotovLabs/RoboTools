package eu.livotov.labs.android.robotools.compat.v1.net.multipart;

import android.content.Context;
import org.apache.http.params.HttpParams;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 8:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressMultipartEntity extends MultipartEntity
{

    private ProgressCallback callback;

    public ProgressMultipartEntity(Part[] parts, ProgressCallback callback)
    {
        super(parts);
        this.callback = callback;
    }

    public ProgressMultipartEntity(Part[] parts, HttpParams params, ProgressCallback callback)
    {
        super(parts, params);
        this.callback = callback;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException
    {
        super.writeTo(new CountingOutputStream(outstream, callback));
    }

    public static class CountingOutputStream extends FilterOutputStream
    {

        private Context ctx;
        private long transferred;
        private ProgressCallback callback;

        public CountingOutputStream(final OutputStream out, ProgressCallback callback)
        {
            super(out);
            this.callback = callback;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException
        {
            out.write(b, off, len);
            this.transferred += len;
            broadcastProgress(transferred);
        }

        public void write(int b) throws IOException
        {
            out.write(b);
            this.transferred++;
            broadcastProgress(transferred);
        }

        private void broadcastProgress(long transferred)
        {
            callback.submitProgressUpdate(transferred);
        }
    }

    public interface ProgressCallback
    {

        void submitProgressUpdate(final long bytesTransferred);
    }
}
