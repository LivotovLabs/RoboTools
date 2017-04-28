package eu.livotov.labs.android.robotools.permissions;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.BuildConfig;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: Grishko Nikita
 * Date: 18.11.15.
 * Time: 12:12.
 */
public final class RTPermissions {

    private static volatile RTPermissions sInstance;

    private String permission;
    private int requestCode = -1;
    private WeakReference<Activity> activity;
    private RTPermissionListener listener;
    private AtomicBoolean isRequestingPermission = new AtomicBoolean(false);

    public static RTPermissions getInstance() {
        if (sInstance == null) {
            synchronized (RTPermissions.class) {
                sInstance = new RTPermissions();
            }
        }
        return sInstance;
    }

    public void registerActivity(final Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public void unregisterActivity() {
        isRequestingPermission.set(false);
        if (this.activity != null) {
            activity.clear();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onPermissionRequestGranted(requestCode);
        } else {
            onPermissionRequestDenied();
        }
    }

    private void onPermissionRequestGranted(int code) {
        finishWithGrantedPermission(permission, code);
    }

    private void onPermissionRequestDenied() {
        finishWithDeniedPermission(permission);
    }

    private void finishWithGrantedPermission(String permission, int code) {
        if (listener != null) {
            listener.onPermissionGranted(permission, code);
        }
        isRequestingPermission.set(false);
    }

    private void finishWithDeniedPermission(String permission) {
        if (listener != null) {
            listener.onPermissionDenied(permission);
        }
        isRequestingPermission.set(false);
    }

    public void checkPermission(String permission, RTPermissionListener listener) {
        checkPermission(permission, listener, -1);
    }

    public void checkPermission(String permission, RTPermissionListener listener, int requestCode) {
        if (isRequestingPermission.getAndSet(true)) {
            if (BuildConfig.DEBUG)
                Log.e(RTPermissions.class.getCanonicalName(), "\"Only one permission request at a time. Currently handling permission: [\" + this.permission + \"]\")");
        }

        this.permission = permission;
        this.listener = listener;
        this.requestCode = requestCode;

        if (isContextAlive()) {
            int permissionState = ContextCompat.checkSelfPermission(activity.get(), permission);
            switch (permissionState) {
                case PackageManager.PERMISSION_DENIED:
                    handleDeniedPermission(permission);
                    break;
                case PackageManager.PERMISSION_GRANTED:
                default:
                    finishWithGrantedPermission(permission, requestCode);
                    break;
            }
        }
    }

    private boolean isContextAlive() {
        return activity.get() != null;
    }

    private void handleDeniedPermission(String permission) {
        if (isContextAlive() && ActivityCompat.shouldShowRequestPermissionRationale(activity.get(), permission)) {
            RTPermissionRationaleToken permissionToken = new RTPermissionRationaleToken(this, permission);
            listener.onPermissionRationaleShouldBeShown(permission, permissionToken);
        } else {
            requestPermission(permission);
        }
    }

    void requestPermission(String permission) {
        if (isContextAlive()) {
            int permissionCode = getPermissionCodeForPermission(permission);
            ActivityCompat.requestPermissions(activity.get(), new String[]{permission}, permissionCode);
        }
    }

    private int getPermissionCodeForPermission(String permission) {
        return requestCode > 0 ? requestCode : 100;
    }

    void onContinuePermissionRequest(String permission) {
        requestPermission(permission);
    }

    void onCancelPermissionRequest(String permission) {
        finishWithDeniedPermission(permission);
    }
}

