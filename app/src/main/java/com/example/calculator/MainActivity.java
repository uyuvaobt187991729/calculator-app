package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView display;
    private String currentInput = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean newInput = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);

        int[] numberButtonIds = {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(v -> {
                Button btn = (Button) v;
                if (newInput) {
                    currentInput = "";
                    newInput = false;
                }
                currentInput += btn.getText().toString();
                display.setText(currentInput);
            });
        }

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (!currentInput.contains(".")) {
                if (currentInput.isEmpty()) currentInput = "0";
                currentInput += ".";
                display.setText(currentInput);
            }
        });

        View.OnClickListener opListener = v -> {
            Button btn = (Button) v;
            if (!currentInput.isEmpty()) {
                firstNumber = Double.parseDouble(currentInput);
            }
            operator = btn.getText().toString();
            newInput = true;
        };

        findViewById(R.id.btnAdd).setOnClickListener(opListener);
        findViewById(R.id.btnSub).setOnClickListener(opListener);
        findViewById(R.id.btnMul).setOnClickListener(opListener);
        findViewById(R.id.btnDiv).setOnClickListener(opListener);

        findViewById(R.id.btnEquals).setOnClickListener(v -> {
            if (!operator.isEmpty() && !currentInput.isEmpty()) {
                double secondNumber = Double.parseDouble(currentInput);
                double result = 0;
                switch (operator) {
                    case "+": result = firstNumber + secondNumber; break;
                    case "−": result = firstNumber - secondNumber; break;
                    case "×": result = firstNumber * secondNumber; break;
                    case "÷":
                        if (secondNumber != 0) result = firstNumber / secondNumber;
                        else { display.setText("Error"); return; }
                        break;
                }
                if (result == Math.floor(result) && !Double.isInfinite(result)) {
                    currentInput = String.valueOf((long) result);
                } else {
                    currentInput = String.valueOf(result);
                }
                display.setText(currentInput);
                operator = "";
                newInput = true;
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput = "";
            operator = "";
            firstNumber = 0;
            newInput = false;
            display.setText("0");
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (!currentInput.isEmpty()) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                display.setText(currentInput.isEmpty() ? "0" : currentInput);
            }
        });
    }
          }
