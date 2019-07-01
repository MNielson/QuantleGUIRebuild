package android.example.quantleguirebuild;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.newventuresoftware.waveform.WaveformView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CaptureTalkFragment extends Fragment {
    private static final String LOG_TAG = "CaptureTalkFragment";
    private OnCaptureTalkFragmentInteractionListener mListener;
    private WaveformView mWaveForm;
    private TextView mTalkLength;
    private BroadcastReceiver receiverAudioBuffer, receiverTalkInfo;
    private IntentFilter filterAudioBuffer, filterTalkInfo;

    public CaptureTalkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        // BroadcastReceiver AudioBuffer
        receiverAudioBuffer = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                short[] audioBuffer = intent.getShortArrayExtra("audioBuffer");
                short[] oldSamples = mWaveForm.getSamples();

                // TODO: display data nicely
                if (oldSamples == null) {
                    int tSum = 0;
                    for(int i = 0; i<audioBuffer.length; i++)
                        tSum += audioBuffer[i];
                    if(tSum != 0) //only start displaying once we have none-empty signal
                        mWaveForm.setSamples(audioBuffer);
                } else {
                    short[] samples = new short[oldSamples.length + audioBuffer.length];
                    int index = oldSamples.length;
                    for (int i = 0; i < audioBuffer.length; i++)
                        samples[i + index] = audioBuffer[i];
                    mWaveForm.setSamples(samples);
                }
            }
        };
        filterAudioBuffer = new IntentFilter(Constants.BUFFER);
        getActivity().registerReceiver(receiverAudioBuffer, filterAudioBuffer);


        // BroadcastReceiver TalkInfo
        receiverTalkInfo = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TalkInfo talkInfo = intent.getExtras().getParcelable("talk_info");

                // TODO: display data nicely
                mTalkLength.setText(Double.toString(talkInfo.talkDuration));


            }
        };
        filterTalkInfo = new IntentFilter(Constants.TALK_INFO);
        getActivity().registerReceiver(receiverTalkInfo, filterTalkInfo);


        super.onResume();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.OnCaptureTalkFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.d(LOG_TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof OnCaptureTalkFragmentInteractionListener) {
            mListener = (OnCaptureTalkFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCaptureTalkFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d(LOG_TAG, "onDetach");
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(LOG_TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_capture_talk, container, false);
        mWaveForm = view.findViewById(R.id.waveformView);
        mTalkLength = view.findViewById(R.id.talkLength);

        //attach main activity interactions
        Switch recordSwitch = view.findViewById(R.id.switch1);
        recordSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.OnSwitchRecordingState(isChecked);
            }
        });
        return view;
    }

    public interface OnCaptureTalkFragmentInteractionListener {
        // TODO: Update argument type and name
        void OnCaptureTalkFragmentInteraction(Uri uri);

        void OnSwitchRecordingState(Boolean shouldRecord);

        void OnClickFromFile();

        void OnClickStopAndSave();

        void OnClickClearAndReset(String speaker, String event);
    }
}
