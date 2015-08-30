package eu.livotov.labs.android.robotools.app.design.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import eu.livotov.labs.android.robotools.R;
import eu.livotov.labs.android.robotools.app.design.bottomsheet.RTBottomSheet;
import eu.livotov.labs.android.robotools.os.RTKeyboard;

/**
 * Created by dlivotov on 30/08/2015.
 */
public class RTDialog
{
    public static void showOptionsDialog(final Activity ctx,
                                         final String title,
                                         final String[] options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener listener)
    {
        showOptionsDialog(ctx, 0, title, Arrays.asList(options), defaultOption, listener);
    }

    public static void showOptionsDialog(final Activity ctx,
                                         final int iconRes,
                                         final String title,
                                         final String[] options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener listener)
    {
        showOptionsDialog(ctx, iconRes, title, Arrays.asList(options), defaultOption, listener);
    }

    public static void showOptionsDialog(final Activity ctx,
                                         final int titleRes,
                                         final int[] optionsRes,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener listener)
    {
        showOptionsDialog(ctx,0,titleRes,optionsRes, defaultOption, listener);
    }

    public static void showOptionsDialog(final Activity ctx,
                                         final int iconRes,
                                         final int titleRes,
                                         final int[] optionsRes,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener listener)
    {
        List<String> optionsStrings = new ArrayList<>();

        for (int optionRes : optionsRes)
        {
            optionsStrings.add(ctx.getString(optionRes));
        }

        showOptionsDialog(ctx, iconRes, ctx.getString(titleRes), optionsStrings, defaultOption, listener);
    }

    public static void showOptionsDialog(final Activity ctx,
                                         final String title,
                                         final Collection options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener r)
    {
        showOptionsDialog(ctx, 0, title, options, defaultOption, r);
    }

    public static void showOptionsDialog(final Activity ctx,
                                         final int iconRes,
                                         final String title,
                                         final Collection options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener r)
    {
        RTBottomSheet.Builder builder = new RTBottomSheet.Builder(ctx);
        int index = 0;

        for (Object option : options)
        {
            builder.sheet(index, option.toString());
        }

        builder.title(title);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                if (r != null)
                {
                    r.selectionCancelled();
                }
            }
        });

        builder.listener(new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (r != null)
                {
                    r.optionSelected(which);
                }
            }
        });

        builder.build().show();
    }

    public static void showMessageBox(final Activity ctx, int iconRes, int titleRes, int messageRes, RTModalDialogResultListener listener)
    {
        showMessageBox(ctx, iconRes, ctx.getString(titleRes), ctx.getString(messageRes), listener);
    }

    public static void showMessageBox(final Activity ctx,
                                      final int iconRes,
                                      final String title,
                                      final String msg,
                                      final RTModalDialogResultListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setIcon(iconRes);
        builder.setMessage(msg);

        if (listener != null)
        {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    listener.onDialogClosed();
                    dialogInterface.dismiss();
                }
            });
        } else
        {
            builder.setPositiveButton("OK", null);
        }

        builder.show();
    }

    public static void showYesNoDialog(final Activity ctx, final int icon,
                                       final int messageRes,
                                       final RTYesNoDialogResultListener listener)
    {
        showYesNoDialog(ctx, icon, ctx.getString(messageRes), listener);
    }

    @Deprecated
    public static void showYesNoDialog(final Activity ctx, final int icon,
                                       final int titleRes,
                                       final int messageRes,
                                       final RTYesNoDialogResultListener listener)
    {
        showYesNoDialog(ctx, icon, ctx.getString(messageRes), listener);
    }

    @Deprecated
    public static void showYesNoDialog(final Activity ctx, final int icon,
                                       final String title,
                                       final String message,
                                       final RTYesNoDialogResultListener listener)
    {
        showYesNoDialog(ctx, icon, message, listener);
    }

    public static void showYesNoDialog(final Activity ctx, final int icon,
                                       final String message,
                                       final RTYesNoDialogResultListener listener)
    {
        RTBottomSheet.Builder builder = new RTBottomSheet.Builder(ctx);
        builder.icon(icon).title(message);
        builder.sheet(1, ctx.getString(R.string.robotools_dialogbtn_yes));
        builder.sheet(2, ctx.getString(R.string.robotools_dialogbtn_no));

        builder.setOnDismissListener(new DialogInterface.OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface dialog)
            {
                if (listener != null)
                {
                    listener.onNo();
                }
            }
        });

        builder.listener(new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (listener!=null)
                {
                    switch (which)
                    {
                        case 1:
                            listener.onYes();
                            break;

                        case 2:
                            listener.onNo();
                            break;

                        default:
                            listener.onNo();
                            break;
                    }
                }
            }
        });

        builder.build().show();
    }

    public static RTInputDialogBuilder buildInputDialog(final Activity ctx)
    {
        return new RTInputDialogBuilder(ctx);
    }

    public static void showNotification(final Context ctx, final String message)
    {
        showNotification(ctx, message, Toast.LENGTH_SHORT);
    }

    public static void showNotification(final Context ctx, int message)
    {
        showNotification(ctx, ctx.getString(message));
    }

    public static void showNotification(final Context ctx, final String message, int length)
    {
        Toast.makeText(ctx, message, length).show();
    }

    public static void showNotification(final Context ctx, int message, int length)
    {
        showNotification(ctx, ctx.getString(message), length);
    }

    public interface RTModalDialogResultListener
    {

        void onDialogClosed();
    }

    public interface RTYesNoDialogResultListener
    {

        void onYes();

        void onNo();
    }

    public interface RTInputDialogResultListener
    {

        void onInputConfirmed(String value);

        void onInputCancelled();
    }

    public interface RTOptionsDialogResultListener
    {

        void optionSelected(int optionIndex);

        void selectionCancelled();
    }

    public static class RTInputDialogBuilder
    {

        private Context ctx;
        private String title, message, hint, defaultValue;
        private Drawable icon;
        private String positiveButton = "OK";
        private String negativeButton;
        private boolean cancellable = true;

        public RTInputDialogBuilder(final Context ctx)
        {
            this.ctx = ctx;
        }

        public RTInputDialogBuilder title(final String title)
        {
            this.title = title;
            return this;
        }

        public RTInputDialogBuilder title(final int titleRes)
        {
            this.title = ctx.getString(titleRes);
            return this;
        }

        public RTInputDialogBuilder message(final String message)
        {
            this.message = message;
            return this;
        }

        public RTInputDialogBuilder message(final int messageRes)
        {
            this.message = ctx.getString(messageRes);
            return this;
        }

        public RTInputDialogBuilder hint(final String hint)
        {
            this.hint = hint;
            return this;
        }

        public RTInputDialogBuilder hint(final int hintRes)
        {
            this.hint = ctx.getString(hintRes);
            return this;
        }

        public RTInputDialogBuilder value(final String defaultValue)
        {
            this.defaultValue = defaultValue;
            return this;
        }

        public RTInputDialogBuilder value(final int defaultValueRes)
        {
            this.defaultValue = ctx.getString(defaultValueRes);
            return this;
        }

        public RTInputDialogBuilder positiveButton(final String txt)
        {
            this.positiveButton = txt;
            return this;
        }

        public RTInputDialogBuilder positiveButton(final int txtRes)
        {
            this.positiveButton = ctx.getString(txtRes);
            return this;
        }

        public RTInputDialogBuilder negativeButton(final String txt)
        {
            this.negativeButton = txt;
            return this;
        }

        public RTInputDialogBuilder negativeButton(final int txtRes)
        {
            this.negativeButton = ctx.getString(txtRes);
            return this;
        }

        public RTInputDialogBuilder icon(final int iconRes)
        {
            icon = ctx.getResources().getDrawable(iconRes);
            return this;
        }

        public RTInputDialogBuilder nega(final Drawable icon)
        {
            this.icon = icon;
            return this;
        }

        public RTInputDialogBuilder cancellable(boolean c)
        {
            this.cancellable = c;
            return this;
        }

        public void build(final RTInputDialogResultListener listener)
        {
            final AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

            if (!TextUtils.isEmpty(title))
            {
                alert.setTitle(title);
            }

            if (!TextUtils.isEmpty(message))
            {
                alert.setMessage(message);
            }

            final EditText input = new EditText(ctx);
            input.setSingleLine(true);

            if (!TextUtils.isEmpty(defaultValue))
            {
                input.setText(defaultValue);
                input.setSelection(0, input.getText().length());
            }

            alert.setView(input);
            alert.setCancelable(true);

            alert.setPositiveButton(positiveButton, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int whichButton)
                {
                    if (listener != null)
                    {
                        listener.onInputConfirmed(input.getText().toString());
                    }
                    dialog.dismiss();
                }
            });

            if (!TextUtils.isEmpty(negativeButton))
            {
                alert.setNegativeButton(negativeButton, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        if (listener != null)
                        {
                            listener.onInputCancelled();
                        }
                        dialog.dismiss();
                    }
                });
            }

            alert.setOnCancelListener(new DialogInterface.OnCancelListener()
            {
                public void onCancel(DialogInterface dialogInterface)
                {
                    if (listener != null)
                    {
                        listener.onInputCancelled();
                    }
                    dialogInterface.dismiss();
                }
            });

            alert.show();
            RTKeyboard.showSoftKeyboardFor(ctx, input);
        }
    }
}
