package com.petclinic.visits.visitsservicenew.Utils;

import java.time.LocalTime;


public class WorkHourParser {

    public static LocalTime getStartTime(String workHourEnum) {
        String[] parts = workHourEnum.replace("Hour_", "").split("_");
        int startHour = Integer.parseInt(parts[0]);
        return LocalTime.of(startHour, 0);
    }

    public static LocalTime getEndTime(String workHourEnum) {
        String[] parts = workHourEnum.replace("Hour_", "").split("_");
        int endHour = Integer.parseInt(parts[1]);
        return LocalTime.of(endHour, 0);
    }
}
