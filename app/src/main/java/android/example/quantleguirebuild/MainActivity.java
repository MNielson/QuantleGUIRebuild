package android.example.quantleguirebuild;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener
        , HistoryFragment.OnHistoryFragmentInteractionListener
        , CaptureTalkFragment.OnCaptureTalkFragmentInteractionListener {

    final String LOG_TAG = "MainActivity";
    boolean mShouldContinue;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(this);
        loadFragment(new CaptureTalkFragment(), false);


        mHandler = new Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {
                short[] audioBuffer = (short[]) inputMessage.obj;
                // could maybe be moved into recording thread itself?
                Intent intent = new Intent();
                intent.setAction(Constants.BUFFER);
                intent.putExtra("audioBuffer", audioBuffer);
                sendBroadcast(intent);
            }
        };
    }

    void recordAudio() throws IOException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

                // buffer size in bytes
                int bufferSize = AudioRecord.getMinBufferSize(Constants.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    bufferSize = Constants.ONE_BUFFER_LEN * 2;
                }
                if (bufferSize < Constants.ONE_BUFFER_LEN * 2)
                    bufferSize = Constants.ONE_BUFFER_LEN * 2;

                short[] audioBuffer = new short[bufferSize / 2];

                AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                        Constants.SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

                if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(LOG_TAG, "Audio Record can't initialize!");
                    return;
                }
                record.startRecording();

                Log.v(LOG_TAG, "Start recording.");

                long shortsRead = 0;
                while (mShouldContinue) {
                    int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);

                    short[] workBuffer = new short[audioBuffer.length];
                    System.arraycopy(audioBuffer, 0, workBuffer, 0, audioBuffer.length);
                    shortsRead += numberOfShort;

                    Message msg = mHandler.obtainMessage();
                    msg.obj = audioBuffer;
                    mHandler.sendMessage(msg);

                }

                record.stop();
                record.release();

                Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
            }
        }).start();
    }


    private boolean loadFragment(Fragment fragment, boolean initialLoad) {
        if (fragment != null) {
            FragmentManager manager = getSupportFragmentManager();
            String backStateName = fragment.getClass().getName();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);
            // TODO: update bottomnavigationbar
            //fragment not in back stack, create it.
            if (!fragmentPopped) {
                manager
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        // exit if there's a single item on the stack as that means there's only the placeholder fragment on the stack
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            // TODO: update bottomnavigationbar
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.navigation_record_audio:
                fragment = new CaptureTalkFragment();
                break;
            case R.id.navigation_history:
                fragment = new HistoryFragment();
                break;
            default:
                break;
        }
        return loadFragment(fragment, true);
    }

    @Override
    public void onHistoryFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnCaptureTalkFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnSwitchRecordingState(Boolean shouldRecord) {
        mShouldContinue = shouldRecord;
        if (shouldRecord) {
            try {
                recordAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
