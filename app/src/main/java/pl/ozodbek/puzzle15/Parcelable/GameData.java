package pl.ozodbek.puzzle15.Parcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class GameData implements Parcelable {

    private int bestTime;
    private int bestSteps;

    protected GameData(Parcel in) {
        bestTime = in.readInt();
        bestSteps = in.readInt();

    }

    public static final Creator<GameData> CREATOR = new Creator<GameData>() {
        @Override
        public GameData createFromParcel(Parcel in) {
            return new GameData(in);
        }

        @Override
        public GameData[] newArray(int size) {
            return new GameData[size];
        }
    };

    public int getBestTime() {
        return bestTime;

    }


    public int getBestSteps() {
        return bestSteps;
    }

    public GameData(int bestTime, int bestSteps) {
        this.bestTime = bestTime;
        this.bestSteps = bestSteps;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bestTime);
        dest.writeInt(bestSteps);

    }
}
