#include <iostream>
#include <string>
#include <vector>
#include <cstring>

#include <headers/nl_thedutchmc_betterplayer_natives_DeepSpeechNativeInterface.h>
#include <errno.h>

#include <samplerate.h>

#define LOG(log) std::cout << log << std::endl;

/**
 * Convert a jstring to a std::string
 * @param env JNIEnv
 * @param jStr jstring to convert
 * @returns Converted jstring to std::string
 */
std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

/**
 * Convert jbyteArray to std::vector<unsigned char>
 * @param env JNIEnv
 * @param jbytes The jbyteArray to convert
 * @returns Converted jbyteArray to std::vector<unsigned char>
 */
std::vector<unsigned char> as_unsigned_char_vector(JNIEnv *env, jbyteArray jbytes) {
    int jbytes_len = env->GetArrayLength(jbytes);
    std::vector<unsigned char> buff(jbytes_len +1, 0);
    env->GetByteArrayRegion(jbytes, 0, jbytes_len, reinterpret_cast<jbyte*>(buff.data()));
    
    return buff;
}

/**
 * Downsample audio
 * 
 * WIP!
 * 
 * @param data_in Pointer to input data
 * @param data_out Pointer to where to put the output data
 */
void downsample(unsigned char* data_in, unsigned char* data_out) {
    int* ERROR = NULL;
    float* data_out_f;

    float* data_in_f = (float*) *data_in;

    int bit_depth = 16;
    int channels_in = 2;
    int channels_out = 1;
    int sample_rate_in = 48000;
    int sample_rate_out = 16000;

    int input_frames_count = (sample_rate_in /1000 * 20)*2;
    int output_frames_count = (sample_rate_out /1000 * 20)*1;

    /*
    long frame_size_in_bits = 1024 * bit_depth * channels_in;
    long frame_size_out_bits = 1024 * bit_depth * channels_in;

    int frame_size_in_bytes = frame_size_in_bits/8;
    int frame_size_out_bytes = frame_size_out_bits/8;

    int input_frames_count = sizeof(*data_in) / frame_size_in_bytes;
    int output_frames_count = sizeof(*data_out) / frame_size_out_bytes;
    */

    SRC_STATE* src_state = src_new(1, 2, ERROR);

    SRC_DATA* src_data = new SRC_DATA();
    src_data->data_in = data_in_f;
    src_data->data_out = data_out_f;
    src_data->end_of_input = 0;
    src_data->input_frames = input_frames_count;
    src_data->output_frames = output_frames_count;
    src_data->src_ratio = sample_rate_in / sample_rate_out;
    
    int returnCode = src_process(src_state, src_data);
}

/**
 * JNI Function
 * 
 * Called from nl.thedutchmc.betterplayer.natvies.DeepSpeechNativeInterface#callNativeMethod(byte[] bytes)
 */
JNIEXPORT void JNICALL Java_nl_thedutchmc_betterplayer_natives_DeepSpeechNativeInterface_callNativeMethod(JNIEnv *env, jobject jobj, jbyteArray jbytearray) {
    std::cout << "Size: " << as_unsigned_char_vector(env, jbytearray).size() << std::endl;

    std::vector<unsigned char> vec_jbytes = as_unsigned_char_vector(env, jbytearray);

    unsigned char arr_jbytes[vec_jbytes.size()]; 
    std::copy(vec_jbytes.begin(), vec_jbytes.end(), arr_jbytes);

    unsigned char* out_bytes;
    std::memset(out_bytes, 0, sizeof(arr_jbytes));
    downsample(arr_jbytes, out_bytes);
}