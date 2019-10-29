LOCAL_PATH := $(call my-dir)
LOCAL_MD_MODULE       := md
LOCAL_GIF_MODULE      := gif
LOCAL_IMGALGTH_MODULE := imgalgth

ifeq ($(origin LOCAL_INCLUDE_PATH), undefined)
LOCAL_INCLUDE_PATH := E:/Include
endif

ifeq ($(origin LOCAL_MODULE_PATH), undefined)
LOCAL_MODULE_PATH := $(LOCAL_INCLUDE_PATH)/android/libs
endif

###############################################################################
# Prebuilt static libraries
#

ifeq ($(all),1)
include $(LOCAL_MODULE_PATH)/$(LOCAL_MD_MODULE).mk
include $(LOCAL_MODULE_PATH)/$(LOCAL_GIF_MODULE).mk
include $(LOCAL_MODULE_PATH)/$(LOCAL_IMGALGTH_MODULE).mk
else
ifeq ($(gifimage),1)
include $(LOCAL_MODULE_PATH)/$(LOCAL_GIF_MODULE).mk
endif

ifeq ($(messagedigests),1)
include $(LOCAL_MODULE_PATH)/$(LOCAL_MD_MODULE).mk
endif

ifeq ($(bitmaputils),1)
include $(LOCAL_MODULE_PATH)/$(LOCAL_IMGALGTH_MODULE).mk
endif
endif

###############################################################################
# Build module
#

include $(CLEAR_VARS)

ifeq ($(NDK_STL),1)
LOCAL_CPPFLAGS += -D__NDK_STLP__
endif

ifeq ($(NDK_DEBUG),1)
LOCAL_CPPFLAGS += -D_CRT_DEBUG_DUMP
LOCAL_LDLIBS   += -llog
else
LOCAL_CPPFLAGS += -O3 -finline-functions -felide-constructors -funswitch-loops
endif

ifeq ($(all),1)
LOCAL_CPPFLAGS += -D__BUILD_GIFIMAGE__ -D__BUILD_FILEUTILS__ -D__BUILD_PROCUTILS__ -D__BUILD_BITMAPUTILS__ -D__BUILD_MESSAGEDIGESTS__
LOCAL_LDLIBS   += -landroid -ljnigraphics
LOCAL_STATIC_LIBRARIES += $(LOCAL_MD_MODULE) $(LOCAL_GIF_MODULE) $(LOCAL_IMGALGTH_MODULE)
else
ifeq ($(gifimage),1)
LOCAL_CPPFLAGS += -D__BUILD_GIFIMAGE__
LOCAL_LDLIBS   += -landroid -ljnigraphics
LOCAL_STATIC_LIBRARIES += $(LOCAL_GIF_MODULE)
endif

ifeq ($(fileutils),1)
LOCAL_CPPFLAGS += -D__BUILD_FILEUTILS__
LOCAL_LDLIBS   += -landroid
endif

ifeq ($(processutils),1)
LOCAL_CPPFLAGS += -D__BUILD_PROCUTILS__
endif

ifeq ($(bitmaputils),1)
LOCAL_CPPFLAGS += -D__BUILD_BITMAPUTILS__
LOCAL_LDLIBS   += -ljnigraphics
LOCAL_STATIC_LIBRARIES += $(LOCAL_IMGALGTH_MODULE)
endif

ifeq ($(messagedigests), 1)
LOCAL_CPPFLAGS += -D__BUILD_MESSAGEDIGESTS__
LOCAL_STATIC_LIBRARIES += $(LOCAL_MD_MODULE)
endif
endif

ifeq ($(origin LOCAL_MODULE_NAME), undefined)
LOCAL_MODULE_NAME := androidext
endif

ifeq ($(origin PACKAGE_GRAPHICS), undefined)
PACKAGE_GRAPHICS := android/ext/graphics/
endif

ifeq ($(origin PACKAGE_UTILITIES), undefined)
PACKAGE_UTILITIES := android/ext/util/
endif

LOCAL_C_INCLUDES += $(LOCAL_INCLUDE_PATH)/common $(LOCAL_INCLUDE_PATH)/android
LOCAL_CPPFLAGS   += -fvisibility=hidden -Wall -Wunused -Wcomment -Wparentheses -Wunused-label -Wchar-subscripts -Wunused-variable \
                    -Wunused-function -Wunused-parameter -Wuninitialized -Wreturn-type -Wformat -Wshadow -Wunused-value -Wpointer-arith \
                    -DPACKAGE_UTILITIES=\"$(PACKAGE_UTILITIES)\" -DPACKAGE_GRAPHICS=\"$(PACKAGE_GRAPHICS)\"
LOCAL_MODULE     := $(LOCAL_MODULE_NAME)
LOCAL_SRC_FILES  += main.cpp

include $(BUILD_SHARED_LIBRARY)
