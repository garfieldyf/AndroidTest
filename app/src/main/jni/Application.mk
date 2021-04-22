# The ARMv7 is significanly faster due to the use of the hardware FPU
# APP_ABI := arm64-v8a armeabi-v7a x86_64 x86

APP_PLATFORM := android-16
APP_CPPFLAGS += -std=c++1y

ifeq ($(origin NDK_ABI), undefined)
APP_ABI := arm64-v8a armeabi-v7a
else
APP_ABI := $(NDK_ABI)
endif

ifeq ($(NDK_STL), 1)
APP_STL := c++_static
else
APP_CPPFLAGS += -fno-rtti -fno-exceptions
endif
