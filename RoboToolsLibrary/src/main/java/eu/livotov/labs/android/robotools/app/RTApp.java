package eu.livotov.labs.android.robotools.app;

import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

import eu.livotov.labs.android.robotools.app.injector.RTInjector;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTApp extends Application
{
    private static RTApp instance;
    private RTInjector.ApplicationInjector injector = new RTInjector.ApplicationInjector(this);

    @Override
    public void onCreate()
    {
        super.onCreate();
        injector.onCreate();
        instance = this;
    }

    @Override
    public void onTerminate()
    {
        instance = null;
        super.onTerminate();
    }

    public static RTApp getInstance()
    {
        return instance;
    }

    public static void showToast(@NonNull CharSequence text, final boolean longToast)
    {
        if (instance!=null)
        {
            Toast.makeText(instance, text, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToast(final int textId, final boolean longToast)
    {
        if (instance!=null)
        {
            Toast.makeText(instance, textId, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }
}

