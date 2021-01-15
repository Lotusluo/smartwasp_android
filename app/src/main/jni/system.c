#include "quiet-jni.h"

jlong jvm_opaque_pointer(void *p) { return (jlong) (intptr_t) p; }

void *recover_pointer(jlong p) { return (void *) (intptr_t) p; }

void throw_error(JNIEnv *env, jclass exc_class, const char *err_fmt, ...) {
    char *err_msg;
    va_list err_args;
    va_start(err_args, err_fmt);
    vasprintf(&err_msg, err_fmt, err_args);
    va_end(err_args);
    (*env)->ThrowNew(env, exc_class, err_msg);
    free(err_msg);
}

size_t to_16bit(float *src, int8_t *dst, size_t len) {
    int16_t *buf = (int16_t *) dst;
    float base = 1 << 15; // 2 ^ 15
    for (int i = 0; i < len; i++) {
        float sample = src[i];
        if (sample > 1.0f) sample = 1.0f;
        if (sample < -1.0f) sample = -1.0f;
        buf[i] = (int16_t) (sample * base);
    }
    return len * 2;
}

size_t from_16bit(int8_t *src, float *dst, size_t len) {
    size_t buf_len = len / 2;
    int16_t *buf = (int16_t *) src;
    float base = 1 << 15; // 2 ^ 15
    for (int i = 0; i < buf_len; i++) {
        dst[i] = (float) buf[i] / base;
    }
    return buf_len;
}

const char *encoder_profile_error_format = "invalid quiet encoder profile, quiet error code=%04d";
const char *decoder_profile_error_format = "invalid quiet decoder profile, quiet error code=%04d";

java_cache cache;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, __unused void *reserved) {
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    // classes
    jclass localEOF =
            (*env)->FindClass(env, "java/io/EOFException");

    cache.java.eof_exception_klass = (jclass) ((*env)->NewGlobalRef(env, localEOF));
    (*env)->DeleteLocalRef(env, localEOF);

    jclass localIO =
            (*env)->FindClass(env, "java/io/IOException");

    cache.java.io_exception_klass = (jclass) ((*env)->NewGlobalRef(env, localIO));
    (*env)->DeleteLocalRef(env, localIO);

    jclass localIllegalArg =
            (*env)->FindClass(env, "java/lang/IllegalArgumentException");
    cache.java.illegal_arg_klass = (jclass) ((*env)->NewGlobalRef(env, localIllegalArg));
    (*env)->DeleteLocalRef(env, localIllegalArg);

    jclass localEncoderProfileClass =
            (*env)->FindClass(env, "cn/iflyos/open/library/EncoderConfig");
    cache.encoder_profile.klass = (jclass) ((*env)->NewGlobalRef(env, localEncoderProfileClass));
    (*env)->DeleteLocalRef(env, localEncoderProfileClass);

    jclass localDecoderProfileClass =
            (*env)->FindClass(env, "cn/iflyos/open/library/DecoderConfig");
    cache.decoder_profile.klass = (jclass) ((*env)->NewGlobalRef(env, localDecoderProfileClass));
    (*env)->DeleteLocalRef(env, localDecoderProfileClass);

    jclass localEncoderClass =
            (*env)->FindClass(env, "cn/iflyos/open/library/QuietEncoder");

    cache.encoder.klass = (jclass) ((*env)->NewGlobalRef(env, localEncoderClass));
    (*env)->DeleteLocalRef(env, localEncoderClass);

    jclass localDecoderClass =
            (*env)->FindClass(env, "cn/iflyos/open/library/QuietDecoder");

    cache.decoder.klass = (jclass) ((*env)->NewGlobalRef(env, localDecoderClass));
    (*env)->DeleteLocalRef(env, localDecoderClass);

    // fields
    cache.encoder_profile.ptr =
            (*env)->GetFieldID(env, cache.encoder_profile.klass, "profile_ptr", "J");

    cache.decoder_profile.ptr =
            (*env)->GetFieldID(env, cache.decoder_profile.klass, "profile_ptr", "J");

    cache.encoder.ptr =
            (*env)->GetFieldID(env, cache.encoder.klass, "enc_ptr", "J");

    cache.decoder.ptr =
            (*env)->GetFieldID(env, cache.decoder.klass, "dec_ptr", "J");

    return JNI_VERSION_1_4;
}
