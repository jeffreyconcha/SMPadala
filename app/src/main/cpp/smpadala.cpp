#include<jni.h>
#include<string>
#include <iostream>

extern "C" JNIEXPORT jstring JNICALL
Java_com_bestfriend_cache_SQLiteCache_key(JNIEnv *env, jclass) {
    std::string api = "xs4CeFfBduKpvE5G";
    return env->NewStringUTF(api.c_str());
}