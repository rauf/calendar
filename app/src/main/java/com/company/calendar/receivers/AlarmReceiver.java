package com.company.calendar.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.company.calendar.MainActivity;
import com.company.calendar.R;
import com.company.calendar.managers.EventManager;
import com.company.calendar.models.Event;

/**
 * Created by abdul on 19-Jun-17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String eventId = intent.getStringExtra(Event.ID_FIELD);
        String title = intent.getStringExtra(Event.TITLE_FIELD);
        //Toast.makeText(context, "Time for event : " + title + "  Deleting the event now", Toast.LENGTH_LONG).show();
        showNotification(context, title);
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);
        EventManager.deleteEvent(context, eventId);
    }

    private void showNotification(Context context, String title) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(title)
                        .setContentText("Event is now deleted");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }

}
