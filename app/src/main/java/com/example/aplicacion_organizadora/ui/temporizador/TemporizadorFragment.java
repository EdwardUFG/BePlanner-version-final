package com.example.aplicacion_organizadora.ui.temporizador;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.aplicacion_organizadora.R;
import java.util.Locale;
public class TemporizadorFragment extends Fragment {

    private TextView timerDisplay, statusDisplay;
    private Button startButton, stopButton;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private static final long WORK_TIME = 5 * 60 * 1000;
    private static final long SHORT_BREAK = 2 * 60 * 1000;
    private static final long LONG_BREAK = 15 * 60 * 1000;
    private static final int TOTAL_POMODOROS = 4;

    private int pomodoroCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_temporizador, container, false);

        timerDisplay = root.findViewById(R.id.timer_display);
        statusDisplay = root.findViewById(R.id.status_display);
        startButton = root.findViewById(R.id.start_button);
        stopButton = root.findViewById(R.id.stop_button);

        startButton.setOnClickListener(v -> {
            if (!isRunning) {
                startPomodoroCycle();
            }
        });

        stopButton.setOnClickListener(v -> {
            if (isRunning && countDownTimer != null) {
                countDownTimer.cancel();
                isRunning = false;
                statusDisplay.setText("Temporizador detenido");
                timerDisplay.setText("00:00");
            }
        });

        return root;
    }

    private void startPomodoroCycle() {
        isRunning = true;
        pomodoroCount++;

        statusDisplay.setText("Pomodoro " + pomodoroCount + " iniciado: ¡Trabaja!");
        startCountdown(WORK_TIME, () -> {
            if (pomodoroCount < TOTAL_POMODOROS) {
                statusDisplay.setText("Descanso corto: 2 minutos");
                startCountdown(SHORT_BREAK, this::startPomodoroCycle);
            } else {
                statusDisplay.setText("¡Descanso largo: 15 minutos!");
                startCountdown(LONG_BREAK, () -> {
                    statusDisplay.setText("Ciclo completo. Reiniciando...");
                    pomodoroCount = 0;
                    isRunning = false;
                });
            }
        });
    }

    private void startCountdown(long duration, Runnable onFinish) {
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                timerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                onFinish.run();
            }
        }.start();
    }
}