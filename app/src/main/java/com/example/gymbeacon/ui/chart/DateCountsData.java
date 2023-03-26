package com.example.gymbeacon.ui.chart;

public class DateCountsData {
    String date;
    String counts;

    public DateCountsData(String date, String counts) {
        this.date = date;
        this.counts = counts;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCounts() {
        return counts;
    }

    public void setCounts(String counts) {
        this.counts = counts;
    }
}
