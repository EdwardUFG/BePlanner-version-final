package com.example.aplicacion_organizadora.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.aplicacion_organizadora.MainActivity;
import com.example.aplicacion_organizadora.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent deepLinkIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("app-organizador://recordatorios"),
                context,
                MainActivity.class
        );

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                deepLinkIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String titulo = intent.getStringExtra("TITULO_RECORDATORIO");
        String descripcion = intent.getStringExtra("DESCRIPCION_RECORDATORIO");
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "recordatorios_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titulo)
                .setContentText(descripcion)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notificationId, builder.build());
    }
}
