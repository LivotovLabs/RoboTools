package eu.livotov.labs.android.robotools.app.design;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTMaterialDesignHelper
{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void colorizeToolbar(Toolbar toolbar, final int toolbarBackgroundColor, int toolbarActionsColor)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            toolbar.setBackgroundColor(toolbarBackgroundColor);
            final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(toolbarActionsColor, PorterDuff.Mode.MULTIPLY);

            for (int i = 0; i < toolbar.getChildCount(); i++)
            {
                final View v = toolbar.getChildAt(i);

                //Step 1 : Changing the color of back button (or open drawer button).
                if (v instanceof ImageButton)
                {
                    //Action Bar back button
                    ((ImageButton) v).getDrawable().setColorFilter(colorFilter);
                }

                if (v instanceof ActionMenuView)
                {
                    for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++)
                    {

                        //Step 2: Changing the color of any ActionMenuViews - icons that
                        //are not back button, nor text, nor overflow menu icon.
                        final View innerView = ((ActionMenuView) v).getChildAt(j);

                        if (innerView instanceof ActionMenuItemView)
                        {
                            int drawablesCount = ((ActionMenuItemView) innerView).getCompoundDrawables().length;
                            for (int k = 0; k < drawablesCount; k++)
                            {
                                if (((ActionMenuItemView) innerView).getCompoundDrawables()[k] != null)
                                {
                                    final int finalK = k;

                                    //Important to set the color filter in seperate thread,
                                    //by adding it to the message queue
                                    //Won't work otherwise.
                                    innerView.post(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }

                //Step 3: Changing the color of title and subtitle.
                toolbar.setTitleTextColor(toolbarActionsColor);
                toolbar.setSubtitleTextColor(toolbarActionsColor);

                //Step 4: Changing the color of the Overflow Menu icon.
                //setOverflowButtonColor(activity, colorFilter);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarColor(final Activity activity, final int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(color);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setSystemStatusBarColor(final Activity activity, final int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            final Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
