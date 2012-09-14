package eu.livotov.labs.android.robotools.net.multipart;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import org.apache.http.params.HttpParams;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 8:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressMultipartEntity extends MultipartEntity {

    public static final String UPLOAD_PROGRESS_BROADCAST = "ProgressMultipartEntity.Progress";
    public static final String EXTRA_BYTES = "bytes";
    private Context ctx;

    public ProgressMultipartEntity(Context ctx, Part[] parts)
    {
        super(parts);
        this.ctx = ctx;
    }

    public ProgressMultipartEntity(Context ctx, Part[] parts, HttpParams params)
    {
        super(parts,params);
        this.ctx = ctx;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException
    {
        super.writeTo(new CountingOutputStream(outstream,ctx));
    }

    public static interface ProgressListener
    {
        void transferred(long num);
    }

    public static class CountingOutputStream extends FilterOutputStream
    {
        private Context ctx;
        private long transferred;

        public CountingOutputStream(final OutputStream out, Context ctx)
        {
            super(out);
            this.ctx = ctx;
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

        private void broadcastProgress(long transferred) {
            ctx.sendBroadcast(new Intent(UPLOAD_PROGRESS_BROADCAST).putExtra(EXTRA_BYTES,transferred));
        }
    }
}
