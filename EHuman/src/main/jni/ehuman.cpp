//
// Created by Administrator on 2018/5/19.
//

#include <stdio.h>
#include <vector>
#include <opencv2/opencv.hpp>
#include <sys/time.h>

#include "mobilenet.hpp"
#include "com_es_ehuman_EHuman.h"

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
    init_mobilenet(jstringToChar(env, binPath), jstringToChar(env, protoPath));
  }