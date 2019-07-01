package android.example.quantleguirebuild;

/**
 * Created by Matthias Niel on 15.04.2019.
 */

import android.app.Activity;
import android.content.Context;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class BufferProcessor {
    HandlerThread myThread;
    Looper mLooper;
    MyBufferHandler mHandler;


    public BufferProcessor(Activity activity, Context context){
        this("Buffer Handling Thread", activity, context);
    }

    public BufferProcessor(String name, Activity activity, Context context){
        myThread = new HandlerThread(name);
        myThread.start();
        mLooper = myThread.getLooper();
        mHandler = new MyBufferHandler(mLooper, activity, context);
    }

    public void process(short[] content){
        Message msg = mHandler.obtainMessage();
        msg.obj = content;
        //msg.obj = Arrays.asList(ArrayUtils.toObject(content));
        mHandler.sendMessage(msg);
    }

}