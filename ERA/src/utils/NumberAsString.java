package utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import controller.Controller;

public class NumberAsString {

    public static String formatAsString(Object amount) {
        Locale locale = new Locale("en", "US");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        symbols.setDecimalSeparator(Controller.DECIMAL_SEPARATOR);
        symbols.setGroupingSeparator(Controller.GROUPING_SEPARATOR);

        if (amount instanceof BigDecimal) {
            int scale = ((BigDecimal) amount).scale();
            if (scale <= 0) {
                return new DecimalFormat("###,##0", symbols).format(amount);
            }

            String ss = "";
            for (int i = 0; i < scale; i++) {
                ss += "0";
            }
            return new DecimalFormat("###,##0." + ss, symbols).format(amount);

        } else if (amount instanceof Integer
                || amount instanceof Long) {
            return new DecimalFormat("###,##0", symbols).format(amount);
        } else {
            return new DecimalFormat("###,##0.000", symbols).format(amount);
        }
    }

    public static String formatAsString(BigDecimal amount, int scale) {
        Locale locale = new Locale("en", "US");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        symbols.setDecimalSeparator(Controller.DECIMAL_SEPARATOR);
        symbols.setGroupingSeparator(Controller.GROUPING_SEPARATOR);

        if (scale <= 0) {
            return new DecimalFormat("###,##0", symbols).format(amount);
        }

        String ss = "";
        for (int i = 0; i < scale; i++) {
            ss += "0";
        }
        return new DecimalFormat("###,##0." + ss, symbols).format(amount);

    }

}
