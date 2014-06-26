package eu.livotov.labs.android.robotools.security;

import android.text.TextUtils;

@SuppressWarnings("unused")
public class CreditCards {

    private CreditCards() {}

    public enum CreditCardType {
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
    public static boolean isValidCreditCardNumber(String number) {
        return getCreditCardType(number) != CreditCardType.UnknownOrInvalid && verifyCreditCardNumber(number);
    }

    public static String formatCreditCardNumberForDisplay(String number) {
        final String num = number.replaceAll(" ", "");
        StringBuilder buf = new StringBuilder();

        //todo: add spaces according to credit card type
        for (int i = 0; i < num.length(); i++) {
            buf.append(num.charAt(i));
            if (i == 3 || i == 7 || i == 11) {
                buf.append(' ');
            }
        }

        return buf.toString();
    }

    public static CreditCardType getCreditCardType(String number) {

        if (TextUtils.isEmpty(number)) {
            return CreditCardType.UnknownOrInvalid;
        }

        final String digit1 = number.substring(0, 1);
        final String digit2 = number.length() > 1 ? number.substring(0, 2) : null;
        final String digit3 = number.length() > 2 ? number.substring(0, 3) : null;
        final String digit4 = number.length() > 3 ? number.substring(0, 4) : null;

        if (TextUtils.isDigitsOnly(number.replaceAll(" ", ""))) {
            if ("4".equals(digit1)) {
                if (number.length() == 13 || number.length() == 16) {
                    return CreditCardType.Visa;
                }
            } else if ("51".compareTo(digit2) < 0 && "55".compareTo(digit2) > 0) {
                if (number.length() == 16) {
                    return CreditCardType.MasterCard;
                }
            } else if ("34".equals(digit2) || "37".equals(digit2)) {
                if (number.length() == 15) {
                    return CreditCardType.AmericanExpress;
                }
            } else if ("2014".equals(digit4) || "2149".equals(digit4)) {
                if (number.length() == 15) {
                    return CreditCardType.EnRoute;
                }
            } else if ("36".equals(digit2) || "38".equals(digit2) || ("300".compareTo(digit3) < 0 && "305".compareTo(digit3) > 0)) {
                if (number.length() == 14) {
                    return CreditCardType.Diners;
                }
            } else if ("5".equals(digit1) || "6".equals(digit1)) {
                if (number.length() >= 12 && number.length() <= 19) {
                    return CreditCardType.Maestro;
                }
            }
        }

        return CreditCardType.UnknownOrInvalid;
    }

    public static CreditCardType guesstCreditCardType(String number) {

        if (TextUtils.isEmpty(number)) {
            return CreditCardType.UnknownOrInvalid;
        }

        final String digit1 = number.substring(0, 1);
        final String digit2 = number.length() > 1 ? number.substring(0, 2) : null;
        final String digit3 = number.length() > 2 ? number.substring(0, 3) : null;
        final String digit4 = number.length() > 3 ? number.substring(0, 4) : null;

        if (TextUtils.isDigitsOnly(number.replaceAll(" ", ""))) {
            if ("4".equals(digit1)) {
                return CreditCardType.Visa;
            } else if ("51".compareTo(digit2) < 0 && "55".compareTo(digit2) > 0) {
                return CreditCardType.MasterCard;
            } else if ("34".equals(digit2) || "37".equals(digit2)) {
                return CreditCardType.AmericanExpress;
            } else if ("2014".equals(digit4) || "2149".equals(digit4)) {
                return CreditCardType.EnRoute;
            } else if ("36".equals(digit2) || "38".equals(digit2) || ("300".compareTo(digit3) < 0 && "305".compareTo(digit3) > 0)) {
                return CreditCardType.Diners;
            } else if ("5".equals(digit1) || "6".equals(digit1)) {
                return CreditCardType.Maestro;
            }
        }

        return CreditCardType.UnknownOrInvalid;
    }

    public static boolean verifyCreditCardNumber(String n) {
        if (TextUtils.isEmpty(n)) {
            return false;
        }

        try {
            int j = n.length();

            String[] s1 = new String[j];
            for (int i = 0; i < n.length(); i++) {
                s1[i] = "" + n.charAt(i);
            }

            int checksum = 0;

            for (int i = s1.length - 1; i >= 0; i -= 2) {
                int k = 0;

                if (i > 0) {
                    k = Integer.valueOf(s1[i - 1]) * 2;
                    if (k > 9) {
                        String s = "" + k;
                        k = Integer.valueOf(s.substring(0, 1)) +
                                Integer.valueOf(s.substring(1));
                    }
                    checksum += Integer.valueOf(s1[i]) + k;
                } else {
                    checksum += Integer.valueOf(s1[0]);
                }
            }
            return ((checksum % 10) == 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
