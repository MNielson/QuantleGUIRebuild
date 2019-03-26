package android.example.quantleguirebuild;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.newventuresoftware.waveform.WaveformView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CaptureTalkFragment extends Fragment {


    private OnCaptureTalkFragmentInteractionListener mListener;
    private WaveformView mWaveForm;
    private BroadcastReceiver receiver;
    private IntentFilter filter;

    public CaptureTalkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        // BroadcastReceiver
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                short[] audioBuffer = intent.getShortArrayExtra("audioBuffer");
                // add Samples to waveformview
                short[] oldSamples = mWaveForm.getSamples();
                short[] samples = new short[oldSamples.length + audioBuffer.length];

                int index = oldSamples.length;

                for (int i = 0; i < oldSamples.length; i++) {
                    samples[i] = oldSamples[i];
                }
                for (int i = 0; i < audioBuffer.length; i++) {
                    samples[i + index] = audioBuffer[i];
                }
                mWaveForm.setSamples(samples);
            }
        };
        filter = new IntentFilter(Constants.BUFFER);
        getActivity().registerReceiver(receiver, filter);
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
        super.onDetach();
        mListener = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_capture_talk, container, false);
        mWaveForm = view.findViewById(R.id.waveformView);

        Switch onOffSwitch = view.findViewById(R.id.switch1);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

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
    }

}
