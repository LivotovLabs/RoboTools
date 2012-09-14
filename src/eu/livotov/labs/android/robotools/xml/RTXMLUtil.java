package eu.livotov.labs.android.robotools.xml;

import android.content.Context;
import eu.livotov.labs.android.robotools.io.RTStreamUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTXMLUtil {

    public static Document parse(InputStream xml) throws IOException
    {
        return parse(xml,"utf-8");
    }

    public static Document parse(InputStream xml, final String xmlEncoding) throws IOException
    {
        StringBuffer xmlstr = new StringBuffer();

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(RTStreamUtil.streamToString(xml, xmlEncoding, false).getBytes(xmlEncoding)));
        } catch (Throwable err)
        {
            throw new DOMException((short) 1, err.getClass().getSimpleName() + " " + err.getMessage());
        }
    }

    public static String toCDATA(String text)
    {
        return "<![CDATA[" + text + "]]>";
    }

    public static String getTextContent(Node n)
    {
        StringBuffer b = new StringBuffer();
        NodeList l = n.getChildNodes();

        for (int i = 0; i < l.getLength(); i++)
        {
            if (l.item(i).getNodeType() == Node.TEXT_NODE)
            {
                b.append(l.item(i).getNodeValue());
            }
        }

        return b.toString();
    }

    public static String getCDATAContent(Node n)
    {
        StringBuffer b = new StringBuffer();
        NodeList l = n.getChildNodes();

        for (int i = 0; i < l.getLength(); i++)
        {
            if (l.item(i).getNodeType() == Node.CDATA_SECTION_NODE)
            {
                b.append(l.item(i).getNodeValue());
            }
        }

        return b.toString();
    }
}
