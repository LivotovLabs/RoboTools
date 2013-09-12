package eu.livotov.labs.android.robotools.net;

import android.os.Parcel;
import android.os.Parcelable;
import org.apache.http.NameValuePair;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 9/14/12
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class RTPostParameter implements NameValuePair, Parcelable
{

    private String name;
    private String value;
    private File attachment;

    public RTPostParameter(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getValue()
    {
        return value;
    }

    public File getAttachment()
    {
        return attachment;
    }

    public void setAttachment(final File attachment)
    {
        this.attachment = attachment;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(name);
        dest.writeString(value);
    }

    public static final Creator<RTPostParameter> CREATOR = new Creator<RTPostParameter>()
    {
        public RTPostParameter createFromParcel(Parcel source)
        {
            return new RTPostParameter(source.readString(), source.readString());
        }

        public RTPostParameter[] newArray(int size)
        {
            return new RTPostParameter[size];
        }
    };
}
