package eu.livotov.labs.android.robotools.compat.v1.crypt;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 28.10.12
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public class RTCryptoError extends RuntimeException {
    public RTCryptoError(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
