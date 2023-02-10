package pl.ozodbek.puzzle15.Models;

import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

import java.util.List;

public interface GameControl {

    void loadNumbers();
    void generateNumbers();
    boolean isSolvable(List<Integer> numbers);
    boolean canMove(int clickedX, int clickedY);
    void swap(int clickedX, int clickedY, AppCompatButton clicked);
    boolean isGameOver();
    void backgroundMusic();
    void showAddMob();
}
