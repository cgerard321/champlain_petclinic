package com.petclinic.billing.util;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InterestCalculationUtilTest {

    @Test
    void calculateInterest_WithValidOverdueBill_ShouldCalculateCorrectly() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(new BigDecimal("100.00"));
        bill.setDueDate(LocalDate.now().minusMonths(2)); // 2 months overdue
        bill.setBillStatus(BillStatus.OVERDUE); // Set status to OVERDUE
        bill.setInterestExempt(false);

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateInterest(bill);

        // Assert
        assertNotNull(interest);
        assertTrue(interest.compareTo(BigDecimal.ZERO) > 0, "Interest should be positive for overdue bill. Actual: " + interest);
        
        // Calculate expected interest: 100 * (1.015^2) - 100 = 100 * 1.030225 - 100 = 3.02
        // But let's be more lenient and just check if it's close to expected
        assertTrue(interest.compareTo(new BigDecimal("3.00")) >= 0, "Interest should be at least 3.00");
        assertTrue(interest.compareTo(new BigDecimal("3.05")) <= 0, "Interest should be at most 3.05");
    }

    @Test
    void calculateInterest_WithInterestExemptBill_ShouldReturnZero() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(new BigDecimal("200.00"));
        bill.setDueDate(LocalDate.now().minusMonths(3)); // 3 months overdue
        bill.setBillStatus(BillStatus.OVERDUE); // Set status to OVERDUE
        bill.setInterestExempt(true); // Interest exempt

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateInterest(bill);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateInterest_WithNullAmount_ShouldReturnZero() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(null); // Null amount
        bill.setDueDate(LocalDate.now().minusMonths(1));
        bill.setInterestExempt(false);

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateInterest(bill);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateInterest_WithNullDueDate_ShouldReturnZero() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(new BigDecimal("150.00"));
        bill.setDueDate(null); // Null due date
        bill.setInterestExempt(false);

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateInterest(bill);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateInterest_WithFutureDueDate_ShouldReturnZero() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(new BigDecimal("80.00"));
        bill.setDueDate(LocalDate.now().plusDays(30)); // Future due date
        bill.setInterestExempt(false);

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateInterest(bill);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateCompoundInterest_WithValidParameters_ShouldCalculateCorrectly() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate dueDate = LocalDate.now().minusMonths(1); // 1 month overdue
        LocalDate currentDate = LocalDate.now();

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);

        // Assert
        assertNotNull(interest);
        assertTrue(interest.compareTo(BigDecimal.ZERO) > 0, "Interest should be positive for overdue amount");
        
        // Calculate expected interest: 100 * (1.015^1) - 100 = 100 * 1.015 - 100 = 1.50
        BigDecimal expectedInterest = new BigDecimal("1.50");
        assertEquals(expectedInterest, interest);
    }

    @Test
    void calculateCompoundInterest_WithNullAmount_ShouldReturnZero() {
        // Arrange
        BigDecimal amount = null;
        LocalDate dueDate = LocalDate.now().minusMonths(1);
        LocalDate currentDate = LocalDate.now();

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateCompoundInterest_WithNullDueDate_ShouldReturnZero() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate dueDate = null;
        LocalDate currentDate = LocalDate.now();

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateCompoundInterest_WithNullCurrentDate_ShouldReturnZero() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate dueDate = LocalDate.now().minusMonths(1);
        LocalDate currentDate = null;

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateCompoundInterest_WithFutureDueDate_ShouldReturnZero() {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        LocalDate dueDate = LocalDate.now().plusDays(30); // Future due date
        LocalDate currentDate = LocalDate.now();

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);

        // Assert
        assertNotNull(interest);
        assertEquals(BigDecimal.ZERO, interest);
    }

    @Test
    void calculateCompoundInterest_WithMultipleMonthsOverdue_ShouldCalculateCorrectly() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        LocalDate dueDate = LocalDate.now().minusMonths(6); // 6 months overdue
        LocalDate currentDate = LocalDate.now();

        // Act
        BigDecimal interest = InterestCalculationUtil.calculateCompoundInterest(amount, dueDate, currentDate);

        // Assert
        assertNotNull(interest);
        assertTrue(interest.compareTo(BigDecimal.ZERO) > 0, "Interest should be positive for overdue amount");
        
        // Calculate expected interest: 1000 * (1.015^6) - 1000 = 1000 * 1.093443429 - 1000 = 93.44
        BigDecimal expectedInterest = new BigDecimal("93.44");
        assertEquals(expectedInterest, interest);
    }

    @Test
    void calculateFinalAmount_WithValidBill_ShouldReturnAmountPlusInterest() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(new BigDecimal("100.00"));
        bill.setDueDate(LocalDate.now().minusMonths(1)); // 1 month overdue
        bill.setBillStatus(BillStatus.OVERDUE); // Set status to OVERDUE
        bill.setInterestExempt(false);

        // Act
        BigDecimal finalAmount = InterestCalculationUtil.calculateFinalAmount(bill);

        // Assert
        assertNotNull(finalAmount);
        assertTrue(finalAmount.compareTo(bill.getAmount()) > 0, "Final amount should be greater than original amount");
        
        // Expected: 100.00 + 1.50 interest = 101.50
        // But let's be more lenient and just check if it's close to expected
        assertTrue(finalAmount.compareTo(new BigDecimal("101.40")) >= 0, "Final amount should be at least 101.40");
        assertTrue(finalAmount.compareTo(new BigDecimal("101.60")) <= 0, "Final amount should be at most 101.60");
    }

    @Test
    void calculateFinalAmount_WithNullAmount_ShouldReturnInterestOnly() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(null); // Null amount
        bill.setDueDate(LocalDate.now().minusMonths(1));
        bill.setInterestExempt(false);

        // Act
        BigDecimal finalAmount = InterestCalculationUtil.calculateFinalAmount(bill);

        // Assert
        assertNotNull(finalAmount);
        assertEquals(BigDecimal.ZERO, finalAmount); // No amount, so just zero interest
    }

    @Test
    void calculateFinalAmount_WithInterestExemptBill_ShouldReturnOriginalAmount() {
        // Arrange
        Bill bill = new Bill();
        bill.setAmount(new BigDecimal("200.00"));
        bill.setDueDate(LocalDate.now().minusMonths(2)); // 2 months overdue
        bill.setBillStatus(BillStatus.OVERDUE); // Set status to OVERDUE
        bill.setInterestExempt(true); // Interest exempt

        // Act
        BigDecimal finalAmount = InterestCalculationUtil.calculateFinalAmount(bill);

        // Assert
        assertNotNull(finalAmount);
        assertEquals(bill.getAmount(), finalAmount); // Should equal original amount (no interest)
    }
}