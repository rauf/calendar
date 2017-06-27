package com.company.calendar.managers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.company.calendar.models.Event;
import com.company.calendar.receivers.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by abdul on 19-Jun-17.
 */

public class AlarmHelper {

    public static String FOR_START_ALARM = "forStart";

    private AlarmHelper() {
        //private
    }

    public static void setAlarm(Context context, Event event) {

        Date localStartTime = DateTimeManager.gmttoLocalDate(event.getStartTime());         //set up alarm using local time
        Date localEndTime = DateTimeManager.gmttoLocalDate(event.getEndTime());

        Calendar start = DateTimeManager.setCalendar(localStartTime);
        Calendar end = DateTimeManager.setCalendar(localEndTime);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent startIntent = getPendingIntent(context, event, event.getStartAlarmId(), true);
        PendingIntent endIntent = getPendingIntent(context, event, event.getEndAlarmId(), false);

        Date currTime = new Date();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (currTime.before(localStartTime)) {      //event in progress
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, start.getTimeInMillis(), startIntent);
            }
            if (currTime.before(localEndTime)) {        //event ended
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, end.getTimeInMillis(), endIntent);
            }
        } else {
            if (currTime.before(localStartTime)) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, start.getTimeInMillis(), startIntent);
            }
            if (currTime.before(localEndTime)) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, end.getTimeInMillis(), endIntent);
            }
        }
    }

    private static PendingIntent getPendingIntent(Context context, Event event, int alarmId, boolean forStart) {

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Event.TITLE_FIELD, event.getTitle());
        intent.putExtra(Event.ID_FIELD, event.getId());
        intent.putExtra(FOR_START_ALARM, forStart);
        return PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    public static void cancelAlarm(Context context, int alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, myIntent, PendingIntent.FLAG_ONE_SHOT);

        alarmManager.cancel(pendingIntent);
    }
}
