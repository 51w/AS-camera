$(warning testbyalex -->$(TARGET_ARCH_ABI))
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavcodec
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/libavcodec-57.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavformat
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/libavformat-57.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libswscale
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/libswscale-4.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavutil
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/libavutil-55.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavfilter
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/libavfilter-6.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libswresample
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/libswresample-2.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libmp4v2
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/libMp4v2.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= liblive555
LOCAL_SRC_FILES:= lib-$(TARGET_ARCH_ABI)/liblive555.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE := crc16
#LOCAL_SRC_FILES :=control_CRC16.c
#LOCAL_LDLIBS := -llog -lz
#LOCAL_STATIC_LIBRARIES := x265
#include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := main
LOCAL_C_INCLUDES :=$(LOCAL_PATH)/ $(LOCAL_PATH)/include/ $(LOCAL_PATH)/mp4v2/ $(LOCAL_PATH)/live/BasicUsageEnvironment/include/ $(LOCAL_PATH)/live/liveMedia/include/ $(LOCAL_PATH)/live/UsageEnvironment/include/ $(LOCAL_PATH)/live/groupsock/include/
# Add your application source files here...

LOCAL_SRC_FILES := com_allwinner_imagetransfer_jni_PreviewJNI.cpp \
    com_allwinner_mr100_jni_CRC16.cpp \
	player.cpp \
	h264tojpg.cpp \
	h264tomp4.cpp \
    h264_sei.cpp \
    h264_stream.cpp

LOCAL_SHARED_LIBRARIES :=libavcodec libavfilter libavformat libswscale libavutil libswresample liblive555 libmp4v2
LOCAL_CPPFLAGS += -fexceptions
LOCAL_PROGUARD_ENABLED:= disabled
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
$(warning testbyalex -->finished)
