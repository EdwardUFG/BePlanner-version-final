package com.example.aplicacion_organizadora.ui.temporizador;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.aplicacion_organizadora.R;

import java.util.Locale;

public class TemporizadorFragment extends Fragment {

    private TextView timerDisplay, statusDisplay;
    private Button startButton, stopButton, selectSoundButton;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private boolean isPaused  = false;

    private static final long WORK_TIME   = 25 * 60 * 1000L;
    private static final long SHORT_BREAK = 5  * 60 * 1000L;
    private static final long LONG_BREAK  = 15 * 60 * 1000L;
    private static final int  TOTAL_POMODOROS = 4;

    private int pomodoroCount = 0;

    private long remainingMillis = 0L;
    private Runnable currentOnFinish = null;

    private static final String PREFS = "timer_prefs";
    private static final String KEY_TONE_URI = "tone_uri";
    private Uri selectedToneUri;

    private MediaPlayer alertPlayer;
    private static final int MAX_BEEP_MS = 5000;
    private final ActivityResultLauncher<Intent> ringtonePicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) return;
                Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null) {
                    selectedToneUri = uri;
                    requireContext().getSharedPreferences(PREFS, 0)
                            .edit().putString(KEY_TONE_URI, uri.toString()).apply();
                    statusDisplay.setText("Tono seleccionado");
                } else {
                    selectedToneUri = null;
                    requireContext().getSharedPreferences(PREFS, 0)
                            .edit().remove(KEY_TONE_URI).apply();
                    statusDisplay.setText("Tono predeterminado");
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_temporizador, container, false);

        timerDisplay = root.findViewById(R.id.timer_display);
        statusDisplay = root.findViewById(R.id.status_display);
        startButton = root.findViewById(R.id.start_button);
        stopButton = root.findViewById(R.id.stop_button);
        selectSoundButton = root.findViewById(R.id.select_sound_button);

        String saved = requireContext().getSharedPreferences(PREFS, 0).getString(KEY_TONE_URI, null);
        if (saved != null) selectedToneUri = Uri.parse(saved);

        startButton.setOnClickListener(v -> {
            if (!isRunning && !isPaused) {
                pomodoroCount = 0;
                startPomodoroCycle();
            }
        });

        stopButton.setOnClickListener(v -> {
            if (isRunning) {

                if (countDownTimer != null) countDownTimer.cancel();
                stopSound();
                isRunning = false;
                isPaused = true;
                stopButton.setText("Seguir");
                statusDisplay.setText("En pausa");
            } else if (isPaused) {

                if (remainingMillis > 0 && currentOnFinish != null) {
                    startCountdown(remainingMillis, currentOnFinish);
                    isRunning = true;
                    isPaused = false;
                    stopButton.setText("Parar");
                    statusDisplay.setText("Reanudado");
                }
            }
        });

        selectSoundButton.setOnClickListener(v -> openRingtonePicker());

        return root;
    }

    private void startPomodoroCycle() {
        isRunning = true;
        isPaused  = false;
        stopButton.setText("Parar");

        pomodoroCount++;
        statusDisplay.setText("Pomodoro " + pomodoroCount + " iniciado: ¡Trabaja!");

        startCountdown(WORK_TIME, () -> {
            playSoundOnce();

            if (pomodoroCount < TOTAL_POMODOROS) {
                statusDisplay.setText("Descanso corto: " + formatMs(SHORT_BREAK));
                startCountdown(SHORT_BREAK, () -> {
                    playSoundOnce();
                    startPomodoroCycle();
                });
            } else {
                statusDisplay.setText("¡Descanso largo: " + formatMs(LONG_BREAK) + "!");
                startCountdown(LONG_BREAK, () -> {
                    playSoundOnce();
                    statusDisplay.setText("Ciclo completo. Reiniciando...");
                    pomodoroCount = 0;
                    isRunning = false;
                    isPaused  = false;
                    stopButton.setText("Parar");
                });
            }
        });
    }

    private void startCountdown(long duration, Runnable onFinish) {
        currentOnFinish = onFinish;

        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                timerDisplay.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                remainingMillis = 0;
                onFinish.run();
            }
        }.start();
    }

    private void openRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_ALARM | RingtoneManager.TYPE_NOTIFICATION | RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Selecciona un tono");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedToneUri);
        ringtonePicker.launch(intent);
    }

    private void playSoundOnce() {
        stopSound();

        Uri uri = selectedToneUri;
        if (uri == null) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        try {
            alertPlayer = new MediaPlayer();
            alertPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
            );
            alertPlayer.setLooping(false);
            alertPlayer.setDataSource(requireContext(), uri);
            alertPlayer.setOnPreparedListener(MediaPlayer::start);
            alertPlayer.setOnCompletionListener(mp -> stopSound());
            alertPlayer.prepareAsync();

            timerDisplay.postDelayed(this::stopSound, MAX_BEEP_MS);
        } catch (Exception e) {
            stopSound();
        }
    }

    private void stopSound() {
        try {
            if (alertPlayer != null) {
                if (alertPlayer.isPlaying()) alertPlayer.stop();
                alertPlayer.reset();
                alertPlayer.release();
            }
        } catch (Exception ignored) { }
        finally { alertPlayer = null; }
    }

    private String formatMs(long ms) {
        int m = (int) (ms / 1000) / 60;
        int s = (int) (ms / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        stopSound();
    }
}
