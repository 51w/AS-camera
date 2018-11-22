#ifdef __ANDROID__
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include "h264tojpg.h"
#include <android/log.h>

static const int MaxExtraDataSize = 512;
AVFrame *avFrame = NULL;
AVCodecContext *codecCtx = NULL;
AVCodec *avCodec = NULL;
AVPacket avPacket;
AVCodecParserContext *parserCtx;
int jpegWidth = 0;
int jpegHeight = 0;

/**
 * 将AVFrame(YUV420格式)保存为JPEG格式的图片
 *
 * @param width YUV420的宽
 * @param height YUV42的高
 *
 */

int writeJPEG(const char *out_file) {
	// 输出文件路径
	// 分配AVFormatContext对象
	LOGI("jpg writeJPEG file.%s", out_file);
	AVFormatContext* pFormatCtx = avformat_alloc_context();
	// 设置输出文件格式
	pFormatCtx->oformat = av_guess_format("mjpeg", NULL, NULL);
	// 创建并初始化一个和该url相关的AVIOContext
	if (avio_open(&pFormatCtx->pb, out_file, AVIO_FLAG_READ_WRITE) < 0) {
		printf("Couldn't open output file.");
		LOGE("jpg Couldn't open output file.\n");
		return -1;
	}

	// 构建一个新stream
	AVStream* pAVStream = avformat_new_stream(pFormatCtx, 0);
	if (pAVStream == NULL) {
		return -1;
	}
	// 设置该stream的信息
	AVCodecContext* pCodecCtx = pAVStream->codec;
	pCodecCtx->codec_id = pFormatCtx->oformat->video_codec;
	pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
	pCodecCtx->pix_fmt = AV_PIX_FMT_YUVJ420P;
	pCodecCtx->width = jpegWidth;
	pCodecCtx->height = jpegHeight;
	pCodecCtx->time_base.num = 1;
	pCodecCtx->time_base.den = 25;
	// Begin Output some information
	av_dump_format(pFormatCtx, 0, out_file, 1);
	// End Output some information
	LOGD("jpg avcodec_find_encoder file");
	// 查找解码器
	AVCodec* pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
	if (!pCodec) {
		printf("Codec not found.");
		LOGE("jpg not found.");
		return -1;
	}

	// 设置pCodecCtx的解码器为pCodec
	if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
		LOGE("jpg not open codec..width is %d, height is %d", jpegWidth, jpegHeight);
		return -1;
	}
	//Write Header
	avformat_write_header(pFormatCtx, NULL);
	int y_size = pCodecCtx->width * pCodecCtx->height;
	//Encode
	// 给AVPacket分配足够大的空间
	AVPacket pkt;
	av_new_packet(&pkt, y_size * 3);
	//
	int got_picture = 0;
	LOGD("jpg avcodec_encode_video2 ");
	int ret = avcodec_encode_video2(pCodecCtx, &pkt, avFrame, &got_picture);
	if (ret < 0) {
		printf("Encode Error.\n");
		LOGE("jpg Error.\n");
		av_free_packet(&pkt);
		avio_close(pFormatCtx->pb);
		avformat_free_context(pFormatCtx);
		return -1;
	}
	LOGD("jpg avcodec_encode_video2 got_picture %d", got_picture);
	if (got_picture == 1) {
		//pkt.stream_index = pAVStream->index;
		ret = av_write_frame(pFormatCtx, &pkt);
	}

	av_free_packet(&pkt);
	//Write Trailer
	av_write_trailer(pFormatCtx);
	printf("Encode Successful.\n");
	LOGD("jpg Successful.\n");
	if (pAVStream) {
		avcodec_close(pAVStream->codec);

	}
	avio_close(pFormatCtx->pb);
	avformat_free_context(pFormatCtx);
	LOGD("jpg writeJPEG end.\n");
	return 0;
}

int decoder_264_init_sprops(SPropRecord* spropRecords, unsigned spropCount) {
	LOGW(
			"jpg decoder_264_init spropCount:%d %s ///// %s", spropCount, spropRecords[0].sPropBytes, spropRecords[1].sPropBytes);
	//if(codecCtx->extradata_size==0){
	uint8_t startCode[] = { 0x00, 0x00, 0x01 };
	uint8_t extraDataBuffer[MaxExtraDataSize];
	int extraDataSize = 0;
	if (spropRecords != NULL) {
		try {
			LOGW("jpg try");

			for (unsigned i = 0; i < spropCount; ++i) {
				memcpy(extraDataBuffer + extraDataSize, startCode,
						sizeof(startCode));
				extraDataSize += sizeof(startCode);
				memcpy(extraDataBuffer + extraDataSize,
						spropRecords[i].sPropBytes,
						spropRecords[i].sPropLength);
				extraDataSize += spropRecords[i].sPropLength;
				if (extraDataSize > MaxExtraDataSize) {
					throw "extradata exceeds size limit";
				}
			}
		} catch (void*) {
			LOGE("jpg catch");
			//extradata exceeds size limit
			return -1;
		}
		codecCtx->extradata = extraDataBuffer;
		codecCtx->extradata_size = extraDataSize;
	} else {
		LOGE("spropRecords == NULL");
		return -1;
	}
	codecCtx->flags = 0;
	LOGD("jpg decoder_264_init_end:%d", extraDataSize);
	return 0;
}
int decoder_264_sprops(SPropRecord *spropRecords) {
//if(codecCtx->extradata_size==0){
	uint8_t startCode[] = { 0x00, 0x00, 0x01 };
	uint8_t extraDataBuffer[MaxExtraDataSize];
	int extraDataSize = 0;
	unsigned spropCount = 2;
	if (spropRecords != NULL) {
		try {
			LOGW("jpg try");
			/*SPropRecord* spropRecords = parseSPropParameterSets(
			 sps, spropCount);*/
			for (unsigned i = 0; i < spropCount; ++i) {
				memcpy(extraDataBuffer + extraDataSize, startCode,
						sizeof(startCode));
				extraDataSize += sizeof(startCode);
				memcpy(extraDataBuffer + extraDataSize,
						spropRecords[i].sPropBytes,
						spropRecords[i].sPropLength);
				extraDataSize += spropRecords[i].sPropLength;
				if (extraDataSize > MaxExtraDataSize) {
					throw "extradata exceeds size limit";
				}
			}
		} catch (void*) {
			LOGE("jpg catch");
			//extradata exceeds size limit
			return -1;
		}
		codecCtx->extradata = extraDataBuffer;
		codecCtx->extradata_size = extraDataSize;
	} else {
		LOGE("spropRecords == NULL");
		return -1;
	}
	codecCtx->flags = 0;
	LOGD("jpg decoder_264_init_end:%d", extraDataSize);
	return 0;
}
void cleanUp() {
	LOGD("jpg cleanUp");
	if (avFrame) {
		av_free(avFrame);
		avFrame = NULL;
		LOGW("jpg avFrame == NULL");
	}
	/*if (avCodec) {
    		av_free(avCodec);
    		avCodec = NULL;
    		LOGW("jpg codecCtx");
    	}*/
	if (codecCtx) {
		avcodec_close(codecCtx);
		codecCtx = NULL;
		LOGW("jpg codecCtx");
	}

	if (parserCtx) {
		av_parser_close(parserCtx);
		parserCtx = NULL;
		LOGW("jpg parserCtx");
	}

	LOGE("jpg cleanUp end");
}

int decoder_alloc() {
	LOGD("jpg decoder_alloc");
	av_register_all();
	avcodec_register_all();
	avformat_network_init();
	av_init_packet(&avPacket);
	AVCodecID codec_id = AV_CODEC_ID_H264;
	avCodec = avcodec_find_decoder(codec_id);
	if (!avCodec) {
		LOGE("jpg codec_init fail:%s", "H264");
		return -1;
	}
	codecCtx = avcodec_alloc_context3(avcodec_find_decoder(codec_id));
	if (!codecCtx) {
		LOGW("jpg avcodec_alloc_context3 fail");
		return -1;
	}
	codecCtx->codec_id = AV_CODEC_ID_H264;
	codecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
	codecCtx->pix_fmt = AV_PIX_FMT_YUV420P;
	codecCtx->width = jpegWidth;
	codecCtx->height = jpegHeight;
	avFrame = av_frame_alloc();
	if (!avFrame) {
		LOGE("jpg avFrame");
		return -1;
	}
	parserCtx = av_parser_init(codec_id);
	if (!parserCtx) {
		LOGE("jpg parse");
		return -1;
	}
	if (avcodec_open2(codecCtx, avCodec, NULL) < 0) {
		LOGE("jpg avcodec_open2 fail");
		return -1;
	}
	//-------------------------------------------------------------//
	LOGW("jpg init decoder_alloc finish");
	return 0;
}

int decoder_video_stream(unsigned char *framePtr, int frameSize) {
	uint8_t *temp_data = NULL;
	LOGD(
			"jpg decoder_video_stream jpegWidth %d,jpegHeight %d", jpegWidth, jpegHeight);
	int numBytes = avpicture_get_size(AV_PIX_FMT_YUV420P, jpegWidth, jpegHeight); //640*480 460800

	if (!framePtr) {
		LOGD("jpg decoder_video_stream numBytes %d", numBytes);
		return -1;
	}
	temp_data = (uint8_t *) malloc(numBytes + 3);
	uint8_t startCode[] = { 0x00, 0x00, 0x01 };
	memcpy(temp_data, startCode, sizeof(startCode));
	memcpy(temp_data + sizeof(startCode), framePtr, frameSize);
	avPacket.data = temp_data;
	avPacket.size = frameSize + sizeof(startCode);
	int got_frame = 0;
	LOGI("extradata :%p", codecCtx->extradata);
	LOGD("jpg avcodec_decode_video2 ");
	auto int len = avcodec_decode_video2(codecCtx, avFrame, &got_frame,
			&avPacket);
	LOGD("jpg len :%d %d", len, got_frame);
	if (len < 0) {
		LOGE("jpg_len < 0");
		free(temp_data);
		avPacket.data = NULL;
		avPacket.size = 0;
		return -1;
	}
	if (got_frame /*&& !silentMode*/) {
		LOGI("jpg success:%d", codecCtx->height);
		free(temp_data);
		avPacket.data = NULL;
		avPacket.size = 0;
		LOGE("jpg decoder_video_stream end");
		return 0;
	}
	return -1;
}

int saveToJpg(uint8_t *buffer, int size, uint8_t *firstBuffer, int firstSize,
		uint8_t *secondBuffer, int secondSize, SPropRecord *spropRecord,
		const char *fileName, int width, int height) {
	LOGD("jpg testDecoder");
	jpegWidth = width;
	jpegHeight = height;
	int result = -1;
	if (!codecCtx) {
		result = decoder_alloc();
		if (result != 0) {
			cleanUp();
			return result;
		}
		result = decoder_264_sprops(spropRecord);
	}
	//LOGD("jpg buffer[0]:%d", buffer[0]==0x65?0:1);
	decoder_video_stream(firstBuffer, firstSize);
	decoder_video_stream(secondBuffer, secondSize);
	result = decoder_video_stream(buffer, size);
	if (result == 0) {
		result = writeJPEG(fileName);
	}
	cleanUp();
	return 0;
}
#endif /*__ANDROID__*/
