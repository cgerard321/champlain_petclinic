package com.petclinic.billing.util;

import java.math.BigDecimal;
import java.math.MathContext;

public class TaxCalculationUtil {
    private static final BigDecimal GOODS_AND_SERVICES_TAX = new BigDecimal("0.05");
    private static final BigDecimal QUEBEC_SALES_TAX = new BigDecimal("0.0975");

    private static final MathContext DECIMAL = new MathContext(2);


    public static BigDecimal calculateTotalTaxes(BigDecimal amount){
        if(amount.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }

        BigDecimal gstAmount = amount.multiply(GOODS_AND_SERVICES_TAX).round(DECIMAL);
        BigDecimal qstAmount = amount.multiply(QUEBEC_SALES_TAX).round(DECIMAL);

        return amount.add(gstAmount).add(qstAmount);
    }

    public static BigDecimal calculateGST(BigDecimal amount){
        if(amount.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return amount.multiply(GOODS_AND_SERVICES_TAX).round(DECIMAL);
    }

    public static BigDecimal calculateQST(BigDecimal amount){
        if(amount.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return amount.multiply(QUEBEC_SALES_TAX).round(DECIMAL);
    }
}
