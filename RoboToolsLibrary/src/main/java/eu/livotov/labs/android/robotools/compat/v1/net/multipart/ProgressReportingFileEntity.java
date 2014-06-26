package eu.livotov.labs.android.robotools.compat.v1.net.multipart;

import org.apache.http.entity.FileEntity;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 8:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressReportingFileEntity extends FileEntity {

    public final static String UPLOAD_PROGRESS_BROADCAST = ProgressReportingFileEntity.class.getName();
    public final static String EXTRA_PROGRESS = "progress";
    public final static String EXTRA_BYTES = "bytes";

    /**
     * Constructor
     *
     * @param file        File to user
     * @param contentType File mime-type
     */
    public ProgressReportingFileEntity(final File file, final String contentType) {
        super(file, contentType);
    }

    /**
     * Writes file to http client outputstream. We watch for progress here and
     * update upload notification by every 10 percent of increase
     *
     * @param outstream Http client outputstream
     * @throws IOException
     */
    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        // check if we have an outputstrean
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }

        // create file input stream
        InputStream instream = new FileInputStream(this.file);

        try {
            // create vars
            byte[] tmp = new byte[4096];
            int total = (int) this.file.length();
            int progress = 0;
            int increment = 10;
            int l;
            int percent;

            // read file and write to http output stream
            while ((l = instream.read(tmp)) != -1) {
                // check progress
                progress = progress + l;
                percent = Math.round(((float) progress / (float) total) * 100);

                // if progress exceeds increment update status notification
                // and adjust increment
                if (percent > increment) {
                    increment += 10;
                    //todo: broadcast notification.update(progress);
                }

                // write to output stream
                outstream.write(tmp, 0, l);
            }

            // flush output stream
            outstream.flush();
        } finally {
            // close input stream
            instream.close();
        }
    }
}
