# The ARMv7 is significanly faster due to the use of the hardware FPU
# APP_ABI := arm64-v8a mips64 x86_64
# APP_ABI := armeabi armeabi-v7a x86 mips
# APP_STL := stlport_static gnustl_static c++_static

APP_ABI := armeabi-v7a arm64-v8a
APP_PLATFORM := android-16
APP_CPPFLAGS += -std=c++11

ifeq ($(NDK_STL),1)
APP_STL := c++_static
else
APP_CPPFLAGS += -fno-rtti -fno-exceptions
endif
