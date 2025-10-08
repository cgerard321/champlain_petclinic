package com.petclinic.billing.util;

import com.petclinic.billing.datalayer.Bill;
import com.petclinic.billing.datalayer.BillResponseDTO;
import com.petclinic.billing.datalayer.BillStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EntityDtoUtilTest {

    @Test
    void toBillResponseDto_WithValidBill_ShouldMapCorrectly() {
        // Arrange
        Bill bill = new Bill();
        bill.setBillId("test-bill-123");
        bill.setCustomerId("customer-456");
        bill.setVisitType("checkup");
        bill.setVetId("vet-789");
        bill.setVetFirstName("Dr. John");
        bill.setVetLastName("Smith");
        bill.setDate(LocalDate.now());
        bill.setAmount(new BigDecimal("100.00"));
        bill.setBillStatus(BillStatus.UNPAID);
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setInterestExempt(false);

        // Act
        BillResponseDTO result = EntityDtoUtil.toBillResponseDto(bill);

        // Assert
        assertNotNull(result);
        assertEquals("test-bill-123", result.getBillId());
        assertEquals("customer-456", result.getCustomerId());
        assertEquals("checkup", result.getVisitType());
        assertEquals("vet-789", result.getVetId());
        assertEquals("Dr. John", result.getVetFirstName());
        assertEquals("Smith", result.getVetLastName());
        assertEquals(bill.getDate(), result.getDate());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(BillStatus.UNPAID, result.getBillStatus());
        assertEquals(bill.getDueDate(), result.getDueDate());
        assertFalse(result.isInterestExempt());
        assertNotNull(result.getInterest());
        assertNotNull(result.getTaxedAmount());
    }

    @Test
    void toBillResponseDto_WithNullAmount_ShouldHandleGracefully() {
        // Arrange
        Bill bill = new Bill();
        bill.setBillId("test-bill-null-amount");
        bill.setCustomerId("customer-456");
        bill.setVisitType("checkup");
        bill.setVetId("vet-789");
        bill.setVetFirstName("Dr. Jane");
        bill.setVetLastName("Doe");
        bill.setDate(LocalDate.now());
        bill.setAmount(null); // Null amount
        bill.setBillStatus(BillStatus.UNPAID);
        bill.setDueDate(LocalDate.now().plusDays(30));
        bill.setInterestExempt(false);

        // Act
        BillResponseDTO result = EntityDtoUtil.toBillResponseDto(bill);

        // Assert
        assertNotNull(result);
        assertEquals("test-bill-null-amount", result.getBillId());
        assertNull(result.getAmount());
        assertNotNull(result.getInterest());
        assertNotNull(result.getTaxedAmount());
        // When amount is null, taxedAmount should equal the interest amount
        assertEquals(result.getInterest().setScale(2, RoundingMode.HALF_UP), result.getTaxedAmount());
    }

    @Test
    void toBillResponseDto_WithOverdueBill_ShouldCalculateInterest() {
        // Arrange
        Bill bill = new Bill();
        bill.setBillId("overdue-bill-123");
        bill.setCustomerId("customer-789");
        bill.setVisitType("surgery");
        bill.setVetId("vet-456");
        bill.setVetFirstName("Dr. Alice");
        bill.setVetLastName("Johnson");
        bill.setDate(LocalDate.now().minusDays(60));
        bill.setAmount(new BigDecimal("500.00"));
        bill.setBillStatus(BillStatus.OVERDUE);
        bill.setDueDate(LocalDate.now().minusDays(30)); // 30 days overdue
        bill.setInterestExempt(false);

        // Act
        BillResponseDTO result = EntityDtoUtil.toBillResponseDto(bill);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        assertNotNull(result.getInterest());
        assertTrue(result.getInterest().compareTo(BigDecimal.ZERO) > 0, "Interest should be greater than zero for overdue bill");
        assertNotNull(result.getTaxedAmount());
        assertTrue(result.getTaxedAmount().compareTo(result.getAmount()) > 0, "Taxed amount should be greater than original amount due to interest");
    }

    @Test
    void toBillResponseDto_WithInterestExemptBill_ShouldHaveZeroInterest() {
        // Arrange
        Bill bill = new Bill();
        bill.setBillId("exempt-bill-123");
        bill.setCustomerId("customer-999");
        bill.setVisitType("emergency");
        bill.setVetId("vet-111");
        bill.setVetFirstName("Dr. Bob");
        bill.setVetLastName("Wilson");
        bill.setDate(LocalDate.now().minusDays(60));
        bill.setAmount(new BigDecimal("300.00"));
        bill.setBillStatus(BillStatus.OVERDUE);
        bill.setDueDate(LocalDate.now().minusDays(30)); // 30 days overdue
        bill.setInterestExempt(true); // Interest exempt

        // Act
        BillResponseDTO result = EntityDtoUtil.toBillResponseDto(bill);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("300.00"), result.getAmount());
        assertNotNull(result.getInterest());
        assertEquals(BigDecimal.ZERO, result.getInterest());
        assertNotNull(result.getTaxedAmount());
        assertEquals(result.getAmount().setScale(2, RoundingMode.HALF_UP), result.getTaxedAmount());
        assertTrue(result.isInterestExempt());
    }

    @Test
    void toBillResponseDto_WithPaidBill_ShouldMapCorrectly() {
        // Arrange
        Bill bill = new Bill();
        bill.setBillId("paid-bill-123");
        bill.setCustomerId("customer-555");
        bill.setVisitType("vaccination");
        bill.setVetId("vet-222");
        bill.setVetFirstName("Dr. Carol");
        bill.setVetLastName("Brown");
        bill.setDate(LocalDate.now().minusDays(15));
        bill.setAmount(new BigDecimal("75.00"));
        bill.setBillStatus(BillStatus.PAID);
        bill.setDueDate(LocalDate.now().minusDays(5));
        bill.setInterestExempt(false);

        // Act
        BillResponseDTO result = EntityDtoUtil.toBillResponseDto(bill);

        // Assert
        assertNotNull(result);
        assertEquals("paid-bill-123", result.getBillId());
        assertEquals(BillStatus.PAID, result.getBillStatus());
        assertEquals(new BigDecimal("75.00"), result.getAmount());
        assertNotNull(result.getInterest());
        assertNotNull(result.getTaxedAmount());
    }
}