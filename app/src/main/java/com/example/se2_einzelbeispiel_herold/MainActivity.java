package com.example.se2_einzelbeispiel_herold;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    // server info
    private static final String SERVER_IP = "se2-submission.aau.at";
    private static final int SERVER_PORT = 20080;

    // ui elements
    private EditText matrNrEditText;
    private TextView serverResponseTextView;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // bind ui elements
        matrNrEditText = findViewById(R.id.matrNrEditText);
        serverResponseTextView = findViewById(R.id.serverResponseTextView);
        sendButton = findViewById(R.id.sendButton);

        // set sendButton behaviour
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String matrNr = matrNrEditText.getText().toString();

                sendMessageToServer(matrNr);
            }
        });
    }

    private void sendMessageToServer(String message) {
        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverResponseTextView.setText(message);
            }
        });
        */

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // establish connection to server
                    Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                    displayServerResponse("Successfully connected :)");

                    // close connection
                    socket.close();
                } catch (IOException e) {
                    // display error message
                    displayServerResponse("Failed to connect to the server :(");
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }

    private void displayServerResponse(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverResponseTextView.setText(message);
            }
        });
    }
}