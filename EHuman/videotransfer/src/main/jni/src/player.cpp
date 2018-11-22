/*

 * SDL_lesson.c
 *
 *  Created on: Aug 8, 2014
 *      Author: clarck
 */

#ifdef __ANDROID__
#include "player.h"
#include "h264_stream.h"
#include <math.h>

//#include "SDL_android.h"//闁搞儳鍋犻惃鐔煎嫉閸濆嫬鐓�
//The attributes of the screen

#define H264_STREAM 0
#define JPEG_STREAM 1
#define H264_STREAM_URL "rtsp://%s:7070/H264VideoSMS"
// Forward function definitions:
// RTSP 'response handlers':
void continueAfterDESCRIBE(RTSPClient* rtspClient, int resultCode,
		char* resultString);
void continueAfterSETUP(RTSPClient* rtspClient, int resultCode,
		char* resultString);
void continueAfterPLAY(RTSPClient* rtspClient, int resultCode,
		char* resultString);
// Other event handler functions:
void subsessionAfterPlaying(void* clientData); // called when a stream's subsession (e.g., audio or video substream) ends
void subsessionByeHandler(void* clientData);
void streamTimerHandler(void* clientData);
int openURL(UsageEnvironment& env, char const* progName, char const* rtspURL);
void setupNextSubsession(RTSPClient* rtspClient);
void shutdownStream(RTSPClient* rtspClient, int exitCode = 1);
void *envThead(void* arg);
void *timeThead(void* arg);

//u_int8_t* reversebytes_uint8t(u_int8_t* fRBuffer, unsigned fSize);
// Define the size of the buffer that we'll use:
#define DUMMY_SINK_RECEIVE_BUFFER_SIZE 100000

static UsageEnvironment* env;
char eventLoopWatchVariable = 0;

int isConnect = 1;

RTSPClient* rtsp_client = NULL;
struct timeval start;
struct timeval end;
long timeuse;
jclass gs_object;
JavaVM *gs_jvm;

const int TYPE_AUDIO = 0;
const int TYPE_VIDEO = 1;

unsigned fSize;
const char *VIDEO_URL = "VIDEO_LOCAL_URL";
SPropRecord *spropRecords = NULL;
unsigned spropCount;
u_int8_t* jpgBuffer = NULL;
u_int8_t* sprop = NULL;

unsigned firstSize;
unsigned secondSize;
unsigned keySize;
u_int8_t * keyBuffer = NULL;
u_int8_t * firstBuffer = NULL;
u_int8_t *secondBuffer = NULL;
int initKeying = 0;
int count_jpg = 0;

int jpgSize = 0;
int isRecord = 0;
int isTakePic = 0;

int lastPFrameNum=-2;
bool isLostFrame=false;

int isShutDown = 0;
int width = 0;
int height = 0;
u_int8_t* spsBuffer;
int spsSize = 0;
u_int8_t* ppsBuffer;
int ppsSize = 0;
int frame_num_max = 0;
//int isLock = 0;

int isDecodeFrame = 0;
AVCodecContext* context;
AVCodec* videoCodec;
AVPacket packet;
AVFrame *picture;

pthread_attr_t thread_attr;
pthread_t envThead_pt; // This function call does not return, unless, at some point in time, "eventLoopWatchVariable" gets set to something non-zero.

void Android_JNI_SetIsConnect(int isConnect);
void Android_JNI_OnSPSFrame(u_int8_t* buffer, unsigned frameSize, int width, int height);
void Android_JNI_OnPPSFrame(u_int8_t* buffer, unsigned frameSize);
void Android_JNI_OnFrame(u_int8_t* buffer, unsigned frameSize);
void initH264DecodeEnv();
void decodeFrame(u_int8_t* inBuffer, unsigned inSize);

void setDecodeFrame(int decodeFrame) {
	isDecodeFrame = decodeFrame;
}

//void Android_JNI_PLAY_FILE_Result();
int init(JNIEnv* jEnv, jclass jCls, jstring serverIP) {
	//TODO in
	gs_object = (jclass) jEnv->NewGlobalRef(jCls);
	LOGE("init");
	jEnv->GetJavaVM(&gs_jvm);
	const char *ip = jEnv->GetStringUTFChars(serverIP, JNI_FALSE);

    initH264DecodeEnv();

	int result = 0;
	TaskScheduler* scheduler = BasicTaskScheduler::createNew();
	env = BasicUsageEnvironment::createNew(*scheduler);

    char url[256] = { 0 };
	sprintf(url, H264_STREAM_URL, ip);
    LOGW("url is:%s", url);
	result = openURL(*env, "nobody", url);
	LOGW("result:%d", result);
	if (result != 0) {
		return result;
	}

    lastPFrameNum=-2;

	struct sched_param param;
    pthread_attr_init(&thread_attr);
    pthread_attr_setschedpolicy(&thread_attr,SCHED_RR);
    param.sched_priority = 90;
    pthread_attr_setschedparam(&thread_attr,&param);

	pthread_create(&envThead_pt, &thread_attr, &envThead, 0);
    //pthread_create(&envThead_pt, NULL, &envThead, 0);
	eventLoopWatchVariable = 0;
	pthread_t pt; // This function call does not return, unless, at some point in time, "eventLoopWatchVariable" gets set to something non-zero.
	pthread_create(&pt, NULL, &timeThead, 0);
	LOGE("end");
	return 0;
}

void initMethod() {
	isConnect = 1;
	isShutDown = 1;
	eventLoopWatchVariable = 0;
}

void shutDown() {
	isShutDown = 1;
	eventLoopWatchVariable = '1';
	while (isShutDown) {
		usleep(100);
	}
 //  pthread_attr_destroy(&thread_attr);
}

void setTake(int mIsTakePic) {
	isTakePic = mIsTakePic;
}

int getTake() {
	return isTakePic;
}
void *envThead(void* arg) {
	LOGI("envThead:%d", eventLoopWatchVariable);
	env->taskScheduler().doEventLoop(&eventLoopWatchVariable);
	LOGE("cleanUp null?");
	if (eventLoopWatchVariable != 0) {
		eventLoopWatchVariable = 0;
		if (rtsp_client) {
			shutdownStream(rtsp_client);
			rtsp_client = NULL;
		}
		cleanAllBuffer();
		isShutDown = 0;

	}
	LOGE("envThead end=%p", rtsp_client);
	pthread_exit(0);
    pthread_attr_destroy(&thread_attr);
	return 0;
}
void *timeThead(void* arg) {
	//闁告梻濮抽柌婊呮媼閳╁啯顦抽柛锝冨妼椤棙鎷呴弴顏嗗惞
	LOGE("timeThead");
	gettimeofday(&start, NULL);
	int i = 0;
	do {
		gettimeofday(&end, NULL);
		timeuse = 1000000 * (end.tv_sec - start.tv_sec) + end.tv_usec
				- start.tv_usec;
		//LOGI("timeuse=%d\n", timeuse);
		if (isConnect != 1) {
			LOGE("Android_JNI_SetIsConnect:%d", isConnect);
			Android_JNI_SetIsConnect(isConnect);
			return NULL;
		}
	} while ((timeuse / 1000) < 3000);
	LOGE("time=%ld\n", timeuse);

	Android_JNI_SetIsConnect(isConnect);
	return NULL;
}

class StreamClientState {
public:
	StreamClientState();
	virtual ~StreamClientState();
public:
	MediaSubsessionIterator* iter;
	MediaSession* session;
	MediaSubsession* subsession;
	TaskToken streamTimerTask;
	double duration;
};

class ourRTSPClient: public RTSPClient {
public:
	static ourRTSPClient* createNew(UsageEnvironment& env, char const* rtspURL,
			int verbosityLevel = 0, char const* applicationName = NULL,
			portNumBits tunnelOverHTTPPortNum = 0);

protected:
	ourRTSPClient(UsageEnvironment& env, char const* rtspURL,
			int verbosityLevel, char const* applicationName,
			portNumBits tunnelOverHTTPPortNum);
	// called only by createNew();
	virtual ~ourRTSPClient();

public:
	StreamClientState scs;
};
class DummySink: public MediaSink {
public:
	static DummySink* createNew(UsageEnvironment& env,
			MediaSubsession& subsession, // identifies the kind of data that's being received
			char const* streamId = NULL); // identifies the stream itself (optional)

private:
	DummySink(UsageEnvironment& env, MediaSubsession& subsession,
			char const* streamId);
	// called only by "createNew()"
	virtual ~DummySink();

	static void afterGettingFrame(void* clientData, unsigned frameSize,
			unsigned numTruncatedBytes, struct timeval presentationTime,
			unsigned durationInMicroseconds);
	void afterGettingFrame(unsigned frameSize, unsigned numTruncatedBytes,
			struct timeval presentationTime, unsigned durationInMicroseconds);

private:
	// redefined virtual functions:
	virtual Boolean continuePlaying();

private:
	u_int8_t* fReceiveBuffer;
	MediaSubsession& fSubsession;
	char* fStreamId;
	pthread_t pts;
};

#define RTSP_CLIENT_VERBOSITY_LEVEL 1 // by default, print verbose output from each "RTSPClient"
static unsigned rtspClientCount = 0; // Counts how many streams (i.e., "RTSPClient"s) are currently in use.

int openURL(UsageEnvironment& env, char const* progName, char const* rtspURL) {
	// Begin by creating a "RTSPClient" object.  Note that there is a separate "RTSPClient" object for each stream that we wish
	// to receive (even if more than stream uses the same "rtsp://" URL).
	LOGW("openURL of %s", rtspURL);
	if (rtsp_client) {
		LOGE("createNew? not it is exist");
		/*Medium::close(rtsp_client);
		 rtsp_client=NULL;*/
	}
	rtsp_client = ourRTSPClient::createNew(env, rtspURL,
			RTSP_CLIENT_VERBOSITY_LEVEL, progName);
	if (rtsp_client == NULL) {
		LOGW(
				"Failed to create a RTSP client for URL \":%s,%s", rtspURL, env.getResultMsg());
		return -1;
	} else {
		LOGW("++rtspClientCount:%u", rtspClientCount);
	}

	++rtspClientCount;

	// Next, send a RTSP "DESCRIBE" command, to get a SDP description for the stream.
	// Note that this command - like all RTSP commands - is sent asynchronously; we do not block, waiting for a response.
	// Instead, the following function call returns immediately, and we handle the RTSP response later, from within the event loop:
	unsigned cmd = rtsp_client->sendDescribeCommand(continueAfterDESCRIBE);
	LOGD("cmd:%u", cmd);
	return 0;
}

// Implementation of the RTSP 'response handlers':

void continueAfterDESCRIBE(RTSPClient* rtspClient, int resultCode,
		char* resultString) {
	LOGW("continueAfterDESCRIBE");
	do {
		UsageEnvironment& env = rtspClient->envir(); // alias
		StreamClientState& scs = ((ourRTSPClient*) rtspClient)->scs; // alias

		if (resultCode != 0) {
			LOGE("Failed to get a SDP description: %s", resultString);
			delete[] resultString;
			break;
		}

		char* const sdpDescription = resultString;
		LOGW("Got a SDP description:\n %s", sdpDescription);
		// Create a media session object from this SDP description:
		scs.session = MediaSession::createNew(env, sdpDescription);
		delete[] sdpDescription; // because we don't need it anymore
		if (scs.session == NULL) {
			LOGW(
					"Failed to create a MediaSession object from the SDP description: %s", env.getResultMsg());
			break;
		} else if (!scs.session->hasSubsessions()) {
			break;
		}
		scs.iter = new MediaSubsessionIterator(*scs.session);
		//TODO isConnect
		isConnect = 0;
		setupNextSubsession(rtspClient);
		return;
	} while (0);

	// An unrecoverable error occurred with this stream.

	//shutdownStream(rtspClient); 闂佹彃绉撮ˇ楣冨礂閹惰姤锛旀繛缈犵閸ゎ參鎮抽弶璺ㄧ＝閻㈩垽鎷�
}
#define REQUEST_STREAMING_OVER_TCP True //True False(默认)
void setupNextSubsession(RTSPClient* rtspClient) {
	LOGW("setupNextSubsession");
	UsageEnvironment& env = rtspClient->envir(); // alias
	StreamClientState& scs = ((ourRTSPClient*) rtspClient)->scs; // alias

	scs.subsession = scs.iter->next();
	if (scs.subsession != NULL) {
		if (!scs.subsession->initiate()) {
			setupNextSubsession(rtspClient); // give up on this subsession; go to the next one
		} else {
			if (scs.subsession->rtcpIsMuxed()) {
				LOGW("client port %d ", scs.subsession->clientPortNum());
			} else {
				LOGW("client ports ");
			}
			rtspClient->sendSetupCommand(*scs.subsession, continueAfterSETUP,
					False, REQUEST_STREAMING_OVER_TCP);
		}
		return;
	}
	if (scs.session->absStartTime() != NULL) {
		rtspClient->sendPlayCommand(*scs.session, continueAfterPLAY,
				scs.session->absStartTime(), scs.session->absEndTime());
	} else {

		scs.duration = scs.session->playEndTime()
				- scs.session->playStartTime();
		rtspClient->sendPlayCommand(*scs.session, continueAfterPLAY);
	}
}

void continueAfterSETUP(RTSPClient* rtspClient, int resultCode,
		char* resultString) {
	LOGW("continueAfterSETUP");
	do {
		UsageEnvironment& env = rtspClient->envir(); // alias
		StreamClientState& scs = ((ourRTSPClient*) rtspClient)->scs; // alias

		if (resultCode != 0) {
			LOGW("continueAfterSETUP:Failed to set up the \"");
			break;
		}
		if (scs.subsession->rtcpIsMuxed()) {
			LOGW(
					"continueAfterSETUP:client port:%c", char(scs.subsession->clientPortNum()));
		} else {

			LOGD("%d", char(scs.subsession->clientPortNum()+ 1));
		}
		scs.subsession->sink = DummySink::createNew(env, *scs.subsession,
				rtspClient->url());
		// perhaps use your own custom "MediaSink" subclass instead
		if (scs.subsession->sink == NULL) {
			LOGW(
					"Failed to create a data sink for the:%s ", env.getResultMsg());
			break;
		}
		scs.subsession->miscPtr = rtspClient; // a hack to let subsession handler functions get the "RTSPClient" from the subsession
		scs.subsession->sink->startPlaying(*(scs.subsession->readSource()),
				subsessionAfterPlaying, scs.subsession);
		if (scs.subsession->rtcpInstance() != NULL) {
			scs.subsession->rtcpInstance()->setByeHandler(subsessionByeHandler,
					scs.subsession);
		}
	} while (0);
	delete[] resultString;
	setupNextSubsession(rtspClient);
}

void continueAfterPLAY(RTSPClient* rtspClient, int resultCode,
		char* resultString) {
	LOGW("continueAfterPLAY");
	Boolean success = False;

	do {
		UsageEnvironment& env = rtspClient->envir(); // alias
		StreamClientState& scs = ((ourRTSPClient*) rtspClient)->scs; // alias

		if (resultCode != 0) {
			LOGD("Failed to start playing session: %s", resultString);
			break;
		}

		if (scs.duration > 0) {
			unsigned const delaySlop = 2; // number of seconds extra to delay, after the stream's expected duration.  (This is optional.)
			scs.duration += delaySlop;
			unsigned uSecsToDelay = (unsigned) (scs.duration * 1000000);
			scs.streamTimerTask = env.taskScheduler().scheduleDelayedTask(
					uSecsToDelay, (TaskFunc*) streamTimerHandler, rtspClient);
		}

		LOGW("Started playing session");
		if (scs.duration > 0) {
			LOGW( " for up to %f seconds", scs.duration);
		}
		success = True;
	} while (0);
	delete[] resultString;

	if (!success) {
		// An unrecoverable error occurred with this stream.
		shutdownStream(rtspClient);
	}
}

// Implementation of the other event handlers:

void subsessionAfterPlaying(void* clientData) {
	LOGW("subsessionAfterPlaying");
	MediaSubsession* subsession = (MediaSubsession*) clientData;
	RTSPClient* rtspClient = (RTSPClient*) (subsession->miscPtr);

	// Begin by closing this subsession's stream:
	Medium::close(subsession->sink);
	subsession->sink = NULL;

	// Next, check whether *all* subsessions' streams have now been closed:
	MediaSession& session = subsession->parentSession();
	MediaSubsessionIterator iter(session);
	while ((subsession = iter.next()) != NULL) {
		if (subsession->sink != NULL)
			return; // this subsession is still active
	}

	// All subsessions' streams have now been closed, so shutdown the client:
	shutdownStream(rtspClient);
}

void subsessionByeHandler(void* clientData) {
	LOGW("subsessionByeHandler");
	MediaSubsession* subsession = (MediaSubsession*) clientData;
	RTSPClient* rtspClient = (RTSPClient*) subsession->miscPtr;
	UsageEnvironment& env = rtspClient->envir(); // alias
	LOGW("Received RTCP \"BYE\" on \"");
	// Now act as if the subsession had closed:
	subsessionAfterPlaying(subsession);
}

void streamTimerHandler(void* clientData) {
	LOGW("streamTimerHandler");
	ourRTSPClient* rtspClient = (ourRTSPClient*) clientData;
	StreamClientState& scs = rtspClient->scs; // alias

	scs.streamTimerTask = NULL;

	// Shut down the stream:
	shutdownStream(rtspClient);
}

void shutdownStream(RTSPClient* rtspClient, int exitCode) {
	LOGW("shutdownStream");
	if (rtspClient) {

		UsageEnvironment& env = rtspClient->envir(); // alias
		StreamClientState& scs = ((ourRTSPClient*) rtspClient)->scs; // alias

		// First, check whether any subsessions have still to be closed:
		if (scs.session != NULL) {
			Boolean someSubsessionsWereActive = False;
			MediaSubsessionIterator iter(*scs.session);
			MediaSubsession* subsession;

			while ((subsession = iter.next()) != NULL) {
				if (subsession->sink != NULL) {
					Medium::close(subsession->sink);
					subsession->sink = NULL;

					if (subsession->rtcpInstance() != NULL) {
						subsession->rtcpInstance()->setByeHandler(NULL, NULL); // in case the server sends a RTCP "BYE" while handling "TEARDOWN"
					}

					someSubsessionsWereActive = True;
				}
			}

			if (someSubsessionsWereActive) {
				// Send a RTSP "TEARDOWN" command, to tell the server to shutdown the stream.
				// Don't bother handling the response to the "TEARDOWN".
				rtspClient->sendTeardownCommand(*scs.session, NULL);
			}
		}
		LOGE("Closing the stream.\n");
		//env << *rtspClient << "Closing the stream.\n";
		if (rtspClient != NULL) {
			LOGE("rtspClient != NULL");
			Medium::close(rtspClient);
			LOGD("rtspClient = NULL");
			rtspClient = NULL;
			LOGE("rtspClient =%p", rtspClient);
		}
		LOGE("Medium::close(rtspClient)");
		// Note that this will also cause this stream's "StreamClientState" structure to get reclaimed.

		if (--rtspClientCount == 0) {
			//notification_yesno(exitCode);
			LOGE("--rtspClientCount == 0");
			eventLoopWatchVariable = 1;
		}
	}
	LOGE("eventLoopWatchVariable end.\n");
}

// Implementation of "ourRTSPClient":

ourRTSPClient* ourRTSPClient::createNew(UsageEnvironment& env,
		char const* rtspURL, int verbosityLevel, char const* applicationName,
		portNumBits tunnelOverHTTPPortNum) {
	return new ourRTSPClient(env, rtspURL, verbosityLevel, applicationName,
			tunnelOverHTTPPortNum);
}

ourRTSPClient::ourRTSPClient(UsageEnvironment& env, char const* rtspURL,
		int verbosityLevel, char const* applicationName,
		portNumBits tunnelOverHTTPPortNum) :
		RTSPClient(env, rtspURL, verbosityLevel, applicationName,
				tunnelOverHTTPPortNum, -1) {
}

ourRTSPClient::~ourRTSPClient() {
}

// Implementation of "StreamClientState":

StreamClientState::StreamClientState() :
		iter(NULL), session(NULL), subsession(NULL), streamTimerTask(NULL), duration(
				0.0) {
}

StreamClientState::~StreamClientState() {
	delete iter;
	if (session != NULL) {
		// We also need to delete "session", and unschedule "streamTimerTask" (if set)
		UsageEnvironment& env = session->envir(); // alias

		env.taskScheduler().unscheduleDelayedTask(streamTimerTask);
		Medium::close(session);
	}
}

// Implementation of "DummySink":

// Even though we're not going to be doing anything with the incoming data, we still need to receive it.

DummySink* DummySink::createNew(UsageEnvironment& env,
		MediaSubsession& subsession, char const* streamId) {
	return new DummySink(env, subsession, streamId);
}

DummySink::DummySink(UsageEnvironment& env, MediaSubsession& subsession,
		char const* streamId) :
		MediaSink(env), fSubsession(subsession) {
	fStreamId = strDup(streamId);
	fReceiveBuffer = new u_int8_t[DUMMY_SINK_RECEIVE_BUFFER_SIZE];
}

DummySink::~DummySink() {
	delete[] fReceiveBuffer;
	delete[] fStreamId;
}

void DummySink::afterGettingFrame(void* clientData, unsigned frameSize,
		unsigned numTruncatedBytes, struct timeval presentationTime,
		unsigned durationInMicroseconds) {
	DummySink* sink = (DummySink*) clientData;
	sink->afterGettingFrame(frameSize, numTruncatedBytes, presentationTime,
			durationInMicroseconds);
}
// If you don't want to see debugging output for each received frame, then comment out the following line:
#define DEBUG_PRINT_EACH_RECEIVED_FRAME 0

void DummySink::afterGettingFrame(unsigned frameSize,
		unsigned numTruncatedBytes, struct timeval presentationTime,
		unsigned durationInMicroseconds) {
	LOGD("afterGettingFrame start");

	// We've just received a frame of data.  (Optionally) print out information about it:
#ifdef DEBUG_PRINT_EACH_RECEIVED_FRAME
	if (fStreamId != NULL) {
		LOGW("fStreamId is %s.", fStreamId);
	}
	if (numTruncatedBytes > 0) {
		LOGW( " (with ");
		//LOGD(numTruncatedBytes);
		LOGW( " bytes truncated)");
	}
	if (fSubsession.rtpSource() != NULL
			&& !fSubsession.rtpSource()->hasBeenSynchronizedUsingRTCP()) {
		//envir() << "!"; // mark the debugging output to indicate that this presentation time is not RTCP-synchronized
		LOGW("!");
	}
#ifdef DEBUG_PRINT_NPT
	LOGW("\tNPT:");
	//LOGD(fSubsession.getNormalPlayTime(presentationTime));
#endif
#endif
	if (!spropRecords) {
		spropRecords = parseSPropParameterSets(
				fSubsession.fmtp_spropparametersets(), spropCount);
	}
	if (count_jpg <= 1) {
		count_jpg++;
		if (!firstBuffer) {
			firstBuffer = new u_int8_t[DUMMY_SINK_RECEIVE_BUFFER_SIZE];
			memcpy(firstBuffer, fReceiveBuffer, frameSize);
			firstSize = frameSize;
		} else if (!secondBuffer) {
			secondBuffer = new u_int8_t[DUMMY_SINK_RECEIVE_BUFFER_SIZE];
			memcpy(secondBuffer, fReceiveBuffer, frameSize);
			secondSize = frameSize;
		}
	}
	//////////////////////
	if (!strcmp(fSubsession.mediumName(), "video")) {
		h264_stream_t* h = h264_new();
		read_nal_unit(h, fReceiveBuffer, frameSize);
		LOGD("h264 nal_unit_type: %d", h->nal->nal_unit_type);

		if(h->nal->nal_unit_type == 7) {
            width = (h->sps->pic_width_in_mbs_minus1 + 1) * 16;
			height = (h->sps->pic_height_in_map_units_minus1 + 1) * 16;

            frame_num_max = pow(2, (h->sps->log2_max_frame_num_minus4+4));

			LOGD("h264 get sps frame, width: %d, height: %d, frame_num_max: %d", width, height, frame_num_max);
			Android_JNI_OnSPSFrame(fReceiveBuffer, frameSize, width, height);

			if(isDecodeFrame) {
				decodeFrame(fReceiveBuffer, frameSize);
			}
		}
        else if(h->nal->nal_unit_type == 8) {
            LOGD("h264 get pps frame.");
            Android_JNI_OnPPSFrame(fReceiveBuffer, frameSize);

			if(isDecodeFrame) {
				decodeFrame(fReceiveBuffer, frameSize);
			}
        }
        else if(h->nal->nal_unit_type == 1) {
            if(isLostFrame) {
                LOGE("h264 error p frame, lost this. frame_num: %d, lastPFrameNum: %d", h->sh->frame_num, lastPFrameNum);
            }
			else if(h->sh->frame_num!=lastPFrameNum+1) {
                isLostFrame = true;
                LOGE("h264 error p frame, lost this. frame_num: %d, lastPFrameNum: %d", h->sh->frame_num, lastPFrameNum);
            }
            else {
                LOGD("h264 correct p frame.");
				if(isDecodeFrame) {
					decodeFrame(fReceiveBuffer, frameSize);
				}
				else {
					Android_JNI_OnFrame(fReceiveBuffer, frameSize);
				}

                lastPFrameNum = h->sh->frame_num;
                if(lastPFrameNum==(frame_num_max-1)) {
                    lastPFrameNum = -1;
                }

                if (isRecord) {
                    writeMp4(fReceiveBuffer, frameSize);
                }
            }
        }
        else if(h->nal->nal_unit_type == 5){
			if(isDecodeFrame) {
				decodeFrame(fReceiveBuffer, frameSize);
			}
			else {
				Android_JNI_OnFrame(fReceiveBuffer, frameSize);
			}

            lastPFrameNum = h->sh->frame_num;
            isLostFrame = false;

            initKeying=1;
            if(!keyBuffer){
                keyBuffer = new u_int8_t[DUMMY_SINK_RECEIVE_BUFFER_SIZE];
            }

            memset(keyBuffer,0,DUMMY_SINK_RECEIVE_BUFFER_SIZE);
            memcpy(keyBuffer, fReceiveBuffer, frameSize);
            initKeying=0;
            keySize=frameSize;

            if (isTakePic) {
                LOGD("jpg end: %d", fReceiveBuffer[0]==65?0:1);
                //picFileName
                jpgBuffer = new u_int8_t[DUMMY_SINK_RECEIVE_BUFFER_SIZE];
                memcpy(jpgBuffer, fReceiveBuffer, frameSize);
                jpgSize = frameSize;
                isTakePic = 0;
            }

            if (isRecord) {
                writeMp4(fReceiveBuffer, frameSize);
            }
        }
        else {
			if(isDecodeFrame) {
				decodeFrame(fReceiveBuffer, frameSize);
			}
			else {
				Android_JNI_OnFrame(fReceiveBuffer, frameSize);
			}

            if (isRecord) {
                writeMp4(fReceiveBuffer, frameSize);
            }
        }

        h264_free(h);
	} else if (false && !strcmp(fSubsession.mediumName(), "audio")) {

	}

    LOGD("afterGettingFrame end");
	// Then continue, to request the next frame of data:
	//isLock = 0;
	continuePlaying();
}

void setRecord(int record) {
	isRecord = record;
}
int getJpgSize() {
	return jpgSize;
}
int getVideoWidth() {
    return width;
}
int getVideoHeight() {
    return height;
}
u_int8_t *getSpsBuffer() {
	return spsBuffer;
}
int getSpsSize() {
	return spsSize;
}
u_int8_t *getPpsBuffer() {
	return ppsBuffer;
}
int getPpsSize() {
	return ppsSize;
}
int getInitKeying(){
	return initKeying;
}
u_int8_t *getJpgBuffer() {
	return jpgBuffer;
}

unsigned getFirstSize() {
	return firstSize;
}

unsigned getSecondSize() {
	return secondSize;
}
unsigned getKeySize() {
	return keySize;
}
u_int8_t *getKeyBuffer() {
	return keyBuffer;
}
u_int8_t *getFirstBuffer() {
	return firstBuffer;
}

u_int8_t *getSecondBuffer() {
	return secondBuffer;
}
void cleanAllBuffer() {
	count_jpg = 0;
	if (firstBuffer) {
		delete firstBuffer;
		firstBuffer = NULL;
		firstSize = 0;
	}
	if (secondBuffer) {
		delete secondBuffer;
		secondBuffer = NULL;
		secondSize = 0;
	}
	if (keyBuffer) {
		delete keyBuffer;
		keyBuffer = NULL;
		keySize = 0;
	}
	if (jpgBuffer) {
		delete jpgBuffer;
		jpgBuffer = NULL;
	}
	if (spropRecords) {
		delete[] spropRecords;
		spropRecords = NULL;
	}
}
void cleanUpJpgBuffer() {
	if (jpgBuffer) {
		delete jpgBuffer;
		jpgBuffer = NULL;
	}
}

unsigned getSpropCount() {
	return spropCount;
}

SPropRecord *getSpropRecords() {
	return spropRecords;
}

Boolean DummySink::continuePlaying() {
	LOGD("continuePlaying");
	if (fSource == NULL) {
		LOGD("fSource == NULL");
		return False; // sanity check (should not happen)
	}
	// Request the next frame of data from our input source.  "afterGettingFrame()" will get called later, when it arrives:
	fSource->getNextFrame(fReceiveBuffer, DUMMY_SINK_RECEIVE_BUFFER_SIZE,
			afterGettingFrame, this, onSourceClosure, this);
	LOGD("getNextFrame True");
	return True;
}

void Android_JNI_OnSPSFrame(u_int8_t* buffer, unsigned frameSize, int width, int height) {
    spsBuffer = (uint8_t *) malloc(frameSize);
    memcpy(spsBuffer, buffer, frameSize);
	spsSize = frameSize;

	JNIEnv *mEnv;
	gs_jvm->AttachCurrentThread((JNIEnv **) &mEnv, NULL); //閻庣數鎳撶花鍙夋交濞嗗繐娈ら柛娆嶅劥椤曗晠鎯傞幋鎺斿晩閻忓繗椴稿Σ鍛婄鎼存繄鐟愰梻鍫涘灩閸ら亶寮０浣藉幀闁硅泛锕よぐ澶愭煂韫囨挸绲块柛鎴犲劋濞肩敻宕烽妸顭戝殙缂佹崘娉曢埢鍏肩▔椤撴繂鈻忛柣顤庢嫹
	//jclass cls = mEnv->GetObjectClass(gs_object);
	jmethodID mid = mEnv->GetStaticMethodID(gs_object, "onJniSPSFrame", "([BII)V");
	if (mid) {
		jbyteArray jbarray = mEnv->NewByteArray(frameSize);	//建立jbarray数组
		jbyte* savedata = (jbyte *) malloc(frameSize);
		memset(savedata, 0, frameSize);
		memcpy(savedata, buffer, frameSize);
		mEnv->SetByteArrayRegion(jbarray, 0, frameSize, savedata);
		mEnv->CallStaticVoidMethod(gs_object, mid, jbarray, width, height);          //回调java方法
		free(savedata);
		savedata = NULL;
	}
	gs_jvm->DetachCurrentThread(); //闁搞儳鍋為弫锟�
}

void Android_JNI_OnPPSFrame(u_int8_t* buffer, unsigned frameSize) {
    ppsBuffer = (uint8_t *) malloc(frameSize);
    memcpy(ppsBuffer, buffer, frameSize);
    ppsSize = frameSize;

    JNIEnv *mEnv;
    gs_jvm->AttachCurrentThread((JNIEnv **) &mEnv, NULL); //閻庣數鎳撶花鍙夋交濞嗗繐娈ら柛娆嶅劥椤曗晠鎯傞幋鎺斿晩閻忓繗椴稿Σ鍛婄鎼存繄鐟愰梻鍫涘灩閸ら亶寮０浣藉幀闁硅泛锕よぐ澶愭煂韫囨挸绲块柛鎴犲劋濞肩敻宕烽妸顭戝殙缂佹崘娉曢埢鍏肩▔椤撴繂鈻忛柣顤庢嫹
    //jclass cls = mEnv->GetObjectClass(gs_object);
    jmethodID mid = mEnv->GetStaticMethodID(gs_object, "onJniPPSFrame", "([B)V");
    if (mid) {
        jbyteArray jbarray = mEnv->NewByteArray(frameSize);	//建立jbarray数组
        jbyte* savedata = (jbyte *) malloc(frameSize);
        memset(savedata, 0, frameSize);
        memcpy(savedata, buffer, frameSize);
        mEnv->SetByteArrayRegion(jbarray, 0, frameSize, savedata);
        mEnv->CallStaticVoidMethod(gs_object, mid, jbarray);          //回调java方法
        free(savedata);
        savedata = NULL;
    }
    gs_jvm->DetachCurrentThread(); //闁搞儳鍋為弫锟�
}

void Android_JNI_OnFrame(u_int8_t* buffer, unsigned frameSize) {
	JNIEnv *mEnv;
	gs_jvm->AttachCurrentThread((JNIEnv **) &mEnv, NULL); //閻庣數鎳撶花鍙夋交濞嗗繐娈ら柛娆嶅劥椤曗晠鎯傞幋鎺斿晩閻忓繗椴稿Σ鍛婄鎼存繄鐟愰梻鍫涘灩閸ら亶寮０浣藉幀闁硅泛锕よぐ澶愭煂韫囨挸绲块柛鎴犲劋濞肩敻宕烽妸顭戝殙缂佹崘娉曢埢鍏肩▔椤撴繂鈻忛柣顤庢嫹
	//jclass cls = mEnv->GetObjectClass(gs_object);
	jmethodID mid = mEnv->GetStaticMethodID(gs_object, "onJniFrame", "([B)V");
	if (mid) {
		jbyteArray jbarray = mEnv->NewByteArray(frameSize);	//建立jbarray数组
		jbyte* savedata = (jbyte *) malloc(frameSize);
		memset(savedata, 0, frameSize);
		memcpy(savedata, buffer, frameSize);
		mEnv->SetByteArrayRegion(jbarray, 0, frameSize, savedata);
		mEnv->CallStaticVoidMethod(gs_object, mid, jbarray);          //回调java方法
		free(savedata);
		savedata = NULL;
	}
	gs_jvm->DetachCurrentThread(); //闁搞儳鍋為弫锟�
}

void initH264DecodeEnv()
{
	av_register_all();
	avcodec_register_all();

	/* find the video decoder */
	if (!(videoCodec=avcodec_find_decoder(AV_CODEC_ID_H264))){
		LOGD("h264_mediacodec not found!");
		exit(1) ;
	}

    videoCodec->capabilities |= AV_CODEC_CAP_SLICE_THREADS;
    context= avcodec_alloc_context3(videoCodec);
    if (context == NULL) {
        LOGD("avcodec_alloc_context3 failed");
        exit(1) ;
    }

    context->thread_type |=FF_THREAD_SLICE;
    context->thread_count = 16;

	int ret = avcodec_open2(context, videoCodec, NULL);

	if( ret < 0 ) {
		LOGD("could not open codec! ret is %d", ret);
		exit(1) ;
	}

	av_init_packet(&packet);

	picture = av_frame_alloc();// Allocate video frame
}

void decodeFrame(u_int8_t* inBuffer, unsigned inSize) {
	int frameFinished = 0;//这个是随便填入数字，没什么作用

	LOGD("decode frame of inSize %d", inSize);
	packet.data = new u_int8_t[inSize+3];//这里填入一个指向完整H264数据帧的指针
	packet.data[0] = 0;
	packet.data[1] = 0;
	packet.data[2] = 1;
	memcpy(packet.data+3, inBuffer, inSize);
	packet.size = inSize+3;//这个填入H264数据帧的大小

	//下面开始真正的解码
	avcodec_decode_video2(context, picture, &frameFinished, &packet);
	LOGD("decode frame of frameFinished %d", frameFinished);
	if(frameFinished) {//成功解码
		LOGD("decode frame of frameFinished...%d, %d, %d", picture->linesize[0], picture->linesize[1], picture->linesize[2]);
		int numBytes = avpicture_get_size(AV_PIX_FMT_YUVJ420P, context->width, context->height);
		uint8_t *fill_buffer = (uint8_t *)av_malloc(numBytes * sizeof(uint8_t));

		int i,j,k;
		for(i = 0 ; i < context->height ; i++)
		{
			memcpy(fill_buffer+context->width*i,
				   picture->data[0]+picture->linesize[0]*i,
				   context->width);
		}
		for(j = 0 ; j < context->height/2 ; j++)
		{
			memcpy(fill_buffer+context->width*i+context->width/2*j,
				   picture->data[1]+picture->linesize[1]*j,
				   context->width/2);
		}
		for(k  =0 ; k < context->height/2 ; k++)
		{
			memcpy(fill_buffer+context->width*i+context->width/2*j+context->width/2*k,
				   picture->data[2]+picture->linesize[2]*k,
				   context->width/2);
		}

		LOGD("decode frame of go jni");
		Android_JNI_OnFrame(fill_buffer, numBytes);

		free(fill_buffer);
	}
}

void Android_JNI_SetIsConnect(int mConnect) {
	LOGD("Android_JNI_SetIsConnect");
	JNIEnv *mEnv;
	gs_jvm->AttachCurrentThread((JNIEnv **) &mEnv, NULL); //閻庣數鎳撶花鍙夋交濞嗗繐娈ら柛娆嶅劥椤曗晠鎯傞幋鎺斿晩閻忓繗椴稿Σ鍛婄鎼存繄鐟愰梻鍫涘灩閸ら亶寮０浣藉幀闁硅泛锕よぐ澶愭煂韫囨挸绲块柛鎴犲劋濞肩敻宕烽妸顭戝殙缂佹崘娉曢埢鍏肩▔椤撴繂鈻忛柣顤庢嫹
	//jclass cls = mEnv->GetObjectClass(gs_object);
	jmethodID mid = mEnv->GetStaticMethodID(gs_object, "playResult", "(I)V");
	LOGD("CallStaticVoidMethod");
	if (mid) {
		LOGD("mid");
		mEnv->CallStaticVoidMethod(gs_object, mid, mConnect);
	}
	gs_jvm->DetachCurrentThread(); //闁搞儳鍋為弫锟�
	LOGD("end");
}

#endif /*__ANDROID__*/

