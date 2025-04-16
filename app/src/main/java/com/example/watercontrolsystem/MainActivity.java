package com.example.watercontrolsystem;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private TextView connectionStatusText, waterLevelText, desiredWaterLevelText;
    private TextView flowVolumeText, desiredFlowVolumeText, flowRateText, desiredFlowRateText;
    private TextView motorPwmText;
    private Button connectButton, increaseWaterBtn, decreaseWaterBtn;
    private Button increaseVolumeBtn, decreaseVolumeBtn, increaseFlowRateBtn, decreaseFlowRateBtn;
    private Button increasePwmBtn, decreasePwmBtn, resetButton;
    private ToggleButton waterLevelToggle, flowVolumeToggle, flowRateToggle, motorToggle;
    
    // Custom views
    private CircularProgressView waterLevelProgress, flowVolumeProgress, flowRateProgress, motorPwmProgress;
    private WaveView waterWaveView;
    private Card3DView waterLevelCard, flowVolumeCard, flowRateCard, motorControlCard;

    // Network variables
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executorService = Executors.newFixedThreadPool(2); // Changed to fixed thread pool
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isConnected = false;

    // System state variables
    private int desiredWaterLevelPercent = 0;
    private float desiredFlowVolume = 0;
    private float desiredFlowRate = 0;
    private int currentPWM = 150;
    private boolean waterLevelActive = false;
    private boolean flowVolumeActive = false;
    private boolean flowRateControlActive = false;
    private boolean motorPWMOn = false;

    private static final String TAG = "WaterControlSystem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        initViews();

        // Set up button click listeners
        setupListeners();
        
        // Initialize circular progress views
        initProgressViews();
        
        // Run initial animations
        runInitialAnimations();
    }

    private void initViews() {
        connectionStatusText = findViewById(R.id.connectionStatusText);
        waterLevelText = findViewById(R.id.waterLevelText);
        desiredWaterLevelText = findViewById(R.id.desiredWaterLevelText);
        flowVolumeText = findViewById(R.id.flowVolumeText);
        desiredFlowVolumeText = findViewById(R.id.desiredFlowVolumeText);
        flowRateText = findViewById(R.id.flowRateText);
        desiredFlowRateText = findViewById(R.id.desiredFlowRateText);
        motorPwmText = findViewById(R.id.motorPwmText);

        connectButton = findViewById(R.id.connectButton);
        increaseWaterBtn = findViewById(R.id.increaseWaterBtn);
        decreaseWaterBtn = findViewById(R.id.decreaseWaterBtn);
        increaseVolumeBtn = findViewById(R.id.increaseVolumeBtn);
        decreaseVolumeBtn = findViewById(R.id.decreaseVolumeBtn);
        increaseFlowRateBtn = findViewById(R.id.increaseFlowRateBtn);
        decreaseFlowRateBtn = findViewById(R.id.decreaseFlowRateBtn);
        increasePwmBtn = findViewById(R.id.increasePwmBtn);
        decreasePwmBtn = findViewById(R.id.decreasePwmBtn);
        resetButton = findViewById(R.id.resetButton);

        waterLevelToggle = findViewById(R.id.waterLevelToggle);
        flowVolumeToggle = findViewById(R.id.flowVolumeToggle);
        flowRateToggle = findViewById(R.id.flowRateToggle);
        motorToggle = findViewById(R.id.motorToggle);
        
        // Initialize custom views
        waterLevelProgress = findViewById(R.id.waterLevelProgress);
        flowVolumeProgress = findViewById(R.id.flowVolumeProgress);
        flowRateProgress = findViewById(R.id.flowRateProgress);
        motorPwmProgress = findViewById(R.id.motorPwmProgress);
        waterWaveView = findViewById(R.id.waterWaveView);
        
        // Get card views
        waterLevelCard = findViewById(R.id.waterLevelCard);
        flowVolumeCard = findViewById(R.id.flowVolumeCard);
        flowRateCard = findViewById(R.id.flowRateCard);
        motorControlCard = findViewById(R.id.motorControlCard);
    }
    
    private void initProgressViews() {
        // Set initial progress values
        waterLevelProgress.setProgress(0, false);
        flowVolumeProgress.setProgress(0, false);
        flowRateProgress.setProgress(0, false);
        
        // Calculate motor pwm percentage
        float pwmPercent = (currentPWM / 255f) * 100f;
        motorPwmProgress.setProgress(pwmPercent, false);
        
        // Set colors
        waterLevelProgress.setProgressColor(getResources().getColor(R.color.accentColor));
        flowVolumeProgress.setProgressColor(getResources().getColor(R.color.accentColorSecondary));
        flowRateProgress.setProgressColor(getResources().getColor(R.color.accentColorLight));
        
        // Set water level in wave view
        waterWaveView.setWaterLevel(0f);
    }
    
    private void runInitialAnimations() {
        // Delayed animation sequence
        new Handler().postDelayed(() -> {
            animateCard(waterLevelCard, 100);
        }, 300);
        
        new Handler().postDelayed(() -> {
            animateCard(flowVolumeCard, 200);
        }, 500);
        
        new Handler().postDelayed(() -> {
            animateCard(flowRateCard, 300);
        }, 700);
        
        new Handler().postDelayed(() -> {
            animateCard(motorControlCard, 400);
        }, 900);
    }
    
    private void animateCard(Card3DView card, long delay) {
        card.setRotationY(-15f);
        card.setAlpha(0.5f);
        
        ValueAnimator rotateAnimator = ValueAnimator.ofFloat(-15f, 0f);
        rotateAnimator.setDuration(800);
        rotateAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            card.setRotationY(val);
        });
        
        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0.5f, 1f);
        alphaAnimator.setDuration(800);
        alphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimator.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            card.setAlpha(val);
        });
        
        new Handler().postDelayed(() -> {
            rotateAnimator.start();
            alphaAnimator.start();
        }, delay);
    }

    private void setupListeners() {
        connectButton.setOnClickListener(v -> toggleConnection());

        // Water Level Control
        increaseWaterBtn.setOnClickListener(v -> {
            desiredWaterLevelPercent = Math.min(desiredWaterLevelPercent + 5, 100);
            updateWaterLevelUI();
            sendCommand("INC_WATER_LEVEL");
        });

        decreaseWaterBtn.setOnClickListener(v -> {
            desiredWaterLevelPercent = Math.max(desiredWaterLevelPercent - 5, 0);
            updateWaterLevelUI();
            sendCommand("DEC_WATER_LEVEL");
        });

        waterLevelToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            waterLevelActive = isChecked;
            sendCommand("TOGGLE_WATER_LEVEL");
        });

        // Flow Volume Control
        increaseVolumeBtn.setOnClickListener(v -> {
            desiredFlowVolume += 1.0f;
            updateFlowVolumeUI();
            sendCommand("INC_FLOW_VOLUME");
        });

        decreaseVolumeBtn.setOnClickListener(v -> {
            desiredFlowVolume = Math.max(desiredFlowVolume - 1.0f, 0.0f);
            updateFlowVolumeUI();
            sendCommand("DEC_FLOW_VOLUME");
        });

        flowVolumeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            flowVolumeActive = isChecked;
            sendCommand("TOGGLE_FLOW_VOLUME");
        });

        // Flow Rate Control
        increaseFlowRateBtn.setOnClickListener(v -> {
            desiredFlowRate = Math.min(desiredFlowRate + 0.5f, 10.0f);
            updateFlowRateUI();
            sendCommand("INC_FLOW_RATE");
        });

        decreaseFlowRateBtn.setOnClickListener(v -> {
            desiredFlowRate = Math.max(desiredFlowRate - 0.5f, 0.0f);
            updateFlowRateUI();
            sendCommand("DEC_FLOW_RATE");
        });

        flowRateToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            flowRateControlActive = isChecked;
            sendCommand("TOGGLE_FLOW_RATE");
        });

        // Motor Control
        increasePwmBtn.setOnClickListener(v -> {
            currentPWM = Math.min(currentPWM + 5, 255);
            updateMotorControlUI();
            sendCommand("INC_PWM");
        });

        decreasePwmBtn.setOnClickListener(v -> {
            currentPWM = Math.max(currentPWM - 5, 0);
            updateMotorControlUI();
            sendCommand("DEC_PWM");
        });

        motorToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            motorPWMOn = isChecked;
            sendCommand("TOGGLE_MOTOR");
        });

        // Reset Button
        resetButton.setOnClickListener(v -> {
            resetSystem();
            sendCommand("RESET_ALL");
        });
    }

    private void toggleConnection() {
        if (isConnected) {
            disconnectFromESP32();
        } else {
            connectToESP32();
        }
    }

    private void connectToESP32() {
        executorService.execute(() -> {
            try {
                // ESP32 WiFi hotspot details
                String host = "192.168.4.1"; // Default ESP32 softAP IP
                int port = 80;

                Log.d(TAG, "Attempting to connect to " + host + ":" + port);
                socket = new Socket(host, port);
                socket.setSoTimeout(5000); // Set timeout for socket operations
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                isConnected = true;
                handler.post(() -> {
                    connectionStatusText.setText("CONNECTED");
                    connectionStatusText.setTextColor(getResources().getColor(R.color.connected));
                    connectButton.setText("DISCONNECT");
                    Toast.makeText(MainActivity.this, "Connected to ESP32", Toast.LENGTH_SHORT).show();
                });

                // Start listening for data from ESP32
                startDataListener();

            } catch (IOException e) {
                Log.e(TAG, "Connection error: " + e.getMessage());
                handler.post(() -> {
                    connectionStatusText.setText("CONNECTION FAILED");
                    connectionStatusText.setTextColor(getResources().getColor(R.color.disconnected));
                    Toast.makeText(MainActivity.this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                isConnected = false;
            }
        });
    }

    private void disconnectFromESP32() {
        executorService.execute(() -> {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error disconnecting: " + e.getMessage());
            }

            isConnected = false;
            handler.post(() -> {
                connectionStatusText.setText("DISCONNECTED");
                connectionStatusText.setTextColor(getResources().getColor(R.color.disconnected));
                connectButton.setText("CONNECT");
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void startDataListener() {
        executorService.execute(() -> {
            try {
                while (isConnected) {
                    if (in != null && in.ready()) {
                        String data = in.readLine();
                        if (data != null) {
                            Log.d(TAG, "Received: " + data);
                            processIncomingData(data);
                        }
                    }
                    Thread.sleep(100); // Small delay to prevent CPU overload
                }
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Data listener error: " + e.getMessage());
                handler.post(() -> {
                    connectionStatusText.setText("DISCONNECTED");
                    connectionStatusText.setTextColor(getResources().getColor(R.color.disconnected));
                    connectButton.setText("CONNECT");
                    Toast.makeText(MainActivity.this, "Connection lost", Toast.LENGTH_SHORT).show();
                });
                isConnected = false;
            }
        });
    }

    private void processIncomingData(String data) {
        // When data comes in from ESP32
        try {
            // Split the data into components based on comma separation
            String[] parts = data.split(",");
            if (parts.length >= 5) {
                // Extract values
                int waterLevel = Integer.parseInt(parts[0].trim());
                float flowVolume = Float.parseFloat(parts[1].trim());
                float flowRate = Float.parseFloat(parts[2].trim());
                int pwmValue = Integer.parseInt(parts[3].trim());
                boolean systemRunning = Integer.parseInt(parts[4].trim()) == 1;

                // Update UI with received values
                handler.post(() -> {
                    waterLevelText.setText(waterLevel + "%");
                    flowVolumeText.setText(String.format("%.1f L", flowVolume));
                    flowRateText.setText(String.format("%.1f L/min", flowRate));
                    motorPwmText.setText(pwmValue + "/255");
                    
                    // Update progress views with animation
                    waterLevelProgress.setProgress(waterLevel, true);
                    float volumePercent = Math.min((flowVolume / 10f) * 100f, 100f);
                    flowVolumeProgress.setProgress(volumePercent, true);
                    flowVolumeProgress.setProgressText(String.format("%.1f L", flowVolume));
                    
                    float ratePercent = Math.min((flowRate / 10f) * 100f, 100f);
                    flowRateProgress.setProgress(ratePercent, true);
                    flowRateProgress.setProgressText(String.format("%.1f L/min", flowRate));
                    
                    float pwmPercent = (pwmValue / 255f) * 100f;
                    motorPwmProgress.setProgress(pwmPercent, true);
                    motorPwmProgress.setProgressText(pwmValue + "");
                    
                    // Update water wave animation
                    waterWaveView.setWaterLevel(waterLevel / 100f);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing data: " + e.getMessage());
        }
    }

    private void sendCommand(String command) {
        if (isConnected && out != null) {
            executorService.execute(() -> {
                try {
                    Log.d(TAG, "Sending command: " + command);
                    out.println(command);
                    out.flush(); // Ensure the command is sent immediately
                } catch (Exception e) {
                    Log.e(TAG, "Error sending command: " + e.getMessage());
                    handler.post(() -> {
                        Toast.makeText(MainActivity.this, "Failed to send command", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Log.d(TAG, "Cannot send command - not connected");
            handler.post(() -> {
                Toast.makeText(MainActivity.this, "Not connected to ESP32", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateWaterLevelUI() {
        desiredWaterLevelText.setText(desiredWaterLevelPercent + "%");
        // Update the circular progress and wave view
        waterLevelProgress.setProgress(desiredWaterLevelPercent, true);
        waterWaveView.setWaterLevel(desiredWaterLevelPercent / 100f);
    }

    private void updateFlowVolumeUI() {
        desiredFlowVolumeText.setText(String.format("%.1f L", desiredFlowVolume));
        // Update the circular progress
        float percent = Math.min((desiredFlowVolume / 10f) * 100f, 100f);
        flowVolumeProgress.setProgress(percent, true);
        flowVolumeProgress.setProgressText(String.format("%.1f L", desiredFlowVolume));
    }

    private void updateFlowRateUI() {
        desiredFlowRateText.setText(String.format("%.1f L/min", desiredFlowRate));
        // Update the circular progress
        float percent = Math.min((desiredFlowRate / 10f) * 100f, 100f);
        flowRateProgress.setProgress(percent, true);
        flowRateProgress.setProgressText(String.format("%.1f L/min", desiredFlowRate));
    }

    private void updateMotorControlUI() {
        motorPwmText.setText(currentPWM + "/255");
        // Update the circular progress
        float percent = (currentPWM / 255f) * 100f;
        motorPwmProgress.setProgress(percent, true);
        motorPwmProgress.setProgressText(currentPWM + "");
    }

    private void resetSystem() {
        desiredWaterLevelPercent = 0;
        desiredFlowVolume = 0;
        desiredFlowRate = 0;
        currentPWM = 150;
        waterLevelActive = false;
        flowVolumeActive = false;
        flowRateControlActive = false;
        motorPWMOn = false;

        updateWaterLevelUI();
        updateFlowVolumeUI();
        updateFlowRateUI();
        updateMotorControlUI();

        waterLevelToggle.setChecked(false);
        flowVolumeToggle.setChecked(false);
        flowRateToggle.setChecked(false);
        motorToggle.setChecked(false);
        
        // Update UI elements
        waterLevelProgress.setProgress(0, true);
        flowVolumeProgress.setProgress(0, true);
        flowRateProgress.setProgress(0, true);
        motorPwmProgress.setProgress(0, true);
        waterWaveView.setWaterLevel(0f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectFromESP32();
        executorService.shutdown();
    }
}