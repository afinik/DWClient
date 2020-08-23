package ru.finik.dwclient.player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ru.finik.dwclient.R;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener {
    public static final String CHANNEL_ID = "";
    private static final String TAG = "Тестовое логирование: ";
    private int duration;
    private int timeToEnd;
    ServiceConnection serviceConnection;
    MyService myService;
    public Handler handler;
    TextView versionTextMemo;
    TextView someTextMemo;
    TextView clientNumber;
    Button playButton;
    Button pauseButton;
    ProgressBar progressBar;
    Button openMusicFileBtn;
    Intent i;
    private MediaPlayer mediaPlayer;
    final int REQUESTCODE = 43544;
//    private final String IP = "18.218.155.22";
//    private final String IP = "ec2-18-188-13-12.us-east-2.compute.amazonaws.com";
    private final String IP = "192.168.0.165";
    private final int PORT = 8189;
    boolean wasOnPrepare = false;
    String clientId;
    TextView lastMessage;
    Thread thread;
    long previousTime;
    long startTime;
    int deltaTime;
    int MAXNUMOFITERATION = 1;
    int currentNumberOfIteration;
    double VERSION = 0.007;
    public static Uri myUri;
    public final String HCODE_MAP_KEY = "App_hash";
    private int clNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_main);


        //        Строчки ниже включать только для отладки в режиме тестирования программы
//        Intent intent;
//        intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
//        startActivity(intent);
        //выбираем папку
        writeAndReadServer("vers." + "/" + VERSION);
        //TODO реализовать сервис для приложения - запуск из пуш уведомлений
        clientId = UUID.randomUUID().toString();
        versionTextMemo = findViewById(R.id.versionTV);
        versionTextMemo.setText("vers. " + VERSION);
        someTextMemo = findViewById(R.id.someTextTV);
        someTextMemo.setText("");
        clientNumber = findViewById(R.id.client_num);
        playButton = findViewById(R.id.btn_play);
        lastMessage = findViewById(R.id.lastMessageTV);
        playButton.setBackgroundResource(android.R.drawable.ic_media_play);
        playButton.setVisibility(View.GONE);
        pauseButton = findViewById(R.id.btn_pause);
        pauseButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        pauseButton.setVisibility(View.GONE);
        openMusicFileBtn = findViewById(R.id.openMusicFileBtn);
        openMusicFileBtn.setBackgroundResource(android.R.drawable.ic_menu_upload);
        openMusicFileBtn.setVisibility(View.VISIBLE);
        //Сколько прошло с начала мелодии
        progressBar = findViewById(R.id.progressBar);
        //делаем ее невидимой при загрузке
        progressBar.setVisibility(View.GONE);


        //TODO делаем новый сервис для progressbar - сделать его рабочим
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.e("Сервис", "1");
                myService = ((MyService.MyBinder) binder).service;
                progressBar.setEnabled(true);
                progressBar.setProgress((int) (myService.getProgress() * 100));
                progressBar.post(new Runnable() {
                    @Override
                    public void run() {
                        if (myService != null) {
                            progressBar.setProgress((int) (myService.getProgress() * 100));
                            progressBar.postDelayed(this, 100);
                        } else {
                            progressBar.setEnabled(false);
                            progressBar.setProgress(0);
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                myService = null;
            }
        };

        handler = new Handler() {
            String s;
            Integer intS;
            Integer minDeltaTime;
            long currentTime;
            long time;
            List<Values> valuesList = new ArrayList();

            class Values {

                private long previousTime;
                private String s;
                private int deltaTime;

                public Values(int deltaTime, long previousTime, String s) {
                    this.previousTime = previousTime;
                    this.s = s;
                    this.deltaTime = deltaTime;
                }

                public long getPreviousTime() {
                    return previousTime;
                }

                public String getS() {
                    return s;
                }

                public int getDeltaTime() {
                    return deltaTime;
                }

                public void setDeltaTime(int deltaTime) {
                    this.deltaTime = deltaTime;
                }
            }

            @Override//метод, работающий в UI-потоке
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String strFromServer = (String) msg.obj;
//                Log.e(TAG, strFromServer + "");
                Log.e("Number", 1 + "");
//                textViewFromHandler.setText(s);

                switch (strFromServer.substring(0, strFromServer.lastIndexOf("/"))) {
                    case "clNum":
                        clNum = Integer.parseInt(strFromServer.substring(strFromServer.lastIndexOf("/") + 1));
                        clientNumber.setText(getResources().getString(R.string.client_number) + " " + clNum);
                        break;
                    case "curtime":
//                       playCircle(strFromServer);
                        //получаем строку и парсим ее
                        if (currentNumberOfIteration >= 1) {
                            //now
                            currentTime = System.currentTimeMillis();
                            // difference between time before writeAndReadServer(String string) and after
                            deltaTime = Math.toIntExact(currentTime - previousTime);
                            //string which received from server
                            s = strFromServer.substring(strFromServer.lastIndexOf("/") + 1);
                            valuesList.add(new Values(deltaTime, previousTime, s));

                            Log.e("Handler", "Полезное сообщение с сервера: " + s);

                            //If this iteration is first
                            if (currentNumberOfIteration == MAXNUMOFITERATION) minDeltaTime = deltaTime;
                            //iteration is not first
                            if (currentNumberOfIteration != MAXNUMOFITERATION) {
                                minDeltaTime = Math.min(minDeltaTime, deltaTime);
                            }

                            currentNumberOfIteration--;
                            if (currentNumberOfIteration != 0) playCycle();
                        }

                        if (currentNumberOfIteration == 0) {
                            //just log
                            Log.e("Handler", "Play");
                            for (Values values: valuesList){
                                if (values.getDeltaTime() == minDeltaTime){
                                    time = values.getPreviousTime();
                                    intS = Integer.parseInt(values.getS());
                                }
                            }

                            long lastTime = System.currentTimeMillis();
                            deltaTime = Math.toIntExact(lastTime - time);
                            mediaPlayer.seekTo(intS + deltaTime);
                            startPlayer();
                            Log.e("Самое быстрое сообщение дошло за(мс)", minDeltaTime + "");
                            lastMessage.setText(minDeltaTime + "");
                            Log.e("Все сообщения на сервер и обратно дошли за(мс)", (lastTime - startTime) + "");
                            playButton.setVisibility(View.GONE);
                            pauseButton.setVisibility(View.VISIBLE);
                            valuesList = new ArrayList();
                        }
                        break;
                    case "vers":
                        s = strFromServer.substring(strFromServer.lastIndexOf("/") + 1);
                        if (s.equals("old")){
                            Log.e("VERSION", "Version is too old");
                        }
                        break;
                    /*case "num":
                        break;
                    */
                }
            }


        };
    }
    //после выбора папки происходит

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case 43544:

                Log.e(TAG, "Путь выбран");
                //TODO реализовать отображение имени файла или композиции в TextView

                openMusicFileBtn.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);


                myUri = data.getData();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


                try {
                    mediaPlayer.setDataSource(getApplicationContext(), myUri);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.prepareAsync();


                } catch (IOException e) {
                    e.printStackTrace();
                }
                pausePlayer();
                Log.e(TAG, myUri + "----------");


                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        stopPlayer();
                    }
                });

                break;
        }
    }

    public void onClickOpenFile(View view) {
        i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(Intent.createChooser(i, getString(R.string.select_audio_file_title)), REQUESTCODE);
        if(myService!=null) {
            myService.play();
/*            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            //send "Start ... " to server for play music on the client (seekto)
        }
    }

    public void onClickPlayBtn(View view) {


        startTime = System.currentTimeMillis();
        currentNumberOfIteration = MAXNUMOFITERATION;
        playCycle();


    }

    private void playCycle() {
        previousTime = System.currentTimeMillis();
        writeAndReadServer("Start = " + clNum + "/" + duration);
    }


    private Map<Integer, Integer> playCycle(String strFromServer, Map map) {
//        map = new HashMap<Integer,Integer>();
        // in map there are serverDeltatime and clientDeltaTime
        if (map == null) {

        }
        Log.e(TAG, "Play");
        String s = strFromServer.substring(strFromServer.lastIndexOf("/") + 1);
        lastMessage.setText(s);
        deltaTime = Math.toIntExact(System.currentTimeMillis() - previousTime);
        mediaPlayer.seekTo(Integer.parseInt(s));
        Log.e("Сообщение на сервер и обратно дошло за(мс)", deltaTime + "");
        Log.e("Сообщение", s + "");
        startPlayer();
        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
        return map;
    }

    public void onClickPauseBtn(View view) {
        if (myService != null || true) {
//                    myService.pause();
            Log.e(TAG, "Pause");
            pauseButton.setVisibility(View.GONE);
            playButton.setVisibility(View.VISIBLE);
            pausePlayerForBtn();
        }

    }

    public void writeAndReadServer(final String message) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(IP, PORT)) {
                    Log.e("write method", "Сокет подключился");
                    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
                         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        Message msgInThread = handler.obtainMessage();
                        writer.println(message + '\n');
                        writer.flush();
                        Log.e("write method", "Отправили сообщение на сервер:" + message);
                        String r = reader.readLine();
                        Log.e("read method", "Получили сообщение с сервера: " + r);
                        msgInThread.obj = r;
                        handler.sendMessage(msgInThread);

                    }
                } catch (IOException e) {
                    Log.e("ошибка", "не подсоединилось - M367");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void startPlayer() {

        mediaPlayer.start();
    }

    private void pausePlayer() {
        mediaPlayer.pause();
    }

    private void pausePlayerForBtn() {
        mediaPlayer.stop();
        try {
            mediaPlayer.prepare();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void stopPlayer() {
        mediaPlayer.stop();
        try {
            mediaPlayer.prepare();
            mediaPlayer.seekTo(0);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying()) {
            stopPlayer();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //тут отправляем сообщение на сервер
        if (!wasOnPrepare) {
            duration = mediaPlayer.getDuration();
            writeAndReadServer("hcode = " + constHCode() + "/" + duration);
            //не отправляем короткий номер на сервер, т.к. он еще не сформирован
        }

        Log.e("Длина трека", duration + "");


        wasOnPrepare = true;
//        mediaPlayer.stop();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    //таймер - задержка в миллисекундах

    private static void timer(long t) {
        try {
            TimeUnit.MILLISECONDS.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private String constHCode(){
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        //если первый запуск приложения (хэш не формировался никогда)
        if (sPref.getString(HCODE_MAP_KEY, "").isEmpty()) {
            SharedPreferences.Editor ed = sPref.edit();
            // кладем в корзину  SharedPreference с ключом HCODE_MAP_KEY генерируемое значение хэша
            String hcode =  UUID.randomUUID().toString();
            ed.putString(HCODE_MAP_KEY, hcode);
            ed.commit();
            return hcode;
        }
        else
        {
            return sPref.getString(HCODE_MAP_KEY, "");
        }
    }
}
