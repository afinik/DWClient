package ru.finik.dwclient.serverconnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ru.finik.dwclient.R;
import ru.finik.dwclient.player.MainActivity;

public class ServerConnectionActivity extends AppCompatActivity {
    MainActivity mainActivity;
    private static final String TAG = "Тестовое логирование";
    EditText textToSend;
    EditText editIP;
    EditText editPort;
    Button startSocket;

    private static String ip;
    private static int port;

    private static final String IP = "192.168.0.165";
    private static final int PORT = 8189;
    private Thread thread;
    private Socket socket;

    boolean gameon;
    TextView textViewFromHandler;
    String s1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connection);
        setTitle("TESTING SERVICE");


        textViewFromHandler = findViewById(R.id.textFromHandler);
        editIP = findViewById(R.id.ipAddress);
        editPort = findViewById(R.id.port);
        editIP.setText(IP);
        textToSend = findViewById(R.id.etMemoToSend);
        textToSend.setText("hcode = " + UUID.randomUUID().toString() + "/" +
                mainActivity.getDuration());
        editPort.setText(PORT + "");
        startSocket = findViewById(R.id.btnStartSend);
        mainActivity = new MainActivity();

        mainActivity.handler = new Handler(){
            @Override//метод, работающий в UI-потоке
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String s = (String) msg.obj;
                Log.e(TAG, s);
                    textViewFromHandler.setText(s);
            }
        };

    }


    public void onClickServ(View v) {
        switch (v.getId()) {
            case R.id.btnStartSend:
                mainActivity.setDuration(234512);
                mainActivity.writeAndReadServer(textToSend.getText().toString());
                Toast.makeText(getApplicationContext(), "Data sent and receive", Toast.LENGTH_LONG).show();
                break;

        }
    }


    //таймер - задержка в миллисекундах
    private static void timer(long t) {
        while (t > 0) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            t -= 1;
            Log.e("Осталось",  t + " секунд");
        }
    }




}
