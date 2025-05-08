package com.example.glicone;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmeReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "AlarmChannel";
    private static final int NOTIFICATION_ID = 1001;
    private static final String ACTION_PARAR_ALARME = "ACTION_PARAR_ALARME";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_PARAR_ALARME.equals(intent.getAction())) {
            pararSom();
            cancelarAlarme(context, intent);
            cancelarNotificacao(context);
        } else {
            String medicamentoNome = intent.getStringExtra("nome");
            long timeInMillis = intent.getLongExtra("timeInMillis", 0);
            iniciarSom(context);
            criarCanalDeNotificacao(context);
            exibirNotificacao(context, medicamentoNome, timeInMillis);
        }
    }

    private void iniciarSom(Context context) {
        if (mediaPlayer == null) {
            Uri somPadrao = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;
            mediaPlayer = MediaPlayer.create(context, somPadrao);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
    }

    private void pararSom() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void cancelarAlarme(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long timeInMillis = intent.getLongExtra("timeInMillis", 0);

        Intent alarmIntent = new Intent(context, AlarmeReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) timeInMillis,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void cancelarNotificacao(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
    }

    private void criarCanalDeNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Alarme Medicamento", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void exibirNotificacao(Context context, String medicamentoNome, long timeInMillis) {
        Intent pararIntent = new Intent(context, AlarmeReceiver.class);
        pararIntent.setAction(ACTION_PARAR_ALARME);
        pararIntent.putExtra("timeInMillis", timeInMillis);
        PendingIntent pararPendingIntent = PendingIntent.getBroadcast(
                context, (int) timeInMillis, pararIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Hora de tomar o medicamento!")
                .setContentText("Tome seu medicamento: " + medicamentoNome)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .addAction(R.drawable.ic_stop, "Parar", pararPendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}