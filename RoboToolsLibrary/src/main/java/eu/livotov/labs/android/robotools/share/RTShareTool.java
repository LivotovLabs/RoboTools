package eu.livotov.labs.android.robotools.share;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.customtabs.CustomTabsIntent;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class which builds various data sharing intents
 */
public class RTShareTool {

    /**
     * Builds an intent to place a call
     *
     * @param number number to call to or url resource of the "tel:" scheme
     * @return
     */
    public static Intent buildCallIntent(@NonNull final String number) {
        return new Intent(Intent.ACTION_CALL).setData(Uri.parse(number.toLowerCase().startsWith("tel:") ? number : String.format("tel:%s", PhoneNumberUtils.stripSeparators(number))));
    }

    /**
     * Builds an intent to open url in a default web browser (or maybe in other app which intercepts this url scheme/part)
     *
     * @param url to open
     * @return
     */
    public static Intent buildInternetAddressIntent(@NonNull final String url) {
        return new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
    }

    /**
     * Opens an internet address (web page) in a custom chrome tab inside your app.
     * This works only in case the target phone has a proepr version of Google Chrome installed, otherwise, the specified url will be opened using the phone's default browser.
     *
     * @param activity activity to open chrome tab in
     * @param url      web address to open in the chrome tab
     */
    public static void openInternetAddressAsChromeTab(@NonNull Activity activity, @NonNull final String url) {
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
    public static Intent buildEmailIntent(@NonNull final String to, @Nullable final String subject, @Nullable final String body) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{to});
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (!TextUtils.isEmpty(subject)) {
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        }

        if (!TextUtils.isEmpty(body)) {
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
    public static Intent buildContactCardIntent(@NonNull final String contactId) {
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
    public static Intent buildShareTextIntent(@NonNull final String text) {
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
    public static Intent buildApplicationIntent(final Context ctx, final String packageName, boolean autoInstall) {
        PackageManager pm = ctx.getPackageManager();
        boolean packagePresent = false;

        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            packagePresent = true;
        } catch (PackageManager.NameNotFoundException e) {
            packagePresent = false;
        }

        if (packagePresent) {
            return pm.getLaunchIntentForPackage(packageName);
        } else if (autoInstall) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName));
        } else {
            return null;
        }
    }

    /**
     * Build an intent to open navigation app from current location to specified point.
     * Typically, a Google Maps app will handle this intent.
     *
     * @param lat destination point latitude
     * @param lon destination point longtitude
     * @return intent that launches a navigation app such as Google Maps
     */
    public static Intent getNavigationIntent(double lat, double lon) {
        Uri gmmIntentUri = Uri.parse(String.format("google.navigation:q=%s,%s", lat, lon));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        return mapIntent;
    }

    /**
     * Build an intent to share image and text
     *
     * @param image Bitmap image to share
     * @param text  String object or null if not need.
     * @return intent to create chooser;
     */
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static Intent buildShareImageAndTextIntent(@NonNull Context context, @NonNull Bitmap image, @Nullable String text) throws IllegalStateException {
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                image, String.format("ShareImage_%s", UUID.randomUUID().toString()), null);

        if (TextUtils.isEmpty(path))
            throw new IllegalStateException("Unable to insert image!");

        Uri imageUri = Uri.parse(path);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(text))
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return shareIntent;
    }


    /**
     * Build an intent to share list of images and text
     *
     * @param images List of Bitmaps to share
     * @param text   String object or null if not need.
     * @return intent to create chooser;
     */
    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public static Intent buildShareListOfImagesAndTextIntent(@NonNull Context context, @NonNull List<Bitmap> images, @Nullable String text) throws IllegalStateException {

        ArrayList<Uri> uriList = new ArrayList<>(images.size());

        for (Bitmap bitmap : images) {
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    bitmap, String.format("ShareImage_%s", UUID.randomUUID().toString()), null);

            if (TextUtils.isEmpty(path))
                throw new IllegalStateException("Unable to insert image!");

            Uri imageUri = Uri.parse(path);
            uriList.add(imageUri);
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        if (!TextUtils.isEmpty(text))
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return shareIntent;
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    public static Intent buildSelectContactIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        return intent;
    }
}
