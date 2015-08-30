package eu.livotov.labs.android.robotools.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import eu.livotov.labs.android.robotools.app.injector.RTInjector;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTFragment extends Fragment
{
    private RTInjector.FragmentInjector injector = new RTInjector.FragmentInjector(this);
    private Activity hostActivity;

    @Override
    public void onAttach(Context ctx)
    {
        super.onAttach(ctx);
        hostActivity = getActivity();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        hostActivity = activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        hostActivity = null;
    }

    /**
     * Nicely replaces current fragment with the new one, using open animation and backstack support.
     */
    public void startFragment(Fragment fragment)
    {
        if (fragment != null)
        {
            getFragmentManager().beginTransaction().replace(getId(), fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).addToBackStack(fragment.toString()).commit();
        }
    }

    /**
     * Invalidates menu of the host activity
     */
    public void invalidateOptionsMenu()
    {
        if (hostActivity != null)
        {
            hostActivity.invalidateOptionsMenu();
        }
    }

    /**
     * Finishes host activity
     */
    public void finishActivity()
    {
        if (hostActivity != null)
        {
            hostActivity.finish();
        }
    }

    /**
     * Pops out this fragment from the backstack, e.g. finishes it
     */
    public void finish()
    {
        getFragmentManager().popBackStack();
    }

    /**
     * Cleans up entire backstack
     */
    public void clearBackStack()
    {
        for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); ++i)
        {
            getFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        injector.onCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return injector.onCreateView(inflater, container);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        injector.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        injector.onStop();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        injector.onDestroyView();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        injector.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        injector.onCreateOptionsMenu(menu, inflater);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyOptionsMenu()
    {
        injector.onDestroyOptionsMenu();
        super.onDestroyOptionsMenu();
    }

    @TargetApi(17)
    public boolean isNestedFragment()
    {
        if (Build.VERSION.SDK_INT >= 17)
        {
            return getParentFragment() != null;
        }
        else
        {
            return false;
        }
    }

    public void showToast(@NonNull final CharSequence text, final boolean longToast)
    {
        Toast.makeText(hostActivity, text, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public void showToast(int textId, final boolean longToast)
    {
        Toast.makeText(hostActivity, textId, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    public boolean isActive()
    {
        return isAdded() && isVisible();
    }

}
