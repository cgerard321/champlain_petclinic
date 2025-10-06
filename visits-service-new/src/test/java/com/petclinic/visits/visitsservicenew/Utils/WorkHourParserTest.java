package com.petclinic.visits.visitsservicenew.Utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkHourParserTest {

    // ==================== getStartTime Tests ====================

    @Test
    void getStartTime_withMorningHour_returnsCorrectTime() {
        // Arrange
        String workHourEnum = "Hour_9_10";

        // Act
        LocalTime result = WorkHourParser.getStartTime(workHourEnum);

        // Assert
        assertThat(result).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    void getStartTime_withAfternoonHour_returnsCorrectTime() {
        // Arrange
        String workHourEnum = "Hour_14_15";

        // Act
        LocalTime result = WorkHourParser.getStartTime(workHourEnum);

        // Assert
        assertThat(result).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    void getStartTime_withEarlyMorning_returnsCorrectTime() {
        // Arrange
        String workHourEnum = "Hour_8_9";

        // Act
        LocalTime result = WorkHourParser.getStartTime(workHourEnum);

        // Assert
        assertThat(result).isEqualTo(LocalTime.of(8, 0));
    }

    @ParameterizedTest
    @CsvSource({
            "Hour_8_9, 8",
            "Hour_9_10, 9",
            "Hour_10_11, 10",
            "Hour_11_12, 11",
            "Hour_14_15, 14",
            "Hour_15_16, 15",
            "Hour_16_17, 16",
            "Hour_17_18, 17"
    })
    void getStartTime_withVariousHours_returnsCorrectTime(String input, int expectedHour) {
        // Act
        LocalTime result = WorkHourParser.getStartTime(input);

        // Assert
        assertThat(result.getHour()).isEqualTo(expectedHour);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    // ==================== getEndTime Tests ====================

    @Test
    void getEndTime_withMorningHour_returnsCorrectTime() {
        // Arrange
        String workHourEnum = "Hour_9_10";

        // Act
        LocalTime result = WorkHourParser.getEndTime(workHourEnum);

        // Assert
        assertThat(result).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void getEndTime_withAfternoonHour_returnsCorrectTime() {
        // Arrange
        String workHourEnum = "Hour_14_15";

        // Act
        LocalTime result = WorkHourParser.getEndTime(workHourEnum);

        // Assert
        assertThat(result).isEqualTo(LocalTime.of(15, 0));
    }

    @ParameterizedTest
    @CsvSource({
            "Hour_8_9, 9",
            "Hour_9_10, 10",
            "Hour_10_11, 11",
            "Hour_11_12, 12",
            "Hour_14_15, 15",
            "Hour_15_16, 16",
            "Hour_16_17, 17",
            "Hour_17_18, 18"
    })
    void getEndTime_withVariousHours_returnsCorrectTime(String input, int expectedHour) {
        // Act
        LocalTime result = WorkHourParser.getEndTime(input);

        // Assert
        assertThat(result.getHour()).isEqualTo(expectedHour);
        assertThat(result.getMinute()).isEqualTo(0);
    }

    // ==================== Edge Cases & Error Handling ====================

    @Test
    void getStartTime_withInvalidFormat_throwsException() {
        // Arrange
        String invalidFormat = "InvalidFormat";

        // Act & Assert
        assertThatThrownBy(() -> WorkHourParser.getStartTime(invalidFormat))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getEndTime_withInvalidFormat_throwsException() {
        // Arrange
        String invalidFormat = "InvalidFormat";

        // Act & Assert
        assertThatThrownBy(() -> WorkHourParser.getEndTime(invalidFormat))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getStartTime_withMissingUnderscore_throwsException() {
        // Arrange
        String invalidFormat = "Hour910";

        // Act & Assert
        assertThatThrownBy(() -> WorkHourParser.getStartTime(invalidFormat))
                .isInstanceOf(Exception.class);
    }

    // ==================== Integration Tests ====================

    @Test
    void getStartAndEndTime_together_haveOneHourDifference() {
        // Arrange
        String workHourEnum = "Hour_9_10";

        // Act
        LocalTime start = WorkHourParser.getStartTime(workHourEnum);
        LocalTime end = WorkHourParser.getEndTime(workHourEnum);

        // Assert
        assertThat(end.minusHours(1)).isEqualTo(start);
    }

    @ParameterizedTest
    @CsvSource({
            "Hour_8_9",
            "Hour_9_10",
            "Hour_10_11",
            "Hour_14_15",
            "Hour_15_16"
    })
    void getStartAndEndTime_allValidHours_maintainOneHourInterval(String workHourEnum) {
        // Act
        LocalTime start = WorkHourParser.getStartTime(workHourEnum);
        LocalTime end = WorkHourParser.getEndTime(workHourEnum);

        // Assert
        assertThat(end).isAfter(start);
        assertThat(end.minusHours(1)).isEqualTo(start);
    }
}