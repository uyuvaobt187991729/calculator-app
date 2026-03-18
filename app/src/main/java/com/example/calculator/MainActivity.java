package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression, tvResult;
    private String currentInput = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean newInput = false;
    private boolean justCalculated = false;
    private double lastOperand = 0;
    private String lastOperator = "";
    private boolean hasPercent = false;
    private double lastPercentRatio = 0;
    private List<String> history = new ArrayList<>();
    private boolean scientificVisible = false;
    private View scientificPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
        scientificPanel = findViewById(R.id.scientificPanel);

        setupNumberButtons();
        setupOperatorButtons();
        setupSpecialButtons();
        setupScientificButtons();

        // Long press to copy result
        tvResult.setOnLongClickListener(v -> {
            String result = tvResult.getText().toString();
            if (!result.isEmpty() && !result.equals("0")) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("result", result);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied: " + result, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        findViewById(R.id.btnHistory).setOnClickListener(v -> showHistory());

        findViewById(R.id.btnScientific).setOnClickListener(v -> {
            scientificVisible = !scientificVisible;
            scientificPanel.setVisibility(scientificVisible ? View.VISIBLE : View.GONE);
        });
    }

    private void setupNumberButtons() {
        int[] ids = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                     R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int id : ids) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> {
                String digit = ((Button) v).getText().toString();
                if (justCalculated) {
                    currentInput = digit;
                    justCalculated = false;
                    hasPercent = false;
                } else if (newInput) {
                    currentInput = digit;
                    newInput = false;
                } else {
                    if (currentInput.equals("0")) currentInput = digit;
                    else currentInput += digit;
                }
                updateDisplay();
            });
        }
    }

    private void setupOperatorButtons() {
        int[] opIds = {R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv};
        String[] ops = {"+", "−", "×", "÷"};
        for (int i = 0; i < opIds.length; i++) {
            final String op = ops[i];
            findViewById(opIds[i]).setOnClickListener(v -> {
                if (!currentInput.isEmpty()) {
                    firstNumber = parseDouble(currentInput);
                }
                operator = op;
                lastOperator = op;
                hasPercent = false;
                lastPercentRatio = 0;
                newInput = true;
                justCalculated = false;
                tvExpression.setText(formatNumber(firstNumber) + " " + op);
            });
        }
    }

    private void setupSpecialButtons() {
        findViewById(R.id.btnEquals).setOnClickListener(v -> calculate());

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput = "";
            operator = "";
            firstNumber = 0;
            newInput = false;
            justCalculated = false;
            hasPercent = false;
            lastPercentRatio = 0;
            tvExpression.setText("");
            tvResult.setText("0");
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (!currentInput.isEmpty() && !justCalculated) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                tvResult.setText(currentInput.isEmpty() ? "0" : currentInput);
            }
        });

        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (newInput || justCalculated) {
                currentInput = "0.";
                newInput = false;
                justCalculated = false;
            } else if (!currentInput.contains(".")) {
                if (currentInput.isEmpty()) currentInput = "0";
                currentInput += ".";
            }
            tvResult.setText(currentInput);
        });

        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            if (!currentInput.isEmpty()) {
                double val = parseDouble(currentInput);
                if (!operator.isEmpty()) {
                    double pctVal = firstNumber * val / 100.0;
                    lastPercentRatio = val / 100.0;
                    hasPercent = true;
                    currentInput = formatNumber(pctVal);
                } else {
                    currentInput = formatNumber(val / 100.0);
                    hasPercent = false;
                }
                tvExpression.setText(formatNumber(firstNumber) + " " + operator + " " + val + "%");
                tvResult.setText(currentInput);
            }
        });

        findViewById(R.id.btnPlusMinus).setOnClickListener(v -> {
            if (!currentInput.isEmpty() && !currentInput.equals("0")) {
                if (currentInput.startsWith("-")) currentInput = currentInput.substring(1);
                else currentInput = "-" + currentInput;
                tvResult.setText(currentInput);
            }
        });

        findViewById(R.id.btnParens).setOnClickListener(v -> {
            long opens = currentInput.chars().filter(c -> c == '(').count();
            long closes = currentInput.chars().filter(c -> c == ')').count();
            if (opens == closes || currentInput.isEmpty()) {
                currentInput += "(";
            } else {
                currentInput += ")";
            }
            tvResult.setText(currentInput);
        });
    }

    private void calculate() {
        // Repeat last operation when = is pressed again
        if (justCalculated && !lastOperator.isEmpty()) {
            double current = parseDouble(tvResult.getText().toString());
            double result;
            if (hasPercent) {
                // Keep adding same % of current value each time
                double addition = current * lastPercentRatio;
                result = current + addition;
                String expr = formatNumber(current) + " + " + (lastPercentRatio * 100) + "% =";
                String resultStr = formatNumber(result);
                tvExpression.setText(expr);
                tvResult.setText(resultStr);
                history.add(0, expr + " " + resultStr);
                currentInput = resultStr;
            } else {
                result = applyOp(current, lastOperator, lastOperand);
                String expr = formatNumber(current) + " " + lastOperator + " " + formatNumber(lastOperand) + " =";
                String resultStr = formatNumber(result);
                tvExpression.setText(expr);
                tvResult.setText(resultStr);
                history.add(0, expr + " " + resultStr);
                currentInput = resultStr;
            }
            return;
        }

        if (operator.isEmpty() || currentInput.isEmpty()) return;

        double secondNumber = parseDouble(currentInput);
        lastOperand = secondNumber;
        lastOperator = operator;

        double result = applyOp(firstNumber, operator, secondNumber);

        String expr = formatNumber(firstNumber) + " " + operator + " " + currentInput + " =";
        String resultStr = formatNumber(result);
        tvExpression.setText(expr);
        tvResult.setText(resultStr);
        history.add(0, expr + " " + resultStr);

        currentInput = resultStr;
        firstNumber = result;
        operator = "";
        newInput = false;
        justCalculated = true;
    }

    private double applyOp(double a, String op, double b) {
        switch (op) {
            case "+": return a + b;
            case "−": return a - b;
            case "×": return a * b;
            case "÷": return b != 0 ? a / b : Double.NaN;
            case "^": return Math.pow(a, b);
            default: return b;
        }
    }

    private void setupScientificButtons() {
        findViewById(R.id.btnSin).setOnClickListener(v -> applyScientific("sin"));
        findViewById(R.id.btnCos).setOnClickListener(v -> applyScientific("cos"));
        findViewById(R.id.btnTan).setOnClickListener(v -> applyScientific("tan"));
        findViewById(R.id.btnSqrt).setOnClickListener(v -> applyScientific("√"));
        findViewById(R.id.btnSquare).setOnClickListener(v -> applyScientific("x²"));
        findViewById(R.id.btnLog).setOnClickListener(v -> applyScientific("log"));
        findViewById(R.id.btnLn).setOnClickListener(v -> applyScientific("ln"));
        findViewById(R.id.btnInverse).setOnClickListener(v -> applyScientific("1/x"));
        findViewById(R.id.btnFactorial).setOnClickListener(v -> applyScientific("!"));
        findViewById(R.id.btnPi).setOnClickListener(v -> {
            currentInput = "3.14159265358979";
            tvResult.setText(currentInput);
            newInput = false;
            justCalculated = false;
        });
        findViewById(R.id.btnE).setOnClickListener(v -> {
            currentInput = "2.71828182845905";
            tvResult.setText(currentInput);
            newInput = false;
            justCalculated = false;
        });
        findViewById(R.id.btnPow).setOnClickListener(v -> {
            if (!currentInput.isEmpty()) {
                firstNumber = parseDouble(currentInput);
                operator = "^";
                lastOperator = "^";
                newInput = true;
                tvExpression.setText(formatNumber(firstNumber) + " ^");
            }
        });
    }

    private void applyScientific(String func) {
        double val = currentInput.isEmpty() ?
                parseDouble(tvResult.getText().toString()) : parseDouble(currentInput);
        double result;
        switch (func) {
            case "sin":  result = Math.sin(Math.toRadians(val)); break;
            case "cos":  result = Math.cos(Math.toRadians(val)); break;
            case "tan":  result = Math.tan(Math.toRadians(val)); break;
            case "√":    result = Math.sqrt(val); break;
            case "x²":   result = val * val; break;
            case "log":  result = Math.log10(val); break;
            case "ln":   result = Math.log(val); break;
            case "1/x":  result = val != 0 ? 1.0 / val : Double.NaN; break;
            case "!":    result = factorial((int) val); break;
            default:     result = val;
        }
        String expr = func + "(" + formatNumber(val) + ") =";
        String resultStr = Double.isNaN(result) ? "Error" : formatNumber(result);
        tvExpression.setText(expr);
        tvResult.setText(resultStr);
        history.add(0, expr + " " + resultStr);
        currentInput = resultStr;
        justCalculated = true;
    }

    private double factorial(int n) {
        if (n < 0) return Double.NaN;
        if (n == 0 || n == 1) return 1;
        double r = 1;
        for (int i = 2; i <= Math.min(n, 20); i++) r *= i;
        return r;
    }

    private void showHistory() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_history, null);
        dialog.setContentView(view);

        RecyclerView rv = view.findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        if (history.isEmpty()) {
            view.findViewById(R.id.tvNoHistory).setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.tvNoHistory).setVisibility(View.GONE);
            rv.setAdapter(new HistoryAdapter(history, item -> {
                String[] parts = item.split(" ");
                currentInput = parts[parts.length - 1];
                tvResult.setText(currentInput);
                justCalculated = true;
                dialog.dismiss();
            }));
        }

        view.findViewById(R.id.btnClearHistory).setOnClickListener(v -> {
            history.clear();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateDisplay() {
        tvResult.setText(currentInput.isEmpty() ? "0" : currentInput);
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.replace("−", "-")); }
        catch (Exception e) { return 0; }
    }

    private String formatNumber(double d) {
        if (Double.isNaN(d)) return "Error";
        if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15)
            return String.valueOf((long) d);
        return String.format("%.10f", d).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
        interface OnItemClick { void onClick(String item); }
        List<String> items;
        OnItemClick listener;
        HistoryAdapter(List<String> items, OnItemClick l) { this.items = items; this.listener = l; }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            h.tv.setText(items.get(pos));
            h.itemView.setOnClickListener(v -> listener.onClick(items.get(pos)));
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) { super(v); tv = v.findViewById(R.id.tvHistoryItem); }
        }
    }
}
