package eu.livotov.labs.android.robotools.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 27.10.12
 * Time: 13:52
 * To change this template use File | Settings | File Templates.
 */
public class RTDialogs {

    public static void showOptionsDialog(final Context ctx,
                                         final String title,
                                         final String[] options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener r) {
        final AlertDialog.Builder ab = new AlertDialog.Builder(ctx);

        ab.setTitle(title);
        ab.setSingleChoiceItems(options, defaultOption, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                r.optionSelected(whichButton);
                dialog.dismiss();
            }
        });

        ab.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                r.selectionCancelled();
            }
        });

        ab.show();
    }

    public static void showOptionsDialog(final Context ctx,
                                         final String title,
                                         final Collection options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener r) {
        final AlertDialog.Builder ab = new AlertDialog.Builder(ctx);

        String[] optionsNames = new String[options.size()];

        int i = 0;
        for (Object opt : options) {
            optionsNames[i] = opt.toString();
            i++;
        }

        ab.setTitle(title);
        ab.setSingleChoiceItems(optionsNames, defaultOption, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                r.optionSelected(whichButton);
                dialog.dismiss();
            }
        });

        ab.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialogInterface) {
                r.selectionCancelled();
            }
        });

        ab.show();
    }

    public static void showMessageBox(final Context ctx, int iconRes, int titleRes, int messageRes) {
        showMessageBox(ctx, iconRes, ctx.getString(titleRes), ctx.getString(messageRes));
    }

    public static void showMessageBox(final Context ctx, int iconRes, final String title, final String msg) {
        showMessageBox(ctx, iconRes, title, msg, null);
    }

    public static void showMessageBox(final Context ctx,
                                      final int iconRes,
                                      final String title,
                                      final String msg,
                                      final RTModalDialogResultListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setIcon(iconRes);
        builder.setMessage(msg);

        if (listener != null) {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onDialogClosed();
                    dialogInterface.dismiss();
                }
            });
        } else {
            builder.setPositiveButton("OK", null);
        }

        builder.show();
    }

    public static void showYesNoDialog(final Context ctx, final int icon, final String title, final String message, final String yesBtnTitle, final String noBtnTitle, final RTYesNoDialogResultListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(icon);

        builder.setPositiveButton(yesBtnTitle, new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialogInterface, final int i)
            {
                if (listener!=null)
                {
                    listener.onYes();
                }
            }
        });

        builder.setNegativeButton(noBtnTitle, new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialogInterface, final int i)
            {
                if (listener!=null)
                {
                    listener.onNo();
                }
            }
        });

        builder.show();
    }

    public static void showNotification(final Context ctx, final String message) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static void showNotification(final Context ctx, int message) {
        showNotification(ctx, ctx.getString(message));
    }

    public interface RTModalDialogResultListener {

        void onDialogClosed();
    }

    public interface RTYesNoDialogResultListener {
        void onYes();
        void onNo();
    }

    public interface RTOptionsDialogResultListener {

        void optionSelected(int optionIndex);

        void selectionCancelled();
    }
}
