package android.example.quantleguirebuild;

/**
 * Created by Matthias on 01.03.2018.
 */

public class PitchDetector {
    static {
        System.loadLibrary("native-lib");
    }
    // Used by JNI to store pointer of CPP side
    @SuppressWarnings("unused")
    private long nativeHandle;

    private native void initCppSide();
    private native double computePitchNative(double[] samples, int startSample, int sampleCount);
    private native double testNative(double [] samples, int sampleCount);

    public PitchDetector(){
        initCppSide();
    }

    public double computePitch(double[] samples, int startSample, int sampleCount)
    {
        return computePitchNative(samples, startSample, sampleCount);
    }

    public double computeSumNative(double[] samples)
    {
        return testNative(samples, samples.length);
    }
}
