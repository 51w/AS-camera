//
// Created by Administrator on 2018/5/19.
//

#include <stdio.h>
#include <vector>
#include <opencv2/opencv.hpp>
#include <sys/time.h>

#include "mobilenet.hpp"
#include "com_es_ehuman_EHuman.h"

#include <android/log.h>

#define TAG "ESAppEHuman"
#define LOGD(TAG, ...) __android_log_print(ANDROID_LOG_DEBUG  , TAG,__VA_ARGS__)


char *jstringToChar(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("UTF-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

JNIEXPORT void JNICALL Java_com_es_ehuman_EHuman_nInitMobilenet
  (JNIEnv *env, jclass jcls, jstring binPath, jstring protoPath) {
    init_mobilenet(jstringToChar(env, protoPath), jstringToChar(env, binPath));
  }

char *save_path_ptr;
cv::Mat t_frame;

JNIEXPORT void JNICALL Java_com_es_ehuman_EHuman_nDetectHuman
  (JNIEnv *env, jclass jcls, jobject resArrayList, jbyteArray nv21ByteArr, jint nv21Len, jint nv21Width, jint nv21Height,jboolean saveInput, jstring savePath) {
        jclass cls_array_list = env->FindClass("java/util/ArrayList");
        jmethodID mid_add = env->GetMethodID(cls_array_list, "add", "(Ljava/lang/Object;)Z");

        jbyte * nv21_jbyte_ptr = (jbyte*)env->GetByteArrayElements(nv21ByteArr, 0);

        cv::Mat in_image_mat(nv21Height + nv21Height/2, nv21Width,CV_8UC1,(unsigned char *)nv21_jbyte_ptr);
        cv::cvtColor(in_image_mat,t_frame,CV_YUV420sp2BGR);
        std::vector<Object> objects;
        LOGD(TAG, "detect_mobilenet start");
        detect_mobilenet(t_frame, objects);
        LOGD(TAG, "detect_mobilenet end");

        if(saveInput) {
            save_path_ptr = jstringToChar(env, savePath);
            cv::imwrite(save_path_ptr,t_frame);
        }

        for(int i = 0;i<objects.size();++i)
        {
            Object object = objects.at(i);
            if(object.prob > 0.4 && object.class_id == 15)
            {
                jintArray res_arr;
                res_arr = env->NewIntArray(4);
                jint* res_ptr = env->GetIntArrayElements(res_arr,0);
                res_ptr[0] = object.rec.x;
                res_ptr[1] = object.rec.y;
                res_ptr[2] = object.rec.width;
                res_ptr[3] = object.rec.height;
                env->SetIntArrayRegion(res_arr, 0, 4, res_ptr);
                env->ReleaseIntArrayElements(res_arr, res_ptr, 0);
                env->CallBooleanMethod(resArrayList, mid_add, res_arr);
            }
        }
  }