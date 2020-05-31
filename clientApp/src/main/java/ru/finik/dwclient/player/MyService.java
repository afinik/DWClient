package ru.finik.dwclient.player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    public MyService() {
    }

    public static final int NOTIFY_ID = 124;
    public static final String ACTION_PAUSE = "ru.finik.StayNotSoClose.ACTION_PAUSE";
    public static final String ACTION_PLAY = "ru.finik.StayNotSoClose.ACTION_PLAY";
    NotificationManager mManager;
    NotificationCompat.Builder mBuilder;

    MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        mediaPlayer = MediaPlayer.create(this, R.raw.dontstay);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setLooping(true);
    }

    private void createNotify() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentLaunch = PendingIntent.getActivity(this, 0,
                activityIntent, 0);
        PendingIntent pendingIntentPause = PendingIntent.getService(this, 0,
                new Intent(this, MyService.class).setAction(ACTION_PAUSE), 0);
        PendingIntent pendingIntentPlay = PendingIntent.getService(this, 0,
                new Intent(this, MyService.class).setAction(ACTION_PLAY), 0);
        NotificationCompat.Action actionPause = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "PAUSE", pendingIntentPause);
        NotificationCompat.Action actionPlay = new NotificationCompat.Action(android.R.drawable.ic_media_play, "PLAY", pendingIntentPlay);
//
        mBuilder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID);
        mBuilder.setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("Our service notify")
                .setContentText("Current music will be forever")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntentLaunch)
//                .addAction(actionPlay)
                .addAction(actionPause);
//        mManager.notify("some info", NOTIFY_ID,  mBuilder.build());
//        updateNotify(pause());

    }

    private void updateNotify(boolean isPaused) {
        PendingIntent pendingIntent = PendingIntent.getService(this, 0,
                new Intent(this, MyService.class).setAction(ACTION_PAUSE), 0);
        NotificationCompat.Action actionPause = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "PLAY/PAUSE", pendingIntent);
//        mBuilder.mActions.get()
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_PAUSE.equals(intent.getAction())) {
            pause();
        } else if (ACTION_PLAY.equals(intent.getAction())) {
            play();
        } else {
            mediaPlayer.start();
            createNotify();
            Log.e("tag", "ntcnefefewfew");
            startForeground(NOTIFY_ID, mBuilder.build());
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void play() {

        mediaPlayer.start();
    }

    public float getProgress() {
        return mediaPlayer.getCurrentPosition() / (float) mediaPlayer.getDuration();
    }

    @Override
    public IBinder onBind(Intent intent) {
        mediaPlayer.start();
        return new MyBinder(this);
    }

    static class MyBinder extends Binder {
        public MyService service;

        public MyBinder(MyService service) {
            this.service = service;
        }
    }
}
