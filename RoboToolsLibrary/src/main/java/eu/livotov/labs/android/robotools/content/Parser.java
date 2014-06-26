package eu.livotov.labs.android.robotools.content;

import android.os.Parcelable;
import eu.livotov.labs.android.robotools.net.Streams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public class Parser {

    private Parser() {}

    public static Document parseXml(InputStream xml) throws IOException {
        return parseXml(xml, "utf-8");
    }

    public static Document parseXml(InputStream xml, final String xmlEncoding) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(Streams.streamToString(xml, xmlEncoding, false).getBytes(xmlEncoding)));
        } catch (Throwable err) {
            throw new DOMException((short) 1, err.getClass().getSimpleName() + " " + err.getMessage());
        }
    }

    public static String toCDATA(String text) {
        return "<![CDATA[" + text + "]]>";
    }

    public static String getTextContent(Node n) {
        StringBuilder b = new StringBuilder();
        NodeList l = n.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                b.append(l.item(i).getNodeValue());
            }
        }
        return b.toString();
    }

    public static String getCDATAContent(Node n) {
        StringBuilder b = new StringBuilder();
        NodeList l = n.getChildNodes();

        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                b.append(l.item(i).getNodeValue());
            }
        }

        return b.toString();
    }
}
