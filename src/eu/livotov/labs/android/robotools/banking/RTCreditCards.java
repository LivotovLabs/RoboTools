package eu.livotov.labs.android.robotools.banking;

import android.text.TextUtils;

/**
 * Created with IntelliJ IDEA.
 * User: dlivotov
 * Date: 31.10.12
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class RTCreditCards
{

    public enum CreditCardType
    {
        UnknownOrInvalid,
        Visa,
        MasterCard,
        AmericanExpress,
        EnRoute,
        Diners
    }

    /**
     * Valid a Credit Card number
     */
    public static boolean isValidCreditCardNumber(String number)
    {
        return getCreditCardType(number) != CreditCardType.UnknownOrInvalid && verifyCreditCardNumber(number);
    }

    public static CreditCardType getCreditCardType(String number)
    {
        final String digit1 = number.substring(0, 1);
        final String digit2 = number.substring(0, 2);
        final String digit3 = number.substring(0, 3);
        final String digit4 = number.substring(0, 4);

        if (TextUtils.isDigitsOnly(number.replaceAll(" ","")))
        {
            if (digit1.equals("4"))
            {
                if (number.length() == 13 || number.length() == 16)
                {
                    return CreditCardType.Visa;
                }
            } else if (digit2.compareTo("51") >= 0 && digit2.compareTo("55") <= 0)
            {
                if (number.length() == 16)
                {
                    return CreditCardType.MasterCard;
                }
            } else if (digit2.equals("34") || digit2.equals("37"))
            {
                if (number.length() == 15)
                {
                    return CreditCardType.AmericanExpress;
                }
            } else if (digit4.equals("2014") || digit4.equals("2149"))
            {
                if (number.length() == 15)
                {
                    return CreditCardType.EnRoute;
                }
            } else if (digit2.equals("36") || digit2.equals("38") || (digit3.compareTo("300") >= 0 && digit3.compareTo("305") <= 0))
            {
                if (number.length() == 14)
                {
                    return CreditCardType.Diners;
                }
            }
        }

        return CreditCardType.UnknownOrInvalid;
    }

    public static boolean verifyCreditCardNumber(String n)
    {
        if (TextUtils.isEmpty(n))
        {
            return false;
        }

        try
        {
            int j = n.length();

            String[] s1 = new String[j];
            for (int i = 0; i < n.length(); i++)
            {
                s1[i] = "" + n.charAt(i);
            }

            int checksum = 0;

            for (int i = s1.length - 1; i >= 0; i -= 2)
            {
                int k = 0;

                if (i > 0)
                {
                    k = Integer.valueOf(s1[i - 1]).intValue() * 2;
                    if (k > 9)
                    {
                        String s = "" + k;
                        k = Integer.valueOf(s.substring(0, 1)).intValue() +
                                    Integer.valueOf(s.substring(1)).intValue();
                    }
                    checksum += Integer.valueOf(s1[i]).intValue() + k;
                } else
                {
                    checksum += Integer.valueOf(s1[0]).intValue();
                }
            }
            return ((checksum % 10) == 0);
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

}
