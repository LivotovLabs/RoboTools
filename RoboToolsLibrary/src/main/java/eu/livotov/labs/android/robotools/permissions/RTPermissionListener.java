package eu.livotov.labs.android.robotools.permissions;

/**
 * Author: Grishko Nikita
 * Date: 18.11.15.
 * Time: 12:08.
 */
public interface RTPermissionListener {

    /**
     * Method called whenever a requested permission has been granted.
     *
     * @param permission The permission that has been requested. One of the values found in {@link
     * android.Manifest.permission}
     */
    void onPermissionGranted(String permission, int requestCode);

    /**
     * Method called whenever a requested permission has been denied.
     *
     * @param permission The permission that has been requested. One of the values found in {@link
     * android.Manifest.permission}
     */
    void onPermissionDenied(String permission);

    /**
     * Method called whenever Android asks the application to inform the user of the need for the
     * requested permission. The request process won't continue until the token is properly used.
     *
     * @param permission The permission that has been requested. One of the values found in {@link
     * android.Manifest.permission}
     * @param token Token used to continue or cancel the permission request process. The permission
     * request process will remain blocked until one of the token methods is called.
     */
    void onPermissionRationaleShouldBeShown(String permission, RTPermissionToken token);
}
