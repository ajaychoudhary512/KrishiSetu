package com.agrilink.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.agrilink.app.R;

public class AgriFirebaseMessagingService {

    private static final String TAG = "AgriFirebaseService";
    private static final String CHANNEL_ID = "agrilink_orders_channel";

    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "FCM Registration Token: " + token);
    }

    public static void showNotification(Context context, String title, String body) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "AgriLink Orders & Escrow Deals",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Real-time notifications for stubble purchases, equipment rentals, and escrow locks.");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
