package eu.livotov.labs.android.robotools.content;

public class ServerException extends Exception {
    public int code;
    public CharSequence message;

    public ServerException(int code, CharSequence message) {
        super(message.toString());
        this.code = code;
        this.message = message;
    }
}
