package eu.livotov.labs.android.robotools.ui.lists;

import android.view.View;

import java.io.Serializable;

/**
 * (c) Livotov Labs Ltd. 2012
 * Date: 03/03/2013
 */
public abstract class RTListHolder<T extends Object> implements Serializable
{

    public RTListHolder()
    {
    }

    public void attachToView(View view)
    {
        inflateControlsFromView(view);
        view.setTag(this);
    }

    public abstract void inflateControlsFromView(final View view);

    public abstract void set(T s);
}
