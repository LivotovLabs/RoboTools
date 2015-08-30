package eu.livotov.labs.android.robotools.app.design.bottomsheet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;

import java.util.List;

import eu.livotov.labs.android.robotools.R;

/**
 * A helper class,
 * <p/>
 * Project: BottomSheet
 * Created by LiaoKai(soarcn) on 2015/7/18.
 */
public class RTBottomSheetHelper
{


    /**
     * Create a BottomSheet Builder for creating share intent chooser.
     * You still need to call show() to display it like:
     * <p/>
     * Intent sharingIntent = new Intent(Intent.ACTION_SEND);
     * shareIntent.setType("text/plain");
     * shareIntent.putExtra(Intent.EXTRA_TEXT, "hello");
     * BottomSheetHelper.shareAction(activity,sharingIntent).show();
     *
     * @param activity Activity instance
     * @param intent   shareIntent
     * @return BottomSheet builder
     */
    public static RTBottomSheet.Builder shareAction(@NonNull final Activity activity, @NonNull final Intent intent)
    {
        RTBottomSheet.Builder builder = new RTBottomSheet.Builder(activity).grid();
        PackageManager pm = activity.getPackageManager();

        final List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);

        for (int i = 0; i < list.size(); i++)
        {
            builder.sheet(i, list.get(i).loadIcon(pm), list.get(i).loadLabel(pm));
        }

        builder.listener(new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(@NonNull DialogInterface dialog, int which)
            {
                ActivityInfo activityInfo = list.get(which).activityInfo;
                ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
                Intent newIntent = (Intent) intent.clone();
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                newIntent.setComponent(name);
                activity.startActivity(newIntent);
            }
        });
        builder.limit(R.integer.bs_initial_grid_row);
        return builder;
    }

}
