package eu.livotov.labs.android.robotools.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 27.10.12
 * Time: 13:52
 * To change this template use File | Settings | File Templates.
 */
public class RTDialogs
{

    public static void showOptionsDialog(final Activity ctx,
                                         final String title,
                                         final String[] options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener r)
    {
        final AlertDialog.Builder ab = new AlertDialog.Builder(ctx);

        ab.setTitle(title);
        ab.setSingleChoiceItems(options, defaultOption, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                r.optionSelected(whichButton);
                dialog.dismiss();
            }
        });

        ab.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            public void onCancel(DialogInterface dialogInterface)
            {
                r.selectionCancelled();
            }
        });

        ab.show();
    }

    public static void showOptionsDialog(final Activity ctx,
                                         final String title,
                                         final Collection options,
                                         final int defaultOption,
                                         final RTOptionsDialogResultListener r)
    {
        final AlertDialog.Builder ab = new AlertDialog.Builder(ctx);

        String[] optionsNames = new String[options.size()];

        int i = 0;
        for (Object opt : options)
        {
            optionsNames[i] = opt.toString();
            i++;
        }

        ab.setTitle(title);
        ab.setSingleChoiceItems(optionsNames, defaultOption, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                r.optionSelected(whichButton);
                dialog.dismiss();
            }
        });

        ab.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            public void onCancel(DialogInterface dialogInterface)
            {
                r.selectionCancelled();
            }
        });

        ab.show();
    }

    public static void showMessageBox(final Activity ctx, int iconRes, int titleRes, int messageRes)
    {
        showMessageBox(ctx, iconRes, ctx.getString(titleRes), ctx.getString(messageRes));
    }

    public static void showMessageBox(final Activity ctx, int iconRes, final String title, final String msg)
    {
        showMessageBox(ctx, iconRes, title, msg, null);
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

    public static void showYesNoDialog(final Activity ctx, final int icon, final String title, final String message, final String yesBtnTitle, final String noBtnTitle, final RTYesNoDialogResultListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(icon);

        builder.setPositiveButton(yesBtnTitle, new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialogInterface, final int i)
            {
                if (listener != null)
                {
                    listener.onYes();
                }
            }
        });

        builder.setNegativeButton(noBtnTitle, new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialogInterface, final int i)
            {
                if (listener != null)
                {
                    listener.onNo();
                }
            }
        });

        builder.show();
    }

    public static RTInputDialogBuilder buildInputDialog(final Activity ctx)
    {
        return new RTInputDialogBuilder(ctx);
    }

    public static void showNotification(final Context ctx, final String message)
    {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    public static void showNotification(final Context ctx, int message)
    {
        showNotification(ctx, ctx.getString(message));
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
