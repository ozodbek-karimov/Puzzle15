package pl.ozodbek.puzzle15;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;


import pl.ozodbek.puzzle15.Singleton.GameSettings;
import pl.ozodbek.puzzle15.databinding.ActivityGameEntryBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogSettingsBinding;

public class ActivityGameEntry extends AppCompatActivity {
    private ActivityGameEntryBinding binding;
    private GameSettings gameSettings;
    private MediaPlayer mediaPlayer1;
    private boolean isPlaying1;
    private boolean isSwitchOn1;
    private SharedPreferences sharedPreferences;

    @SuppressLint({"ResourceType", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        backgroundMusic();
        gameSettings = GameSettings.getInstance(this);


        binding.startGame.setOnClickListener(v -> {

            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());

            startActivity(new Intent(ActivityGameEntry.this, SplashScreenLoading.class));
            finish();
        });

        binding.practiceGame.setOnClickListener(v -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());

            startActivity(new Intent(this, ActivityGamePractice.class));
            finish();
        });

        binding.ratingGame.setOnClickListener(e ->{
            Toast.makeText(this, "This is rating button ", Toast.LENGTH_SHORT).show();

        });

        binding.settingsGame.setOnClickListener(v -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            CustomdialogSettingsBinding settings = CustomdialogSettingsBinding.inflate(getLayoutInflater());
            builder.setView(settings.getRoot());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setWindowAnimations(R.anim.fade_in);

            settings.musicSwitch.setOn(isSwitchOn1);
            settings.vibrationSwitch.setOn(gameSettings.isVibrationEnabled());
            settings.soundSwitch.setOn(gameSettings.isSoundEnabled());

            settings.musicSwitch.setOnToggledListener((buttonView, isChecked) -> toggleSwitch1(isChecked));
            settings.soundSwitch.setOnToggledListener((buttonView, isChecked) -> {
                gameSettings.setSoundEnabled(isChecked);
            });
            settings.vibrationSwitch.setOnToggledListener((buttonView, isChecked) -> {
                gameSettings.setVibrationEnabled(isChecked);
            });

            dialog.show();

        });

        binding.aboutGame.setOnClickListener(u ->{
            Toast.makeText(this, "This is about button ", Toast.LENGTH_SHORT).show();
        });

    }

    private void backgroundMusic() {
        sharedPreferences = getSharedPreferences("music_prefs", MODE_PRIVATE);

        isPlaying1 = sharedPreferences.getBoolean("isPlaying1", true);

        isSwitchOn1 = sharedPreferences.getBoolean("isSwitchOn1", true);

        mediaPlayer1 = MediaPlayer.create(this, R.raw.backmusicmain);
        if (isPlaying1 && isSwitchOn1) {
            mediaPlayer1.setLooping(true);
            mediaPlayer1.start();
        }
    }

    public void toggleSwitch1(boolean isChecked) {
        isSwitchOn1 = isChecked;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isSwitchOn1", isSwitchOn1);
        editor.apply();

        if (isSwitchOn1) {
            if (mediaPlayer1 != null) {
                mediaPlayer1.setLooping(true);
                mediaPlayer1.start();
            }
            isPlaying1 = true;
        } else {
            mediaPlayer1.pause();
            isPlaying1 = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) {
            mediaPlayer1.pause();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer1 != null && isPlaying1 && isSwitchOn1) {
            mediaPlayer1.setLooping(true);
            mediaPlayer1.start();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer1 != null) {
            mediaPlayer1.stop();
            mediaPlayer1.release();
        }

    }
}
