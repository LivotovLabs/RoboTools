package eu.livotov.labs.android.robotools.share;

import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 10/07/2014
 */
public class RTShareTool
{

    public static Intent buildCallIntent(final String number)
    {
        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(number.toLowerCase().startsWith("tel:") ? number : String.format("tel:%s", PhoneNumberUtils.stripSeparators(number))));

        return callIntent;
    }

    public static Intent buildInternetAddressIntent(final String url)
    {
        return new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
    }

    public static Intent buildEmailIntent(final String to, final String subject, final String body)
    {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{to});
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!TextUtils.isEmpty(subject))
        {
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        }

        if (!TextUtils.isEmpty(body))
        {
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        }

        return emailIntent;
    }

    public static Intent buildContactCardIntent(final String contactId)
    {
        final Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent buildShareTextIntent(final String text)
    {
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, text);

        return textIntent;
    }

}
