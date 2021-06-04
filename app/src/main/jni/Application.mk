# The ARMv7 is significanly faster due to the use of the hardware FPU
# APP_ABI := arm64-v8a armeabi-v7a x86_64 x86
# APP_STL := c++_shared

ifeq ($(NDK_STL), 1)
APP_STL := c++_static
endif

ifeq ($(NDK_DEBUG), 1)
APP_CPPFLAGS += -frtti
else
APP_CPPFLAGS += -fno-rtti
endif

ifeq ($(origin NDK_ABI), undefined)
APP_ABI := arm64-v8a armeabi-v7a
else
APP_ABI := $(NDK_ABI)
endif

APP_PLATFORM := android-16
APP_CPPFLAGS += -std=c++1y -fno-exceptions