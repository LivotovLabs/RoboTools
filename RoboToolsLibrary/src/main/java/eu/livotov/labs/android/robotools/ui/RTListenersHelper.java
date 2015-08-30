package eu.livotov.labs.android.robotools.ui;

import android.view.View;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTListenersHelper
{
    public static void setOnClickListeners(final View.OnClickListener listener, final View... views)
    {
        for (View view : views)
        {
            view.setOnClickListener(listener);
        }
    }

    public static void setOnClickListeners(final View.OnClickListener listener, final View viewGroup, final int... viewIdentifiers)
    {
        for (int view : viewIdentifiers)
        {
            View vv = viewGroup.findViewById(view);

            if (vv != null)
            {
                vv.setOnClickListener(listener);
            }
        }
    }
}
