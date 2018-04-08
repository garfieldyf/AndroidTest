///////////////////////////////////////////////////////////////////////////////
// errutils.h
//
// Copyright(c) 2018, Garfield. All rights reserved.
// Author  : Garfield
// Version : 1.0
// Creation Date : 2012/5/23

#ifndef __ERRUTILS_H__
#define __ERRUTILS_H__

#include "jniutil.h"

///////////////////////////////////////////////////////////////////////////////
// JNI native methods in this file:
//
// toString()

namespace ErrnoException {

///////////////////////////////////////////////////////////////////////////////
// Class:     ErrnoException
// Method:    toString
// Signature: (I)Ljava/lang/String;

JNIEXPORT_METHOD(jstring) toString(JNIEnv* env, jclass /*clazz*/, jint errnum)
{
    assert(env);

    char error[MAX_PATH];
    ::strerror_r(errnum, error, _countof(error));

    return env->NewStringUTF(error);
}

///////////////////////////////////////////////////////////////////////////////
// Register native methods functions
//

__STATIC_INLINE__ jint registerNativeMethods(JNIEnv* env)
{
    assert(env);
    LOGD("Register class " PACKAGE_UTILITIES "ErrnoException native methods.\n");

    const JNINativeMethod methods[] = { { "toString", "(I)Ljava/lang/String;", (void*)toString } };
    return JNI::jclass_t(env, PACKAGE_UTILITIES "ErrnoException").registerNatives(methods, _countof(methods));
}

}  // namespace ErrnoException

#endif  // __ERRUTILS_H__
