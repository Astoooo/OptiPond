package com.example.optipond.Utils;

public class MathUtils {

    public static String roundOff(double value){
        double phValue = value;
        boolean hasDecimal = phValue % 1 != 0;
        if (hasDecimal){
            phValue = Math.round(phValue * 100.0) / 100.0;
        }

        return String.valueOf(phValue);
    }

}
