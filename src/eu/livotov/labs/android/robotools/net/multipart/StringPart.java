package eu.livotov.labs.android.robotools.net.multipart;
import org.apache.http.util.EncodingUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple string parameter for a multipart post
 *
 * @author <a href="mailto:mattalbright@yahoo.com">Matthew Albright</a>
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *
 * @since 2.0
 */
public class StringPart extends PartBase {

    /** Default content encoding of string parameters. */
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    /** Default charset of string parameters*/
    public static final String DEFAULT_CHARSET = "US-ASCII";

    /** Default transfer encoding of string parameters*/
    public static final String DEFAULT_TRANSFER_ENCODING = "8bit";

    /** Contents of this StringPart. */
    private byte[] content;

    /** The String value of this part. */
    private String value;

    /**
     * Constructor.
     *
     * @param name The name of the part
     * @param value the string to post
     * @param charset the charset to be used to encode the string, if <code>null</code>
     * the {@link #DEFAULT_CHARSET default} is used
     */
    public StringPart(String name, String value, String charset) {

        super(
            name,
            DEFAULT_CONTENT_TYPE,
            charset == null ? DEFAULT_CHARSET : charset,
            DEFAULT_TRANSFER_ENCODING
        );
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null");
        }
        if (value.indexOf(0) != -1) {
            // See RFC 2048, 2.8. "8bit Data"
            throw new IllegalArgumentException("NULs may not be present in string parts");
        }
        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param name The name of the part
     * @param value the string to post
     */
    public StringPart(String name, String value) {
        this(name, value, null);
    }

    /**
     * Gets the content in bytes.  Bytes are lazily created to allow the charset to be changed
     * after the part is created.
     *
     * @return the content in bytes
     */
    private byte[] getContent() {
        if (content == null) {
            content = EncodingUtils.getBytes(value, getCharSet());
        }
        return content;
    }

    /**
     * Writes the data to the given OutputStream.
     * @param out the OutputStream to write to
     * @throws IOException if there is a write error
     */
    @Override
    protected void sendData(OutputStream out) throws IOException {
        out.write(getContent());
    }

    /**
     * Return the length of the data.
     * @return The length of the data.
     * @see Part#lengthOfData()
     */
    @Override
    protected long lengthOfData() {
        return getContent().length;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.multipart.BasePart#setCharSet(java.lang.String)
     */
    @Override
    public void setCharSet(String charSet) {
        super.setCharSet(charSet);
        this.content = null;
    }

}