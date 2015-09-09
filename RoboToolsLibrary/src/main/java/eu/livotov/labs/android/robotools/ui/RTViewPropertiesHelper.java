package eu.livotov.labs.android.robotools.ui;

import android.view.View;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTViewPropertiesHelper
{
    public static void setEnabled(final boolean enabled, final View... views)
    {
        for (View view : views)
        {
            view.setEnabled(enabled);
        }
    }

    public static void setVisibility(final int visibility, final View... views)
    {
        for (View view : views)
        {
            view.setVisibility(visibility);
        }
    }

}
