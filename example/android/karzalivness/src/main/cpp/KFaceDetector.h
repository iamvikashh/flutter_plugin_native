//
// Created by Pawan Bhardwaj on 22/06/21.
//

#include <jni.h>
#include <string>
#include "FaceDetector.h"
#include "FaceLandmarker.h"
#include "android/log.h"

#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "native-lib", __VA_ARGS__))

#ifndef LIVENESDK_KFACEDETECTOR_H
#define LIVENESDK_KFACEDETECTOR_H

namespace karza {

    class KFaceDetector {

    seeta::FaceDetector faceDetector;

    seeta::FaceLandmarker *faceLandmarker{};

    public:
        KFaceDetector(std::string path, std::string path2);

        KFaceDetector(std::string path);

        ~KFaceDetector();

        unsigned char *bgrInput(JNIEnv *env, jintArray image_data);

        SeetaFaceInfoArray detectFaces(unsigned char * data, int width, int height, int channels);

        float * processOutput(SeetaFaceInfoArray infoArray);

        float * detectLandmark(const SeetaImageData &image, const SeetaRect &face);

    };
}

#endif //LIVENESDK_KFACEDETECTOR_H

