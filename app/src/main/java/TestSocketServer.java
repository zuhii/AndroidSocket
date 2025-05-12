import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestSocketServer {
    public static void main(String[] args) {
        final int PORT = 12345;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버 시작, 포트: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),true);

                String inputLine = in.readLine();
                System.out.println("받은 메시지: " + inputLine);

                out.println("서버에서 받은 메시지: " + inputLine);

                Log.d("TestSocketServer", "받은 메시지: " + inputLine);

                clientSocket.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
