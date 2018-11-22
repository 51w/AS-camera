/*

 * SDL_lesson.c
 *
 *  Created on: Aug 8, 2014
 *      Author: clarck
 */

#ifdef __ANDROID__
#ifndef _H264TOJPG
#define _H264TOJPG

#ifdef __cplusplus

#include "SDL_logger.h"
#include <stdint.h>
#include <stdio.h>
#include <string.h>
extern "C" {
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libavutil/avutil.h"

}
#include "liveMedia.hh"
int writeJPEG(const char *out_file);
int decoder_264_init_sprops(SPropRecord* spropRecords, unsigned spropCount);
void cleanUp();
int decoder_alloc();
int decoder_video_stream(unsigned char *framePtr, int frameSize);
int saveToJpg(uint8_t *buffer, int size,uint8_t *firstBuffer, int firstSize, uint8_t *secondBuffer, int secondSize,
		SPropRecord *spropRecord,const char *fileName, int width, int height);
int decoder_264_sprops(SPropRecord *spropRecord);
#endif /*__ANDROID__*/
#endif
#endif
