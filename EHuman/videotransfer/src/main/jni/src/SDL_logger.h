/*
 * SDL_logger.h
 *
 *  Created on: 2015-11-18
 *      Author: Meng.MY
 */


#ifndef LOG_H_
#define LOG_H_

#include <android/log.h>

#define APPNAME "SDL_Lesson1"

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , APPNAME, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO , APPNAME, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN , APPNAME, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR , APPNAME, __VA_ARGS__)

#endif /* LOG_H_ */

