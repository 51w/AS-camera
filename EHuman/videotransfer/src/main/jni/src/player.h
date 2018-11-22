/*

 * SDL_lesson.c
 *
 *  Created on: Aug 8, 2014
 *      Author: clarck
 */

#ifdef __ANDROID__
#ifndef _PLAYER
#define _PLAYER

#ifdef __cplusplus
#include <jni.h>
#include <unistd.h>
#include <android/log.h>

#include <pthread.h>

#include "SDL_logger.h"

#include "BasicUsageEnvironment.hh"
#include "ServerMediaSession.hh"
#include "liveMedia.hh"
#include "h264tomp4.h"
#include "h264tojpg.h"
#include <sys/time.h>

int init(JNIEnv* jEnv, jclass jCls, jstring serverIP) ;
void shutDown() ;
SPropRecord *getSpropRecords();
u_int8_t *getJpgBuffer();
void cleanUpJpgBuffer();
void setRecord(int record) ;
int getJpgSize();
int getTake();
unsigned getSpropCount();

int getInitKeying();
void setTake(int mIsTakePic);
unsigned getFirstSize();
unsigned getSecondSize();
unsigned getKeySize();
u_int8_t *getKeyBuffer();
u_int8_t *getFirstBuffer();
u_int8_t *getSecondBuffer();
void cleanAllBuffer();

int getVideoWidth();
int getVideoHeight();
u_int8_t *getSpsBuffer();
int getSpsSize();
u_int8_t *getPpsBuffer();
int getPpsSize();

void setDecodeFrame(int decodeFrame);

#endif
#endif /*__ANDROID__*/
#endif
