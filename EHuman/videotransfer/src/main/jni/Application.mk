
# Uncomment this if you're using STL in your project
# See CPLUSPLUS-SUPPORT.html in the NDK documentation for more information
# APP_STL := stlport_static 

APP_ABI :=armeabi armeabi-v7a x86 mips
APP_STL	:=gnustl_shared
# APP_PLATFORM := android-17
APP_CPPFLAGS += -Wno-error=format-security