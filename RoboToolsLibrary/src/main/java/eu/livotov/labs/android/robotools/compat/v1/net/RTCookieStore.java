package eu.livotov.labs.android.robotools.compat.v1.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import eu.livotov.labs.android.robotools.compat.v1.crypt.RTBase64;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 27.10.12
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class RTCookieStore implements CookieStore
{

    private static final String CS_KEY = "robotools.net.cstore";
    private List<Cookie> cookies = new ArrayList<Cookie>();

    public RTCookieStore()
    {
    }

    public void addCookie(final Cookie cookie)
    {
        int index = getCookieIndex(cookie.getName());
        if (index != -1)
        {
            cookies.remove(index);
        }
        cookies.add(cookie);
    }

    private int getCookieIndex(String key)
    {
        for (int i = 0; i < cookies.size(); i++)
        {
            Cookie cookie = cookies.get(i);
            if (cookie.getName().equalsIgnoreCase(key))
            {
                return i;
            }
        }

        return -1;
    }

    public List<Cookie> getCookies()
    {
        return cookies;
    }

    public Cookie getCookie(final String key)
    {
        clearExpired(new Date(System.currentTimeMillis()));
        for (Cookie cookie : cookies)
        {
            if (cookie.getName().equalsIgnoreCase(key))
            {
                return cookie;
            }
        }

        return null;
    }

    public boolean hasCookie(final String name)
    {
        for (Cookie ck : cookies)
        {
            if (ck.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }

        return false;
    }

    public boolean clearExpired(final Date date)
    {
        List<Cookie> toExpire = new ArrayList<Cookie>();

        for (Cookie cookie : cookies)
        {
            if (cookie.isExpired(date))
            {
                toExpire.add(cookie);
            }
        }

        if (toExpire.size() > 0)
        {
            cookies.removeAll(toExpire);
            toExpire.clear();
            return true;
        }

        return false;
    }

    public void clear()
    {
        cookies.clear();
    }

    public void saveCookieStore(final Context ctx) throws IOException
    {
        final List<Cookie> serialisableCookies = new ArrayList<Cookie>(cookies.size());

        for (Cookie cookie : cookies)
        {
            serialisableCookies.add(new SerializableCookie(cookie));
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(serialisableCookies);
        oos.flush();
        oos.close();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CS_KEY, RTBase64.encodeToString(bos.toByteArray(), RTBase64.DEFAULT));
        editor.commit();
    }

    public void readCookieStore(final Context ctx)
    {
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
            ByteArrayInputStream bis = new ByteArrayInputStream(RTBase64.decode(preferences.getString(CS_KEY, ""), RTBase64.DEFAULT));
            ObjectInputStream ois = new ObjectInputStream(bis);
            List<Cookie> restoredCookies = (ArrayList<Cookie>) ois.readObject();
            ois.close();
            cookies.clear();
            cookies.addAll(restoredCookies);
        } catch (Throwable err)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(CS_KEY);
            editor.commit();
            err.printStackTrace();
        }
    }

    public class SerializableCookie implements Cookie, Externalizable
    {

        private static final int NAME = 0x01;
        private static final int VALUE = 0x02;
        private static final int COMMENT = 0x04;
        private static final int COMMENT_URL = 0x08;
        private static final int EXPIRY_DATE = 0x10;
        private static final int DOMAIN = 0x20;
        private static final int PATH = 0x40;
        private static final int PORTS = 0x80;

        private transient int nullMask = 0;
        private transient Cookie cookie;

        public SerializableCookie()
        {
            super();
        }

        public SerializableCookie(final Cookie cookie)
        {
            super();

            this.cookie = cookie;
        }

        public String getName()
        {
            return cookie.getName();
        }

        public String getValue()
        {
            return cookie.getValue();
        }

        public String getComment()
        {
            return cookie.getComment();
        }

        public String getCommentURL()
        {
            return cookie.getCommentURL();
        }

        public Date getExpiryDate()
        {
            return cookie.getExpiryDate();
        }

        public boolean isPersistent()
        {
            return cookie.isPersistent();
        }

        public String getDomain()
        {
            return cookie.getDomain();
        }

        public String getPath()
        {
            return cookie.getPath();
        }

        public int[] getPorts()
        {
            return cookie.getPorts();
        }

        public boolean isSecure()
        {
            return cookie.isSecure();
        }

        public int getVersion()
        {
            return cookie.getVersion();
        }

        public boolean isExpired(final Date date)
        {
            return cookie.isExpired(date);
        }

        public void writeExternal(final ObjectOutput out) throws IOException
        {
            nullMask |= (getName() == null) ? NAME : 0;
            nullMask |= (getValue() == null) ? VALUE : 0;
            nullMask |= (getComment() == null) ? COMMENT : 0;
            nullMask |= (getCommentURL() == null) ? COMMENT_URL : 0;
            nullMask |= (getExpiryDate() == null) ? EXPIRY_DATE : 0;
            nullMask |= (getDomain() == null) ? DOMAIN : 0;
            nullMask |= (getPath() == null) ? PATH : 0;
            nullMask |= (getPorts() == null) ? PORTS : 0;

            out.writeInt(nullMask);

            if ((nullMask & NAME) == 0)
            {
                out.writeUTF(getName());
            }

            if ((nullMask & VALUE) == 0)
            {
                out.writeUTF(getValue());
            }

            if ((nullMask & COMMENT) == 0)
            {
                out.writeUTF(getComment());
            }

            if ((nullMask & COMMENT_URL) == 0)
            {
                out.writeUTF(getCommentURL());
            }

            if ((nullMask & EXPIRY_DATE) == 0)
            {
                out.writeLong(getExpiryDate().getTime());
            }

            out.writeBoolean(isPersistent());

            if ((nullMask & DOMAIN) == 0)
            {
                out.writeUTF(getDomain());
            }

            if ((nullMask & PATH) == 0)
            {
                out.writeUTF(getPath());
            }

            if ((nullMask & PORTS) == 0)
            {
                out.writeInt(getPorts().length);

                for (int p : getPorts())
                {
                    out.writeInt(p);
                }
            }

            out.writeBoolean(isSecure());
            out.writeInt(getVersion());
        }


        public void readExternal(final ObjectInput in) throws IOException,
                                                              ClassNotFoundException
        {
            nullMask = in.readInt();

            String name = null;
            String value = null;
            String comment = null;
            String commentURL = null;
            Date expiryDate = null;
            boolean isPersistent = false;
            String domain = null;
            String path = null;
            int[] ports = null;
            boolean isSecure = false;
            int version = 0;

            if ((nullMask & NAME) == 0)
            {
                name = in.readUTF();
            }

            if ((nullMask & VALUE) == 0)
            {
                value = in.readUTF();
            }

            if ((nullMask & COMMENT) == 0)
            {
                comment = in.readUTF();
            }

            if ((nullMask & COMMENT_URL) == 0)
            {
                commentURL = in.readUTF();
            }

            if ((nullMask & EXPIRY_DATE) == 0)
            {
                expiryDate = new Date(in.readLong());
            }

            isPersistent = in.readBoolean();

            if ((nullMask & DOMAIN) == 0)
            {
                domain = in.readUTF();
            }

            if ((nullMask & PATH) == 0)
            {
                path = in.readUTF();
            }

            if ((nullMask & PORTS) == 0)
            {
                final int len = in.readInt();

                ports = new int[len];

                for (int i = 0; i < len; i++)
                {
                    ports[i] = in.readInt();
                }
            }

            isSecure = in.readBoolean();
            version = in.readInt();

            final BasicClientCookie bc = new BasicClientCookie(name, value);

            bc.setComment(comment);
            bc.setDomain(domain);
            bc.setExpiryDate(expiryDate);
            bc.setPath(path);
            bc.setSecure(isSecure);
            bc.setVersion(version);

            this.cookie = bc;
        }

        @Override
        public String toString()
        {
            if (cookie == null)
            {
                return "null";
            } else
            {
                return cookie.toString();
            }
        }


    }
}