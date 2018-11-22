/*

 * SDL_lesson.c
 *
 *  Created on: Aug 8, 2014
 *      Author: clarck
 */

#ifdef __ANDROID__
#ifndef _H264TOMP4
#define _H264TOMP4

#ifdef __cplusplus
#include "liveMedia.hh"
#include "SDL_logger.h"
bool initMp4(const char *fileName, u_int8_t *spsBuffer, int spsSize,
			 u_int8_t *ppsBuffer, int ppsSize, int width, int height);
void writeMp4(uint8_t *buffer,int size);
int writeJpegMp4(uint8_t *buffer, int size, const char *fileName,
		SPropRecord *spropRecord, int width, int height) ;
bool recording();
void closeMp4();
int getVopType(const void *p);
#endif /*__ANDROID__*/
#endif
#endif
