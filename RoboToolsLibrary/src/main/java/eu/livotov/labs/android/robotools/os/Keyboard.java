package eu.livotov.labs.android.robotools.os;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;


public class Keyboard {

    private Keyboard() {
    }

    public static void hideKeyboard(final Activity ctx) {
        if(ctx != null) {
            InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (ctx.getCurrentFocus() != null) {
                inputManager.hideSoftInputFromWindow(ctx.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void hideKeyboard(final View view) {
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(final Activity ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (ctx.getCurrentFocus() != null) {
            inputManager.showSoftInput(ctx.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void showKeyboard(final View view) {
        InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void showKeyboard(final View view, long delay) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard(view);
            }
        }, delay);
    }

    public static void showSoftKeyboardFor(final Context ctx, final View view) {
        try {
            InputMethodManager mgr = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (view != null) {
                view.requestFocus();
                mgr.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            } else {
                mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        } catch (Throwable err) {
            Log.e(Keyboard.class.getName(), err.getMessage(), err);
        }
    }

    public static void hideSoftKeyboardFor(final Activity activity, final View view) {
        if (view != null) {
            try {
                InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (Throwable err) {
                Log.e(Keyboard.class.getName(), err.getMessage(), err);
            }
        } else {
            try {
                InputMethodManager mgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                mgr.hideSoftInputFromWindow(activity.findViewById(android.R.id.content).getWindowToken(), 0);
                mgr.hideSoftInputFromWindow(activity.findViewById(android.R.id.content).getApplicationWindowToken(), 0);
            } catch (Throwable err) {
                Log.e(Keyboard.class.getName(), err.getMessage(), err);
            }
        }
    }
}
