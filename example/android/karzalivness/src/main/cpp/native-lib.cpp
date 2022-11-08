#include <jni.h>
#include <string>
#include "android/log.h"
#include "FaceDetector.h"
#include "KFaceDetector.h"

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sdk_karzalivness_KFaceDetector_initDetector(JNIEnv *env, jclass clazz
                                                     , jstring location, jstring landmark_path) {
    const char* str = env->GetStringUTFChars(location, JNI_FALSE);
    const char* str2 = env->GetStringUTFChars(landmark_path, JNI_FALSE);
    return (jlong) new karza::KFaceDetector(std::string(str), std::string(str2));
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_sdk_karzalivness_KFaceDetector_init2(JNIEnv *env, jclass clazz, jstring face_model) {
    const char* str = env->GetStringUTFChars(face_model, JNI_FALSE);
    return (jlong) new karza::KFaceDetector(std::string(str));
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_sdk_karzalivness_KFaceDetector_findFaces(JNIEnv *env, jclass clazz, jlong object,
                                                 jintArray pixels, jint width, jint height,
                                                 jint channel) {
    unsigned char * image = ((karza::KFaceDetector*)object)->bgrInput(env, pixels);
//    LOGI("BGR conversion  %d", (image == nullptr));
//    LOGI("Pointer 1 %p", (karza::KFaceDetector*)object);
    auto infoArray = ((karza::KFaceDetector*)object)->detectFaces(image,width, height, channel);
    //To do java conversion from C++, we need to get the float pointer array into java type array.
    jfloatArray array = env->NewFloatArray(infoArray.size * 5);
    auto output = ((karza::KFaceDetector*)object)->processOutput(infoArray);
    env->SetFloatArrayRegion(array, 0, infoArray.size * 5, output);
    return array;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_sdk_karzalivness_KFaceDetector_releaseModel(JNIEnv *env, jclass clazz, jlong object) {
//    LOGI("Release Pointer Model Called %lld", object);
    if( object ){
//        LOGI("Pointer 2 %p", (karza::KFaceDetector*)object);
        free((karza::KFaceDetector*)object);
//        LOGI("Object Pointer Released");
    }
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_sdk_karzalivness_KFaceDetector_getLandMarks(JNIEnv *env, jclass clazz, jlong object,
                                                     jintArray pixels, jint width, jint height,
                                                     jint channel, jint x, jint y, jint rect_width,
                                                     jint rect_height) {
    unsigned char * image = ((karza::KFaceDetector*)object)->bgrInput(env, pixels);
//    LOGI("BGR conversion  %d", (image == nullptr));
    SeetaImageData imageData = {};
    imageData.height = height;
    imageData.width = width;
    imageData.channels = channel;
    imageData.data = image;
    SeetaRect rect = {x, y, rect_width, rect_height};
    jfloatArray array = env->NewFloatArray(10);
    auto output = ((karza::KFaceDetector*)object)->detectLandmark(imageData, rect);
    env->SetFloatArrayRegion(array, 0, 10, output);
    return array;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sdk_karzalivness_KFaceDetector_checkBrightness(JNIEnv *env, jclass clazz,
                                                        jintArray pixels) {
    jlong size = env->GetArrayLength(pixels);
    jint *body = env->GetIntArrayElements(pixels, JNI_FALSE);
    float r = 0, g = 0, b = 0;
    for (int i = 0; i < size; ++i) {
        int temp = body[i];
        r += (float) (temp >> 16 & 255);
        g += (float) (temp >> 8 & 255);
        b += (float) (temp & 255);
    }
    r = r / size;
    g = g / size;
    b = b / size;
    double b_val = sqrt((0.299 * pow(r, 2)) + (0.587 * pow(g, 2)) + (0.114 * pow(b, 2)));
    float bucket[] = {0.0, 28.33, 56.66, 85.0, 113.33,141.66, 170.0, 198.33, 226.66, 255.0};
    int brightness_index = 0;
    for (int i = 0; i < 10; ++i) {
        if (i == 0 && b_val <= bucket[i]) {
            return 0;
        } else if (b_val > bucket[i - 1] && b_val <= bucket[i]) {
            return i;
        }
    }
    return brightness_index;
}