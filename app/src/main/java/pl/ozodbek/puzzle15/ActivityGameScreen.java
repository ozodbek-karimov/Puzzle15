package pl.ozodbek.puzzle15;

import static pl.ozodbek.puzzle15.R.anim.fade_in;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.daimajia.androidanimations.library.attention.PulseAnimator;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import pl.ozodbek.puzzle15.Models.GameControl;
import pl.ozodbek.puzzle15.Singleton.GameSettings;
import pl.ozodbek.puzzle15.databinding.ActivityGameScreenBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogQuitBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogSettingsBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogShuffleBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogStopStartBinding;
import pl.ozodbek.puzzle15.databinding.CustomtwinDialogBinding;

public class ActivityGameScreen extends AppCompatActivity implements GameControl {
    private ActivityGameScreenBinding binding;
    private AppCompatButton emptyButton;
    private GameSettings gameSettings;
    private SharedPreferences sharedPref, sharedPreferences;
    private MediaPlayer mediaPlayer1;
    private final List<Integer> numbers = new ArrayList<>();
    private int x = 3, y = 3, counter = 1, steps = 0;
    private long pausedTime, pausedTimeShuffle;
    private Snackbar snackbar;
    private boolean isPlaying1, isSwitchOn1, doubleBackToExitPressedOnce = false, isPauseDialogShown;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint({"ResourceType", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gameSettings = GameSettings.getInstance(this);
        backgroundMusic();
        showAddMob();
        loadNumbers();
        generateNumbers();


        binding.shuffleBtn.setOnClickListener(v -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());
            binding.card.setVisibility(View.INVISIBLE);
            pausedTimeShuffle = SystemClock.elapsedRealtime() - binding.timer.getBase();
            binding.timer.stop();
            if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) {
                mediaPlayer1.pause();
            }

            AlertDialog.Builder builderSecond = new AlertDialog.Builder(this);
            CustomdialogShuffleBinding customDialogSecond = CustomdialogShuffleBinding.inflate(getLayoutInflater());
            builderSecond.setCancelable(false);
            builderSecond.setView(customDialogSecond.getRoot());

            AlertDialog dialogSecond = builderSecond.create();
            dialogSecond.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogSecond.getWindow().setWindowAnimations(fade_in);


            customDialogSecond.no.setOnClickListener(v4 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());
                if (mediaPlayer1 != null && isPlaying1 && isSwitchOn1) {
                    mediaPlayer1.setLooping(true);
                    mediaPlayer1.start();
                }
                binding.card.setVisibility(View.VISIBLE);
                binding.timer.setBase(SystemClock.elapsedRealtime() - pausedTimeShuffle);
                binding.timer.start();
                dialogSecond.dismiss();

                dialogSecond.setOnDismissListener(dialog2 -> dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out));
            });


            customDialogSecond.yes.setOnClickListener(v3 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getSaveBtnSound());
                binding.timer.setBase(SystemClock.elapsedRealtime() - pausedTimeShuffle);
                binding.card.setVisibility(View.VISIBLE);
                binding.timer.setBase(SystemClock.elapsedRealtime() - pausedTimeShuffle);
                binding.timer.start();
                if (mediaPlayer1 != null && isPlaying1 && isSwitchOn1) {
                    mediaPlayer1.setLooping(true);
                    mediaPlayer1.start();
                }

                for (int i = 0; i < binding.gridContainer2.getChildCount(); i++) {
                    binding.gridContainer2.getChildAt(i).setVisibility(View.VISIBLE);
                }
                steps = 0;
                updateMoves(0); //stepsni 0 ga ozgartiradi..
                generateNumbers();

                dialogSecond.dismiss();
                dialogSecond.setOnDismissListener(dialog2 -> dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out));
            });

            dialogSecond.show();

        });

        binding.pauseResume.setOnClickListener(v -> {
            binding.timer.stop();
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());
            binding.card.setVisibility(View.INVISIBLE);
            if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) {
                mediaPlayer1.pause();
            }

            pausedTime = SystemClock.elapsedRealtime() - binding.timer.getBase();
            binding.pauseResume.setBackgroundResource(R.drawable.play_button);

            AlertDialog.Builder builderFirst = new AlertDialog.Builder(this);
            CustomdialogStopStartBinding customDialogFirst = CustomdialogStopStartBinding.inflate(getLayoutInflater());
            builderFirst.setCancelable(false);
            builderFirst.setView(customDialogFirst.getRoot());

            AlertDialog dialogFirst = builderFirst.create();
            dialogFirst.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogFirst.getWindow().setWindowAnimations(fade_in);
            dialogFirst.setCancelable(false);

            customDialogFirst.settingsGame.setOnClickListener(p -> {
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

            customDialogFirst.continuee.setOnClickListener(v1 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());
                binding.card.setVisibility(View.VISIBLE);
                if (mediaPlayer1 != null && isPlaying1 && isSwitchOn1) {
                    mediaPlayer1.setLooping(true);
                    mediaPlayer1.start();
                }

                binding.timer.setBase(SystemClock.elapsedRealtime() - pausedTime);
                binding.timer.start();
                binding.pauseResume.setBackgroundResource(R.drawable.pause_btn);
                dialogFirst.dismiss();
                dialogFirst.setOnDismissListener(dialog2 -> dialogFirst.getWindow().setWindowAnimations(R.anim.fade_out));

            });

            customDialogFirst.quit.setOnClickListener(v2 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());
                binding.card.setVisibility(View.INVISIBLE);

                AlertDialog.Builder builderSecond = new AlertDialog.Builder(this);
                CustomdialogQuitBinding customDialogSecond = CustomdialogQuitBinding.inflate(getLayoutInflater());

                builderSecond.setCancelable(false);
                builderSecond.setView(customDialogSecond.getRoot());

                AlertDialog dialogSecond = builderSecond.create();
                dialogSecond.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialogSecond.getWindow().setWindowAnimations(fade_in);
                dialogFirst.dismiss();


                customDialogSecond.cancel.setOnClickListener(v4 -> {
                    gameSettings.vibrate();
                    gameSettings.playSound(gameSettings.getDialogBtnSound());
                    binding.card.setVisibility(View.VISIBLE);
                    binding.pauseResume.setBackgroundResource(R.drawable.pause_btn);
                    binding.timer.setBase(SystemClock.elapsedRealtime() - pausedTime);
                    binding.timer.start();
                    if (mediaPlayer1 != null && isPlaying1 && isSwitchOn1) {
                        mediaPlayer1.setLooping(true);
                        mediaPlayer1.start();
                    }
                    dialogFirst.dismiss();
                    dialogSecond.dismiss();

                    dialogSecond.setOnDismissListener(dialog2 -> dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out));
                });


                customDialogSecond.finish.setOnClickListener(v3 -> {
                    gameSettings.vibrate();
                    gameSettings.playSound(gameSettings.getStatusBtnSound());

                    dialogSecond.dismiss();
                    startActivity(new Intent(this, ActivityGameEntry.class));
                    finish();
                    dialogSecond.setOnDismissListener(dialog2 -> dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out));
                });
                dialogSecond.show();
            });
            dialogFirst.show();
        });


    }


    @Override
    public void loadNumbers() {
        for (int i = 1; i <= 16; i++) {
            numbers.add(i);
        }
    }

    @Override
    public void generateNumbers() {
        binding.timer.setBase(SystemClock.elapsedRealtime());
        binding.timer.stop();
        binding.timer.start();
        do {
//            Collections.shuffle(numbers);
        } while (!isSolvable(numbers));

        for (int i = 0; i < binding.gridContainer2.getChildCount(); i++) {
            if (numbers.get(i) == 16) {
                String tag = binding.gridContainer2.getChildAt(i).getTag().toString();
                x = tag.charAt(0) - '0';
                y = tag.charAt(1) - '0';
                emptyButton = (AppCompatButton) binding.gridContainer2.getChildAt(i);
                emptyButton.setVisibility(View.INVISIBLE);
            }
            ((AppCompatButton) binding.gridContainer2.getChildAt(i)).setText(String.valueOf(numbers.get(i)));
        }
    }

    @Override
    public boolean isSolvable(List<Integer> numbers) {
        int counter = 0;
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i) == 16) {
                counter += i / 4 + 1;
                continue;

            }
            for (int j = i + 1; j < numbers.size(); j++) {
                if (numbers.get(i) > numbers.get(j)) {
                    counter++;
                }
            }
        }
        return counter % 2 == 0;
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    public void buttonClick(View view) {
        counter = 1;
        AppCompatButton clicked = (AppCompatButton) view;
        String tag = view.getTag().toString();
        int clickedX = tag.charAt(0) - '0';
        int clickedY = tag.charAt(1) - '0';

        if (canMove(clickedX, clickedY)) {
            swap(clickedX, clickedY, clicked);
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getGameBtnSound());

            updateMoves();
        }
        if (isGameOver()) {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getGameWinBtn());

            binding.card.setVisibility(View.INVISIBLE);
            binding.timer.setVisibility(View.INVISIBLE);
            binding.stepsCount.setVisibility(View.INVISIBLE);
            binding.st.setVisibility(View.INVISIBLE);
            binding.pauseResume.setVisibility(View.INVISIBLE);
            binding.shuffleBtn.setVisibility(View.INVISIBLE);
            binding.adView.setVisibility(View.INVISIBLE);
            binding.confettiAnimation.setVisibility(View.VISIBLE);
            binding.timer.stop();
            mediaPlayer1.stop();

            // USER YUTSA OCHILADIGAN DIALOG
            winDialog();

        }
    }

    @Override
    public boolean canMove(int clickedX, int clickedY) {

        return (Math.abs(clickedX + clickedY - (x + y)) == 1 && Math.abs(clickedX - x) != 2 && Math.abs(clickedY - y) != 2);
    }

    @Override
    public void swap(int clickedX, int clickedY, AppCompatButton clicked) {
        String text = clicked.getText().toString();
        Drawable tempBackround = emptyButton.getBackground();
        clicked.setVisibility(View.INVISIBLE);
        clicked.setText("");

        emptyButton.setText(text);
        emptyButton.setBackground(clicked.getBackground());
        emptyButton.setVisibility(View.VISIBLE);
        clicked.setBackground(tempBackround);

        emptyButton = clicked;
        x = clickedX;
        y = clickedY;
    }

    @Override
    public boolean isGameOver() {

        for (int i = 0; i < 15; i++) {
            Button checker = (AppCompatButton) binding.gridContainer2.getChildAt(i);
            if (checker.getText().toString().isEmpty()) break;
            if (Integer.parseInt(checker.getText().toString()) != counter) {
                return false;
            } else {
                counter++;
            }
            if (counter == 16) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void backgroundMusic() {
        sharedPreferences = getSharedPreferences("music_prefs", MODE_PRIVATE);

        isPlaying1 = sharedPreferences.getBoolean("isPlaying1", true);

        isSwitchOn1 = sharedPreferences.getBoolean("isSwitchOn1", true);

        mediaPlayer1 = MediaPlayer.create(this, R.raw.backmusic);
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

    @SuppressLint("VisibleForTests")
    @Override
    public void showAddMob() {
        AdView adView = new AdView(this);

        adView.setAdSize(AdSize.BANNER);

        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        MobileAds.initialize(this, initializationStatus -> {
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
    }

    private void updateMoves() {
        steps++;
        binding.stepsCount.setText(String.valueOf(steps));

    }

    private void updateMoves(int steps) {
        binding.stepsCount.setText(String.valueOf(steps));
    }

    private String formatTime(Chronometer chronometer) {
        if (chronometer == null) {
            return "00:00";
        }
        long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
        int minutes = (int) (elapsedMillis / 1000) / 60;
        int seconds = (int) (elapsedMillis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @SuppressLint("ResourceType")
    private void winDialog() {

        AlertDialog.Builder builderSecond = new AlertDialog.Builder(this);
        CustomtwinDialogBinding customDialogSecond = CustomtwinDialogBinding.inflate(getLayoutInflater());
        builderSecond.setCancelable(false);
        builderSecond.setView(customDialogSecond.getRoot());

        AlertDialog dialogSecond = builderSecond.create();
        dialogSecond.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogSecond.getWindow().setWindowAnimations(fade_in);

        customDialogSecond.time.setText(formatTime(binding.timer));
        customDialogSecond.score.setText(binding.stepsCount.getText().toString());

        List<String> words = new ArrayList<>();
        words.add("Incredible !!");
        words.add("Amazing !!");
        words.add("Wonderful !!");
        words.add("Fantastic !!");
        words.add("Exceptional !!");
        words.add("Outstanding !!");
        words.add("Impressive !!");
        words.add("Phenomenal !!");
        words.add("Marvelous !!");
        words.add("Brilliant !!");

        Collections.shuffle(words);
        String shuffledWords = words.get(0);
        customDialogSecond.awardWords.setText(shuffledWords);
        customDialogSecond.again.setOnClickListener(v4 -> {
            binding.card.setVisibility(View.VISIBLE);
            binding.timer.setVisibility(View.VISIBLE);
            binding.stepsCount.setVisibility(View.VISIBLE);
            binding.st.setVisibility(View.VISIBLE);
            binding.pauseResume.setVisibility(View.VISIBLE);
            binding.shuffleBtn.setVisibility(View.VISIBLE);
            binding.adView.setVisibility(View.VISIBLE);
            binding.confettiAnimation.setVisibility(View.GONE);

            mediaPlayer1 = MediaPlayer.create(this, R.raw.backmusic);
            if (isPlaying1 && isSwitchOn1) {
                mediaPlayer1.setLooping(true);
                mediaPlayer1.start();
            }

            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getDialogBtnSound());


            for (int i = 0; i < binding.gridContainer2.getChildCount(); i++) {
                binding.gridContainer2.getChildAt(i).setVisibility(View.VISIBLE);
            }

            steps = 0;
            updateMoves(0);
            generateNumbers();
            dialogSecond.dismiss();

            dialogSecond.setOnDismissListener(dialog2 -> {
                dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out);
            });
        });
        customDialogSecond.complete.setOnClickListener(v3 -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());
            startActivity(new Intent(this, ActivityGameEntry.class));
            finish();
        });
        customDialogSecond.share.setOnClickListener(v4 -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getDialogBtnSound());

            String message =

                    "Hi👋, i just finished Puzzle 15 game with " + binding.stepsCount.getText().toString() +
                            " steps💪, " + " during " + formatTime(binding.timer) +
                            "⏰. Check out the game at: Play Market";

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(share, "Share the game with your friends"));


        });
        customDialogSecond.rate.setOnClickListener(b -> {
            Snackbar.make(binding.gameLayout, "Coming soon ... ", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.WHITE)
                    .show();

        });
        dialogSecond.show();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            snackbar.dismiss();
            startActivity(new Intent(this, ActivityGameEntry.class));
            finish();
            return;
        }
        doubleBackToExitPressedOnce = true;
        snackbar = Snackbar.make(binding.gameLayout, "Double click to leave !", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.WHITE);
        snackbar.show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1000);

    }

    @SuppressLint("ResourceType")
    @Override
    protected void onPause() {
        super.onPause();
        binding.timer.stop();
        if (!isPauseDialogShown) {
            isPauseDialogShown = true;
        }

        if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) {
            mediaPlayer1.pause();
        }

        sharedPref = getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        long elapsedTime = SystemClock.elapsedRealtime() - binding.timer.getBase();
        String steps = binding.stepsCount.getText().toString();

        editor.putLong("elapsed_time", elapsedTime);
        editor.putString("steps", steps);
        editor.apply();

    }

    @SuppressLint("ResourceType")
    private void showPauseDialog() {
        sharedPref = getSharedPreferences("data", MODE_PRIVATE);
        long elapsedTime = sharedPref.getLong("elapsed_time", 0);
        binding.timer.stop();
        gameSettings.vibrate();
        gameSettings.playSound(gameSettings.getStatusBtnSound());
        binding.card.setVisibility(View.INVISIBLE);
        binding.pauseResume.setBackgroundResource(R.drawable.play_button);
        if (mediaPlayer1 != null && mediaPlayer1.isPlaying()) {
            mediaPlayer1.pause();
        }

        AlertDialog.Builder builderFirst = new AlertDialog.Builder(this);
        CustomdialogStopStartBinding customDialogFirst = CustomdialogStopStartBinding.inflate(getLayoutInflater());
        builderFirst.setCancelable(false);
        builderFirst.setView(customDialogFirst.getRoot());

        AlertDialog dialogFirst = builderFirst.create();
        dialogFirst.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogFirst.getWindow().setWindowAnimations(fade_in);
        dialogFirst.setCancelable(false);
        customDialogFirst.settingsGame.setOnClickListener(p -> {
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

        customDialogFirst.continuee.setOnClickListener(v1 -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getDialogBtnSound());
            binding.card.setVisibility(View.VISIBLE);
            if (mediaPlayer1 != null && isPlaying1 && isSwitchOn1) {
                mediaPlayer1.setLooping(true);
                mediaPlayer1.start();
            }

            binding.timer.setBase(SystemClock.elapsedRealtime() - elapsedTime);
            binding.timer.start();
            binding.pauseResume.setBackgroundResource(R.drawable.pause_btn);
            dialogFirst.dismiss();
            dialogFirst.setOnDismissListener(dialog2 -> dialogFirst.getWindow().setWindowAnimations(R.anim.fade_out));

        });

        customDialogFirst.quit.setOnClickListener(v2 -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getDialogBtnSound());
            binding.card.setVisibility(View.INVISIBLE);

            AlertDialog.Builder builderSecond = new AlertDialog.Builder(this);
            CustomdialogQuitBinding customDialogSecond = CustomdialogQuitBinding.inflate(getLayoutInflater());

            builderSecond.setCancelable(false);
            builderSecond.setView(customDialogSecond.getRoot());

            AlertDialog dialogSecond = builderSecond.create();
            dialogSecond.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogSecond.getWindow().setWindowAnimations(fade_in);


            customDialogSecond.cancel.setOnClickListener(v4 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());
                binding.card.setVisibility(View.VISIBLE);
                binding.pauseResume.setBackgroundResource(R.drawable.pause_btn);
                binding.timer.setBase(SystemClock.elapsedRealtime() - elapsedTime);
                binding.timer.start();
                if (mediaPlayer1 != null && isPlaying1 && isSwitchOn1) {
                    mediaPlayer1.setLooping(true);
                    mediaPlayer1.start();
                }
                dialogSecond.dismiss();
                dialogFirst.dismiss();

                dialogSecond.setOnDismissListener(dialog2 -> dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out));
            });


            customDialogSecond.finish.setOnClickListener(v3 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getStatusBtnSound());

                dialogSecond.dismiss();
                startActivity(new Intent(this, ActivityGameEntry.class));
                finish();
                dialogSecond.setOnDismissListener(dialog2 -> dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out));
            });
            dialogSecond.show();
        });
        dialogFirst.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPauseDialogShown) {
            showPauseDialog();
        }

    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer1 != null) {
            mediaPlayer1.stop();
            mediaPlayer1.release();
        }
        super.onDestroy();
    }

}


