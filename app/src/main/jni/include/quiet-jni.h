#include <jni.h>
#include <quiet.h>

#ifndef __QUIET_JNI_H_
#define __QUIET_JNI_H_

jlong jvm_opaque_pointer(void *p);

void *recover_pointer(jlong p);

void throw_error(JNIEnv *env, jclass exc_class, const char *err_fmt, ...);

size_t to_16bit(float *src, int8_t *dst, size_t len);

size_t from_16bit(int8_t *src, float *dst, size_t len);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_EncoderConfig_nativeOpen
        (JNIEnv *, jobject, jstring, jstring);

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_EncoderConfig_nativeFree
        (JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_DecoderConfig_nativeOpen
        (JNIEnv *, jobject, jstring, jstring);

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_DecoderConfig_nativeFree
        (JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeCreate
        (JNIEnv *, jobject, jobject, jint);

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeDestroy
        (JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeSend
        (JNIEnv *, jobject, jbyteArray, jlong, jlong);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeGetFrameLength
        (JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeEmit
        (JNIEnv *, jobject, jfloatArray, jlong, jlong);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeEmitBytes
        (JNIEnv *, jobject, jbyteArray, jlong, jlong);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeCreate
        (JNIEnv *, jobject, jobject, jint);

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeDestroy
        (JNIEnv *, jobject);

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeConsume
        (JNIEnv *, jobject, jfloatArray, jlong, jlong);

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeConsumeBytes
        (JNIEnv *, jobject, jbyteArray, jlong, jlong);

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeFlush
        (JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeReceive
        (JNIEnv *, jobject, jbyteArray, jlong, jlong);

typedef struct {
    jclass eof_exception_klass;
    jclass io_exception_klass;
    jclass out_of_memory_error_klass;
    jclass illegal_arg_klass;
} java_java_cache;

typedef struct {
    jclass klass;
    jfieldID ptr;
} java_encoder_profile_cache;

typedef struct {
    jclass klass;
    jfieldID ptr;
} java_decoder_profile_cache;

typedef struct {
    jclass klass;
    jfieldID ptr;
} java_encoder_cache;

typedef struct {
    jclass klass;
    jfieldID ptr;
} java_decoder_cache;

typedef struct {
    java_java_cache java;
    java_encoder_profile_cache encoder_profile;
    java_decoder_profile_cache decoder_profile;
    java_encoder_cache encoder;
    java_decoder_cache decoder;
} java_cache;

extern java_cache cache;

extern const char *encoder_profile_error_format;
extern const char *decoder_profile_error_format;

#endif // __QUIET_JNI_H_
