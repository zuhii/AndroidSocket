package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_webview);
//
//        webView = findViewById(R.id.webView);
//
//        webView.setWebViewClient(new WebViewClient());
//        webView.setWebChromeClient(new WebChromeClient());
//
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUseWideViewPort(true);
//
//        webView.loadUrl("http://www.naver.com");

        setContentView(R.layout.activity_main);

        Button btnSocket = findViewById(R.id.btnSocket);
        btnSocket.setOnClickListener(v -> connectSocket());
    }

    private void connectSocket() {
        Log.d("connectSocket", "소켓 연결 클릭");
        new Thread(() -> {
            try {
                Log.d("connectSocket", "소켓 연결 스레드");
                Socket socket = new Socket("10.0.0.199",12456);
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                output.println("Hello Server");
                String response = input.readLine();

                runOnUiThread(() -> showNotification(this, response));

                socket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showNotification(Context context, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel("notification_channel", "알림", NotificationManager.IMPORTANCE_DEFAULT);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notification_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("서버 응답")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify(1, builder.build());
    }
}