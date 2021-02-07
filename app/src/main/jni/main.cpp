///////////////////////////////////////////////////////////////////////////////
// main.cpp
//
// Author : Garfield
// Creation Date : 2012/4/16

#ifdef __BUILD_GIFIMAGE__
#include "gifimage.h"
#endif

#ifdef __BUILD_FILEUTILS__
#include "fileutils.h"
#endif

#ifdef __BUILD_MESSAGEDIGESTS__
#include "msgdigts.h"
#endif

#ifdef __BUILD_BITMAPUTILS__
#include "bmputils.h"
#endif

#ifdef __BUILD_PROCUTILS__
#include "procutils.h"
#endif

#ifndef NDEBUG
__STATIC_INLINE__ jstring getByteOrder(JNIEnv* env)
{
    assert(env);

    jclass clazz = env->FindClass("java/nio/ByteOrder");
    assert(clazz != NULL);

    jobject byteOrder = env->CallStaticObjectMethod(clazz, env->GetStaticMethodID(clazz, "nativeOrder", "()Ljava/nio/ByteOrder;"));
    assert(byteOrder != NULL);

    return (jstring)env->CallObjectMethod(byteOrder, env->GetMethodID(clazz, "toString", "()Ljava/lang/String;"));
}
#endif  // NDEBUG

__BEGIN_DECLS
///////////////////////////////////////////////////////////////////////////////
// The VM calls JNI_OnLoad when the native library is loaded.
//

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* /*reserved*/)
{
    JNIEnv* env = NULL;
    verify(vm->GetEnv((void**)&env, JNI_VERSION_1_4), JNI_OK);

#ifndef NDEBUG
    const uint32_t num = 0x12345678;
    JNI::jstring_t jbyteOrder(env, getByteOrder(env));
    const char* byteOrders[] = { "BIG_ENDIAN", "LITTLE_ENDIAN" };
    const uint32_t index = (*((const char*)&num) == 0x12 ? 0 : 1);
    LOGD("JNI version = 0x%08x, sdk = %d, sizeof(void*) = %zu, sizeof(int) = %zu, sizeof(long) = %zu, sizeof(Color) = %zu, java byteOrder = %s, native byteOrder = %s\n", env->GetVersion(), ::android_get_device_api_level(), sizeof(void*), sizeof(int), sizeof(long), sizeof(__NS::Color), jbyteOrder.str(), byteOrders[index]);
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
