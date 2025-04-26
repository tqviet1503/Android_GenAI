package com.example.genai;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.google.ai.client.generativeai.GenerativeModel;

import java.util.Random;

import io.noties.markwon.Markwon;

public class MainActivity extends AppCompatActivity {
    private TextView aiAdviceTextView;
    private TextView toggleAdviceButton;
    private boolean isAdviceExpanded = false;
    private TextView heartRateValue, spo2Value, temperatureValue, bloodPressureValue, caloriesValue, stepsValue, movingTimeValue, adviceText;
    private ProgressBar movingTimeProgressBar, caloriesProgressBar, stepsProgressBar;
    private int heartRate;
    private LineChart heartRateChart, systolicChart, temperatureChart, spo2Chart;
    private LineChart heartRateBigChart, systolicBigChart, temperatureBigChart, spo2BigChart;
    private HealthMetrics heartRateChartManager, systolicChartManager, temperatureChartManager, bloodOxygenChartManager;
    private HealthMetrics heartRateBigChartManager, systolicBigChartManager, temperatureBigChartManager, bloodOxygenBigChartManager;
    private int spo2;
    private float temperature;
    private int bloodPressure;
    private int movingTime, calories, steps;
    private Button analyzeButton, nurseAnalyzeButton;
    private ProgressBar loadingIndicator;
    private AIService aiService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // Generate random values
            heartRate = random.nextInt(41) + 60; // 60-100 BPM
            spo2 = random.nextInt(11) + 90; // 90-100%
            temperature = 36.0f + random.nextFloat() * 2.0f; // 36.0-38.0°C
            bloodPressure = 100 + random.nextInt(6) * 10; // Generates 100, 110, 120, ..., 150

            movingTime = random.nextInt(31); // Generates 0-30 mins
            calories = random.nextInt(301) + 100; // Generates 100-500 kcal
            steps = random.nextInt(6001); // Generates 0-6000 steps

            updateHealthMetrics();

            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize TextViews
        aiAdviceTextView = findViewById(R.id.aiAdvice);
        toggleAdviceButton = findViewById(R.id.toggleAdvice);
        adviceText = findViewById(R.id.adviceText);

        // get view references
        heartRateValue = findViewById(R.id.heartRateValue);
        spo2Value = findViewById(R.id.spo2Value);
        temperatureValue = findViewById(R.id.temperatureValue);
        bloodPressureValue = findViewById(R.id.bloodPressureValue);

        analyzeButton = findViewById(R.id.analyzeButton);
        nurseAnalyzeButton = findViewById(R.id.nurseAnalyzeButton);

        loadingIndicator = findViewById(R.id.loadingIndicator);
        movingTimeProgressBar = findViewById(R.id.movingTimeProgressBar);
        caloriesProgressBar = findViewById(R.id.caloriesProgressBar);
        stepsProgressBar = findViewById(R.id.stepsProgressBar);
        movingTimeValue = findViewById(R.id.movingTimeValue);
        caloriesValue = findViewById(R.id.caloriesValue);
        stepsValue = findViewById(R.id.stepsValue);

//        heartRateChart = findViewById(R.id.heartRateChart);
//        systolicChart = findViewById(R.id.bloodPressureChart);
//        temperatureChart = findViewById(R.id.temperatureChart);
//        spo2Chart = findViewById(R.id.bloodOxygenChart);

        heartRateBigChart = findViewById(R.id.heartRateBigChart);
        systolicBigChart = findViewById(R.id.bloodPressureBigChart);
        temperatureBigChart = findViewById(R.id.temperatureBigChart);
        spo2BigChart = findViewById(R.id.bloodOxygenBigChart);

        setupAdviceToggle();

        heartRateChartManager = new HealthMetrics(this, heartRateChart, "Heart Rate (BPM)", Color.parseColor("#FF9999"), false); // Pastel Red
        systolicChartManager = new HealthMetrics(this, systolicChart, "Systolic BP (mmHg)", Color.parseColor("#FFCC99"), false); // Pastel Orange
        temperatureChartManager = new HealthMetrics(this, temperatureChart, "Temperature (°C)", Color.parseColor("#B5EAD7"), false); // Pastel Green
        bloodOxygenChartManager = new HealthMetrics(this, spo2Chart, "Blood Oxygen (%)", Color.parseColor("#FBB6CE"), false); // Pastel Pink

        heartRateBigChartManager = new HealthMetrics(this, heartRateBigChart, "Heart Rate (BPM)", Color.parseColor("#FF9999"), true); // Pastel Red
        systolicBigChartManager = new HealthMetrics(this, systolicBigChart, "Systolic BP (mmHg)", Color.parseColor("#FFCC99"), true); // Pastel Orange
        temperatureBigChartManager = new HealthMetrics(this, temperatureBigChart, "Temperature (°C)", Color.parseColor("#B5EAD7"), true); // Pastel Green
        bloodOxygenBigChartManager = new HealthMetrics(this, spo2BigChart, "Blood Oxygen (%)", Color.parseColor("#FBB6CE"), true); // Pastel Pink

        // ai service object
        aiService = new AIService();

        analyzeButton.setOnClickListener(v -> analyzeHealthStatus());
        nurseAnalyzeButton.setOnClickListener(v -> nurseAdvice());
        handler.post(updateRunnable);
    }

    private void setupAdviceToggle() {
        aiAdviceTextView = findViewById(R.id.aiAdvice);
        toggleAdviceButton = findViewById(R.id.toggleAdvice);

        if (aiAdviceTextView == null || toggleAdviceButton == null) {
            return;
        }

        aiAdviceTextView.setMaxLines(3);

        toggleAdviceButton.setOnClickListener(v -> {
            isAdviceExpanded = !isAdviceExpanded;
            aiAdviceTextView.setMaxLines(isAdviceExpanded ? Integer.MAX_VALUE : 3);
            toggleAdviceButton.setText(isAdviceExpanded ? "Show Less" : "Show More");
        });
    }

    private void updateHealthMetrics() {
        heartRateValue.setText(heartRate + " BPM");
        spo2Value.setText(spo2 + "%");
        temperatureValue.setText(String.format("%.2f°C", temperature));
        int systolic = bloodPressure;
        int diastolic = 0;
        diastolic = systolic - (30 + random.nextInt(21));
        bloodPressureValue.setText(systolic + "/" + diastolic);
        movingTimeProgressBar.setProgress(movingTime);
        caloriesProgressBar.setProgress(calories);
        stepsProgressBar.setProgress(steps);

        movingTimeValue.setText(String.format("%d", movingTime));
        caloriesValue.setText(String.format("%d", calories));
        stepsValue.setText(String.format("%d", steps));

        heartRateChartManager.updateChart(heartRate);
        systolicChartManager.updateChart(systolic);
        temperatureChartManager.updateChart(temperature);
        bloodOxygenChartManager.updateChart(spo2);

        heartRateBigChartManager.updateChart(heartRate);
        systolicBigChartManager.updateChart(systolic);
        temperatureBigChartManager.updateChart(temperature);
        bloodOxygenBigChartManager.updateChart(spo2);
    }

    private void analyzeHealthStatus() {
        loadingIndicator.setVisibility(View.VISIBLE);
        aiAdviceTextView.setVisibility(View.GONE);
        adviceText.setText("Doctor Analysis");
        adviceText.setTextColor(Color.parseColor("#2196F3"));
        analyzeButton.setEnabled(false);

        String heartRate = heartRateValue.getText().toString();
        String spo2 = spo2Value.getText().toString();
        String temperature = temperatureValue.getText().toString();
        String bloodPressure = bloodPressureValue.getText().toString();
        String movingTime = String.valueOf(movingTimeProgressBar.getProgress());
        String calories = String.valueOf(caloriesProgressBar.getProgress());
        String steps = String.valueOf(stepsProgressBar.getProgress());

        aiService.analyzeHealthMetrics(heartRate, spo2, temperature, bloodPressure, movingTime, calories, steps)
            .addOnSuccessListener(response -> {
                String rawText = response.getText();
                Markwon markwon = Markwon.create(this);
                Spanned markdown = markwon.toMarkdown(rawText);
                aiAdviceTextView.setText(markdown);
                aiAdviceTextView.setVisibility(View.VISIBLE);

                loadingIndicator.setVisibility(View.GONE);
                analyzeButton.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                aiAdviceTextView.setText("Unable to generate advice at this time. Please try again.");
                aiAdviceTextView.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);
                analyzeButton.setEnabled(true);
            });
    }

    private void nurseAdvice() {
        loadingIndicator.setVisibility(View.VISIBLE);
        aiAdviceTextView.setVisibility(View.GONE);
        analyzeButton.setEnabled(false);
        adviceText.setText("Nurse Advice");
        adviceText.setTextColor(Color.parseColor("#e66386"));

        String heartRate = heartRateValue.getText().toString();
        String spo2 = spo2Value.getText().toString();
        String temperature = temperatureValue.getText().toString();
        String bloodPressure = bloodPressureValue.getText().toString();
        String movingTime = String.valueOf(movingTimeProgressBar.getProgress());
        String calories = String.valueOf(caloriesProgressBar.getProgress());
        String steps = String.valueOf(stepsProgressBar.getProgress());

        aiService.nurseAdvice(heartRate, spo2, temperature, bloodPressure, movingTime, calories, steps)
                .addOnSuccessListener(response -> {
                    String rawText = response.getText();
                    Markwon markwon = Markwon.create(this);
                    Spanned markdown = markwon.toMarkdown(rawText);
                    aiAdviceTextView.setText(markdown);
                    aiAdviceTextView.setVisibility(View.VISIBLE);

                    loadingIndicator.setVisibility(View.GONE);
                    analyzeButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    aiAdviceTextView.setText("Unable to generate advice at this time. Please try again.");
                    aiAdviceTextView.setVisibility(View.VISIBLE);
                    loadingIndicator.setVisibility(View.GONE);
                    analyzeButton.setEnabled(true);
                });
    }
}