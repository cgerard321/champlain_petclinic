package com.petclinic.billing.util;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * Utility class for calculating compound interest on overdue bills.
 * This centralizes the interest calculation logic to avoid code duplication
 * across multiple classes (EntityDtoUtil, BillServiceImpl, PdfGenerator).
 */
public class InterestCalculationUtil {

    /**
     * Monthly interest rate of 1.5% for overdue bills
     */
    public static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.015");

    /**
     * Calculates the compound interest for an overdue bill.
     * 
     * @param bill The bill to calculate interest for
     * @return The calculated interest amount, or BigDecimal.ZERO if no interest applies
     */
    public static BigDecimal calculateInterest(Bill bill) {
        if (bill.isInterestExempt()) {
            return BigDecimal.ZERO;
        }
        
        if (bill.getBillStatus() == BillStatus.OVERDUE && bill.getDueDate() != null) {
            return calculateCompoundInterest(bill.getAmount(), bill.getDueDate(), LocalDate.now());
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * Calculates compound interest for a given amount, due date, and current date.
     * 
     * @param amount The principal amount
     * @param dueDate The date the bill was due
     * @param currentDate The current date for calculation
     * @return The calculated interest amount
     */
    public static BigDecimal calculateCompoundInterest(BigDecimal amount, LocalDate dueDate, LocalDate currentDate) {
        if (amount == null || dueDate == null || currentDate == null) {
            return BigDecimal.ZERO;
        }

        Period period = Period.between(dueDate, currentDate);
        int overdueMonths = period.getYears() * 12 + period.getMonths();
        
        if (overdueMonths <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal onePlusRate = BigDecimal.ONE.add(MONTHLY_INTEREST_RATE);
        BigDecimal compounded = onePlusRate.pow(overdueMonths);
        BigDecimal finalAmount = amount.multiply(compounded).setScale(2, RoundingMode.HALF_UP);
        
        return finalAmount.subtract(amount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates the final amount (principal + compound interest) for an overdue bill.
     * 
     * @param bill The bill to calculate the final amount for
     * @return The final amount including interest
     */
    public static BigDecimal calculateFinalAmount(Bill bill) {
        BigDecimal interest = calculateInterest(bill);
        return bill.getAmount().add(interest);
    }

    /**
     * Calculates the number of overdue months between a due date and current date.
     * 
     * @param dueDate The date the bill was due
     * @param currentDate The current date for calculation
     * @return The number of months the bill is overdue
     */
    public static int calculateOverdueMonths(LocalDate dueDate, LocalDate currentDate) {
        if (dueDate == null || currentDate == null) {
            return 0;
        }
        
        Period period = Period.between(dueDate, currentDate);
        return Math.max(0, period.getYears() * 12 + period.getMonths());
    }
}