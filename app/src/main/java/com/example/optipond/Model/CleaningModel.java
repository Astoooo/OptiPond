package com.example.optipond.Model;

public class CleaningModel {
    String startDate;
    String endDate;
    String startWaterLevel;
    String endWaterLevel;
    String startPhValue;
    String endPhValue;
    String docId;

    public CleaningModel(String startDate, String endDate, String startWaterLevel, String endWaterLevel, String startPhValue, String endPhValue, String docId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.startWaterLevel = startWaterLevel;
        this.endWaterLevel = endWaterLevel;
        this.startPhValue = startPhValue;
        this.endPhValue = endPhValue;
        this.docId = docId;
    }

    public String getDocId(){
        return docId;
    }
    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartWaterLevel() {
        return startWaterLevel;
    }

    public String getEndWaterLevel() {
        return endWaterLevel;
    }

    public String getStartPhValue() {
        return startPhValue;
    }

    public String getEndPhValue() {
        return endPhValue;
    }
}
