#include "quiet-jni.h"

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeCreate(
        JNIEnv *env, __unused jobject This, jobject conf, jint sample_rate) {
    jlong j_profile = (*env)->GetLongField(env, conf, cache.decoder_profile.ptr);
    quiet_decoder_options *opt = (quiet_decoder_options *) recover_pointer(j_profile);

    quiet_decoder *dec = quiet_decoder_create(opt, sample_rate);

    return jvm_opaque_pointer(dec);
}

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeDestroy(
        JNIEnv *env, jobject This) {
    jlong j_dec = (*env)->GetLongField(env, This, cache.decoder.ptr);
    quiet_decoder *dec = (quiet_decoder *) recover_pointer(j_dec);

    if (dec == NULL) return;

    quiet_decoder_close(dec);
    quiet_decoder_destroy(dec);
}

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeConsume(
        JNIEnv *env, jobject This, jfloatArray sample, jlong offset, jlong len) {
    jlong j_dec = (*env)->GetLongField(env, This, cache.decoder.ptr);
    quiet_decoder *dec = (quiet_decoder *) recover_pointer(j_dec);

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

    quiet_decoder_consume(dec, buf + offset, (size_t) len);

    (*env)->ReleaseFloatArrayElements(env, sample, buf, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeConsumeBytes(
        JNIEnv *env, jobject This, jbyteArray sample, jlong offset, jlong len) {
    jlong j_dec = (*env)->GetLongField(env, This, cache.decoder.ptr);
    quiet_decoder *dec = (quiet_decoder *) recover_pointer(j_dec);

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

    float *pcm = malloc(sizeof(float) * (len / 2));
    size_t pcm_len = from_16bit(buf + offset, pcm, (size_t) len);

    quiet_decoder_consume(dec, pcm, pcm_len);

    (*env)->ReleaseByteArrayElements(env, sample, buf, JNI_ABORT);
    free(pcm);
}

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeFlush(
        JNIEnv *env, jobject This) {
    jlong j_dec = (*env)->GetLongField(env, This, cache.decoder.ptr);
    quiet_decoder *dec = (quiet_decoder *) recover_pointer(j_dec);

    quiet_decoder_flush(dec);
}

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_QuietDecoder_nativeReceive(
        JNIEnv *env, jobject This, jbyteArray frame, jlong offset, jlong len) {
    jlong j_dec = (*env)->GetLongField(env, This, cache.decoder.ptr);
    quiet_decoder *dec = (quiet_decoder *) recover_pointer(j_dec);

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

    ssize_t received = quiet_decoder_recv(dec, (uint8_t *) buf + offset, (size_t) len);
    if (received < 0) {
        received = 0;
        quiet_error err = quiet_get_last_error();
        if (err != quiet_would_block) {
            throw_error(env, cache.java.io_exception_klass, "Unspecified I/O Error %d", err);
        }
    }

    (*env)->ReleaseByteArrayElements(env, frame, buf, 0);

    return received;
}
