package pl.ozodbek.puzzle15;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.ozodbek.puzzle15.Models.GameControl;
import pl.ozodbek.puzzle15.Singleton.GameSettings;
import pl.ozodbek.puzzle15.databinding.ActivityPracticeBinding;
import pl.ozodbek.puzzle15.databinding.CustomdialogShuffleBinding;
import pl.ozodbek.puzzle15.databinding.CustomtwinDialogBinding;
import pl.ozodbek.puzzle15.databinding.CustomtwinDialogPracticeBinding;

public class ActivityGamePractice extends AppCompatActivity implements GameControl {
    private final List<Integer> numbers = new ArrayList<>();
    private AppCompatButton emptyButton;
    private int x = 3, y = 3, counter = 1;
    private GameSettings gameSettings;
    private MediaPlayer mediaPlayer1;
    private boolean isPlaying1, isSwitchOn1, doubleBackToExitPressedOnce = false;
    private Snackbar snackbar;
    private ActivityPracticeBinding binding;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityPracticeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        loadNumbers();  // 16 - RAQAM BLOCKLARGA JOYLASHADI..
        generateNumbers(); // RAQAMALRNI JOYLAB BOLGACH, INVERSIYAGA TEKSHIRILADI...
        showAddMob();
        backgroundMusic();

        gameSettings = GameSettings.getInstance(this);

        binding.back.setOnClickListener(v -> {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());


            startActivity(new Intent(this, ActivityGameEntry.class));
            finish();
        });


        binding.shuffleBtnPractice.setOnClickListener(v -> {

            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getStatusBtnSound());
            binding.card.setVisibility(View.INVISIBLE);


            // BU YERDA , USER SHUFFLE QIMOQCHI BO'LSA DIALOG OCHILADI VA NO YOKI YES TANLANADI...

            AlertDialog.Builder shuffleDialog = new AlertDialog.Builder(this);
            CustomdialogShuffleBinding shuffleDialogLayout = CustomdialogShuffleBinding.inflate(getLayoutInflater());
            shuffleDialog.setCancelable(false);
            shuffleDialog.setView(shuffleDialogLayout.getRoot());

            AlertDialog dialogLayoutBuilder = shuffleDialog.create();
            dialogLayoutBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogLayoutBuilder.getWindow().setWindowAnimations(R.anim.fade_in);


            shuffleDialogLayout.no.setOnClickListener(v4 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());
                binding.card.setVisibility(View.VISIBLE);


                dialogLayoutBuilder.dismiss();
                dialogLayoutBuilder.setOnDismissListener(dialog2 -> dialogLayoutBuilder.getWindow().setWindowAnimations(R.anim.fade_out));
            });


            shuffleDialogLayout.yes.setOnClickListener(v3 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getSaveBtnSound());
                binding.back.setVisibility(View.VISIBLE);
                binding.shuffleBtnPractice.setVisibility(View.VISIBLE);
                binding.card.setVisibility(View.VISIBLE);

                dialogLayoutBuilder.dismiss();
                for (int i = 0; i < binding.gridContainer2.getChildCount(); i++) {
                    binding.gridContainer2.getChildAt(i).setVisibility(View.VISIBLE);
                }
                generateNumbers();

            });

            dialogLayoutBuilder.show();

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
        do {
            Collections.shuffle(numbers);

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

    @SuppressLint("ResourceType")
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


        }
        if (isGameOver()) {
            gameSettings.vibrate();
            gameSettings.playSound(gameSettings.getGameWinBtn());
            binding.card.setVisibility(View.INVISIBLE);
            AlertDialog.Builder builderSecond = new AlertDialog.Builder(this);
            CustomtwinDialogPracticeBinding customDialogSecond = CustomtwinDialogPracticeBinding.inflate(getLayoutInflater());
            builderSecond.setCancelable(false);
            builderSecond.setView(customDialogSecond.getRoot());

            AlertDialog dialogSecond = builderSecond.create();
            dialogSecond.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogSecond.getWindow().setWindowAnimations(R.anim.fade_in);


            customDialogSecond.again.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_start_dialogs));
            customDialogSecond.again.setOnClickListener(v4 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());
                binding.card.setVisibility(View.VISIBLE);

                for (int i = 0; i < binding.gridContainer2.getChildCount(); i++) {
                    binding.gridContainer2.getChildAt(i).setVisibility(View.VISIBLE);
                }
                generateNumbers();
                dialogSecond.dismiss();


                dialogSecond.setOnDismissListener(dialog2 -> {
                    dialogSecond.getWindow().setWindowAnimations(R.anim.fade_out);
                });
            });


            customDialogSecond.complete.setOnClickListener(v3 -> {
                gameSettings.vibrate();
                gameSettings.playSound(gameSettings.getDialogBtnSound());

                dialogSecond.dismiss();
                startActivity(new Intent(this, ActivityGameEntry.class));
                if (!isFinishing()) {
                    finish();
                }
            });
            dialogSecond.show();
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
        SharedPreferences sharedPreferences = getSharedPreferences("music_prefs", MODE_PRIVATE);

        isPlaying1 = sharedPreferences.getBoolean("isPlaying1", true);

        isSwitchOn1 = sharedPreferences.getBoolean("isSwitchOn1", true);

        mediaPlayer1 = MediaPlayer.create(this, R.raw.backmusic);
        if (isPlaying1 && isSwitchOn1) {
            mediaPlayer1.setLooping(true);
            mediaPlayer1.start();
        }
    }

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

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            snackbar.dismiss();
            startActivity(new Intent(this, ActivityGameEntry.class));
            finish();
            return;
        }
        doubleBackToExitPressedOnce = true;
        snackbar = Snackbar.make(binding.practiceLayout, "Double click to leave !", Snackbar.LENGTH_SHORT)
                .setBackgroundTint(Color.WHITE);
        snackbar.show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 1000);

    }
}