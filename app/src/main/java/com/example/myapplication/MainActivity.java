package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.util.Output;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView sendText;
    TextView serverText;
    LinearLayout sendLayout;
    WebView webView;

    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS} ,1);
            }
        }

        editText = findViewById(R.id.editText);
        sendText = findViewById(R.id.sendText);
        serverText = findViewById(R.id.serverText);
        sendLayout = findViewById(R.id.sendLayout);
        webView = findViewById(R.id.webView);

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> {
            final String data = editText.getText().toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    send(data);
                    handler.post(() -> editText.setText(""));
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

        Button webViewButton = findViewById(R.id.webViewButton);
        webViewButton.setOnClickListener(v -> {
            webView.setVisibility(View.VISIBLE);

            webView.setWebViewClient(new WebViewClient());
            webView.setWebChromeClient(new WebChromeClient());
            webView.loadUrl("https://www.naver.com/");
        });
    }

    public void send(String data) {
        try {
            int portNumber = 5001;
            Socket socket = new Socket("localhost", portNumber);
            printClientLog("소켓 연결됨");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write(data + "\n");
            writer.flush();
            printClientLog("데이터 전송함");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printClientLog("서버로부터 받음: " + reader.readLine());
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
            printServerLog("서버 시작함: " + portNumber);

            while (true) {
                Socket socket = server.accept();
                InetAddress clientHost = socket.getLocalAddress();
                int clientPort = socket.getPort();
                printServerLog("클라이언트 연결됨: " + clientHost + ":" + clientPort);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputString = reader.readLine();
                printServerLog("데이터 받음: " + inputString);

                notification(inputString);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(inputString + " from server\n");
                writer.flush();
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