package com.agrilink.app.models;

public class LaborItem {
    private String category;
    private String location;
    private String wage;
    private String workerCount;
    private String date;
    private String skill;
    private boolean isUrgent;
    private String details;

    public LaborItem(String category, String location, String wage, String workerCount, String date, String skill, boolean isUrgent) {
        this.category = category;
        this.location = location;
        this.wage = wage;
        this.workerCount = workerCount;
        this.date = date;
        this.skill = skill;
        this.isUrgent = isUrgent;
    }

    public LaborItem(String category, String details, String wage) {
        this.category = category;
        this.details = details;
        this.wage = wage;
        this.location = "Indore, MP";
        this.workerCount = "15 workers";
        this.date = "18 Aug";
        this.skill = category;
        this.isUrgent = true;
    }

    public String getCategory() { return category; }
    public String getLocation() { return location != null ? location : "Indore, MP"; }
    public String getWage() { return wage; }
    public String getWorkerCount() { return workerCount != null ? workerCount : "15 workers"; }
    public String getDate() { return date != null ? date : "18 Aug"; }
    public String getSkill() { return skill != null ? skill : category; }
    public boolean isUrgent() { return isUrgent; }
    public String getDetails() { return details; }
}
