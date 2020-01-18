///////////////////////////////////////////////////////////////////////////////
// gifimage.h
//
// Author : Garfield
// Creation Date : 2015/4/13

#ifndef __GIFIMAGE_H__
#define __GIFIMAGE_H__

#include "gif_lib.h"
#include "gdiutil.h"
#include "strmutil.h"
#include "fileutil.h"

///////////////////////////////////////////////////////////////////////////////
// JNI native methods in this file:
//
// nativeDecodeFile()
// nativeDecodeArray()
// nativeDecodeStream()
// nativeDraw()
// nativeDestroy()
// nativeGetWidth()
// nativeGetHeight()
// nativeGetFrameDelay()
// nativeGetFrameCount()

namespace GIFImage {

///////////////////////////////////////////////////////////////////////////////
// Global variables
//

// Class FileDescriptor field IDs.
static jfieldID _descriptorID;

///////////////////////////////////////////////////////////////////////////////
// Global functions
//

template <typename _Ty>
__STATIC_INLINE__ int GifReadProc(GifFileType* GIF, GifByteType* buffer, int size)
{
    assert(GIF);
    assert(buffer);
    assert(size > 0);

    return reinterpret_cast<_Ty*>(GIF->UserData)->read(buffer, size);
}

template <typename _Ty>
__STATIC_INLINE__ __NS::GIFImage* GifDecodeImage(_Ty& userData)
{
#ifndef NDEBUG
    int error = D_GIF_SUCCEEDED;
    GifFileType* GIF = ::DGifOpen(&userData, GifReadProc<_Ty>, &error);
    if (GIF == NULL)
    {
        LOGE("Couldn't open GIF, error = %d, error message = %s\n", error, ::GifErrorString(error));
        return NULL;
    }

    if (::DGifSlurp(GIF) != GIF_OK)
    {
        LOGE("Couldn't decode GIF, error = %d, error message = %s\n", GIF->Error, ::GifErrorString(GIF->Error));
        ::DGifCloseFile(GIF, NULL);
        return NULL;
    }

    if (GIF->ImageCount < 1 || GIF->SWidth <= 0 || GIF->SHeight <= 0)
    {
        LOGE("Invalid GIF image, frameCount = %d, width = %d, height = %d\n", GIF->ImageCount, GIF->SWidth, GIF->SHeight);
        ::DGifCloseFile(GIF, NULL);
        return NULL;
    }

    __NS::GIFImage* result = new __NS::GIFImage(GIF);
    result->dump();
#else
    __NS::GIFImage* result = NULL;
    if (GifFileType* GIF = ::DGifOpen(&userData, GifReadProc<_Ty>, NULL))
    {
        if (::DGifSlurp(GIF) == GIF_OK && GIF->ImageCount >= 1 && GIF->SWidth > 0 && GIF->SHeight > 0)
            result = new __NS::GIFImage(GIF);
        else
            ::DGifCloseFile(GIF, NULL);
    }
#endif  // NDEBUG

    return result;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeDecodeFile
// Signature: (Ljava/io/FileDescriptor;)J

JNIEXPORT_METHOD(jlong) nativeDecodeFile(JNIEnv* env, jclass /*clazz*/, jobject fd)
{
    assert(fd);
    assert(env);

    __NS::FileHandle file(env->GetIntField(fd, _descriptorID));
    return reinterpret_cast<jlong>(GifDecodeImage(file));
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeDecodeArray
// Signature: ([BII)J

JNIEXPORT_METHOD(jlong) nativeDecodeArray(JNIEnv* env, jclass /*clazz*/, jbyteArray data, jint offset, jint length)
{
    assert(env);
    assert(data);
    assert(offset >= 0 && length >= 0);
    assert(env->GetArrayLength(data) - offset >= length);

    __NS::ByteArrayInputStream is(env, data, length, offset);
    return reinterpret_cast<jlong>(GifDecodeImage(is));
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeDecodeStream
// Signature: (Ljava/io/InputStream;[B)J

JNIEXPORT_METHOD(jlong) nativeDecodeStream(JNIEnv* env, jclass /*clazz*/, jobject stream, jbyteArray tempStorage)
{
    assert(env);
    assert(stream);
    assert(tempStorage);

    __NS::BufferedInputStream is(env, stream, tempStorage);
    return reinterpret_cast<jlong>(GifDecodeImage(is));
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeDraw
// Signature: (Landroid/graphics/Bitmap;JI)Z

JNIEXPORT_METHOD(jboolean) nativeDraw(JNIEnv* env, jclass /*clazz*/, jobject bitmapCanvas, jlong nativeImage, jint frameIndex)
{
    assert(env);
    assert(nativeImage);
    assert(bitmapCanvas);
    assert(frameIndex >= 0 && frameIndex < reinterpret_cast<__NS::GIFImage*>(nativeImage)->getFrameCount());

    void* canvas = NULL;
    __NS::Bitmap jbitmapCanvas(env, bitmapCanvas);
    const jboolean successful = (jbitmapCanvas.lockPixels(canvas) == ANDROID_BITMAP_RESULT_SUCCESS);
    if (successful)
        reinterpret_cast<__NS::GIFImage*>(nativeImage)->draw((uint32_t*)canvas, frameIndex);

    return successful;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeDestroy
// Signature: (J)V

JNIEXPORT_METHOD(void) nativeDestroy(JNIEnv* /*env*/, jclass /*clazz*/, jlong nativeImage)
{
    assert(nativeImage);
    delete reinterpret_cast<__NS::GIFImage*>(nativeImage);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeGetWidth
// Signature: (J)I

JNIEXPORT_METHOD(jint) nativeGetWidth(JNIEnv* /*env*/, jclass /*clazz*/, jlong nativeImage)
{
    assert(nativeImage);
    return reinterpret_cast<__NS::GIFImage*>(nativeImage)->getWidth();
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeGetHeight
// Signature: (J)I

JNIEXPORT_METHOD(jint) nativeGetHeight(JNIEnv* /*env*/, jclass /*clazz*/, jlong nativeImage)
{
    assert(nativeImage);
    return reinterpret_cast<__NS::GIFImage*>(nativeImage)->getHeight();
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeGetFrameDelay
// Signature: (JI)I

JNIEXPORT_METHOD(jint) nativeGetFrameDelay(JNIEnv* /*env*/, jclass /*clazz*/, jlong nativeImage, jint frameIndex)
{
    assert(nativeImage);
    assert(frameIndex >= 0 && frameIndex < reinterpret_cast<__NS::GIFImage*>(nativeImage)->getFrameCount());

    return reinterpret_cast<__NS::GIFImage*>(nativeImage)->getFrameDelay(frameIndex);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     GIFImage
// Method:    nativeGetFrameCount
// Signature: (J)I

JNIEXPORT_METHOD(jint) nativeGetFrameCount(JNIEnv* /*env*/, jclass /*clazz*/, jlong nativeImage)
{
    assert(nativeImage);
    return reinterpret_cast<__NS::GIFImage*>(nativeImage)->getFrameCount();
}

///////////////////////////////////////////////////////////////////////////////
// Register native methods functions
//

__STATIC_INLINE__ jint registerNativeMethods(JNIEnv* env)
{
    assert(env);
    LOGD("Register class " PACKAGE_GRAPHICS "GIFImage native methods.\n");

    // Initializes class FileDescriptor field IDs.
    _descriptorID = JNI::jclass_t(env, "java/io/FileDescriptor").getFieldID("descriptor", "I");
    assert(_descriptorID != NULL);

    // Registers class GIFImage native methods.
    const JNINativeMethod methods[] =
    {
        { "nativeDestroy", "(J)V", (void*)nativeDestroy },
        { "nativeGetWidth", "(J)I", (void*)nativeGetWidth },
        { "nativeGetHeight", "(J)I", (void*)nativeGetHeight },
        { "nativeDecodeArray", "([BII)J", (void*)nativeDecodeArray },
        { "nativeGetFrameCount", "(J)I", (void*)nativeGetFrameCount },
        { "nativeGetFrameDelay", "(JI)I", (void*)nativeGetFrameDelay },
        { "nativeDraw", "(Landroid/graphics/Bitmap;JI)Z", (void*)nativeDraw },
        { "nativeDecodeFile", "(Ljava/io/FileDescriptor;)J", (void*)nativeDecodeFile },
        { "nativeDecodeStream", "(Ljava/io/InputStream;[B)J", (void*)nativeDecodeStream },
    };

    return JNI::jclass_t(env, PACKAGE_GRAPHICS "GIFImage").registerNatives(methods, _countof(methods));
}

}  // namespace GIFImage

#endif  // __GIFIMAGE_H__
