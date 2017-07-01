package com.company.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.company.MainActivity;
import com.company.R;
import com.company.managers.AlarmHelper;
import com.company.managers.EventManager;
import com.company.models.Event;

/**
 * Created by abdul on 22-Jun-17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String eventId = intent.getStringExtra(Event.ID_FIELD);
        String title = intent.getStringExtra(Event.TITLE_FIELD);
        boolean start = intent.getBooleanExtra(AlarmHelper.FOR_START_ALARM, true);

        showNotification(context, title, start);
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);

        if (!start)  EventManager.deleteEvent(context, eventId, false);
    }

    private void showNotification(Context context, String title, boolean start) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(title);

        if (start) {
            mBuilder.setContentText("Event has started");
        } else {
            mBuilder.setContentText("Event has ended. Deleting from Database");
        }
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }
}
