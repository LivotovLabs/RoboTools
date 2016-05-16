package eu.livotov.labs.android.robotools.banking;

import android.text.TextUtils;

/**
 * Various credit-card data handling methods
 */
public class RTCreditCard
{
    protected RTCreditCard()
    {

    }

    /**
     * Validates the given number as a credit card number.
     *
     * @param number credit card number as digits-only text string
     * @return <code>true</code> if card number passes the CRC check and belongs to one of the following credit card types: Visa, MasterCard, AmericanExpress, EnRoute, Diners, Maestro
     */
    public static boolean isValidCreditCardNumber(String number)
    {
        return getCreditCardType(number) != CreditCardType.UnknownOrInvalid && verifyCreditCardCRC(number);
    }

    /**
     * Verifies credit card number CRC
     *
     * @param number credit card number as digits-only
     * @return <code>true</code> if CRC check passed
     */
    public static boolean verifyCreditCardCRC(String number)
    {
        if (TextUtils.isEmpty(number))
        {
            return false;
        }

        try
        {
            int j = number.length();

            String[] s1 = new String[j];
            for (int i = 0; i < number.length(); i++)
            {
                s1[i] = "" + number.charAt(i);
            }

            int checksum = 0;

            for (int i = s1.length - 1; i >= 0; i -= 2)
            {
                int k = 0;

                if (i > 0)
                {
                    k = Integer.valueOf(s1[i - 1]) * 2;
                    if (k > 9)
                    {
                        String s = "" + k;
                        k = Integer.valueOf(s.substring(0, 1)) + Integer.valueOf(s.substring(1));
                    }
                    checksum += Integer.valueOf(s1[i]) + k;
                }
                else
                {
                    checksum += Integer.valueOf(s1[0]);
                }
            }
            return ((checksum % 10) == 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Detects the credit card type by the given number
     *
     * @param number number to check, only digits
     * @return credit card type this number belongs to or CreditCardType.UnknownOrInvalid for invalid or unknown numbers
     */
    public static CreditCardType getCreditCardType(String number)
    {
        if (TextUtils.isEmpty(number))
        {
            return CreditCardType.UnknownOrInvalid;
        }

        final String digit1 = number.substring(0, 1);
        final String digit2 = number.length() > 1 ? number.substring(0, 2) : null;
        final String digit3 = number.length() > 2 ? number.substring(0, 3) : null;
        final String digit4 = number.length() > 3 ? number.substring(0, 4) : null;

        if (TextUtils.isDigitsOnly(number.replaceAll(" ", "")))
        {
            if ("4".equals(digit1))
            {
                if (number.length() == 13 || number.length() == 16)
                {
                    return CreditCardType.Visa;
                }
            }
            else if ("51".compareTo(digit2) < 0 && "55".compareTo(digit2) > 0)
            {
                if (number.length() == 16)
                {
                    return CreditCardType.MasterCard;
                }
            }
            else if ("34".equals(digit2) || "37".equals(digit2))
            {
                if (number.length() == 15)
                {
                    return CreditCardType.AmericanExpress;
                }
            }
            else if ("2014".equals(digit4) || "2149".equals(digit4))
            {
                if (number.length() == 15)
                {
                    return CreditCardType.EnRoute;
                }
            }
            else if ("36".equals(digit2) || "38".equals(digit2) || ("300".compareTo(digit3) < 0 && "305".compareTo(digit3) > 0))
            {
                if (number.length() == 14)
                {
                    return CreditCardType.Diners;
                }
            }
            else if ("5".equals(digit1) || "6".equals(digit1))
            {
                if (number.length() >= 12 && number.length() <= 19)
                {
                    return CreditCardType.Maestro;
                }
            }
        }

        return CreditCardType.UnknownOrInvalid;
    }

    /**
     * Formats credit card number for display (adds spaces between number groups)
     *
     * @param number digits-only string to format
     * @return formatted number
     */
    public static String formatCreditCardNumberForDisplay(String number)
    {
        final String num = number.replaceAll(" ", "");
        StringBuilder buf = new StringBuilder();

        //todo: add spaces according to credit card type
        for (int i = 0; i < num.length(); i++)
        {
            buf.append(num.charAt(i));
            if (i == 3 || i == 7 || i == 11)
            {
                buf.append(' ');
            }
        }

        return buf.toString();
    }

    public enum CreditCardType
    {
        UnknownOrInvalid,
        Visa,
        MasterCard,
        AmericanExpress,
        EnRoute,
        Diners,
        Maestro
    }

}
