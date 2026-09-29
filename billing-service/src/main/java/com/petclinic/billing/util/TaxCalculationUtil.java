package com.petclinic.billing.util;

import com.petclinic.billing.datalayer.Bill;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class TaxCalculationUtil {
    private static final BigDecimal GOODS_AND_SERVICES_TAX = new BigDecimal("0.05");
    private static final BigDecimal QUEBEC_SALES_TAX = new BigDecimal("0.09975");

    public static BigDecimal calculateTotalTaxes(BigDecimal amount){
        if(amount == null || amount.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }

        BigDecimal gstAmount = amount.multiply(GOODS_AND_SERVICES_TAX).setScale(2, RoundingMode.HALF_UP);
        BigDecimal qstAmount = amount.multiply(QUEBEC_SALES_TAX).setScale(2, RoundingMode.HALF_UP);

        return amount.add(gstAmount).add(qstAmount);
    }

    public static BigDecimal calculateGST(BigDecimal amount){
        if(amount == null || amount.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return amount.multiply(GOODS_AND_SERVICES_TAX).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateQST(BigDecimal amount){
        if(amount == null || amount.equals(BigDecimal.ZERO)){
            return BigDecimal.ZERO;
        }
        return amount.multiply(QUEBEC_SALES_TAX).setScale(2, RoundingMode.HALF_UP);
    }
}
