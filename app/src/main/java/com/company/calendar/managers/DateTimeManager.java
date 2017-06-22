package com.company.calendar.managers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by abdul on 22-Jun-17.
 */

public class DateTimeManager {

    public static Date toGMT(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gmt = new Date(sdf.format(date));
        return gmt;
    }

    public static Date gmttoLocalDate(Date date) {
        String timeZone = Calendar.getInstance().getTimeZone().getID();
        Date local = new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
        return local;
    }

    public static int getHour(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getMinute(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public static int getDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static String getTimeString(Date date) {
        SimpleDateFormat printFormat = new SimpleDateFormat("HH : mm");
        return printFormat.format(date);
    }


    public static String getDateString(Date date) {
        SimpleDateFormat printFormat = new SimpleDateFormat("dd / MM / YYYY");
        return printFormat.format(date);
    }

    public static Date setTime(Date startTime, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND,0);

        cal.set(Calendar.YEAR, getYear(startTime));
        cal.set(Calendar.MONTH, getMonth(startTime));
        cal.set(Calendar.DAY_OF_MONTH, getDay(startTime));

        return cal.getTime();
    }

    public static Date setDate(Date startTime, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        cal.set(Calendar.MINUTE, getMinute(startTime));
        cal.set(Calendar.HOUR_OF_DAY, getHour(startTime));
        return cal.getTime();
    }

    public static Calendar setCalendar(Date date) {
        Calendar cal = Calendar.getInstance();

        cal.set(getYear(date),
                getMonth(date),
                getDay(date),
                getHour(date),
                getMinute(date));

        return cal;
    }

}
