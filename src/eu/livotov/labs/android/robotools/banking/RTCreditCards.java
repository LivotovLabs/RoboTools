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
        Diners,
        Maestro
    }

    /**
     * Valid a Credit Card number
     */
    public static boolean isValidCreditCardNumber(String number)
    {
        return getCreditCardType(number) != CreditCardType.UnknownOrInvalid && verifyCreditCardNumber(number);
    }

    public static String formatCreaditCardNumberForDisplay(String number)
    {
        final String num = number.replaceAll(" ", "");
        StringBuffer buf = new StringBuffer();

        //todo: add spaces according to credit card type
        for (int i = 0; i < num.length(); i++)
        {
            buf.append(num.charAt(i));
            if (i == 3 || i == 7 || i == 11)
            {
                buf.append(" ");
            }
        }

        return buf.toString();
    }

    public static CreditCardType getCreditCardType(String number)
    {

        if (TextUtils.isEmpty(number))
        {
            return CreditCardType.UnknownOrInvalid;
        }

        final String digit1 = number.substring(0, 1);
        final String digit2 = number.length() > 1 ? number.substring(0, 2) : "";
        final String digit3 = number.length() > 2 ? number.substring(0, 3) : "";
        final String digit4 = number.length() > 3 ? number.substring(0, 4) : "";

        if (TextUtils.isDigitsOnly(number.replaceAll(" ", "")))
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
            } else if (digit4.equals("5018") || digit4.equals("5020") || digit4.equals("5038") || digit4.equals("5893")
                    || digit4.equals("6304") || digit4.equals("6759") || digit4.equals("6761") || digit4.equals("6762")
                    || digit4.equals("6763") || digit4.equals("0604"))
            {
                if (number.length() >= 12 && number.length() <= 19)
                {
                    return CreditCardType.Maestro;
                }
            }
        }

        return CreditCardType.UnknownOrInvalid;
    }

    public static CreditCardType guesstCreditCardType(String number)
    {

        if (TextUtils.isEmpty(number))
        {
            return CreditCardType.UnknownOrInvalid;
        }

        final String digit1 = number.substring(0, 1);
        final String digit2 = number.length() > 1 ? number.substring(0, 2) : "";
        final String digit3 = number.length() > 2 ? number.substring(0, 3) : "";
        final String digit4 = number.length() > 3 ? number.substring(0, 4) : "";

        if (TextUtils.isDigitsOnly(number.replaceAll(" ", "")))
        {
            if (digit1.equals("4"))
            {
                return CreditCardType.Visa;
            } else if (digit2.compareTo("51") >= 0 && digit2.compareTo("55") <= 0)
            {
                return CreditCardType.MasterCard;
            } else if (digit2.equals("34") || digit2.equals("37"))
            {
                return CreditCardType.AmericanExpress;
            } else if (digit4.equals("2014") || digit4.equals("2149"))
            {
                return CreditCardType.EnRoute;
            } else if (digit2.equals("36") || digit2.equals("38") || (digit3.compareTo("300") >= 0 && digit3.compareTo("305") <= 0))
            {
                return CreditCardType.Diners;
            } else if (digit4.equals("5018") || digit4.equals("5020") || digit4.equals("5038") || digit4.equals("5893")
                    || digit4.equals("6304") || digit4.equals("6759") || digit4.equals("6761") || digit4.equals("6762")
                    || digit4.equals("6763") || digit4.equals("0604"))
            {
                return CreditCardType.Maestro;
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
