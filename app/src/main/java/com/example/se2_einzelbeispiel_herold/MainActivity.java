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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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
        sendButton.setOnClickListener(v -> {
            String matrNr = matrNrEditText.getText().toString();
            sendMessageToServer(matrNr);
        });

        // set calculateButton behaviour
        calculateButton.setOnClickListener(v -> {
            String matrNr = matrNrEditText.getText().toString();
            calculateResult(matrNr);
        });
    }

    private void sendMessageToServer(String message) {
        new Thread(() -> {
            Socket socket = null;
            OutputStream out = null;
            BufferedReader in = null;
            try {
                // Establish connection to server
                socket = new Socket(SERVER_IP, SERVER_PORT);
                Log.i("HELP", "CONNECTED");
                displayServerResponse("Successfully connected :)");

                // Reader/writer
                out = socket.getOutputStream();
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.i("HELP", "SET UP READER/WRITER");

                // check message length before sending
                if(message == null || message.isEmpty()) {
                    displayServerResponse("Bitte MatrNr eingeben ;)");
                    return;
                }

                // convert matrnr to byte array
                int i = Integer.parseInt(message);
                ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
                buffer.putInt(i);
                byte[] bytes = buffer.array();

                // send message
                out.write(bytes);
                out.flush();
                Log.i("HELP", "MESSAGE SENT");

                // Wait for response
                socket.setSoTimeout(30000);
                // Read response
                String serverResponse = in.readLine();

                // Display response
                if (serverResponse != null) {
                    displayServerResponse(serverResponse);
                    Log.i("HELP", "MESSAGE RECEIVED");
                } else {
                    displayServerResponse("No Response :(");
                    Log.i("HELP", "MESSAGE TIMEOUT");
                }

                // Close connection and streams
                in.close();
                out.close();
                socket.close();
                Log.i("HELP", "NORMAL CLOSE");

            } catch (SocketTimeoutException e) {
                // Handle timeout exception
                displayServerResponse("Timeout occurred while waiting for response :(");
            } catch (IOException e) {
                // Display error message
                displayServerResponse("Something went wrong :(");
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("HELP", "FINALLY CLOSE");
            }
        }).start();
    }

    private void displayServerResponse(String message) {
        runOnUiThread(() -> serverResponseTextView.setText(message));
    }

    private void calculateResult(String input) {
        new Thread(() -> {
            if(input == null || input.isEmpty()) {
                displayResult("Bitte MatrNr eingeben ;)");
                return;
            }

            // convert input string to arraylist
            List<Integer> digits = new ArrayList<>();
            for (int i = 0; i < input.length(); i++) {
                digits.add(Character.getNumericValue(input.charAt(i)));
            }

            // if there's only one digit, stop here and return
            if(digits.size() == 1) {
                displayResult("Nur eine Ziffer :(");
                return;
            }

            String resultText = "";

            // compare each digit combination
            for (int i = 0; i < digits.size() - 1; i++) {
                for (int j = i + 1; j < digits.size(); j++) {
                    int num1 = digits.get(i);
                    int num2 = digits.get(j);

                    // calculate gcd
                    int gcd = calculateGCD(num1, num2);

                    if (gcd >= 2) {
                        resultText += "(" + i + ", " + j + ") ";
                    }
                }
            }

            if (resultText.isEmpty()) {
                displayResult("No pairs with gcd > 1 :(");
            } else {
                displayResult(resultText);
            }
        }).start();
    }

    // https://www.javatpoint.com/java-program-to-find-gcd-of-two-numbers
    private int calculateGCD(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    private void displayResult(String message) {
        runOnUiThread(() -> resultTextView.setText(message));
    }
}