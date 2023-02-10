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


import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.google.android.material.snackbar.Snackbar;

import pl.ozodbek.puzzle15.Singleton.GameSettings;
import pl.ozodbek.puzzle15.databinding.ActivityGameEntryBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogNameBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogSettingsBinding;

public class ActivityGameEntry extends AppCompatActivity {
    private ActivityGameEntryBinding binding;
    private GameSettings gameSettings;
    private MediaPlayer mediaPlayer1;
    private boolean isPlaying1, isSwitchOn1;
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

            binding.startGame.setVisibility(View.INVISIBLE);
            binding.practiceGame.setVisibility(View.INVISIBLE);
            binding.settingsGame.setVisibility(View.INVISIBLE);
            binding.exitGame.setVisibility(View.INVISIBLE);
            AlertDialog.Builder builderSecond = new AlertDialog.Builder(this);
            CustomdialogNameBinding customDialogSecond = CustomdialogNameBinding.inflate(getLayoutInflater());
            builderSecond.setCancelable(false);
            builderSecond.setView(customDialogSecond.getRoot());


            AlertDialog dialogSecond = builderSecond.create();
            dialogSecond.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogSecond.getWindow().setWindowAnimations(R.anim.fade_in);


            customDialogSecond.cancel.setOnClickListener(v4 -> {

                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());

                binding.startGame.setVisibility(View.VISIBLE);
                binding.practiceGame.setVisibility(View.VISIBLE);
                binding.settingsGame.setVisibility(View.VISIBLE);
                binding.exitGame.setVisibility(View.VISIBLE);

                dialogSecond.dismiss();

                dialogSecond.setOnDismissListener(dialog2 -> {
                    dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out);
                });
            });


            customDialogSecond.done.setOnClickListener(v3 -> {

                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getStatusBtnSound());


                String exportedName = customDialogSecond.nameEdit.getText().toString();
                if (!exportedName.isEmpty()) {
                    gameSettings.vibrate();
                    gameSettings.playSound(gameSettings.getStatusBtnSound());

                    startActivity(new Intent(this, ActivityGameScreen.class)
                            .putExtra("Exported_name", exportedName));
                    finish();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(customDialogSecond.nameEdit.getWindowToken(), 0);
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(customDialogSecond.nameEdit.getWindowToken(), 0);
                    Snackbar.make(binding.getRoot(), "Please enter your name !!", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.WHITE)
                            .show();
                }
            });

            dialogSecond.show();


        });

        binding.practiceGame.setOnClickListener(v -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());

            startActivity(new Intent(this, ActivityGamePractice.class));
            finish();
        });

        binding.settingsGame.setOnClickListener(v -> {
            binding.exitGame.setVisibility(View.INVISIBLE);
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            CustomdialogSettingsBinding settings = CustomdialogSettingsBinding.inflate(getLayoutInflater());
            builder.setCancelable(false);
            builder.setView(settings.getRoot());

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setWindowAnimations(R.anim.fade_in);
            settings.back.setOnClickListener(v1 -> {
                binding.exitGame.setVisibility(View.VISIBLE);
                if (gameSettings.isVibrationEnabled()) {
                    gameSettings.vibrate();
                }
                if (gameSettings.isVibrationEnabled()) {
                    gameSettings.playSound(gameSettings.getDialogBtnSound());
                }

                dialog.dismiss();
            });


            settings.save.setOnClickListener(v1 -> {
                binding.exitGame.setVisibility(View.VISIBLE);
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());
                dialog.dismiss();

            });
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

        binding.exitGame.setOnClickListener(v -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());

            finish();
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
