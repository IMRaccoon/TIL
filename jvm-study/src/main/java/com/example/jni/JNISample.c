#include <jni.h>
#include "com_example_jni_JNISample.h"

JNIEXPORT void JNICALL Java_com_example_jni_JNISample_nativeMethod(JNIEnv *env, jobject obj) {
       printf("JNI는 이렇게 동작해요");
}
