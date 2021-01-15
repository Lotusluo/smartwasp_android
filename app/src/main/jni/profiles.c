#include "quiet-jni.h"

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_EncoderConfig_nativeOpen(
        JNIEnv *env, __unused  jobject This, jstring j_profiles, jstring j_key) {
    const char *profiles = (*env)->GetStringUTFChars(env, j_profiles, 0);
    const char *key = (*env)->GetStringUTFChars(env, j_key, 0);

    quiet_encoder_options *opt = quiet_encoder_profile_str(profiles, key);
    if (!opt) {
        throw_error(env, cache.java.illegal_arg_klass, encoder_profile_error_format,
                    quiet_get_last_error());
    }

    (*env)->ReleaseStringUTFChars(env, j_key, key);
    (*env)->ReleaseStringUTFChars(env, j_profiles, profiles);

    return jvm_opaque_pointer(opt);
}

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_EncoderConfig_nativeFree(
        JNIEnv *env, jobject This) {
    jlong j_profile = (*env)->GetLongField(env, This, cache.encoder_profile.ptr);
    quiet_encoder_options *opt = (quiet_encoder_options *) recover_pointer(j_profile);
    free(opt);
}

JNIEXPORT jlong JNICALL Java_cn_iflyos_open_library_DecoderConfig_nativeOpen(
        JNIEnv *env, __unused jobject This, jstring j_profiles, jstring j_key) {
    const char *profiles = (*env)->GetStringUTFChars(env, j_profiles, 0);
    const char *key = (*env)->GetStringUTFChars(env, j_key, 0);

    quiet_decoder_options *opt = quiet_decoder_profile_str(profiles, key);
    if (!opt) {
        throw_error(env, cache.java.illegal_arg_klass, decoder_profile_error_format,
                    quiet_get_last_error());
    }

    (*env)->ReleaseStringUTFChars(env, j_key, key);
    (*env)->ReleaseStringUTFChars(env, j_profiles, profiles);

    return jvm_opaque_pointer(opt);
}

JNIEXPORT void JNICALL Java_cn_iflyos_open_library_DecoderConfig_nativeFree(
        JNIEnv *env, jobject This) {
    jlong j_profile = (*env)->GetLongField(env, This, cache.decoder_profile.ptr);
    quiet_decoder_options *opt = (quiet_decoder_options *) recover_pointer(j_profile);
    free(opt);
}
