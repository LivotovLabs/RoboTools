package eu.livotov.labs.android.robotools.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import eu.livotov.labs.android.robotools.app.injector.RTInjector;
import eu.livotov.labs.android.robotools.ui.RTListenersHelper;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTActivity extends Activity
{
    private RTInjector.ActivityInjector injector = new RTInjector.ActivityInjector(this);
    private AtomicBoolean displayed = new AtomicBoolean(false);

    public void showToast(@NonNull CharSequence text, final boolean longToast)
    {
        Toast.makeText(getApplicationContext(), text, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public void showToast(final int textId, final boolean longToast)
    {
        Toast.makeText(getApplicationContext(), textId, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        injector.onCreate();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        injector.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        injector.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        injector.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        injector.onDestroy();
    }


    @Override
    protected void onResume()
    {
        displayed.set(true);
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        displayed.set(false);
        super.onPause();
    }

    public void setOnClickListeners(final View.OnClickListener listener, final View... views)
    {
        RTListenersHelper.setOnClickListeners(listener, views);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setNavigationBarColor(int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(color);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setSystemStatusBarColor(int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
