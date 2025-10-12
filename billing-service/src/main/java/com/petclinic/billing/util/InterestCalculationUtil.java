package com.petclinic.billing.util;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * Utility class for calculating compound interest on overdue bills.
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
        
        // For paid bills, return the stored interest (preserves the interest that was paid)
        if (bill.getBillStatus() == BillStatus.PAID && bill.getInterest() != null) {
            return bill.getInterest();
        }
        
        // For overdue bills or unpaid bills past due date, calculate current interest
        if ((bill.getBillStatus() == BillStatus.OVERDUE || 
             (bill.getBillStatus() == BillStatus.UNPAID && bill.getDueDate() != null && bill.getDueDate().isBefore(LocalDate.now()))) 
            && bill.getDueDate() != null) {
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
     * Calculates the total amount owed (principal + compound interest) for an overdue bill.
     * If the bill amount is null, returns zero as no calculation can be performed.
     * 
     * @param bill The bill to calculate the total amount for
     * @return The total amount including interest, or zero if amount is null
     */
    public static BigDecimal calculateTotalAmountOwed(Bill bill) {
        if (bill.getAmount() == null) {
            return BigDecimal.ZERO;
        }
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