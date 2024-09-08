package com.example.optipond.Model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
@RequiresApi(api = Build.VERSION_CODES.O)
public class ReadingModel {
    String waterPercentage;
    String phValue;

    String tds;
    String oxygen;
    boolean isHome;
    String date;



    public ReadingModel(String waterPercentage, String phValue,String tds, String oxygen, boolean isHome,String date) {
        this.waterPercentage = waterPercentage;
        this.phValue = phValue;
        this.oxygen = oxygen;
        this.tds = tds;
        this.isHome = isHome;
        this.date = date;


    }

    public String getDate (){
        return date;
    }
    public boolean getIsHome(){
        return isHome;
    }
    public String getWaterPercentage() {
        return waterPercentage;
    }

    public void setWaterPercentage(String waterPercentage) {
        this.waterPercentage = waterPercentage;
    }

    public String getPhValue() {
        return phValue;
    }

    public String getOxygen(){
        return oxygen;
    }
    public  String getTds(){
        return tds;
    }





}
