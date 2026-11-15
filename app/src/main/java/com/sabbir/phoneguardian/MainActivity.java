
package com.sabbir.phoneguardian;

import android.app.Activity;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import android.widget.*;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import java.util.Locale;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private TextView status;
    private TextView answer;
    private Button voiceButton;

    private boolean ttsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createScreen();
        initializeVoice();
        requestPermissionsIfNeeded();
    }

    private void createScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(25, 35, 25, 35);
        root.setBackgroundColor(Color.rgb(9, 25, 20));

        TextView title = new TextView(this);
        title.setText("SABBIR AI");
        title.setTextSize(30);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("PHONE GUARDIAN");
        subtitle.setTextSize(17);
        subtitle.setTextColor(Color.rgb(60, 220, 150));
        subtitle.setGravity(Gravity.CENTER);

        TextView mic = new TextView(this);
        mic.setText("🎙️");
        mic.setTextSize(65);
        mic.setGravity(Gravity.CENTER);

        status = new TextView(this);
        status.setText("আপনার ব্যক্তিগত AI সহকারী");
        status.setTextColor(Color.WHITE);
        status.setTextSize(17);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 20, 10, 20);

        answer = new TextView(this);
        answer.setText("আমি আপনার নির্দেশের অপেক্ষায় আছি।");
        answer.setTextColor(Color.LTGRAY);
        answer.setTextSize(16);
        answer.setGravity(Gravity.CENTER);
        answer.setPadding(10, 20, 10, 20);

        voiceButton = new Button(this);
        voiceButton.setText("🎙 কথা বলুন");
        voiceButton.setTextSize(18);

        voiceButton.setOnClickListener(v -> startListening());

        Button notificationButton = new Button(this);
        notificationButton.setText("নোটিফিকেশন অনুমতি দিন");

        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            );
            startActivity(intent);
        });

        root.addView(title);
        root.addView(subtitle);
        root.addView(mic);
        root.addView(status);
        root.addView(answer);

        root.addView(voiceButton,
            new LinearLayout.LayoutParams(
                -1, -2
            )
        );

        root.addView(notificationButton,
            new LinearLayout.LayoutParams(
                -1, -2
            )
        );

        setContentView(root);
    }

    private void initializeVoice() {

        tts = new TextToSpeech(this, result -> {

            if (result == TextToSpeech.SUCCESS) {

                int language = tts.setLanguage(
                    new Locale("bn", "BD")
                );

                if (language == TextToSpeech.LANG_MISSING_DATA ||
                    language == TextToSpeech.LANG_NOT_SUPPORTED) {

                    tts.setLanguage(new Locale("bn"));
                }

                ttsReady = true;
            }
        });
    }

    private void requestPermissionsIfNeeded() {

        ArrayList<String> permissions = new ArrayList<>();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(
                permissions.toArray(new String[0]), 100
            );
        }
    }

    private void startListening() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissionsIfNeeded();
            status.setText("মাইক্রোফোন অনুমতি প্রয়োজন।");
            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("এই ফোনে Speech Recognition পাওয়া যাচ্ছে না।");
            return;
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(
            new RecognitionListener() {

                @Override
                public void onReadyForSpeech(Bundle params) {
                    status.setText("বলুন, আমি শুনছি...");
                }

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    status.setText("আপনার কথা বুঝছি...");
                }

                @Override
                public void onError(int error) {
                    status.setText("আবার চেষ্টা করুন।");
                }

                @Override
                public void onResults(Bundle results) {

                    ArrayList<String> matches =
                        results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        );

                    if (matches != null && !matches.isEmpty()) {
                        processCommand(matches.get(0));
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {}

                @Override
                public void onEvent(int eventType, Bundle params) {}
            }
        );

        Intent intent = new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "bn-BD"
        );

        speechRecognizer.startListening(intent);
    }

    private void processCommand(String command) {

        answer.setText("আপনি বলেছেন: " + command);

        if (command.contains("ফোন কোথায়")) {

            speak("ভাই, আমি এখানে! আমি এখানে!");

        } else if (command.contains("ব্যাটারি")) {

            android.content.IntentFilter filter =
                new android.content.IntentFilter(
                    android.content.Intent.ACTION_BATTERY_CHANGED
                );

            android.content.Intent battery =
                registerReceiver(null, filter);

            if (battery != null) {
                int level = battery.getIntExtra(
                    android.os.BatteryManager.EXTRA_LEVEL, -1
                );

                speak("ভাই, আপনার ফোনের ব্যাটারি " +
                    level + " শতাংশ।");
            }

        } else if (command.contains("নোটিফিকেশন")) {

            speak("ভাই, নোটিফিকেশন পড়ার ব্যবস্থা পরবর্তী ধাপে যুক্ত হবে।");

        } else {

            speak("ভাই, আপনার কথা শুনেছি। এই কাজের ব্যবস্থা এখনো তৈরি হচ্ছে।");
        }
    }

    private void speak(String message) {

        status.setText(message);

        if (ttsReady && tts != null) {
            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "SABBIR_AI"
            );
        }
    }

    @Override
    protected void onDestroy() {

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
    }
            
