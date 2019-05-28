#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_martin_mytranvice_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C" JNIEXPORT void JNICALL
Java_com_example_martin_mytranvice_Mp3Converter_init(JNIEnv *env, jclass type, jint inSampleRate,
jint channel, jint mode, jint outSampleRate,
        jint outBitRate, jint quality) {
    lameInit(inSampleRate, channel, mode, outSampleRate, outBitRate, quality);
}