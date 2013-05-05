package eu.livotov.labs.android.robotools.ui;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 11/29/12
 * Time: 8:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RTKeyboard
{

    public static void showSoftKeyboardFor(final Context ctx, final View view)
    {
        view.postDelayed(new Runnable()
        {
            public void run()
            {
                try
                {
                    InputMethodManager mgr = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                } catch (Throwable err)
                {
                    Log.e(RTKeyboard.class.getName(), err.getMessage(), err);
                }
            }
        }, 200);
    }

    public static void hideSoftKeyboardFor(final Activity activity, final View view)
    {
        if (view != null)
        {
            view.postDelayed(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    } catch (Throwable err)
                    {
                        Log.e(RTKeyboard.class.getName(), err.getMessage(), err);
                    }
                }
            }, 200);
        } else
        {
            activity.runOnUiThread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    } catch (Throwable err)
                    {
                        Log.e(RTKeyboard.class.getName(), err.getMessage(), err);
                    }
                }
            });
        }
    }
}
