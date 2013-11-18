/*
 * Copyright (c) 2013. All Rights Reserved
 * Written by Yavor Paunov
 */

package walk.around.utils;

public class UnitFormatter {

    static public String metersToString(int meters) {
        String formatted;

        int kilometers = meters / 1000;
        int metersRemainder = meters % 1000;

        if(kilometers > 0 || metersRemainder > 500) {
            // Show as km
            formatted = String.format("%d.%dkm", kilometers, metersRemainder);
        } else {
            // Show as m
            formatted = String.format("%dm", meters);
        }

        return formatted;
    }

    static public String secondsToString(int seconds) {
        String formatted = null;

        int hours = seconds / (60 * 60);
        seconds -= hours * 60 * 60;
        int minutes = seconds / 60;
        seconds -= minutes * 60;

        if (seconds > 0) {
            minutes++;
        }

        if(hours > 0) {
            formatted = String.format("%d hours %d minutes", hours, minutes);
        } else {
            formatted = String.format("%d minutes", minutes);
        }

        return formatted;
    }

}
