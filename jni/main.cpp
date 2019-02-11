///////////////////////////////////////////////////////////////////////////////
// main.cpp
//
// Copyright(c) 2019, Garfield. All rights reserved.
// Author  : Garfield
// Version : 1.0
// Creation Date : 2012/4/16

#include "main.h"

#ifdef __BUILD_GIFIMAGE__
#pragma message "Compiling " PACKAGE_GRAPHICS "GIFImage native methods."
#include "gifimage.h"
#endif

#ifdef __BUILD_FILEUTILS__
#pragma message "Compiling " PACKAGE_UTILITIES "FileUtils native methods."
#include "fileutils.h"
#endif

#ifdef __BUILD_MESSAGEDIGESTS__
#pragma message "Compiling " PACKAGE_UTILITIES "MessageDigests native methods."
#include "msgdigts.h"
#endif

#ifdef __BUILD_BITMAPUTILS__
#pragma message "Compiling " PACKAGE_UTILITIES "BitmapUtils native methods."
#include "bmputils.h"
#endif

#ifdef __BUILD_PROCUTILS__
#pragma message "Compiling " PACKAGE_UTILITIES "ProcessUtils native methods."
#include "procutils.h"
#endif

///////////////////////////////////////////////////////////////////////////////
// Global variables
//

#ifdef __BUILD_GIFIMAGE__
// Class FileDescriptor field IDs.
STDCEXPORT jfieldID _descriptorID;
#endif

#ifdef __BUILD_FILEUTILS__
// Class FileUtils method IDs.
STDCEXPORT jmethodID _setStatID;

// Class ScanCallback method IDs.
STDCEXPORT jmethodID _onScanFileID;
#endif

__BEGIN_DECLS
///////////////////////////////////////////////////////////////////////////////
// The VM calls JNI_OnLoad when the native library is loaded.
//

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* /*reserved*/)
{
    JNIEnv* env = NULL;
    verify(vm->GetEnv((void**)&env, JNI_VERSION_1_4), JNI_OK);

#ifndef NDEBUG
    const int num = 0x12345678;
    const char* bigEndian = "BIG_ENDIAN";
    const char* littleEndian = "LITTLE_ENDIAN";
    const char* byteOrder = (*((const char*)&num) == 0x12 ? bigEndian : littleEndian);
    LOGD("sdk = %d, sizeof(void*) = %zu, sizeof(int) = %zu, sizeof(long) = %zu, byteOrder = %s\n", ::__android_sdk_version(), sizeof(void*), sizeof(int), sizeof(long), byteOrder);
#endif  // NDEBUG

#ifdef __BUILD_GIFIMAGE__
    verify(GIFImage::registerNativeMethods(env), JNI_OK);
#endif

#ifdef __BUILD_FILEUTILS__
    verify(FileUtils::registerNativeMethods(env), JNI_OK);
#endif

#ifdef __BUILD_BITMAPUTILS__
    verify(BitmapUtils::registerNativeMethods(env), JNI_OK);
#endif

#ifdef __BUILD_PROCUTILS__
    verify(ProcessUtils::registerNativeMethods(env), JNI_OK);
#endif

#ifdef __BUILD_MESSAGEDIGESTS__
    verify(MessageDigests::registerNativeMethods(env), JNI_OK);
#endif

    return JNI_VERSION_1_4;
}
__END_DECLS
