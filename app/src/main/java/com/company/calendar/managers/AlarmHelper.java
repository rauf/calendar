package com.company.calendar.managers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.company.calendar.models.Event;
import com.company.calendar.receivers.AlarmReceiver;

import java.util.Calendar;

/**
 * Created by abdul on 19-Jun-17.
 */

public class AlarmHelper {

    private AlarmHelper() {
        //private
    }

    public static int setAlarm(Context context, Event event, int alarmId, int year, int month, int date, int hourOfDay, int mins) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.clear();
        cal.set(year, month, date, hourOfDay, mins);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = getPendingIntent(context, event, alarmId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmMgr.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        } else {
            alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
        }

        return alarmId;
    }

    private static PendingIntent getPendingIntent(Context context, Event event, int alarmId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Event.TITLE_FIELD, event.getTitle());
        intent.putExtra(Event.ID_FIELD, event.getId());
        return PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    public static void cancelAlarm(Context context, int alarmId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(context,
                AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, alarmId, myIntent, PendingIntent.FLAG_ONE_SHOT);

        alarmManager.cancel(pendingIntent);
    }
}
