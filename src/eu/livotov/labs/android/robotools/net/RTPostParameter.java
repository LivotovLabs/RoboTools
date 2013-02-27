package eu.livotov.labs.android.robotools.net;

import org.apache.http.NameValuePair;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTPostParameter implements NameValuePair {

    private String name;
    private String value;

    public RTPostParameter(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }
}
