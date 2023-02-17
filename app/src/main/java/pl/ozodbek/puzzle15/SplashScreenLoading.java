package pl.ozodbek.puzzle15;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;

import java.util.Locale;

import pl.ozodbek.puzzle15.databinding.ActivitySplashScreenLoadingBinding;

@SuppressLint("CustomSplashScreen")
public class SplashScreenLoading extends AppCompatActivity {

    private ActivitySplashScreenLoadingBinding binding;
    private int progressStatus = 0;
    private final Handler handler = new Handler();
    private boolean seekBarLoadingFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenLoadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Start the SeekBar animation
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;
                handler.post(() -> binding.seekBar.setProgress(progressStatus));
                try {
                    // Sleep for 50 milliseconds
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Set seekBarLoadingFinished to true when the loading is complete
            seekBarLoadingFinished = true;
        }).start();

        binding.percentage.setText("0%");

        // Set a listener to update the percentage on SeekBar progress change
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the percentage TextView with the progress percentage
                binding.percentage.setText(String.format(Locale.getDefault(), "%d%%", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Wait for 3 seconds before launching the main activity
        handler.postDelayed(() -> {
            if (seekBarLoadingFinished) {
                startActivity(new Intent(this, ActivityGameScreen.class));
                finish();
            }
        }, 1500);
    }
}