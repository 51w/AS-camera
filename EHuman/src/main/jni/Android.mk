LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCVROOT:=$(LOCAL_PATH)/OpenCV-android-sdk
#OPENCVROOT:=$(LOCAL_PATH)/opencv341/OpenCV-android-sdk
OPENCVROOT:=$(LOCAL_PATH)/../../../../../opencv-3.2.0-android-sdk/OpenCV-android-sdk
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
#OPENCV_LIB_TYPE:=SHARED
OPENCV_LIB_TYPE:=STATIC

include ${OPENCVROOT}/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := mobilenet.cpp \
                    v20/fdssttracker.cpp v20/fhog.cpp v20/runtracker.cpp \
                    v20/lbp.cpp \
                    ehuman.cpp

LOCAL_CPPFLAGS += -fexceptions -frtti -fopenmp
LOCAL_CFLAGS   += -fopenmp
LOCAL_CPPFLAGS += -fopenmp
LOCAL_LDLIBS   += -fopenmp

LOCAL_LDFLAGS := $(LOCAL_PATH)/lib/$(TARGET_ARCH_ABI)/libncnn.a
#LOCAL_LDFLAGS := $(LOCAL_PATH)/lib/libncnn-armeabi-v7a.a   # v7
#LOCAL_LDFLAGS := $(LOCAL_PATH)/lib/libncnn.a  # v8

LOCAL_C_INCLUDES +=$(LOCAL_PATH)/include
LOCAL_C_INCLUDES +=$(LOCAL_PATH)/v20

LOCAL_LDLIBS += -llog -ljnigraphics -fopenmp

LOCAL_MODULE := ehuman

LOCAL_ARM_NEON := true


#include $(BUILD_EXECUTABLE)
include $(BUILD_SHARED_LIBRARY)