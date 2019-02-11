///////////////////////////////////////////////////////////////////////////////
// procutils.h
//
// Copyright(c) 2019, Garfield. All rights reserved.
// Author  : Garfield
// Version : 2.0
// Creation Date : 2012/4/16

#ifndef __PROCUTILS_H__
#define __PROCUTILS_H__

#include <pwd.h>
#include <grp.h>
#include "jniutil.h"

///////////////////////////////////////////////////////////////////////////////
// JNI native methods in this file:
//
// myGid()
// myUserName()
// myGroupName()
// getUserName()
// getGroupName()

namespace ProcessUtils {

///////////////////////////////////////////////////////////////////////////////
// Global functions
//

__STATIC_INLINE__ jstring getUserNameImpl(JNIEnv* env, jint uid)
{
    assert(env);

    struct passwd* pwd = ::getpwuid(uid);
    __check_error2(pwd == NULL, "Couldn't get user name - uid = %d", uid);
    return (pwd != NULL ? env->NewStringUTF(pwd->pw_name) : NULL);
}

__STATIC_INLINE__ jstring getGroupNameImpl(JNIEnv* env, jint gid)
{
    assert(env);

    struct group* grp = ::getgrgid(gid);
    __check_error2(grp == NULL, "Couldn't get group name - gid = %d", gid);
    return (grp != NULL ? env->NewStringUTF(grp->gr_name) : NULL);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     ProcessUtils
// Method:    myGid
// Signature: ()I

JNIEXPORT_METHOD(jint) myGid(JNIEnv* /*env*/, jclass /*clazz*/)
{
    return ::getgid();
}

///////////////////////////////////////////////////////////////////////////////
// Class:     ProcessUtils
// Method:    myUserName
// Signature: ()Ljava/lang/String;

JNIEXPORT_METHOD(jstring) myUserName(JNIEnv* env, jclass /*clazz*/)
{
    assert(env);
    return getUserNameImpl(env, ::getuid());
}

///////////////////////////////////////////////////////////////////////////////
// Class:     ProcessUtils
// Method:    myGroupName
// Signature: ()Ljava/lang/String;

JNIEXPORT_METHOD(jstring) myGroupName(JNIEnv* env, jclass /*clazz*/)
{
    assert(env);
    return getGroupNameImpl(env, ::getgid());
}

///////////////////////////////////////////////////////////////////////////////
// Class:     ProcessUtils
// Method:    getUserName
// Signature: (I)Ljava/lang/String;

JNIEXPORT_METHOD(jstring) getUserName(JNIEnv* env, jclass /*clazz*/, jint uid)
{
    assert(env);
    return getUserNameImpl(env, uid);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     ProcessUtils
// Method:    getGroupName
// Signature: (I)Ljava/lang/String;

JNIEXPORT_METHOD(jstring) getGroupName(JNIEnv* env, jclass /*clazz*/, jint gid)
{
    assert(env);
    return getGroupNameImpl(env, gid);
}

///////////////////////////////////////////////////////////////////////////////
// Register native methods functions
//

__STATIC_INLINE__ jint registerNativeMethods(JNIEnv* env)
{
    assert(env);

    const JNINativeMethod methods[] =
    {
        { "myGid", "()I", (void*)myGid },
        { "myUserName", "()Ljava/lang/String;", (void*)myUserName },
        { "myGroupName", "()Ljava/lang/String;", (void*)myGroupName },
        { "getUserName", "(I)Ljava/lang/String;", (void*)getUserName },
        { "getGroupName", "(I)Ljava/lang/String;", (void*)getGroupName },
    };

    LOGD("Register class " PACKAGE_UTILITIES "ProcessUtils native methods.\n");
    return JNI::jclass_t(env, PACKAGE_UTILITIES "ProcessUtils").registerNatives(methods, _countof(methods));
}

}  // namespace ProcessUtils

#endif  // __PROCUTILS_H__
