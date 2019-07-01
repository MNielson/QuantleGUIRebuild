package android.example.quantleguirebuild;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.util.Log;
import android.view.MenuItem;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener
        , HistoryFragment.OnHistoryFragmentInteractionListener
        , CaptureTalkFragment.OnCaptureTalkFragmentInteractionListener {

    // Requesting permission to RECORD_AUDIO
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    final String LOG_TAG = "MainActivity";
    boolean mShouldContinue;
    private Handler mHandler;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private BufferProcessor mBufferProcessor;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        BottomNavigationView navigation = findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(this);
        loadFragment(new CaptureTalkFragment(), false);

        mBufferProcessor = new BufferProcessor(this, getApplicationContext());

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
                mBufferProcessor.process(audioBuffer);
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
            //TODO check if we went to home fragment
            // if we went to home, remove (most of) stack
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
        //TODO check if we went to home fragment
        // if we went to home, remove (most of) stack
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();
        } else {
            // TODO: update bottomnavigationbar
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
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

    @Override
    public void OnClickFromFile() {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, Constants.SINGLE_FILE);
    }

    @Override
    public void OnClickStopAndSave() {
        
    }

    @Override
    public void OnClickClearAndReset(String speaker, String event) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //TODO: start thread and work there

        switch (requestCode) {
            case (Constants.SINGLE_FILE):
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    InputStream is;
                    try {
                        is = getContentResolver().openInputStream(uri);
                        byte[] tbytes = IOUtils.toByteArray(is);
                        //skip wav header
                        byte[] bytes = new byte[tbytes.length - 44];
                        bytes = Arrays.copyOfRange(tbytes, 44, tbytes.length);
                        short[] content = new short[bytes.length / 2];
                        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(content);


                        int numBuffers = (int) Math.round(Math.ceil((double) content.length / Constants.ONE_BUFFER_LEN));
                        boolean lastNeedsPadding = (((double) content.length / Constants.ONE_BUFFER_LEN) % 1 != 0);

                        for (int i = 0; i < numBuffers; i++) {
                            short[] b = new short[Constants.ONE_BUFFER_LEN];
                            if ((i + 1) == numBuffers && lastNeedsPadding) {
                                Arrays.fill(b, (short) 0);
                                b = Arrays.copyOfRange(content, i * Constants.ONE_BUFFER_LEN, content.length);
                            } else
                                b = Arrays.copyOfRange(content, i * Constants.ONE_BUFFER_LEN, (i + 1) * Constants.ONE_BUFFER_LEN);
                            mBufferProcessor.process(b);
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                }
        }

        // thread work ends here
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean writeTalkToJson(OutputStream out, TalkInfo talkInfo){
        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("talkDuration").value(talkInfo.talkDuration);
            writer.name("numSyllables").value(talkInfo.numSyllables);
            writer.name("numWords").value(talkInfo.numWords);
            writer.name("numPauses").value(talkInfo.numPauses);
            writer.name("numClauses").value(talkInfo.numClauses);
            writer.name("sumPauseDuration").value(talkInfo.sumPauseDuration);

            writer.name("paceHistogram");  writeIntArray(writer, talkInfo.paceHistogram);
            writer.name("pausesByLength"); writeIntArray(writer, talkInfo.pausesByLength);
            writer.name("pitchHistogram"); writeIntArray(writer, talkInfo.pitchHistogram);
            writer.name("powerHistogram"); writeIntArray(writer, talkInfo.powerHistogram);

            writer.name("volumaxi"); writeFloatArray(writer, talkInfo.volumaxi);
            writer.name("pitches");  writeFloatArray(writer, talkInfo.pitches);

            writer.name("flesch_reading_ease").value(talkInfo.flesch_reading_ease);
            writer.name("flesch_kincaid_grade_ease").value(talkInfo.flesch_kincaid_grade_ease);
            writer.name("gunning_fog_index").value(talkInfo.gunning_fog_index);
            writer.name("forecast_grade_level").value(talkInfo.forecast_grade_level);
            writer.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void writeIntArray(JsonWriter writer, int[] arr) throws IOException {
        writer.beginArray();
        for(int i = 0; i<arr.length; i++)
            writer.value(arr[i]);
        writer.endArray();
    }
    private void writeFloatArray(JsonWriter writer, float[] arr) throws IOException {
        writer.beginArray();
        for(int i = 0; i<arr.length; i++)
            writer.value(arr[i]);
        writer.endArray();
    }
}
