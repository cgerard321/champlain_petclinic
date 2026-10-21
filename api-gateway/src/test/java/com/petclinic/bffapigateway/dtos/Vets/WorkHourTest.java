package com.petclinic.bffapigateway.dtos.Vets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class WorkHourTest {

    @Test
    @DisplayName("Must validate the total quantity " +
                "and values of the enum via values and valueOf")

    void workHour_values_shouldReturnAllEnumConstants(){

        WorkHour[] hours = WorkHour.values();


        //this tests that the enum is not empty (hours are given)
        assertThat(hours).isNotEmpty();

        assertEquals(WorkHour.Hour_8_9, WorkHour.valueOf("Hour_8_9"));
        assertEquals(WorkHour.Hour_19_20, WorkHour.valueOf("Hour_19_20"));


    }

    @ParameterizedTest
    @EnumSource(WorkHour.class) //it iterates through 8_9 and 19_20 so we dont neet to right manually for each
    @DisplayName("Garantees the WorkHour's constant return a valid description")

    void getTimeRange_shouldReturnValidRangeForEveryEnumConstant(WorkHour workHour){
        String timeRange = workHour.getTimeRange();

        //checks if it's not null or empty
        assertNotNull(timeRange);
        assertThat(timeRange).isNotBlank();
    }

    //this does not have enumSource but since the first test validates, this one should work if the previous one worked
    @Test
    @DisplayName("")
    void getTimeRange_shouldReturnExactExpectedFormat() {
        assertEquals("8am-9am", WorkHour.Hour_8_9.getTimeRange());
        assertEquals("19pm-20pm", WorkHour.Hour_19_20.getTimeRange());
    }
}






