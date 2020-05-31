/*version c0.001*/

package ru.finik.dwclient.player;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
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
import java.net.SocketException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ru.finik.dwclient.R;
import ru.finik.dwclient.serverconnection.ServerConnectionActivity;

public class MainActivity extends Activity implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "Тестовое логирование: ";
    private int sesId;
    private int duration;
    ServiceConnection serviceConnection;
    MyService myService;
    public Handler handler;
    private String r;

    TextView someTextMemo;

    Button playButton;
    Button pauseButton;
    Drawable background;
    ProgressBar progressBar;
    Button openMusicFileBtn;
    Intent i;
    private MediaPlayer mediaPlayer;
    final int REQUESTCODE = 43544;
    public static final String CHANNEL_ID = "ru.finik.StayNotSoClose.my_channel";
    private int track;
    private final String IP = "192.168.0.165";
    private final int PORT = 8189;
    boolean wasOnPrepare = false;
    private int myState;
    TextView settbtn1;
    TextView settbtn2;
    String clientId;
    TextView lastMessage;
    Thread thread;
    long startTime;
    int deltaTime;


    private final static String FILE_NAME = "content.txt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //        Строчки ниже включать только для отладки в режиме тестирования программы
//        Intent intent;
//        intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
//        startActivity(intent);
        //выбираем папку
        //TODO реализовать запись clientId в файл
        clientId = UUID.randomUUID().toString();
//        Log.e(TAG, "Clnid = " + clientId);
//        writeToServer("Cl nid = " +  clientId);
//        startActivityForResult(Intent.createChooser(i,"укажите директорию для сохранения")
        myState = 0;
        someTextMemo = findViewById(R.id.someTextTV);
        someTextMemo.setText("");
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
        settbtn1 = findViewById(R.id.setbut1);
        settbtn2 = findViewById(R.id.setbut2);
        //Сколько прошло с начала мелодии
        progressBar = findViewById(R.id.progressBar);
        //делаем ее невидимой при загрузке
        progressBar.setVisibility(View.GONE);

//        btnStart.setBackground(getDrawable(R.drawable.red_selector));
//        background = btnStart.getBackground();
//        Log.e("background = ", background.toString());
        //делаем новый сервис для progressbar, да?
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
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
            @Override//метод, работающий в UI-потоке
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String strFromServer = (String) msg.obj;
//                Log.e(TAG, strFromServer + "");
                Log.e("Number", 1 + "");
//                textViewFromHandler.setText(s);

                switch (strFromServer.substring(0, strFromServer.lastIndexOf("/"))){
                    case "num":

                        break;
                    case "curtime":
                        String s = strFromServer.substring(strFromServer.lastIndexOf("/") + 1);
                        lastMessage.setText(s);
                        deltaTime = Math.toIntExact(System.currentTimeMillis() - startTime);
                        mediaPlayer.seekTo(Integer.parseInt(s));
                        Log.e("Сообщение на сервер и обратно дошло за(мс)", deltaTime + "");
                        Log.e("Сообщение", s + "");
                        startPlayer();
                        playButton.setVisibility(View.GONE);
                        pauseButton.setVisibility(View.VISIBLE);
                        break;
                   /* case "num":
                        break;
                    case "num":
                        break;
                    */
                }
            }


        };
//        lastMessage.setText(handler.obtainMessage().what);


    }
    //после выбора папки происходит

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case 43544:
//                Log.e(TAG, data.getData() + "");
//                Log.e(TAG, data + "");
//                Log.e(TAG, data.getPackage() + "");
                Log.e(TAG, "Путь выбран");
//                Log.e(TAG,  data.getData().getLastPathSegment());
//                Log.e(TAG,  data.getData().getPath());
                //имя файла в логе
//                Log.e(TAG,  data.getData().getLastPathSegment().substring(data.getData().getLastPathSegment().lastIndexOf('/') + 1));
                //выключаем кнопку выбора папки и включаем кнопку плэй и прогресс бар после выбора папка
                openMusicFileBtn.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);


                Uri myUri = data.getData();
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
    }

    public void onClickPlayBtn(View view) {
//        if(myService!=null || true){
//                    myService.play();
//                    try {
//                        mediaPlayer.prepare();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
        //send "Start ... " to server for play music on the client (seekto)

        startTime = System.currentTimeMillis();
        writeAndReadServer("Start = " + duration);

//            Log.e(TAG, "Play");


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

    public void onClickSeekTo(View view) {

        startPlayer();
        //writeToServer("getgt");
        mediaPlayer.seekTo(Integer.parseInt(lastMessage.getText().toString()));
        //  mediaPlayer.seekTo(25830);
    }

    public void onClickSetBtn1(View view) {
        if (myState == 0) myState = 1; //1
        else if (myState == 1) myState = 2; //2
        else if (myState == 2) myState = 0; //0
        Log.e("setstatus ", myState + "");
    }

    public void onClickSetBtn2(View view) {
        if (myState == 1) myState = 0;
        else if (myState == 2) myState = 3;
        else if (myState == 3) {
            Intent intent;
            intent = new Intent(MainActivity.this, ServerConnectionActivity.class);
            startActivity(intent);
        }
        Log.e("setstatus ", myState + "");
    }
    //TODO make this method returned String (may be)
    //TODO read method is not correct

    public void readFromServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(IP, PORT);) {
                    Log.e("read method", "сокет подключился");
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                        //TODO тут происходит ошибка
                        String r = reader.readLine();
                        Log.e("read method", "in = " + r.substring(r.length() - 10));
                        Log.e("read method", "in = TESTTTTTTTTTTTTTTTTTT");
                    } catch (SocketException s) {
                        s.printStackTrace();
                        Log.e("read method", "сокет закрыт");
                        Log.e("read method", s.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("read method", "Произошла IOException при чтении - 277");
                        Log.e("read method", e.getMessage());
                    }
                } catch (IOException e) {
                    Log.e("read method", "ошибка IOException - не подсоединилось - 281");
                    e.printStackTrace();
//
                }
            }
        });
        thread.start();
    }

    //write method is correct
    public void writeToServer(final String message) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(IP, PORT)) {

                    Log.e("write method", "Сокет подключился");
                    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())))) {
                        writer.println(message + '\n');
                        writer.flush();
                    }
                    Log.e("Отправили сообщение на сервер", message);
                } catch (IOException e) {
                    Log.e("ошибка", "не подсоединилось - MainAct сточка 321");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
//

    }
//TODO

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
                        Log.e("Отправили сообщение на сервер", message);
                        r = reader.readLine();
//                        String rsub =  r.substring(r.length() - 10);
                        Log.e("read method", "Получили сообщение с сервера: " + r);
                        msgInThread.obj = r;
                        r = null;
                        Log.e("Number", 2 + "");
                        handler.sendMessage(msgInThread);
//                        handler.sendEmptyMessage(3);

                    }
                } catch (IOException e) {
                    Log.e("ошибка", "не подсоединилось - M368");
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void startPlayer() {

        mediaPlayer.start();
    }

    private void pausePlayer(){
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


    public String getFileName(String url) {
        if (url.isEmpty()) {
            throw new IllegalArgumentException("Url can not be empty");
        }
        String fileName = null;
        String arrayString[] = url.split("/");
        if (arrayString.length > 1) {
            fileName = arrayString[arrayString.length - 1];
            return fileName;
        } else {
            throw new IllegalArgumentException("Url is not valid, url: " + url); //or change on something more smart
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //тут отправляем сообщение на сервер
        if (!wasOnPrepare) {
            duration = mediaPlayer.getDuration();
            writeAndReadServer("hcode = " + UUID.randomUUID().toString() + "/" + duration);
            //TODO - тут присваивает поле для тестов someTextMemo.setText("durat = " + duration);
        }

        Log.e("Длина трека", duration + "");


        wasOnPrepare = true;
//        mediaPlayer.stop();

    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
/*    private static void timer(long t) {
        Log.e("Осталось",  t/1000 + " секунд");
        while (t >= 1000) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
//                e.printStackTrace();
                }
            t -= 1000;
            Log.e("Осталось",  t/1000 + " секунд");
        }
        while (t > 0 && t < 1000) {
            try {
                TimeUnit.MILLISECONDS.sleep(t);
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }
    }*/

}
