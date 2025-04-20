package com.example.genai;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AIService {
    private static final String API_KEY = "AIzaSyBJkZFe_bAULrQku-ObSaoURfFqAjW5brE";
    private final GenerativeModelFutures model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public AIService() {
        GenerativeModel baseModel = new GenerativeModel("gemini-2.0-flash", API_KEY);
        this.model = GenerativeModelFutures.from(baseModel);
    }

    public Task<GenerateContentResponse> analyzeHealthMetrics(String heartRate, String spo2, String temperature, String bloodPressure,
                                                              String movingTime, String calories, String steps) {
        String prompt = String.format(
                "Use the tone like a real doctor, your name is Jack. evaluate the following health metrics for a healthy adult and provide concise, actionable advice on potential health concerns or lifestyle improvements: " +
                        "Heart Rate: %s BPM, SpO2: %s, Body Temperature: %s, Blood Pressure: %s mmHg, Moving Time: %s minutes (daily), Calories Burned: %s kcal (daily), Steps: %s (daily)",
                heartRate, spo2, temperature, bloodPressure, movingTime, calories, steps
        );

        TaskCompletionSource<GenerateContentResponse> tcs = new TaskCompletionSource<>();

        executor.execute(() -> {
            try {
                // Create a Content object with the prompt
                com.google.ai.client.generativeai.type.Content content =
                        new com.google.ai.client.generativeai.type.Content.Builder()
                                .addText(prompt)
                                .build();

                // Pass the Content object to generateContent
                GenerateContentResponse response;
                response = model.generateContent(content).get();
                tcs.setResult(response);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });

        return tcs.getTask();
    }

    public Task<GenerateContentResponse> nurseAdvice(String heartRate, String spo2, String temperature, String bloodPressure,
                                                              String movingTime, String calories, String steps) {
        String prompt = String.format(
                "Use the tone like a real nurse, your name is Sarah, greetings the patient before answer. As a nurse providing caring and practical guidance, review the following health metrics for a healthy adult and offer concise, actionable advice tailored to their needs. Focus on what to eat, what activities to do, and lifestyle habits to improve their health: " +
                        "Heart Rate: %s BPM, SpO2: %s%%, Body Temperature: %s, Blood Pressure: %s mmHg, Moving Time: %s minutes (daily), Calories Burned: %s kcal (daily), Steps: %s (daily). " +
                        "Suggest specific foods to support their health, recommend daily activities or exercises, and advise on lifestyle changes like sleep, hydration, or stress management.",
                heartRate, spo2, temperature, bloodPressure, movingTime, calories, steps
        );

        TaskCompletionSource<GenerateContentResponse> tcs = new TaskCompletionSource<>();

        executor.execute(() -> {
            try {
                // Create a Content object with the prompt
                com.google.ai.client.generativeai.type.Content content =
                        new com.google.ai.client.generativeai.type.Content.Builder()
                                .addText(prompt)
                                .build();

                // Pass the Content object to generateContent
                GenerateContentResponse response;
                response = model.generateContent(content).get();
                tcs.setResult(response);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });

        return tcs.getTask();
    }
}