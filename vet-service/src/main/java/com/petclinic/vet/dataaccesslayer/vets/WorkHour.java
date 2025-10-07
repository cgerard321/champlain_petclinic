package com.petclinic.vet.dataaccesslayer.vets;

public enum WorkHour {
    Hour_8_9("8am-9am"),
    Hour_9_10("9am-10am"),
    Hour_10_11("10am-11am"),
    Hour_11_12("11am-12am"),
    Hour_12_13("12am-13pm"),
    Hour_13_14("13pm-14pm"),
    Hour_14_15("14pm-15pm"),
    Hour_15_16("15pm-16pm"),
    Hour_16_17("16pm-17pm"),
    Hour_17_18("17pm-18pm"),
    Hour_18_19("18pm-19pm"),
    Hour_19_20("19pm-20pm");

    private String timeRange;

    WorkHour(String timeRange) {
        this.timeRange = timeRange;
    }

    public String getTimeRange() {
        return timeRange;
    }
}
