#ifdef __ANDROID__

#include "h264tomp4.h"
#include "mp4v2/mp4v2.h"
#include "SDL_logger.h"
int getVopType(const void *p) {
	uint8_t *temp = (uint8_t *) p;
	LOGD("_sps getVopType %u ", temp[0]);
	return (temp[0] == 0x65) ? 0 : 1;
}

MP4FileHandle fileHandle = MP4_INVALID_FILE_HANDLE;
MP4TrackId video;
bool isFirstKeyFrame = false;
bool isRecording = false;
bool isWriting = false;
bool initMp4(const char *fileName, u_int8_t *spsBuffer, int spsSize,
             u_int8_t *ppsBuffer, int ppsSize, int width, int height) {
	if (!spsBuffer) {
		LOGE("fmtp_sps null");
		return false;
	}
	fileHandle = MP4Create(fileName, 0);
	if (fileHandle == MP4_INVALID_FILE_HANDLE) {
		LOGE("MP4_INVALID_FILE_HANDLE");
		return false;
	}for (int i = 0; i < spsSize; i++) {
        LOGD("initMp4_sps:%x", spsBuffer[i]);
    }
    for (int i = 0; i < ppsSize; i++) {
        LOGD("initMp4_pps:%x", ppsBuffer[i]);
    }

	//设置mp4文件的时间单位
	MP4SetTimeScale(fileHandle, 90000);
	//创建视频track //根据ISO/IEC 14496-10 可知sps的第二个，第三个，第四个字节分别是 AVCProfileIndication,profile_compat,AVCLevelIndication     其中90000/20  中的20>是fps
	video = MP4AddH264VideoTrack(fileHandle, 90000, 90000 / 30, width, height,
                                 spsBuffer[1], spsBuffer[2], spsBuffer[3], 3);
	LOGD("_sps:video %d", video);
	if (video == MP4_INVALID_TRACK_ID) {
		MP4Close(fileHandle, 0);

		return false;
	}

	//设置sps和pps
	MP4AddH264SequenceParameterSet(fileHandle, video, spsBuffer, spsSize);
	MP4AddH264PictureParameterSet(fileHandle, video, ppsBuffer, ppsSize);
	MP4SetVideoProfileLevel(fileHandle, 0x7F);
	isFirstKeyFrame = true;

	isRecording = true;
	return true;
}

bool recording() {
	return isRecording;
}
/*int writeJpegMp4(uint8_t *buffer, int size, const char *fileName,
		SPropRecord *spropRecord, int width, int height) {

	LOGD("_sps:writeJpegMp4 %d %d", size, (buffer[0] == 0x65) ? 0 : 1);
	//buf 00 00 00 01 65
	int iskeyframe = (buffer[0] == 0x65) ? 0 : 1;  //0为i帧
	if (iskeyframe != 0) {
		LOGD("_sps:getVopType");
		return 1;
	}
	if (!spropRecord) {
		LOGE("fmtp_sps null");
		return 1;
	}
	MP4FileHandle jpgHandle = MP4Create(fileName, 0);
	if (jpgHandle == MP4_INVALID_FILE_HANDLE) {
		LOGE("MP4_INVALID_FILE_HANDLE");
		return 1;
	}
	uint16_t _spsSize = spropRecord[0].sPropLength;
	uint16_t _ppsSize = spropRecord[1].sPropLength;
	uint8_t *_sps_temp = (uint8_t *) malloc(_spsSize);
	uint8_t *_pps_temp = (uint8_t *) malloc(_ppsSize);
	memcpy(_sps_temp, spropRecord[0].sPropBytes, _spsSize);
	memcpy(_pps_temp, spropRecord[1].sPropBytes, _ppsSize);
	const uint8_t *_sps = _sps_temp;
	const uint8_t *_pps = _pps_temp;

	//设置mp4文件的时间单位
	MP4SetTimeScale(jpgHandle, 90000);
	//创建视频track //根据ISO/IEC 14496-10 可知sps的第二个，第三个，第四个字节分别是 AVCProfileIndication,profile_compat,AVCLevelIndication     其中90000/20  中的20>是fps
	for (int i = 0; i < _spsSize; i++) {
		LOGD("_sps:%x", _sps[i]);
	}
	for (int i = 0; i < _ppsSize; i++) {
		LOGD("_pps:%x", _pps[i]);
	}
	video = MP4AddH264VideoTrack(jpgHandle, 90000, 90000 / 30, width, height,
			_sps[1], _sps[2], _sps[3], 3);
	LOGD("_sps:video %d", video);
	if (video == MP4_INVALID_TRACK_ID) {
		MP4Close(jpgHandle, 0);
		free(_sps_temp);
		_sps = NULL;
		free(_pps_temp);
		_pps = NULL;
		return 1;
	}
	//设置sps和pps
	MP4AddH264SequenceParameterSet(jpgHandle, video, _sps, _spsSize);
	MP4AddH264PictureParameterSet(jpgHandle, video, _pps, _ppsSize);
	MP4SetVideoProfileLevel(jpgHandle, 0x7F);
	isFirstKeyFrame = True;
	free(_sps_temp);
	_sps_temp = NULL;
	free(_pps_temp);
	_pps_temp = NULL;
	uint8_t *mp4Buffer = (uint8_t *) malloc(size + 4);
	int nalsize = size;
	mp4Buffer[0] = (nalsize & 0xff000000) >> 24;
	mp4Buffer[1] = (nalsize & 0x00ff0000) >> 16;
	mp4Buffer[2] = (nalsize & 0x0000ff00) >> 8;
	mp4Buffer[3] = nalsize & 0x000000ff;
	memcpy(mp4Buffer + 4, buffer, size);
	LOGD("_sps MP4WriteSample	:video %d", video);
	MP4WriteSample(jpgHandle, video, mp4Buffer, size + 4, MP4_INVALID_DURATION,
			0, !iskeyframe);
	free(mp4Buffer);
	mp4Buffer = NULL;
	LOGD("_sps MP4WriteSample end");
	MP4Close(jpgHandle, 0);
	jpgHandle = MP4_INVALID_FILE_HANDLE;
	LOGD("_sps:closeMp4");
	return 0;
}*/
void writeMp4(uint8_t *buffer, int size) {
	LOGD("_sps:writeMp4 %d %c", size, buffer[0]);
	isWriting = true;
	//buf 00 00 00 01 65
	int iskeyframe = (buffer[0] == 0x65) ? 0 : 1;  //0为i帧
	if (iskeyframe != 0 && isFirstKeyFrame) {
		LOGD("_sps:getVopType");
        isWriting = false;
		return;
	}
	uint8_t *mp4Buffer = (uint8_t *) malloc(size + 4);
	int nalsize = size;
	mp4Buffer[0] = (nalsize & 0xff000000) >> 24;
	mp4Buffer[1] = (nalsize & 0x00ff0000) >> 16;
	mp4Buffer[2] = (nalsize & 0x0000ff00) >> 8;
	mp4Buffer[3] = nalsize & 0x000000ff;
	memcpy(mp4Buffer + 4, buffer, size);
	LOGD("_sps MP4WriteSample	:video %d", video);
	MP4WriteSample(fileHandle, video, mp4Buffer, size + 4, MP4_INVALID_DURATION,
			0, !iskeyframe);
	free(mp4Buffer);
	mp4Buffer = NULL;
	isFirstKeyFrame = false;
	isWriting = false;
	LOGD("_sps MP4WriteSample end");
}

void closeMp4() {
	LOGD("_sps:closeMp4 %d",isRecording?1:0);
	while (isWriting) {
		usleep(5*1000);//休眠5毫秒
	}

	if (fileHandle != MP4_INVALID_FILE_HANDLE) {
		LOGD("_sps:closeMp4");
		MP4Close(fileHandle, 0);
		fileHandle = MP4_INVALID_FILE_HANDLE;
		isFirstKeyFrame = True;
	}

	isRecording = false;
}

#endif /*__ANDROID__*/
