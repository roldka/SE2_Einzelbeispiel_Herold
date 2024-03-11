package com.example.se2_einzelbeispiel_herold;

import android.os.Bundle;
import android.util.Log;
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
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
        // solve task 4 (12140272 % 7 = 4)
        calculateButton.setOnClickListener(v -> {
            String matrNr = matrNrEditText.getText().toString();
            calculateResult(matrNr);
        });
    }

    private void sendMessageToServer(String message) {
        new Thread(() -> {
            Socket socket = null;
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                // establish connection to server
                socket = new Socket(SERVER_IP, SERVER_PORT);
                Log.i("HELP", "CONNECTED");
                displayServerResponse("Successfully connected :)");

                // reader/writer
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.i("HELP", "SET UP READER/WRITER");

                // check message length before sending
                if(message == null || message.isEmpty()) {
                    displayServerResponse("Bitte MatrNr eingeben ;)");
                    return;
                }

                // send message
                out.println(message);
                Log.i("HELP", "MESSAGE SENT");

                // wait for response
                socket.setSoTimeout(10000);
                // read response
                String serverResponse = in.readLine();

                // display response
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
                // handle timeout exception
                displayServerResponse("Timeout :(");
            } catch (IOException e) {
                // display error message
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
                    out.close();
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
                displayResult("Keine Paare mit gcd > 1 :(");
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