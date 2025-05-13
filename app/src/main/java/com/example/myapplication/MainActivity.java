package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView sendText;
    TextView serverText;
    LinearLayout sendLayout;

    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        sendText = findViewById(R.id.sendText);
        serverText = findViewById(R.id.serverText);
        sendLayout = findViewById(R.id.sendLayout);

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> {
            final String data = editText.getText().toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    send(data);
                }
            }).start();
        });

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    startServer();
                }
            }).start();

            startButton.setVisibility(View.GONE);
            sendLayout.setVisibility(View.VISIBLE);
        });
    }

    public void send(String data) {
        try {
            int portNumber = 5001;
            Socket socket = new Socket("localhost", portNumber);
            printClientLog("소켓 연결됨");
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(data);
            outputStream.flush();
            printClientLog("데이터 전송함");

            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            printClientLog("서버로부터 받음: " + inputStream.readObject());
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        try {
            int portNumber = 5001;

            ServerSocket server = new ServerSocket(portNumber);
            printServerLog("서버 시작함: " +portNumber);

            while (true) {
                Socket socket = server.accept();
                InetAddress clientHost = socket.getLocalAddress();
                int clientPort = socket.getPort();
                printServerLog("클라이언트 연결됨: " + clientHost + ":" + clientPort);

                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                Object obj = inputStream.readObject();
                printServerLog("데이터 받음: " + obj);

                String message = String.valueOf(obj);
                notification(message);

                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(obj + " from server");
                outputStream.flush();
                printServerLog("데이터 보냄");

                socket.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notification(String message) {
        String channelId = "message";
        String channelName = "메시지 알림";

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder bulider = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("새 메시지")
                .setContentText(message)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), bulider.build());
    }

    public void printClientLog(String data) {
        Log.d("MainActivity", data);

        handler.post(new Runnable() {
            @Override
            public void run() {
                sendText.append(data + "\n");
            }
        });
    }

    public void printServerLog(String data) {
        Log.d("MainActivity", data);

        handler.post(new Runnable() {
            @Override
            public void run() {
                serverText.append(data + "\n");
            }
        });
    }



}