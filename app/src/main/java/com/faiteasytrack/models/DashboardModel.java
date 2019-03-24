package com.faiteasytrack.models;

public class DashboardModel {

    private int statisticsInfoType;
    private boolean containsData;

    public DashboardModel() {
    }

    public int getStatisticsInfoType() {
        return statisticsInfoType;
    }

    public void setStatisticsInfoType(int statisticsInfoType) {
        this.statisticsInfoType = statisticsInfoType;
    }

    public boolean isContainsData() {
        return containsData;
    }

    public void setContainsData(boolean containsData) {
        this.containsData = containsData;
    }
}
