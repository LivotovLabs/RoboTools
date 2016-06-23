package eu.livotov.labs.android.robotools.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

/**
 * Utility class which builds various data sharing intents
 */
public class RTShareTool
{

    /**
     * Builds an intent to place a call
     *
     * @param number number to call to or url resource of the "tel:" scheme
     * @return
     */
    public static Intent buildCallIntent(@NonNull final String number)
    {
        return new Intent(Intent.ACTION_CALL).setData(Uri.parse(number.toLowerCase().startsWith("tel:") ? number : String.format("tel:%s", PhoneNumberUtils.stripSeparators(number))));
    }

    /**
     * Builds an intent to open url in a default web browser (or maybe in other app which intercepts this url scheme/part)
     *
     * @param url to open
     * @return
     */
    public static Intent buildInternetAddressIntent(@NonNull final String url)
    {
        return new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
    }

    public static void openInternetAddressAsChromeTab(@NonNull Activity activity, @NonNull final String url)
    {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

    /**
     * Builds an intent to open new e-mail compose screen in one of e-mail apps installed in the system
     *
     * @param to      recipient address
     * @param subject e-mail subject
     * @param body    e-mail body
     * @return
     */
    public static Intent buildEmailIntent(@NonNull final String to, @Nullable final String subject, @Nullable final String body)
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

    /**
     * Builds an intent to open a contact card
     *
     * @param contactId contact id
     * @return
     */
    public static Intent buildContactCardIntent(@NonNull final String contactId)
    {
        final Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    /**
     * Builds an intent to share piece of text
     *
     * @param text text to share
     * @return
     */
    public static Intent buildShareTextIntent(@NonNull final String text)
    {
        Intent textIntent = new Intent(Intent.ACTION_SEND);
        textIntent.setType("text/plain");
        textIntent.putExtra(Intent.EXTRA_TEXT, text);

        return textIntent;
    }

    /**
     * Builds an intent to launch another application or force user to install it from Google Play
     *
     * @param ctx         context
     * @param packageName application id (package name) to launch
     * @param autoInstall when set to <code>true</code> and target app is not installed, a Google Play app page will be open, suggesting user to install the app.
     * @return intent to launch app or Google Play or  null, if no app installed and autoInstall argument was set to <code>false</code>
     */
    public static Intent buildApplicationIntent(final Context ctx, final String packageName, boolean autoInstall)
    {
        PackageManager pm = ctx.getPackageManager();
        boolean packagePresent = false;

        try
        {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            packagePresent = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            packagePresent = false;
        }

        if (packagePresent)
        {
            return pm.getLaunchIntentForPackage(packageName);
        }
        else if (autoInstall)
        {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName));
        }
        else
        {
            return null;
        }
    }

}
