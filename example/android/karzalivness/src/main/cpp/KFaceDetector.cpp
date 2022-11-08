//
// Created by Pawan Bhardwaj on 22/06/21.
//

#include "KFaceDetector.h"
#include <iostream>
#include <fstream>

unsigned char * input_image;

//void write_to_file(unsigned char *, int);

karza::KFaceDetector::KFaceDetector(const std::string path, const std::string path2) : faceDetector(
        {seeta::ModelSetting(path, SeetaDevice::SEETA_DEVICE_AUTO)}) {
//    LOGI("%s - %p", "KFaceDetector constructor called", &faceDetector);
    faceLandmarker = new seeta::FaceLandmarker(seeta::ModelSetting(path2
            , SeetaDevice::SEETA_DEVICE_AUTO));
}


karza::KFaceDetector::KFaceDetector(std::string path) : faceDetector(
        {seeta::ModelSetting(path, SeetaDevice::SEETA_DEVICE_AUTO)}) {
    faceLandmarker = nullptr;
}


unsigned char *karza::KFaceDetector::bgrInput(JNIEnv *env, jintArray image_data) {
    //To save memory as char is of 1 byte and we have pixel/color values from 0-255, we give return type as char
    //and not int.
    jlong size = env->GetArrayLength(image_data);
    jint *body = env->GetIntArrayElements(image_data, JNI_FALSE);
    if (input_image != nullptr){
        free(input_image);
    }
    input_image = new unsigned char [size * 3];
    int i = 0;
    for (int var11 = 0; i < size; ++i) {
        auto pixel = body[i];
        auto r = (float)(pixel >> 16 & 255);
        auto g = (float)(pixel >> 8 & 255);
        auto b = (float)(pixel & 255);
        input_image[var11++] = b;
        input_image[var11++] = g;
        input_image[var11++] = r;
    }
    return input_image;
}

SeetaFaceInfoArray karza::KFaceDetector::detectFaces(unsigned char *data, int width
                                                     , int height, int channels) {
//    LOGI("%s %d, %d, %d %p", "detectFaces : ", width, height, channels, data);
    SeetaImageData imageData = {};
    imageData.height = height;
    imageData.width = width;
    imageData.channels = channels;
    imageData.data = data;
//    std::chrono::milliseconds ms = duration_cast< std::chrono::milliseconds >(
//            std::chrono::system_clock::now().time_since_epoch()
//    );
    SeetaFaceInfoArray array = faceDetector.detect(imageData);
//    std::chrono::milliseconds ms2 = duration_cast< std::chrono::milliseconds >(
//            std::chrono::system_clock::now().time_since_epoch()
//    );
//    LOGI("Infer time %lld", (ms2.count()- ms.count()));
    return array;
}

karza::KFaceDetector::~KFaceDetector() {
    delete input_image;
    input_image = nullptr;
    delete faceLandmarker;
}

float *karza::KFaceDetector::processOutput(SeetaFaceInfoArray infoArray) {
    auto * output = new float[infoArray.size * 5];
    int index = 0;
    for (int i = 0; i < infoArray.size; ++i) {
//        LOGI("%s", "Step 4");
        auto &face = infoArray.data[i];
        auto &pos = face.pos;
        output[index++] = face.score;
        output[index++] = pos.x;
        output[index++] = pos.y;
        output[index++] = pos.width;
        output[index++] = pos.height;
//        LOGI("Score %f Pos ( Width %d,  Height %d, X %d, Y %d)", face.score, pos.width, pos.height, pos.x, pos.y);
    }
    return output;
}

float *karza::KFaceDetector::detectLandmark(const SeetaImageData &image, const SeetaRect &face) {
    auto landMarks = faceLandmarker->mark(image, face);
    auto * output = new float[landMarks.size() * 2];
    int index = 0;
    for (auto &point : landMarks){
//        LOGI("X = %f - Y = %f", point.x, point.y);
        output[index++] = point.x;
        output[index++] = point.y;
    }
    return output;
}

void write_to_file(unsigned char * data, int size){
    char name[100];
    sprintf(name,"%s_.txt", "/sdcard/Download/test_con");
    std::ofstream file(name);
    if (file.is_open()){
        for(int count = 0; count < size; count ++){
            file << (int)data[count] << "," ;
        }
    }
//    LOGI("%s : file name  %s", "File written", name);
}

