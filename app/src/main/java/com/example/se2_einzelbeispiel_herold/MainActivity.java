package com.example.se2_einzelbeispiel_herold;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MainActivity extends AppCompatActivity {

    // server info
    private static final String SERVER_IP = "se2-submission.aau.at";
    private static final int SERVER_PORT = 20080;

    // ui elements
    private EditText matrNrEditText;
    private TextView serverResponseTextView;
    private TextView resultTextView;
    private Button sendButton;

    private Button calculateButton;
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
        resultTextView = findViewById(R.id.resultTextView);
        sendButton = findViewById(R.id.sendButton);
        calculateButton = findViewById(R.id.calculateButton);

        // set sendButton behaviour
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String matrNr = matrNrEditText.getText().toString();

                sendMessageToServer(matrNr);
            }
        });

        // set calculateButton behaviour
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String matrNr = matrNrEditText.getText().toString();

                calculateResult(matrNr);
            }
        });
    }

    private void sendMessageToServer(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = null;
                try {
                    // Establish connection to server
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    Log.i("HELP", "CONNECTED");
                    displayServerResponse("Successfully connected :)");

                    // Reader/writer
                    OutputStream out = socket.getOutputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    // Send message
                    out.write(message.getBytes("UTF-8"));
                    out.flush();

                    // Wait for response
                    socket.setSoTimeout(3000);
                    // Read response
                    String serverResponse = in.readLine();

                    // Display response
                    if (serverResponse != null) {
                        displayServerResponse(serverResponse);
                    } else {
                        displayServerResponse("No Response :(");
                    }

                    // Close connection and streams
                    in.close();
                    out.close();
                    socket.close();

                } catch (SocketTimeoutException e) {
                    // Handle timeout exception
                    displayServerResponse("Timeout occurred while waiting for response :(");
                } catch (IOException e) {
                    // Display error message
                    displayServerResponse("Something went wrong :(");
                    e.printStackTrace();
                } finally {
                    // Close the socket
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private void displayServerResponse(String message) {
        runOnUiThread(() -> serverResponseTextView.setText(message));
    }

    private void calculateResult(String input) {
        displayResult(input);
    }

    private void displayResult(String message) {
        runOnUiThread(() -> resultTextView.setText(message));
    }
}