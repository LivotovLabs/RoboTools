package eu.livotov.labs.android.robotools.share;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

    public static Intent buildApplicationIntent(final Context ctx, final String packageName, boolean autoInstall)
    {
        PackageManager pm = ctx.getPackageManager();
        boolean packagePresent = false;

        try
        {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            packagePresent = true;
        } catch (PackageManager.NameNotFoundException e)
        {
            packagePresent = false;
        }

        if (packagePresent)
        {
            return pm.getLaunchIntentForPackage(packageName);
        } else
        {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
            //return new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName));
        }

    }

}
