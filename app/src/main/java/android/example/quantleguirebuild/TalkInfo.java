package android.example.quantleguirebuild;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by Matthias Niel on 15.04.2019.
 */
public class TalkInfo implements Parcelable {

    public double talkDuration;
    public int numSyllables;
    public int numWords;
    public int numPauses;
    public int numClauses;
    public int sumPauseDuration;

    public int[]  paceHistogram;
    public int[] wordsBySyllables;
    public int[] pausesByLength;
    public int[] pitchHistogram;
    public int[] powerHistogram;

    public float[] volumaxi;
    public float[] pitches;

    // comprehension scores, computed after the talk is over
    float flesch_reading_ease;
    float flesch_kincaid_grade_ease;
    float gunning_fog_index;
    float forecast_grade_level;


    public TalkInfo() {
        talkDuration = 0;
        numSyllables = 0;
        numWords = 0;
        numPauses = 0;
        numClauses = 0;
        sumPauseDuration = 0;

        paceHistogram = new int[20];
        wordsBySyllables = new int[4];
        pausesByLength = new int[6];
        pitchHistogram = new int[20];
        powerHistogram = new int[20];

        volumaxi = new float[4];
        pitches = new float[3];

        Arrays.fill(paceHistogram, 0);
        Arrays.fill(wordsBySyllables, 0);
        Arrays.fill(pausesByLength, 0);
        Arrays.fill(pitchHistogram, 0);
        Arrays.fill(powerHistogram, 0);

        Arrays.fill(volumaxi, 0);
        Arrays.fill(pitches, 0);
    }

    protected TalkInfo(Parcel in) {
        talkDuration = in.readDouble();
        numSyllables = in.readInt();
        numWords = in.readInt();
        numPauses = in.readInt();
        numClauses = in.readInt();
        sumPauseDuration = in.readInt();
        paceHistogram = in.createIntArray();
        wordsBySyllables = in.createIntArray();
        pausesByLength = in.createIntArray();
        pitchHistogram = in.createIntArray();
        powerHistogram = in.createIntArray();
        volumaxi = in.createFloatArray();
        pitches = in.createFloatArray();
        flesch_reading_ease = in.readFloat();
        flesch_kincaid_grade_ease = in.readFloat();
        gunning_fog_index = in.readFloat();
        forecast_grade_level = in.readFloat();
    }

    public static final Creator<TalkInfo> CREATOR = new Creator<TalkInfo>() {
        @Override
        public TalkInfo createFromParcel(Parcel in) {
            return new TalkInfo(in);
        }

        @Override
        public TalkInfo[] newArray(int size) {
            return new TalkInfo[size];
        }
    };

    public void reset(){
        talkDuration = 0;
        numSyllables = 0;
        numWords = 0;
        numPauses = 0;
        numClauses = 0;
        sumPauseDuration = 0;

        Arrays.fill(paceHistogram, 0);
        Arrays.fill(wordsBySyllables, 0);
        Arrays.fill(pausesByLength, 0);
        Arrays.fill(pitchHistogram, 0);
        Arrays.fill(powerHistogram, 0);
    }


    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(talkDuration);
        dest.writeInt(numSyllables);
        dest.writeInt(numWords);
        dest.writeInt(numPauses);
        dest.writeInt(numClauses);
        dest.writeInt(sumPauseDuration);
        dest.writeIntArray(paceHistogram);
        dest.writeIntArray(wordsBySyllables);
        dest.writeIntArray(pausesByLength);
        dest.writeIntArray(pitchHistogram);
        dest.writeIntArray(powerHistogram);
        dest.writeFloatArray(volumaxi);
        dest.writeFloatArray(pitches);
        dest.writeFloat(flesch_reading_ease);
        dest.writeFloat(flesch_kincaid_grade_ease);
        dest.writeFloat(gunning_fog_index);
        dest.writeFloat(forecast_grade_level);
    }
}
