package com.petclinic.billing.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatBillUtil {

    private FormatBillUtil() {

    }

    public static String formatCurrency(BigDecimal value, String currency) {
        Locale locale = "USD".equalsIgnoreCase(currency) ? Locale.US : Locale.CANADA;
        return NumberFormat.getCurrencyInstance(locale).format(value);
    }

    public static BigDecimal convertFromCad(BigDecimal value, String currency) {
        if (value == null) return BigDecimal.ZERO;
        if ("USD".equalsIgnoreCase(currency)) {
            return value.multiply(new BigDecimal("0.73"));
        }
        // Default/base: CAD
        return value;
    }
}
