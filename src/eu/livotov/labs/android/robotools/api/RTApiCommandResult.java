package eu.livotov.labs.android.robotools.api;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 29.01.13
 */
public abstract class RTApiCommandResult {
    private RTApiCommand command;

    protected void loadResponseData(RTApiCommand command, String data) {
        this.command = command;
        processCommandSpecificData(data);
    }

    protected abstract void processCommandSpecificData(final String json);

    public RTApiCommand getCommand() {
        return command;
    }
}
