package android.example.quantleguirebuild;

/**
 * Created by Matthias Niel on 26.03.2019.
 */
public class Constants {
    public static final int ONE_BUFFER_LEN = 2048;
    public static final int SAMPLE_RATE = 44100; // 16k for speech. 44.1k for music.

    public static final String BUFFER = "buffer";
    public static final String TALK_INFO = "talk_info";

    //////////////////
    //REQUEST CODES //
    /////////////////
    public static final int SINGLE_FILE    = 1;
    public static final int MULTIPLE_FILES = 3;
}
