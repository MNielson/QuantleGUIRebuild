package android.example.quantleguirebuild;

/**
 * Created by Matthias Niel on 15.04.2019.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.List;


public class MyBufferHandler extends Handler {

    final double MIN_RELATIVE_STE_DIFF = 0.07;
    final double ONE_BUFFER_TIME = (0.0232 / 60);
    final String LOG_TAG = "MyBufferHandler";

    private Context mContext;

    private final PitchDetector mPitchDetector;
    private final FFT fft;
    private final int MIN_EXTREMA_X_DIFF = Constants.ONE_BUFFER_LEN / 2;
    private final Activity mactivity;
    // Number of processed buffers
    int buffercounter = 0;
    // Local extrema algorithm variables
    double p0_elem = -1, p1_elem = 0, lmin_elem = 0, lmax_elem = 0;
    int p0_index = 0, p1_index = 0, p2_index = 0;
    int wordpart = 0;
    int pitchindex = -1;
    int spm_syllables_last_10sec;
    float spm_time_last_10sec;


    double ste, maxste = 0, MIN_EXTREMA_Y_DIFF = 0;
    // long-term maxima: 4 x 5s
    double[] volumaxi = new double[4];
    int voluindex = -1;
    private TalkInfo mTalkInfo;


    private boolean hasChanged = false;


    public MyBufferHandler(Looper looper, Activity activity, Context context) {
        super(looper);
        mPitchDetector = new PitchDetector();
        fft = new FFT(Constants.ONE_BUFFER_LEN);
        mactivity = activity;
        mTalkInfo = new TalkInfo();
        mContext = context;

    }

    private void reset() {
        // TODO: reset dywapitch tracking

        // reset local variables
        buffercounter = 0;
        MIN_EXTREMA_Y_DIFF = 0;
        voluindex = -1;
        pitchindex = 0;
        maxste = 0;
        spm_syllables_last_10sec = 0;
        spm_time_last_10sec = 0;

        // reset talk info
        mTalkInfo.reset();

    }

    // process buffer
    public void handleMessage(Message msg) {
        short[] buffer = (short[]) msg.obj;


        assert buffer.length <= Constants.ONE_BUFFER_LEN;

        buffercounter++;

        // talk duration
        incrementTalkDuration();

        // syllable estimation
        syllableEstimation_2(buffer, buffer.length);

        // pitch estimation
        double pitch = computePitch(buffer, buffer.length);

        if(hasChanged)
        {
            // TODO: send parcel
            Intent intent = new Intent();
            intent.setAction(Constants.TALK_INFO);
            intent.putExtra("talk_info", mTalkInfo);
            mContext.sendBroadcast(intent);

            hasChanged = false;
        }

    }

    private void syllableEstimation_1(List<Short> buffer, double pitch) {
        // perform FFT
        double[] re = new double[Constants.ONE_BUFFER_LEN];
        double[] im = new double[Constants.ONE_BUFFER_LEN];
        for (int i = 0; i < buffer.size(); i++) {
            re[i] = buffer.get(i).doubleValue();
            im[i] = 0;
        }
        fft.fft(re, im);

        // Algorithm: https://ieeexplore.ieee.org/document/4317582/ (Subband-Based Correlation Approach)
        // Step 1: Morgan and Fosler-Lussier, compute a trajectory that is the average product over all pairs of compressed sub-band energy trajectories.
        // Use 4 bands max, otherwise NON-real-time (quadratic computational complexity)
        double y = 0;
        int n_bands = 20;
        int L = Constants.ONE_BUFFER_LEN / 2 / n_bands;
        for (int i = 0; i < n_bands - 1; i++) {
            for (int j = i + 1; j < n_bands; j++) {
                double sum_i = 0;
                double sum_j = 0;
                for (int k = i * L; k < i * L + L; k++)
                    sum_i += Math.pow(re[k], 2.0);
                for (int k = j * L; k < j * L + L; k++)
                    sum_j += Math.pow(re[k], 2.0);
                y += sum_i * sum_j;
            }
        }

        // Step 2: Syllable recognition algorithm (local extrema finding). Online algorithm.
        // Extremas are either /\ or \/ --> Estimated by: p0_elem --> p1_elem --> y.
        double MIN_EXTREMA_Y_DIFF = 0;

        p2_index += buffer.size();
        if (p0_elem <= p1_elem && p1_elem <= y) {
            p1_elem = y;
            p1_index = p2_index;
        } else if (p0_elem <= p1_elem && p1_elem > y && pitch >= 60 && pitch < 400) {
            if (Math.abs(p1_elem - y) > MIN_EXTREMA_Y_DIFF) {
                if ((p1_index > MIN_EXTREMA_X_DIFF || p0_elem < 0) && p2_index - p1_index > MIN_EXTREMA_X_DIFF) {
                    /*
#ifdef DEBUG_OUTPUT
                    printf("RateS,%d,%f\n", buffercounter,p1_elem); fflush(stdout);  // local max found: (p1_index, p1_elem)
#endif
                    */
                    process_maximum(p1_index, p1_elem);

                    p0_elem = p1_elem;
                    p1_elem = y;
                    int offset = p1_index;
                    p0_index = p1_index - offset;
                    p1_index = p2_index - offset;
                    p2_index = p2_index - offset;
                }
            }
        } else if (p0_elem >= p1_elem && p1_elem >= y) {
            p1_elem = y;
            p1_index = p2_index;
        } else if (p0_elem >= p1_elem && p1_elem < y) {
            if (Math.abs(p1_elem - y) > MIN_EXTREMA_Y_DIFF) {
                if ((p1_index > MIN_EXTREMA_X_DIFF || p0_elem < 0) && p2_index - p1_index > MIN_EXTREMA_X_DIFF) {
                /*
#ifdef DEBUG_OUTPUT
                    printf("RateF,%d,%f\n", buffercounter,p1_elem); fflush(stdout); // local min found: (p1_index, p1_elem)
#endif
                */
                    process_minimum(p1_index, p1_elem);

                    p0_elem = p1_elem;
                    p1_elem = y;
                    int offset = p1_index;
                    p0_index = p1_index - offset;
                    p1_index = p2_index - offset;
                    p2_index = p2_index - offset;
                }
            }
        }
    }

    void syllableEstimation_2(short[] buffer, int len) {
        if (voluindex == -1)
            MIN_EXTREMA_Y_DIFF = MIN_RELATIVE_STE_DIFF;
        else
            MIN_EXTREMA_Y_DIFF = MIN_RELATIVE_STE_DIFF * (0.6 * volumaxi[voluindex] +
                    0.25 * volumaxi[(4 + voluindex - 1) % 4] +
                    0.1 * volumaxi[(4 + voluindex - 2) % 4] +
                    0.05 * volumaxi[(4 + voluindex - 3) % 4]);

        // compute ste - short-time energy in buffer
        ste = 0;
        for (int i = 0; i < len; i++) {
            ste += Math.pow(buffer[i], 2);

            //float element = *((float *) (buffer + i * sizeof(float) ));
            //ste += powf(element,2);
        }

        /**
         * Syllable recognition algorithm (local extrema finding). Online algorithm.
         * Extremas are either /\ or \/ --> p0[index] - p1[index] - ste_sqrt[i].
         */
        p2_index += len;

        if (p0_elem <= p1_elem && p1_elem <= ste) {
            p1_elem = ste;
            p1_index = p2_index;
        } else if (p0_elem <= p1_elem && p1_elem > ste) {
            if (Math.abs(p1_elem - ste) > MIN_EXTREMA_Y_DIFF) {
                if ((p1_index > MIN_EXTREMA_X_DIFF || p0_elem < 0) && p2_index - p1_index > MIN_EXTREMA_X_DIFF) {
/*
#ifdef DEBUG_OUTPUT
                    printf("SyllableNucleiMax,%d,%f\n", buffercounter, p1_elem);
                    fflush(stdout);  // local max found: (p1_index, p1_elem)
#endif
*/
                    process_maximum(p1_index, p1_elem);

                    lmax_elem = p1_elem;
                    p0_elem = p1_elem;
                    p1_elem = ste;
                    int offset = p1_index;
                    p0_index = p1_index - offset;
                    p1_index = p2_index - offset;
                    p2_index = p2_index - offset;
                }
            }
        } else if (p0_elem >= p1_elem && p1_elem >= ste) {
            p1_elem = ste;
            p1_index = p2_index;
        } else if (p0_elem >= p1_elem && p1_elem < ste) {
            if (Math.abs(p1_elem - ste) > MIN_EXTREMA_Y_DIFF) {
                if ((p1_index > MIN_EXTREMA_X_DIFF || p0_elem < 0) && p2_index - p1_index > MIN_EXTREMA_X_DIFF) {
/*
#ifdef DEBUG_OUTPUT
                    printf("SyllableNucleiMin,%d,%f\n", buffercounter, p1_elem);
                    fflush(stdout); // local min found: (p1_index, p1_elem)
#endif
*/
                    process_minimum(p1_index, p1_elem);

                    lmin_elem = p1_elem;
                    p0_elem = p1_elem;
                    p1_elem = ste;
                    int offset = p1_index;
                    p0_index = p1_index - offset;
                    p1_index = p2_index - offset;
                    p2_index = p2_index - offset;
                }
            }
        }

        if (ste > MIN_EXTREMA_Y_DIFF)
            power_estimation(ste);

        // update 5sec maximum
        maxste = (ste > maxste) ? ste : maxste;
        int buffers_in_five_sec = (int) ((float) 1.0 / ONE_BUFFER_TIME / 20); // one minute contains 1 / ONE_BUFFER_TIME buffers
        if (buffercounter % buffers_in_five_sec == 0) {
            if (voluindex == -1) {
                for (int j = 0; j < 4; j++)
                    volumaxi[j] = maxste;
                voluindex = 0;
            } else {
                voluindex = (voluindex + 1) % 4;
                volumaxi[voluindex] = maxste;
            }
            maxste = 0;
        }
/*
#ifdef DEBUG_OUTPUT
        printf("STE,%d,%f\n", buffercounter, ste);
        fflush(stdout);
#endif
*/
    }


    void power_estimation(double value) {
        double volume = 10 * Math.log10(1 + value);
/*
#ifdef DEBUG_OUTPUT
        printf("Volume,%d,%f\n", buffercounter,volume); fflush(stdout);
#endif
*/
        volume = (volume < 19) ? volume : 19;
        int idx = (int) Math.round(volume);
        //counters.power_histogram[idx]++;
    }

    private void process_maximum(int index, double value) {
        hasChanged = true;
        wordpart++;
        mTalkInfo.numSyllables++;

        // compute rate variability over the past 10s
        spm_syllables_last_10sec++;
        int buffers_in_10sec = (int) ((float) 1.0 / ONE_BUFFER_TIME / 6); // one minute contains 1 / ONE_BUFFER_TIME buffers
        if (buffercounter - spm_time_last_10sec >= buffers_in_10sec) {
            float rate = spm_syllables_last_10sec * 6; // #syllables / 10 sec * 60 sec in one minute
            if (rate < 599 && rate > 0)
                mTalkInfo.paceHistogram[(int) rate/30]++;
            spm_syllables_last_10sec = 0;
            spm_time_last_10sec = buffercounter;
        }

    }

    // new pause, new word or gap between syllables in a word
    private void process_minimum(int index, double value) {
        double pause = ((double) index) / 44100;
        // UI code goes here
        //Found a minimum


        float inter_syl_dist = ((float) index) / 44100; // duration in s
        float avg_inter_syl_dist = (float)mTalkInfo.sumPauseDuration / (float)mTalkInfo.numPauses;

        // this is a pause between words
        if (inter_syl_dist >= 0.1) {
            hasChanged = true;
            mTalkInfo.numPauses++;
            mTalkInfo.sumPauseDuration += inter_syl_dist;
            if (inter_syl_dist >= 0.1 && inter_syl_dist < 0.2)
                mTalkInfo.pausesByLength[0]++;
            else if (inter_syl_dist >= 0.2 && inter_syl_dist < 0.4)
                mTalkInfo.pausesByLength[1]++;
            else if (inter_syl_dist >= 0.4 && inter_syl_dist < 0.7)
                mTalkInfo.pausesByLength[2]++;
            else if (inter_syl_dist >= 0.7 && inter_syl_dist < 1)
                mTalkInfo.pausesByLength[3]++;
            else if (inter_syl_dist >= 1 && inter_syl_dist < 1.5)
                mTalkInfo.pausesByLength[4]++;
            else if (inter_syl_dist >= 1.5)
                mTalkInfo.pausesByLength[5]++;

            // Increase clause counter if pause >= factor * average pause length
            if (inter_syl_dist >= avg_inter_syl_dist * 2) // new clause
                mTalkInfo.numClauses++;
        }

        // Increase number of words
        if (inter_syl_dist >= 0.25 * avg_inter_syl_dist) {
            hasChanged = true;
            mTalkInfo.numWords++;
            mTalkInfo.wordsBySyllables[(wordpart>4) ? 3 : wordpart-1]++;
            wordpart = 0;
        }

    }

    private double computePitch(short[] buffer, int len) {
        double[] primSamples = new double[len];
        for (int i = 0; i < len; i++)
            primSamples[i] = (double)buffer[i];
        return mPitchDetector.computePitch(primSamples, 0, len);
    }

    private void incrementTalkDuration() {
        hasChanged = true;
        mTalkInfo.talkDuration += ONE_BUFFER_TIME;
    }

    private void computeComprehensionScores(){
        // Flesch Reading Ease
        mTalkInfo.flesch_reading_ease = 0;
        if (mTalkInfo.numWords > 0 && mTalkInfo.numClauses > 0) {
            mTalkInfo.flesch_reading_ease = (float) (206.835 - 1.015 * ((float) mTalkInfo.numWords)/mTalkInfo.numClauses -
                                84.6 * ((float) mTalkInfo.numSyllables)/mTalkInfo.numWords);
            mTalkInfo.flesch_reading_ease = (float) fit_to_interval(mTalkInfo.flesch_reading_ease, 0, 100);
        }

        // Flesch-Kincaid Grade Ease
        mTalkInfo.flesch_kincaid_grade_ease = 0;
        if (mTalkInfo.numClauses > 0 && mTalkInfo.numWords > 0) {
            mTalkInfo.flesch_kincaid_grade_ease = (float) (0.39 * ((float) mTalkInfo.numWords)/mTalkInfo.numClauses +
                                11.8 * ((float) mTalkInfo.numSyllables)/mTalkInfo.numWords - 15.59);
            mTalkInfo.flesch_kincaid_grade_ease = (float) fit_to_interval(mTalkInfo.flesch_kincaid_grade_ease, 0, 20);
        }

        // Gunning Fog Index
        int num_hard_words = mTalkInfo.wordsBySyllables[2] + mTalkInfo.wordsBySyllables[3];
        mTalkInfo.gunning_fog_index = 0;
        if (mTalkInfo.numClauses > 0 && mTalkInfo.numWords > 0) {
            mTalkInfo.gunning_fog_index = (float) (0.4 * ( ((float) mTalkInfo.numWords)/mTalkInfo.numClauses +
                                ((float) num_hard_words)/mTalkInfo.numWords ));
            mTalkInfo.gunning_fog_index = (float) fit_to_interval(mTalkInfo.gunning_fog_index, 0, 20);
        }

        // Forecast Grade Level
        mTalkInfo.forecast_grade_level = 0;
        if (mTalkInfo.numWords > 0) {
            mTalkInfo.forecast_grade_level = 20 - 15 * ((float) mTalkInfo.wordsBySyllables[0])/mTalkInfo.numWords;
            mTalkInfo.forecast_grade_level = (float) fit_to_interval(mTalkInfo.forecast_grade_level, 0, 20);
        }
    }

    private float fit_to_interval(float foo, int bar, int foobar)
    {
        Log.e(LOG_TAG, "Using unimplemented function fit_to_interval.");
        return foo;
    }

}
