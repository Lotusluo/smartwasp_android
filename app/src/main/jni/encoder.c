#include "quiet-jni.h"

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeCreate(
        JNIEnv *env, __unused jobject This, jobject conf, jint sample_rate) {
    jlong j_profile = (*env)->GetLongField(env, conf, cache.encoder_profile.ptr);
    quiet_encoder_options *opt = (quiet_encoder_options *) recover_pointer(j_profile);

    quiet_encoder *enc = quiet_encoder_create(opt, sample_rate);

    return jvm_opaque_pointer(enc);
}

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeDestroy(
        JNIEnv *env, jobject This) {
    jlong j_enc = (*env)->GetLongField(env, This, cache.encoder.ptr);
    quiet_encoder *enc = (quiet_encoder *) recover_pointer(j_enc);

    quiet_encoder_destroy(enc);
}

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeSend(
        JNIEnv *env, jobject This, jbyteArray frame, jlong offset, jlong len) {
    jlong j_enc = (*env)->GetLongField(env, This, cache.encoder.ptr);
    quiet_encoder *enc = (quiet_encoder *) recover_pointer(j_enc);

    jbyte *buf = (*env)->GetByteArrayElements(env, frame, NULL);
    size_t buf_len = (size_t) (*env)->GetArrayLength(env, frame);

    if (offset < 0) {
        offset = 0;
    }
    if (offset >= buf_len) {
        offset = buf_len - 1;
    }
    if (len < 0) {
        len = 0;
    }
    if (len > (buf_len - offset)) {
        len = buf_len - offset;
    }

    ssize_t sent = quiet_encoder_send(enc, buf + offset, (size_t) len);

    if (sent < 0) {
        sent = 0;
        quiet_error err = quiet_get_last_error();
        throw_error(env, cache.java.io_exception_klass, "Unspecified I/O Error %d", err);
    }

    (*env)->ReleaseByteArrayElements(env, frame, buf, JNI_ABORT);

    return sent;
}

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeEmit(
        JNIEnv *env, jobject This, jfloatArray sample, jlong offset, jlong len) {
    jlong j_enc = (*env)->GetLongField(env, This, cache.encoder.ptr);
    quiet_encoder *enc = (quiet_encoder *) recover_pointer(j_enc);

    jfloat *buf = (*env)->GetFloatArrayElements(env, sample, NULL);
    size_t buf_len = (size_t) (*env)->GetArrayLength(env, sample);

    if (offset < 0) {
        offset = 0;
    }
    if (offset >= buf_len) {
        offset = buf_len - 1;
    }
    if (len < 0) {
        len = 0;
    }
    if (len > (buf_len - offset)) {
        len = buf_len - offset;
    }

    ssize_t emitted = quiet_encoder_emit(enc, buf + offset, (size_t) len);

    if (emitted == 0) {
        throw_error(env, cache.java.eof_exception_klass, "EOF");
    } else if (emitted < 0) {
        emitted = 0;
        quiet_error err = quiet_get_last_error();
        if (err != quiet_would_block) {
            throw_error(env, cache.java.io_exception_klass, "Unspecified I/O Error %d", err);
        }
    }

    (*env)->ReleaseFloatArrayElements(env, sample, buf, 0);

    return emitted;
}

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietEncoder_nativeEmitBytes(
        JNIEnv *env, jobject This, jbyteArray sample, jlong offset, jlong len) {
    jlong j_enc = (*env)->GetLongField(env, This, cache.encoder.ptr);
    quiet_encoder *enc = (quiet_encoder *) recover_pointer(j_enc);

    jbyte *buf = (*env)->GetByteArrayElements(env, sample, NULL);
    size_t buf_len = (size_t) (*env)->GetArrayLength(env, sample);

    if (offset < 0) {
        offset = 0;
    }
    if (offset >= buf_len) {
        offset = buf_len - 1;
    }
    if (len % 2 == 1) {
        len -= 1;
    }
    if (len < 0) {
        len = 0;
    }
    if (len > (buf_len - offset)) {
        len = buf_len - offset;
    }

    float *src = malloc(sizeof(float) * (len / 2));
    ssize_t emitted = quiet_encoder_emit(enc, src, (size_t) (len / 2));

    if (emitted == 0) {
        throw_error(env, cache.java.eof_exception_klass, "EOF");
    } else if (emitted < 0) {
        emitted = 0;
        quiet_error err = quiet_get_last_error();
        if (err != quiet_would_block) {
            throw_error(env, cache.java.io_exception_klass, "Unspecified I/O Error %d", err);
        }
    } else {
        emitted = to_16bit(src, buf + offset, (size_t) emitted);
    }

    (*env)->ReleaseByteArrayElements(env, sample, buf, 0);
    free(src);

    return emitted;
}
