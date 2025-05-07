package com.example.glicone;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

public class AlarmeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicamentoNome = intent.getStringExtra("nome");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicamento_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Hora de tomar o medicamento!")
                .setContentText("Não se esqueça de tomar: " + medicamentoNome)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(new long[]{0, 500, 250, 500})
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "medicamento_channel",
                    "Alarme Medicamento",
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0, builder.build());
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void agendarAlarme(Context context, long triggerAtMillis, String nomeMedicamento, long dataFinalMillis, String tipoRepeticao) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return;
            }
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmeReceiver.class);
        intent.putExtra("nome", nomeMedicamento);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                nomeMedicamento.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }

        if (System.currentTimeMillis() < dataFinalMillis) {
            long intervalMillis = 0;
            switch (tipoRepeticao) {
                case "diariamente":
                    intervalMillis = AlarmManager.INTERVAL_DAY;
                    break;
                case "semanalmente":
                    intervalMillis = AlarmManager.INTERVAL_DAY * 7;
                    break;
                case "mensalmente":
                    intervalMillis = 30L * 24 * 60 * 60 * 1000;
                    break;
            }

            if (intervalMillis > 0) {
                long nextTriggerAtMillis = triggerAtMillis + intervalMillis;

                if (nextTriggerAtMillis <= dataFinalMillis) {
                    alarmManager.cancel(pendingIntent);
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerAtMillis, pendingIntent);
                }
            }
        }
    }
}