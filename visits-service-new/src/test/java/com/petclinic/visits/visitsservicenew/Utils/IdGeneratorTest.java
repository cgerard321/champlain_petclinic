package com.petclinic.visits.visitsservicenew.Utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class IdGeneratorTest {
    private static final ZoneId TIMEZONE = ZoneId.of("America/Montreal");

    @Test
    public void testGenerateVisitIdDateAndIncrement() {
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");

        String expectedYear = LocalDate.now(TIMEZONE).format(yearFormatter);
        String expectedMonth = LocalDate.now(TIMEZONE).format(monthFormatter);
        String expectedDay = LocalDate.now(TIMEZONE).format(dayFormatter);

        String id1 = IdGenerator.generateVisitId();
        assertNotNull(id1);

        String[] parts1 = id1.split("-");
        assertEquals(3, parts1.length, "id should have three parts separated by hyphens");
        assertEquals("VIST", parts1[0], "prefix should be VIST");
        assertTrue(parts1[1].matches("\\d{4}"), "yyMM part should be 4 digits");
        assertTrue(parts1[2].matches("\\d{4}"), "dd+seq part should be 4 digits");

        // verify year+month and day
        assertEquals(expectedYear + expectedMonth, parts1[1], "yyMM must match today");
        assertEquals(expectedDay, parts1[2].substring(0, 2), "dd must match today");

        // extract and validate sequence
        String seq1Str = parts1[2].substring(2);
        assertTrue(seq1Str.matches("\\d{2}"), "sequence must be two digits");
        int seq1 = Integer.parseInt(seq1Str);
        assertTrue(seq1 >= 1, "sequence must be >= 1");

        // generate second id and assert increment
        String id2 = IdGenerator.generateVisitId();
        String[] parts2 = id2.split("-");
        assertEquals(parts1[0], parts2[0], "prefix should remain the same");
        assertEquals(parts1[1], parts2[1], "date part should remain the same for same day");

        String seq2Str = parts2[2].substring(2);
        assertTrue(seq2Str.matches("\\d{2}"), "sequence must be two digits");
        int seq2 = Integer.parseInt(seq2Str);

        assertEquals(seq1 + 1, seq2, "sequence should increment by 1 for consecutive calls");
    }


    @Test
    public void testGenerateReviewIdDateAndIncrement() {
        DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");

        String expectedYear = LocalDate.now(TIMEZONE).format(yearFormatter);
        String expectedMonth = LocalDate.now(TIMEZONE).format(monthFormatter);
        String expectedDay = LocalDate.now(TIMEZONE).format(dayFormatter);

        String id1 = IdGenerator.generateReviewId();
        assertNotNull(id1);

        String[] parts1 = id1.split("-");
        assertEquals(3, parts1.length, "id should split into three parts");
        assertEquals("REVIEW", parts1[0], "prefix should be REVIEW");
        assertTrue(parts1[1].matches("\\d{4}"), "yyMM part should be 4 digits");
        assertTrue(parts1[2].matches("\\d{4}"), "dd+seq part should be 4 digits");

        // verify year+month and day
        assertEquals(expectedYear + expectedMonth, parts1[1], "yyMM must match today");
        assertEquals(expectedDay, parts1[2].substring(0, 2), "dd must match today");

        // extract and validate sequence
        String seq1Str = parts1[2].substring(2);
        assertTrue(seq1Str.matches("\\d{2}"), "sequence must be two digits");
        int seq1 = Integer.parseInt(seq1Str);
        assertTrue(seq1 >= 1, "sequence must be >= 1");

        // generate second id and assert increment
        String id2 = IdGenerator.generateReviewId();
        String[] parts2 = id2.split("-");
        assertEquals(parts1[0], parts2[0], "prefix should remain the same");
        assertEquals(parts1[1], parts2[1], "date part should remain the same for same day");

        String seq2Str = parts2[2].substring(2);
        assertTrue(seq2Str.matches("\\d{2}"), "sequence must be two digits");
        int seq2 = Integer.parseInt(seq2Str);

        assertEquals(seq1 + 1, seq2, "sequence should increment by 1 for consecutive calls");
    }


}
