///////////////////////////////////////////////////////////////////////////////
// msgdigts.h
//
// Author : Garfield
// Creation Date : 2012/12/10

#ifndef __MSGDIGTS_H__
#define __MSGDIGTS_H__

#include "md.h"
#include "stdutil.h"
#include "fileutil.h"
#include "strmutil.h"

///////////////////////////////////////////////////////////////////////////////
// JNI native methods in this file:
//
// computeFile()
// computeString()
// computeByteArray()

namespace MessageDigests {

///////////////////////////////////////////////////////////////////////////////
// Enumerations
//

enum
{
    MD5    = 0,     // The MD5 hashing algorithm.
    SHA1   = 1,     // The SHA1(SHA) hashing algorithm.
    SHA256 = 2,     // The SHA256 hashing algorithm.
    SHA384 = 3,     // The SHA384 hashing algorithm.
    SHA512 = 4,     // The SHA512 hashing algorithm.
};

///////////////////////////////////////////////////////////////////////////////
// Global functions
//

template <typename TMessageDigest>
__STATIC_INLINE__ jint digestImpl(TMessageDigest& digest, JNIEnv* env, jbyteArray result, jsize offset)
{
    assert(env);
    assert(result);
    assert(offset >= 0);
    assert(env->GetArrayLength(result) - offset >= TMessageDigest::MESSAGE_DIGEST_LENGTH);

    u_char buffer[TMessageDigest::MESSAGE_DIGEST_LENGTH];
    digest.digest(buffer);
    env->SetByteArrayRegion(result, offset, TMessageDigest::MESSAGE_DIGEST_LENGTH, (const jbyte*)buffer);

    return TMessageDigest::MESSAGE_DIGEST_LENGTH;
}

template <typename TMessageDigest>
__STATIC_INLINE__ jint computeFileImpl(const __NS::File& file, JNIEnv* env, jbyteArray result, jint offset)
{
    assert(env);
    assert(result);
    assert(offset >= 0);
    assert(!file.isEmpty());

    ssize_t readBytes;
    u_char buffer[8192];
    TMessageDigest digest;

    while ((readBytes = file.read(buffer, sizeof(buffer))) > 0)
        digest.update(buffer, readBytes);

    return (readBytes == 0 ? digestImpl(digest, env, result, offset) : 0);
}

template <typename TMessageDigest>
__STATIC_INLINE__ jint computeStringImpl(JNIEnv* env, jstring str, jbyteArray result, jint offset)
{
    assert(env);
    assert(str);
    assert(result);
    assert(offset >= 0);

    TMessageDigest digest;
    JNI::_jstring_t<1024> jstr(env, str);
    digest.update((const u_char*)jstr.str(), jstr.length);

    return digestImpl(digest, env, result, offset);
}

template <typename TMessageDigest>
__STATIC_INLINE__ jint computeByteArrayImpl(JNIEnv* env, jbyteArray data, jint dataOffset, jint dataLength, jbyteArray result, jint offset)
{
    assert(env);
    assert(data);
    assert(offset >= 0);
    assert(dataOffset >= 0 && dataLength >= 0);
    assert(env->GetArrayLength(data) - dataOffset >= dataLength);

    int32_t readBytes;
    u_char buffer[8192];
    TMessageDigest digest;

    __NS::ByteArrayInputStream is(env, data, dataLength, dataOffset);
    while ((readBytes = is.read(buffer, _countof(buffer))) > 0)
        digest.update(buffer, readBytes);

    return digestImpl(digest, env, result, offset);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     MessageDigests
// Method:    computeFile
// Signature: (Ljava/lang/String;[BII)I

JNIEXPORT_METHOD(jint) computeFile(JNIEnv* env, jclass /*clazz*/, jstring filename, jbyteArray result, jint offset, jint algorithm)
{
    assert(env);
    assert(result);
    assert(filename);

    jint length = 0;
    __NS::File file;
    if (file.open(JNI::jstring_t(env, filename), O_RDONLY) == 0)
    {
        switch (algorithm)
        {
        case SHA1:
            length = computeFileImpl<__NS::SHA1>(file, env, result, offset);
            break;

        case SHA256:
            length = computeFileImpl<__NS::SHA256>(file, env, result, offset);
            break;

        case SHA384:
            length = computeFileImpl<__NS::SHA384>(file, env, result, offset);
            break;

        case SHA512:
            length = computeFileImpl<__NS::SHA512>(file, env, result, offset);
            break;

        default:
            length = computeFileImpl<__NS::MD5>(file, env, result, offset);
            break;
        }
    }

    return length;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     MessageDigests
// Method:    computeString
// Signature: (Ljava/lang/String;[BII)I

JNIEXPORT_METHOD(jint) computeString(JNIEnv* env, jclass /*clazz*/, jstring str, jbyteArray result, jint offset, jint algorithm)
{
    assert(env);
    assert(str);
    assert(result);

    jint length;
    switch (algorithm)
    {
    case SHA1:
        length = computeStringImpl<__NS::SHA1>(env, str, result, offset);
        break;

    case SHA256:
        length = computeStringImpl<__NS::SHA256>(env, str, result, offset);
        break;

    case SHA384:
        length = computeStringImpl<__NS::SHA384>(env, str, result, offset);
        break;

    case SHA512:
        length = computeStringImpl<__NS::SHA512>(env, str, result, offset);
        break;

    default:
        length = computeStringImpl<__NS::MD5>(env, str, result, offset);
        break;
    }

    return length;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     MessageDigests
// Method:    computeByteArray
// Signature: ([BII[BII)I

JNIEXPORT_METHOD(jint) computeByteArray(JNIEnv* env, jclass /*clazz*/, jbyteArray data, jint dataOffset, jint dataLength, jbyteArray result, jint offset, jint algorithm)
{
    assert(env);
    assert(data);
    assert(result);

    jint length;
    switch (algorithm)
    {
    case SHA1:
        length = computeByteArrayImpl<__NS::SHA1>(env, data, dataOffset, dataLength, result, offset);
        break;

    case SHA256:
        length = computeByteArrayImpl<__NS::SHA256>(env, data, dataOffset, dataLength, result, offset);
        break;

    case SHA384:
        length = computeByteArrayImpl<__NS::SHA384>(env, data, dataOffset, dataLength, result, offset);
        break;

    case SHA512:
        length = computeByteArrayImpl<__NS::SHA512>(env, data, dataOffset, dataLength, result, offset);
        break;

    default:
        length = computeByteArrayImpl<__NS::MD5>(env, data, dataOffset, dataLength, result, offset);
        break;
    }

    return length;
}

///////////////////////////////////////////////////////////////////////////////
// Register native methods functions
//

__STATIC_INLINE__ jint registerNativeMethods(JNIEnv* env)
{
    assert(env);

    const JNINativeMethod methods[] =
    {
        { "computeByteArray", "([BII[BII)I", (void*)computeByteArray },
        { "computeFile", "(Ljava/lang/String;[BII)I", (void*)computeFile },
        { "computeString", "(Ljava/lang/String;[BII)I", (void*)computeString },
    };

    LOGD("Register class " PACKAGE_UTILITIES "MessageDigests native methods.\n");
    return JNI::jclass_t(env, PACKAGE_UTILITIES "MessageDigests").registerNatives(methods, _countof(methods));
}

}  // namespace MessageDigests

#endif  // __MSGDIGTS_H__
