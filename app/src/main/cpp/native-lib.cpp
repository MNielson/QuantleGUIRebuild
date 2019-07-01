#include <jni.h>
#include <string>
#include "handle.h"
#include "PitchDetector.h"


extern "C" {
JNIEXPORT jstring JNICALL

Java_android_example_quantleguirebuild_PitchDetector_stringFromJNI(JNIEnv *env,
                                                                   jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


JNIEXPORT void JNICALL
Java_android_example_quantleguirebuild_PitchDetector_initCppSide(JNIEnv *env, jobject obj) {
    PitchDetector *inst = new PitchDetector();
    setHandle(env, obj, inst);


}

JNIEXPORT jdouble JNICALL
Java_android_example_quantleguirebuild_PitchDetector_computePitchNative(JNIEnv *env, jobject obj, jdoubleArray jarray,
                                                     jint startsample, jint samplecount) {
    PitchDetector *inst = getHandle<PitchDetector>(env, obj);
    return (jdouble) inst->computePitch(env->GetDoubleArrayElements(jarray, 0), startsample,
                                        samplecount);
}

JNIEXPORT jdouble JNICALL
Java_android_example_quantleguirebuild_PitchDetector_testNative(JNIEnv *env, jobject obj, jdoubleArray jarray,
                                             jint samplecount) {
    PitchDetector *inst = getHandle<PitchDetector>(env, obj);
    return (jdouble) inst->testMe(env->GetDoubleArrayElements(jarray, 0), samplecount);
}


/*
JNIEXPORT void JNICALL
Java_pitchdetector_PitchDetectorWrapper_initCppSide(JNIEnv *env, jobject obj) {
    PitchDetector *inst = new PitchDetector();
    setHandle(env, obj, inst);
}


JNIEXPORT jdouble JNICALL
Java_pitchdetector_PitchDetectorWrapper_computePitch(JNIEnv *env, jobject obj, jdoubleArray jarray,
                                                     jint startsample, jint samplecount) {
    PitchDetector *inst = getHandle<PitchDetector>(env, obj);
    return (jdouble) inst->computePitch(env->GetDoubleArrayElements(jarray, 0), startsample,
                                        samplecount);
}


JNIEXPORT jdouble JNICALL
Java_pitchdetector_PitchDetectorWrapper_test(JNIEnv *env, jobject obj, jdoubleArray jarray,
                                             jint samplecount) {
    PitchDetector *inst = getHandle<PitchDetector>(env, obj);
    return (jdouble) inst->testMe(env->GetDoubleArrayElements(jarray, 0), samplecount);
}
*/
}