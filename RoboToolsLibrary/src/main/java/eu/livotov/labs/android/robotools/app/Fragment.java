package eu.livotov.labs.android.robotools.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;
import eu.livotov.labs.android.robotools.R;
import eu.livotov.labs.android.robotools.content.RequestQueue;
import eu.livotov.labs.android.robotools.injector.Injector;
import eu.livotov.labs.android.robotools.os.Keyboard;

import java.io.File;

/**
 * Расширенный класс фрагмента.
 * Содержит в себе множество вкусных возможностей.
 *
 * Автохандлинг ошибок запуска Activity,
 * Легкий показ тостов,
 * Возможность мгновенно заменить себя на другой фрагмент,
 * исправленая система ресурсов,
 * автоматическое скрытие клавиатуры при завершении,
 * лоадер сетевых запросов,
 * куча делегатов к Activity.
 */
public class Fragment extends android.support.v4.app.Fragment {

    private FragmentActivity mActivity;
    private RequestQueue mLoader;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    /**
     * Заменяет текущий фрагмент на указаный с анимацией открытия и
     * добавлянием в BackStack
     */
    public void startFragment(Fragment fragment) {
        if(fragment != null) {
            getFragmentManager().beginTransaction().replace(getId(), fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).
                    addToBackStack(fragment.toString()).commit();
        }
    }

    public ActionBar getActionBar() {
        return mActivity.getActionBar();
    }

    public void invalidateOptionsMenu() {
        mActivity.invalidateOptionsMenu();
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        return mActivity.startActionMode(callback);
    }

    public RequestQueue getRestLoader() {
        if(mLoader == null) {
            mLoader = RequestQueue.with(mActivity);
        }
        return mLoader;
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), R.string.application_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * (non-Javadoc)
     * Standard class loader method to load a class and resolve it.
     *
     * @see android.app.Activity#getLayoutInflater()
     */
    public LayoutInflater getLayoutInflater() {
        return getActivity().getLayoutInflater();
    }

    /**
     * (non-Javadoc)
     * Standard class loader method to load a class and resolve it.
     *
     * @see android.app.Activity#getApplicationContext()
     */
    public Context getContext() {
        return getActivity();
    }

    /**
     * Завершает родительскую Activity
     */
    public void finishActivity() {
        getActivity().finish();
    }

    /**
     * Завершает
     */
    public void finish() {
        getFragmentManager().popBackStack();
    }

    public void clearBackStack() {
        for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); ++i) {
            getFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Keyboard.hideKeyboard(mActivity);
        Injector.recycle(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return Injector.init(this, container);
    }

    public boolean isNestedFragment() {
        return getParentFragment() != null;
    }

    public void showToast(CharSequence text) {
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int textId) {
        Toast.makeText(mActivity, textId, Toast.LENGTH_SHORT).show();
    }

    public boolean isActive() {
        return isAdded() && isVisible();
    }

    public MenuInflater getMenuInflater() {
        return mActivity.getMenuInflater();
    }

    public void setProgressBarVisibility(boolean visible) {
        mActivity.setProgressBarVisibility(visible);
    }

    public void setProgressBarIndeterminateVisibility(boolean visible) {
        mActivity.setProgressBarIndeterminateVisibility(visible);
    }

    public void setProgressBarIndeterminate(boolean indeterminate) {
        mActivity.setProgressBarIndeterminate(indeterminate);
    }

    public void setProgress(int progress) {
        mActivity.setProgress(progress);
    }

    public Application getApplication() {
        return mActivity.getApplication();
    }

    public WindowManager getWindowManager() {
        return mActivity.getWindowManager();
    }

    public void setTitle(CharSequence title) {
        getActionBar().setTitle(title);
    }

    public void setTitle(int titleId) {
        getActionBar().setTitle(titleId);
    }

    public CharSequence getTitle() {
        return getActionBar().getTitle();
    }

    public void runOnUiThread(Runnable action) {
        mActivity.runOnUiThread(action);
    }

    public AssetManager getAssets() {
        return mActivity.getAssets();
    }

    public PackageManager getPackageManager() {
        return mActivity.getPackageManager();
    }

    public ContentResolver getContentResolver() {
        return mActivity.getContentResolver();
    }

    public Looper getMainLooper() {
        return mActivity.getMainLooper();
    }

    public Context getApplicationContext() {
        return mActivity.getApplicationContext();
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        return mActivity.getSharedPreferences(name, mode);
    }

    public File getExternalCacheDir() {
        return mActivity.getExternalCacheDir();
    }

    public void sendBroadcast(Intent intent) {
        mActivity.sendBroadcast(intent);
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        mActivity.sendBroadcast(intent, receiverPermission);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        mActivity.sendOrderedBroadcast(intent, receiverPermission);
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        mActivity.sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void sendStickyBroadcast(Intent intent) {
        mActivity.sendStickyBroadcast(intent);
    }

    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        mActivity.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    public void removeStickyBroadcast(Intent intent) {
        mActivity.removeStickyBroadcast(intent);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return mActivity.registerReceiver(receiver, filter);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return mActivity.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        mActivity.unregisterReceiver(receiver);
    }

    public ComponentName startService(Intent service) {
        return mActivity.startService(service);
    }

    public boolean stopService(Intent name) {
        return mActivity.stopService(name);
    }

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return mActivity.bindService(service, conn, flags);
    }

    public void unbindService(ServiceConnection conn) {
        mActivity.unbindService(conn);
    }
}
