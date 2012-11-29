package eu.livotov.labs.android.robotools.ui;

import android.app.Activity;
import android.content.Context;
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
public class RTKeyboard {

    public static void showSoftKeyboardFor(Context ctx, View view)
    {
        InputMethodManager mgr = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void hideSoftKeyboardFor(Activity activity, View view)
    {
        InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (view != null)
        {
            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else
        {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }
}
