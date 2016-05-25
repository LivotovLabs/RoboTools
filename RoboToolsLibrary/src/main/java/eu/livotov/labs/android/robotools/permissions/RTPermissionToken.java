package eu.livotov.labs.android.robotools.permissions;

/**
 * Author: Grishko Nikita
 * Date: 18.11.15.
 * Time: 12:38.
 */
public interface RTPermissionToken {

    /**
     * Continues with the permission request process
     */
    void continuePermissionRequest();

    /**
     * Cancels the permission request process
     */
    void cancelPermissionRequest();
}
