///////////////////////////////////////////////////////////////////////////////
// fileutils.h
//
// Copyright(c) 2018, Garfield. All rights reserved.
// Author  : Garfield
// Version : 4.0
// Creation Date : 2012/5/23

#ifndef __FILEUTILS_H__
#define __FILEUTILS_H__

#ifdef __NDK_STLP__
#include <list>
#include <string>
#endif

#include "fileutil.h"

///////////////////////////////////////////////////////////////////////////////
// JNI native methods in this file:
//
// mkdirs()
// scanFiles()
// listFiles()
// copyFile()
// moveFile()
// fileAccess()
// fileStatus()
// getFileMode()
// getFileCount()
// getFileLength()
// deleteFiles()
// createFile()
// createUniqueFile()

namespace FileUtils {

///////////////////////////////////////////////////////////////////////////////
// Enumerations
//

enum
{
    // The returned values of onScanFile.
    SC_CONTINUE = 0,
    SC_STOP  = 1,
    SC_BREAK = 2,
    SC_BREAK_PARENT = 3,

    // The flag use with mkdirs function.
    FLAG_IGNORE_FILENAME = 0x01,

    // Ignores the hidden file (start with ".").
    FLAG_IGNORE_HIDDEN_FILE = 0x01,

    // The flag use with scanFiles function.
    FLAG_SCAN_FOR_DESCENDENTS = 0x02,
};

///////////////////////////////////////////////////////////////////////////////
// Global functions
//

__STATIC_INLINE__ int scanFilter(const struct dirent* entry)
{
    return (entry->d_name[0] != '.');
}

__STATIC_INLINE__ jint createDirectory(const char* filename)
{
    assert(filename);

    stdutil::char_sequence path;
    __NS::splitPath(filename, path);

    return (path.empty() ? EINVAL : __NS::createDirectory(path.data, path.size));
}

__STATIC_INLINE__ void buildUniqueFileName(char* path, size_t length, const stdutil::char_sequence& dirPath, const char* name)
{
    assert(path);
    assert(name);
    assert(!dirPath.empty());

    ::snprintf(path, length, "%.*s/%s", dirPath.size, dirPath.data, name);
    if (::access(path, F_OK) == 0)
    {
        // Builds the unique filename.
        char format[MAX_PATH];
        if (const char* ext = ::strrchr(name, '.'))
            ::snprintf(format, _countof(format), "%.*s/%.*s(%%d)%s", dirPath.size, dirPath.data, (uint32_t)(ext - name), name, ext);
        else
            ::snprintf(format, _countof(format), "%.*s/%s(%%d)", dirPath.size, dirPath.data, name);

        int index = 0;
        do
        {
            ::snprintf(path, length, format, ++index);
        } while (::access(path, F_OK) == 0);
    }
}

#ifdef __NDK_STLP__
__STATIC_INLINE__ jint scanDescendentFiles(JNIEnv* env, const char* path, int (*filter)(const struct dirent*), jobject callback, jint& result)
{
    assert(env);
    assert(path);
    assert(filter);
    assert(callback);

    std::list<std::string> dirPaths;
    dirPaths.push_back(path);

    jint errnum = 0;
    do
    {
        const std::string& dirPath = dirPaths.front();
        __NS::Directory<> dir(filter);
        if ((errnum = dir.open(dirPath.c_str())) == 0)
        {
            char filePath[MAX_PATH];
            const int length = ::snprintf(filePath, _countof(filePath), "%s/", dirPath.c_str());

            for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
            {
                ::strlcpy(filePath + length, entry->d_name, _countof(filePath) - length);
                result = env->CallIntMethod(callback, _onScanFileID, JNI::jstringRef(env, filePath).str, (jint)entry->d_type);
                if (result == SC_STOP) {
                    dirPaths.clear();
                    break;
                } else if (result == SC_BREAK_PARENT) {
                    break;
                } else if (result == SC_BREAK) {
                    continue;
                } else if (entry->d_type == DT_DIR && ::access(filePath, F_OK) == 0) {
                    // If filePath is a directory adds it to dirPaths.
                    dirPaths.push_back(filePath);
                }
            }
        }

        // Removes the dirPath from the dirPaths.
        dirPaths.pop_front();
    } while (errnum == 0 && dirPaths.size() > 0);

    return errnum;
}
#else
static inline jint scanDescendentFiles(JNIEnv* env, const char* dirPath, int (*filter)(const struct dirent*), jobject callback, jint& result)
{
    assert(env);
    assert(filter);
    assert(dirPath);
    assert(callback);

    __NS::Directory<> dir(filter);
    jint errnum = dir.open(dirPath);
    if (errnum == 0)
    {
        char filePath[MAX_PATH];
        const int length = ::snprintf(filePath, _countof(filePath), "%s/", dirPath);

        for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
        {
            ::strlcpy(filePath + length, entry->d_name, _countof(filePath) - length);
            result = env->CallIntMethod(callback, _onScanFileID, JNI::jstringRef(env, filePath).str, (jint)entry->d_type);
            if (result == SC_BREAK) {
                continue;
            } else if (result == SC_STOP || result == SC_BREAK_PARENT) {
                break;
            }

            // Scans the sub directory.
            if (entry->d_type == DT_DIR && ::access(filePath, F_OK) == 0 && ((errnum = scanDescendentFiles(env, filePath, filter, callback, result)) != 0 || result == SC_STOP)) {
                break;
            }
        }
    }

    return errnum;
}
#endif  // __NDK_STLP__

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    mkdirs
// Signature: (Ljava/lang/String;I)I

JNIEXPORT_METHOD(jint) mkdirs(JNIEnv* env, jclass /*clazz*/, jstring path, jint flags)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, path) == 0, "path == null || path.length() == 0", EINVAL);

    const JNI::jstring_t jpath(env, path);
    return ((flags & FLAG_IGNORE_FILENAME) ? createDirectory(jpath): __NS::createDirectory(jpath, jpath.length));
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    scanFiles
// Signature: (Ljava/lang/String;L PACKAGE_UTILITIES FileUtils$ScanCallback;I)I

JNIEXPORT_METHOD(jint) scanFiles(JNIEnv* env, jclass /*clazz*/, jstring dirPath, jobject callback, jint flags)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, dirPath) == 0 || callback == NULL, "dirPath == null || dirPath.length() == 0 || callback == null", EINVAL);

    jint errnum;
    const JNI::jstring_t jdirPath(env, dirPath);
    if (flags & FLAG_SCAN_FOR_DESCENDENTS)
    {
        jint result;
        errnum = scanDescendentFiles(env, jdirPath, ((flags & FLAG_IGNORE_HIDDEN_FILE) ? scanFilter : __NS::defaultFilter), callback, result);
    }
    else
    {
        __NS::Directory<> dir((flags & FLAG_IGNORE_HIDDEN_FILE) ? scanFilter : __NS::defaultFilter);
        if ((errnum = dir.open(jdirPath)) == 0)
        {
            char path[MAX_PATH];
            const int length = ::snprintf(path, _countof(path), "%s/", jdirPath.str());

            for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
            {
                ::strlcpy(path + length, entry->d_name, _countof(path) - length);
                if (env->CallIntMethod(callback, _onScanFileID, JNI::jstringRef(env, path).str, (jint)entry->d_type) != SC_CONTINUE)
                    break;
            }
        }
    }

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    listFiles
// Signature: (Ljava/lang/String;IL PACKAGE_UTILITIES Pools$Factory;Ljava/util/List;)I

JNIEXPORT_METHOD(jint) listFiles(JNIEnv* env, jclass clazz, jstring dirPath, jint flags, jobject factory, jobject outDirents)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, dirPath) == 0 || factory == NULL || outDirents == NULL, "dirPath == null || dirPath.length() == 0 || factory == null || outDirents == null", EINVAL);

    const JNI::jstring_t jdirPath(env, dirPath);
    __NS::Directory<> dir((flags & FLAG_IGNORE_HIDDEN_FILE) ? scanFilter : __NS::defaultFilter);
    jint errnum = dir.open(jdirPath);
    if (errnum == 0)
    {
        char path[MAX_PATH];
        const int length = ::snprintf(path, _countof(path), "%s/", jdirPath.str());
        for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
        {
            ::strlcpy(path + length, entry->d_name, _countof(path) - length);
            env->CallStaticVoidMethod(clazz, _addDirentID, outDirents, JNI::jstringRef(env, path).str, (jint)entry->d_type, factory);
        }
    }

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    copyFile
// Signature: (Ljava/lang/String;Ljava/lang/String;)I

JNIEXPORT_METHOD(jint) copyFile(JNIEnv* env, jclass /*clazz*/, jstring src, jstring dst)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, src) == 0 || JNI::getLength(env, dst) == 0, "src == null || src.length() == 0 || dst == null || dst.length() == 0", EINVAL);

    // Create the destination file directory.
    const JNI::jstring_t jdst(env, dst);
    jint errnum = createDirectory(jdst);
    if (errnum == 0)
    {
        __NS::File srcFile, dstFile;
        const JNI::jstring_t jsrc(env, src);
        if ((errnum = srcFile.open(jsrc, O_RDONLY)) == 0 && (errnum = dstFile.open(jdst)) == 0)
        {
            // Copy the source file to destination file.
            char buffer[8192];
            for (ssize_t readBytes = 0; (readBytes = srcFile.read(buffer, sizeof(buffer))) != 0; )
            {
                if (readBytes == -1)
                {
                    errnum = __android_check_error(errno, "Couldn't read '%s' file", jsrc.str());
                    break;
                }

                if (dstFile.write(buffer, readBytes) == -1)
                {
                    errnum = __android_check_error(errno, "Couldn't write '%s' file", jdst.str());
                    break;
                }
            }

            // Checks the destination file exists.
            if (errnum == 0)
                errnum = __NS::fileAccess(jdst, F_OK);
            else
                ::remove(jdst);     // Deletes file, if copy failure.
        }
    }

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    moveFile
// Signature: (Ljava/lang/String;Ljava/lang/String;)I

JNIEXPORT_METHOD(jint) moveFile(JNIEnv* env, jclass /*clazz*/, jstring src, jstring dst)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, src) == 0 || JNI::getLength(env, dst) == 0, "src == null || src.length() == 0 || dst == null || dst.length() == 0", EINVAL);

    // Create the destination file directory.
    const JNI::jstring_t jdst(env, dst);
    jint errnum = createDirectory(jdst);
    if (errnum == 0)
    {
        const JNI::jstring_t jsrc(env, src);
        errnum = __android_check_error((::rename(jsrc, jdst) == 0 ? 0 : errno), "Couldn't move '%s' to '%s'", jsrc.str(), jdst.str());
    }

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    access
// Signature: (Ljava/lang/String;I)I

JNIEXPORT_METHOD(jint) fileAccess(JNIEnv* env, jclass /*clazz*/, jstring path, jint mode)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, path) == 0, "path == null || path.length() == 0", EINVAL);

    return __NS::fileAccess(JNI::jstring_t(env, path), mode);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    stat
// Signature: (Ljava/lang/String;L PACKAGE_UTILITIES FileUtils$Stat;)I

JNIEXPORT_METHOD(jint) fileStatus(JNIEnv* env, jclass clazz, jstring path, jobject outStat)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, path) == 0 || outStat == NULL, "path == null || path.length() == 0 || outStat == null", EINVAL);

    struct stat buf;
    const jint errnum = __NS::fileStatus(JNI::jstring_t(env, path), buf);
    if (errnum == 0)
        env->CallStaticVoidMethod(clazz, _setStatID, outStat, (jint)buf.st_mode, (jint)buf.st_uid, (jint)buf.st_gid, (jlong)buf.st_size, (jlong)buf.st_blocks, (jlong)buf.st_blksize, (jlong)buf.st_mtime * MILLISECONDS);

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    getFileMode
// Signature: (Ljava/lang/String;)I

JNIEXPORT_METHOD(jint) getFileMode(JNIEnv* env, jclass /*clazz*/, jstring path)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, path) == 0, "path == null || path.length() == 0", 0);

    struct stat buf;
    return (__NS::fileStatus(JNI::jstring_t(env, path), buf) == 0 ? buf.st_mode : 0);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    getFileCount
// Signature: (Ljava/lang/String;I)I

JNIEXPORT_METHOD(jint) getFileCount(JNIEnv* env, jclass /*clazz*/, jstring dirPath, jint flags)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, dirPath) == 0, "dirPath == null || dirPath.length() == 0", 0);

    jint size = 0;
    __NS::Directory<> dir((flags & FLAG_IGNORE_HIDDEN_FILE) ? scanFilter : __NS::defaultFilter);
    if (dir.open(JNI::jstring_t(env, dirPath)) == 0)
    {
        for (struct dirent* entry; dir.read(entry) == 0 && entry != NULL; )
            ++size;
    }

    return size;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    getFileLength
// Signature: (Ljava/lang/String;)J

JNIEXPORT_METHOD(jlong) getFileLength(JNIEnv* env, jclass /*clazz*/, jstring filename)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, filename) == 0, "filename == null || filename.length() == 0", 0);

    struct stat buf;
    return (__NS::fileStatus(JNI::jstring_t(env, filename), buf) == 0 ? buf.st_size : 0);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    deleteFiles
// Signature: (Ljava/lang/String;Z)I

JNIEXPORT_METHOD(jint) deleteFiles(JNIEnv* env, jclass /*clazz*/, jstring path, jboolean deleteSelf)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, path) == 0, "path == null || path.length() == 0", EINVAL);

    struct stat buf;
    const JNI::jstring_t jpath(env, path);
    return (::lstat(jpath, &buf) == 0 ? (S_ISDIR(buf.st_mode) ? __NS::deleteFiles(jpath, deleteSelf) : __NS::deleteFile(jpath)) : errno);
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    createFile
// Signature: (Ljava/lang/String;J)I

JNIEXPORT_METHOD(jint) createFile(JNIEnv* env, jclass /*clazz*/, jstring filename, jlong length)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, filename) == 0, "filename == null || filename.length() == 0", EINVAL);

    const JNI::jstring_t jfilename(env, filename);
    jint errnum = createDirectory(jfilename);
    if (errnum == 0)
    {
        __NS::File file;
        if ((errnum = file.open(jfilename)) == 0)
        {
            if (length > 0 && (errnum = file.truncate(length)) != 0)
                ::remove(jfilename);    // Deletes file, if truncate failure.
        }
    }

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    createUniqueFile
// Signature: (Ljava/lang/String;J)Ljava/lang/String;

JNIEXPORT_METHOD(jstring) createUniqueFile(JNIEnv* env, jclass /*clazz*/, jstring filename, jlong length)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, filename) == 0, "filename == null || filename.length() == 0", NULL);

    jstring result = NULL;
    stdutil::char_sequence dirPath;
    const JNI::jstring_t jfilename(env, filename);

    // Create the file directory.
    const char* name = __NS::splitPath(jfilename, dirPath);
    if (!dirPath.empty() && __NS::createDirectory(dirPath.data, dirPath.size) == 0)
    {
        // Builds the unique filename.
        char path[MAX_PATH];
        buildUniqueFileName(path, _countof(path), dirPath, name);

        // Create the file.
        __NS::File file;
        if (file.open(path) == 0)
        {
            if (length <= 0 || file.truncate(length) == 0)
                result = env->NewStringUTF(path);
            else
                ::remove(path);     // Deletes file, if truncate failure.
        }
    }

    return result;
}

///////////////////////////////////////////////////////////////////////////////
// Register native methods functions
//

__STATIC_INLINE__ jint registerNativeMethods(JNIEnv* env)
{
    assert(env);
    LOGD("Register class " PACKAGE_UTILITIES "FileUtils native methods.\n");

    // Initializes class ScanCallback method IDs.
    _onScanFileID = JNI::jclass_t(env, PACKAGE_UTILITIES "FileUtils$ScanCallback").getMethodID("onScanFile", "(Ljava/lang/String;I)I");
    assert(_onScanFileID != NULL);

    // Initializes class FileUtils method IDs.
    const JNI::jclass_t clazz(env, PACKAGE_UTILITIES "FileUtils");
    _setStatID   = clazz.getStaticMethodID("setStat", "(L" PACKAGE_UTILITIES "FileUtils$Stat;IIIJJJJ)V");
    _addDirentID = clazz.getStaticMethodID("addDirent", "(Ljava/util/List;Ljava/lang/String;IL" PACKAGE_UTILITIES "Pools$Factory;)V");
    assert(_setStatID != NULL && _addDirentID != NULL);

    // Registers class FileUtils native methods.
    const JNINativeMethod methods[] =
    {
        { "mkdirs", "(Ljava/lang/String;I)I", (void*)mkdirs },
        { "access", "(Ljava/lang/String;I)I", (void*)fileAccess },
        { "createFile", "(Ljava/lang/String;J)I", (void*)createFile },
        { "getFileMode", "(Ljava/lang/String;)I", (void*)getFileMode },
        { "deleteFiles", "(Ljava/lang/String;Z)I", (void*)deleteFiles },
        { "getFileCount", "(Ljava/lang/String;I)I", (void*)getFileCount },
        { "getFileLength", "(Ljava/lang/String;)J", (void*)getFileLength },
        { "copyFile", "(Ljava/lang/String;Ljava/lang/String;)I", (void*)copyFile },
        { "moveFile", "(Ljava/lang/String;Ljava/lang/String;)I", (void*)moveFile },
        { "createUniqueFile", "(Ljava/lang/String;J)Ljava/lang/String;", (void*)createUniqueFile },
        { "stat", "(Ljava/lang/String;L" PACKAGE_UTILITIES "FileUtils$Stat;)I", (void*)fileStatus },
        { "scanFiles", "(Ljava/lang/String;L" PACKAGE_UTILITIES "FileUtils$ScanCallback;I)I", (void*)scanFiles },
        { "listFiles", "(Ljava/lang/String;IL" PACKAGE_UTILITIES "Pools$Factory;Ljava/util/List;)I", (void*)listFiles },
    };

    return clazz.registerNatives(methods, _countof(methods));
}

}  // namespace FileUtils

#endif  // __FILEUTILS_H__
