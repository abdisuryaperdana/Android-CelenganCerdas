package com.example.celengancerdas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String celenganName = intent.getStringExtra("CELENGAN_NAME");

        // Tampilkan notifikasi
        showNotification(context, celenganName);
    }

    private void showNotification(Context context, String celenganName) {
        String channelId = "ReminderChannel";
        String channelName = "Reminder Channel";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.piggybank)
                .setContentTitle("Reminder: Jangan Lupa Menabung untuk")
                .setContentText(celenganName + " Yaaa!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Membuat channel notifikasi untuk Android Oreo dan versi di atasnya
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        playNotificationSound(context);
        // Tampilkan notifikasi
        notificationManager.notify(123, builder.build());
    }

    public void playNotificationSound(Context context) {
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + context.getPackageName() + "/raw/notif");
            Ringtone r = RingtoneManager.getRingtone(context, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


