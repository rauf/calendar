package com.company.calendar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

import com.company.calendar.models.Event;

/**
 * Created by abdul on 19-Jun-17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String eventId = intent.getStringExtra(Event.ID_FIELD);
        String title = intent.getStringExtra(Event.TITLE_FIELD);
        Toast.makeText(context, "Time for event : " + title + "  Deleting the event now", Toast.LENGTH_LONG).show();
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);
        //EventManager.deleteEvent(context, eventId);
    }
}
