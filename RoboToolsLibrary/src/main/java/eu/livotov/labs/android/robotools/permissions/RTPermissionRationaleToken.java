package eu.livotov.labs.android.robotools.permissions;

/**
 * Author: Grishko Nikita
 * Date: 18.11.15.
 * Time: 12:39.
 */
public class RTPermissionRationaleToken implements RTPermissionToken {

    private final RTPermissions permissions;
    private final String permission;
    private boolean isTokenResolved = false;

    public RTPermissionRationaleToken(RTPermissions instance, String permission) {
        this.permissions = instance;
        this.permission = permission;
    }

    @Override public void continuePermissionRequest() {
        if (!isTokenResolved) {
            permissions.onContinuePermissionRequest(permission);
            isTokenResolved = true;
        }
    }

    @Override public void cancelPermissionRequest() {
        if (!isTokenResolved) {
            permissions.onCancelPermissionRequest(permission);
            isTokenResolved = true;
        }
    }
}
