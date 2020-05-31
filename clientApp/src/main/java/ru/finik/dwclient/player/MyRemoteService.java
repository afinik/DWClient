package ru.finik.dwclient.player;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import ru.finik.dwclient.R;

import static ru.finik.dwclient.player.MyService.NOTIFY_ID;

public class MyRemoteService extends Service {
    public static final String ACTION_PLAY = "ru.finik.StayNotSoClose.ACTION_PLAY";
    NotificationManager mManager;
    NotificationCompat.Builder mBuilder;
    MediaPlayer mediaPlayer;
    public boolean isPlaying = false;
    public MyRemoteService() {

    }
    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        mediaPlayer = MediaPlayer.create(this, R.raw.dontstay);
        mediaPlayer.setVolume(0.5f,0.5f);
        mediaPlayer.setLooping(true);
    }

    private void createNotify(){
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentLaunch = PendingIntent.getActivity(this,0,activityIntent,0);
        PendingIntent pendingIntentPlay = PendingIntent.getService(this,
                0,
                new Intent(this,MyRemoteService.class).setAction(ACTION_PLAY),0);
        NotificationCompat.Action actionPlay = new NotificationCompat.Action(android.R.drawable.ic_media_play,
                "play", pendingIntentPlay);


        mBuilder = new NotificationCompat.Builder(this,MainActivity.CHANNEL_ID);
        mBuilder.setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("Our service notify")
                .setContentText("какой-то текст")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntentLaunch)
                .addAction(actionPlay);
        //.addAction(actionPlay);
        // mManager.notify(NOTIFY_ID,mBuilder.build());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_PLAY.equals(intent.getAction())){
            if(isPlaying){
                pause();
            } else {
                play();
            }
        } else {
            mediaPlayer.start();
            createNotify();
            Notification notification = mBuilder.build();
            startForeground(NOTIFY_ID,notification);
            isPlaying = true;
        }

        return START_STICKY;
    }
    public void pause(){
        isPlaying = false;

        mediaPlayer.pause();
    }
    public void play(){
        isPlaying = true;

        mediaPlayer.start();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }
}
