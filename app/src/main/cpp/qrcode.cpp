#include <jni.h>
#include <string>

extern "C" JNIEXPORT jbyteArray

JNICALL
Java_com_ysq_jni_DataHandler_arrayFromJNI(
        JNIEnv *env, jobject,
        jbyteArray originalArray, jint w, jint h) {
    jsize size = env->GetArrayLength(originalArray);
    jbyteArray resultArray = env->NewByteArray(size);
    jbyte *resultArr = env->GetByteArrayElements(resultArray, NULL);
    jbyte *originalArr = env->GetByteArrayElements(originalArray, NULL);
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            resultArr[x * h + h - y - 1] = originalArr[x + y * w];
        }
    }
    env->ReleaseByteArrayElements(resultArray, resultArr, 0);
    return resultArray;
}
