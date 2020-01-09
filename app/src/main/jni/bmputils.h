///////////////////////////////////////////////////////////////////////////////
// bmputils.h
//
// Copyright(c) 2019, Garfield. All rights reserved.
// Author  : Garfield
// Version : 1.0
// Creation Date : 2013/6/8

#ifndef __BITMAPUTILS_H__
#define __BITMAPUTILS_H__

#include "jniutil.h"
#include "gdiutil.h"
#include "imgalgth.h"

///////////////////////////////////////////////////////////////////////////////
// JNI native methods in this file:
//
// grayBitmap()
// blurBitmap()
// binaryBitmap()
// spreadBitmap()
// mosaicBitmap()
// mirrorBitmap()
// inverseBitmap()

namespace BitmapUtils {

///////////////////////////////////////////////////////////////////////////////
// Global functions
//

template <typename THandler>
__STATIC_INLINE__ jboolean handleBitmap(JNIEnv* env, jobject bitmap, THandler handler)
{
    assert(env);
    assert(bitmap);

    void* pixels = NULL;
    AndroidBitmapInfo info;
    __NS::Bitmap jbitmap(env, bitmap);

    // Gets the bitmap info and lock pixels.
    const jboolean successful = (jbitmap.getBitmapInfo(info) == ANDROID_BITMAP_RESULT_SUCCESS && jbitmap.lockPixels(pixels) == ANDROID_BITMAP_RESULT_SUCCESS);
    if (successful)
    {
    #ifndef NDEBUG
        jbitmap.checkMutable(info);
    #endif  // NDEBUG

        handler(pixels, info.width, info.height);
    }

    return successful;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     BitmapUtils
// Method:    grayBitmap
// Signature: (Landroid/graphics/Bitmap;)Z

JNIEXPORT_METHOD(jboolean) grayBitmap(JNIEnv* env, jclass /*clazz*/, jobject bitmap)
{
    assert(env);
    AssertThrowErrnoException(env, bitmap == NULL, "bitmap == null", JNI_FALSE);

#ifndef NDEBUG
    return handleBitmap(env, bitmap, [](void* pixels, uint32_t width, uint32_t height)
    {
        const __NS::Color* colors = (const __NS::Color*)pixels;
        const uint32_t size = ::__Min(width / 4, 30U);
        for (uint32_t i = 0; i < size; ++i)
            colors[i].dump();

        ::Android_grayBitmap(pixels, width, height);
    });
#else
    return handleBitmap(env, bitmap, ::Android_grayBitmap);
#endif  // NDEBUG
}

///////////////////////////////////////////////////////////////////////////////
// Class:     BitmapUtils
// Method:    blurBitmap
// Signature: (Landroid/graphics/Bitmap;I)Z

JNIEXPORT_METHOD(jboolean) blurBitmap(JNIEnv* env, jclass /*clazz*/, jobject bitmap, jint radius)
{
    assert(env);
    AssertThrowErrnoException(env, bitmap == NULL, "bitmap == null", JNI_FALSE);

    return handleBitmap(env, bitmap, [radius](void* pixels, uint32_t width, uint32_t height) { ::Android_blurBitmap(pixels, width, height, radius); });
}

///////////////////////////////////////////////////////////////////////////////
// Class:     BitmapUtils
// Method:    binaryBitmap
// Signature: (Landroid/graphics/Bitmap;Z)Z

JNIEXPORT_METHOD(jboolean) binaryBitmap(JNIEnv* env, jclass /*clazz*/, jobject bitmap, jboolean grayscale)
{
    assert(env);
    AssertThrowErrnoException(env, bitmap == NULL, "bitmap == null", JNI_FALSE);

    return handleBitmap(env, bitmap, [grayscale](void* pixels, uint32_t width, uint32_t height) { ::Android_binaryBitmap(pixels, width, height, grayscale); });
}

///////////////////////////////////////////////////////////////////////////////
// Class:     BitmapUtils
// Method:    spreadBitmap
// Signature: (Landroid/graphics/Bitmap;I)Z

JNIEXPORT_METHOD(jboolean) spreadBitmap(JNIEnv* env, jclass /*clazz*/, jobject bitmap, jint spreadSize)
{
    assert(env);
    AssertThrowErrnoException(env, bitmap == NULL, "bitmap == null", JNI_FALSE);

    return handleBitmap(env, bitmap, [spreadSize](void* pixels, uint32_t width, uint32_t height) { ::Android_spreadBitmap(pixels, width, height, spreadSize); });
}

///////////////////////////////////////////////////////////////////////////////
// Class:     BitmapUtils
// Method:    mosaicBitmap
// Signature: (Landroid/graphics/Bitmap;I)Z

JNIEXPORT_METHOD(jboolean) mosaicBitmap(JNIEnv* env, jclass /*clazz*/, jobject bitmap, jint mosaicSize)
{
    assert(env);
    AssertThrowErrnoException(env, bitmap == NULL, "bitmap == null", JNI_FALSE);

    return handleBitmap(env, bitmap, [mosaicSize](void* pixels, uint32_t width, uint32_t height) { ::Android_mosaicBitmap(pixels, width, height, mosaicSize); });
}

///////////////////////////////////////////////////////////////////////////////
// Class:     BitmapUtils
// Method:    mirrorBitmap
// Signature: (Landroid/graphics/Bitmap;Z)Z

JNIEXPORT_METHOD(jboolean) mirrorBitmap(JNIEnv* env, jclass /*clazz*/, jobject bitmap, jboolean horizontal)
{
    assert(env);
    AssertThrowErrnoException(env, bitmap == NULL, "bitmap == null", JNI_FALSE);

    return handleBitmap(env, bitmap, [horizontal](void* pixels, uint32_t width, uint32_t height) { ::Android_mirrorBitmap(pixels, width, height, horizontal); });
}

///////////////////////////////////////////////////////////////////////////////
// Class:     BitmapUtils
// Method:    inverseBitmap
// Signature: (Landroid/graphics/Bitmap;)Z

JNIEXPORT_METHOD(jboolean) inverseBitmap(JNIEnv* env, jclass /*clazz*/, jobject bitmap)
{
    assert(env);
    AssertThrowErrnoException(env, bitmap == NULL, "bitmap == null", JNI_FALSE);

    return handleBitmap(env, bitmap, ::Android_inverseBitmap);
}

///////////////////////////////////////////////////////////////////////////////
// Register native methods functions
//

__STATIC_INLINE__ jint registerNativeMethods(JNIEnv* env)
{
    assert(env);

    const JNINativeMethod methods[] =
    {
        { "grayBitmap", "(Landroid/graphics/Bitmap;)Z", (void*)grayBitmap },
        { "blurBitmap", "(Landroid/graphics/Bitmap;I)Z", (void*)blurBitmap },
        { "spreadBitmap", "(Landroid/graphics/Bitmap;I)Z", (void*)spreadBitmap },
        { "mosaicBitmap", "(Landroid/graphics/Bitmap;I)Z", (void*)mosaicBitmap },
        { "mirrorBitmap", "(Landroid/graphics/Bitmap;Z)Z", (void*)mirrorBitmap },
        { "binaryBitmap", "(Landroid/graphics/Bitmap;Z)Z", (void*)binaryBitmap },
        { "inverseBitmap", "(Landroid/graphics/Bitmap;)Z", (void*)inverseBitmap },
    };

    LOGD("Register class " PACKAGE_GRAPHICS "BitmapUtils native methods.\n");
    return JNI::jclass_t(env, PACKAGE_GRAPHICS "BitmapUtils").registerNatives(methods, _countof(methods));
}

}  // namespace BitmapUtils

#endif  // __BITMAPUTILS_H__
