#include<jni.h>
#include<string.h>

jstring Java_com_bestfriend_cache_SQLiteCache_key(JNIEnv* env, jobject obj) {
    return (*env) -> NewStringUTF(env, "xs4CeFfBduKpvE5G");
}