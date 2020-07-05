///////////////////////////////////////////////////////////////////////////////
// fileutils.h
//
// Author : Garfield
// Creation Date : 2012/5/23

#ifndef __FILEUTILS_H__
#define __FILEUTILS_H__

#ifdef __NDK_STLP__
#include <list>
#include <string>
#include <utility>
#endif

#include "fileutil.h"

///////////////////////////////////////////////////////////////////////////////
// JNI native methods in this file:
//
// mkdirs()
// moveFile()
// listFiles()
// scanFiles()
// createFile()
// deleteFiles()
// compareFile()
// getFileStatus()
// computeFileSizes()
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

    // The buffer size for compareFile function.
    _BUF_SIZE = 4096,

    // The flag use for mkdirs function.
    FLAG_IGNORE_FILENAME = 0x01,

    // Ignores the hidden file (start with ".").
    FLAG_IGNORE_HIDDEN_FILE = 0x01,

    // The flags use for scanFiles function.
    FLAG_SCAN_FOR_DESCENDENTS = 0x02,
};

///////////////////////////////////////////////////////////////////////////////
// Global variables
//

// Class FileUtils method IDs.
static jmethodID _setStatID;
static jmethodID _addDirentID;

// Class ScanCallback method IDs.
static jmethodID _onScanFileID;

///////////////////////////////////////////////////////////////////////////////
// Global functions
//

__STATIC_INLINE__ jint createDirectory(const char* filename)
{
    assert(filename);

    stdutil::char_sequence dirPath;
    __NS::splitPath(filename, dirPath);

    return (dirPath.empty() ? EINVAL : __NS::createDirectory(dirPath.data, dirPath.size));
}

__STATIC_INLINE__ int buildPath(char (&outPath)[MAX_PATH], const char* path, size_t length)
{
    assert(path);
    return ::snprintf(outPath, _countof(outPath), (path[length - 1] == '/' ? "%s" : "%s/"), path);
}

__STATIC_INLINE__ jboolean compareLength(const char* file1, const char* file2)
{
    assert(file1);
    assert(file2);

    struct stat buf;
    jboolean result = JNI_FALSE;
    if (::stat(file1, &buf) == 0)
    {
        const off_t length = buf.st_size;
        result = (::stat(file2, &buf) == 0 && length == buf.st_size);
    }

    return result;
}

__STATIC_INLINE__ ssize_t readFile(const __NS::File& file, uint8_t (&buf)[_BUF_SIZE])
{
    assert(!file.isEmpty());

    ssize_t count = 0, readBytes;
    while ((readBytes = file.read(buf + count, _countof(buf) - count)) != 0)
    {
        if (readBytes == -1)
        {
            count = -1;
            break;
        }

        count += readBytes;
        if (count == _countof(buf))
            break;
    }

    return count;
}

template <typename _Filter>
__STATIC_INLINE__ jint listFilesImpl(JNIEnv* env, jclass clazz, const char* path, jobject outDirents)
{
    assert(env);
    assert(path);
    assert(outDirents);

    __NS::Directory<_Filter> dir;
    jint errnum = dir.open(path);
    if (errnum == 0)
    {
        for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
            env->CallStaticVoidMethod(clazz, _addDirentID, outDirents, JNI::jstringRef_t(env, entry->d_name).get(), (jint)entry->d_type);
    }

    return errnum;
}

template <typename _Filter>
__STATIC_INLINE__ jint scanFilesImpl(JNIEnv* env, const char* path, size_t length, jobject callback, jobject cookie)
{
    assert(env);
    assert(path);
    assert(callback);

    __NS::Directory<_Filter> dir;
    jint errnum = dir.open(path);
    if (errnum == 0)
    {
        char filePath[MAX_PATH];
        const int offset = buildPath(filePath, path, length);

        for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
        {
            ::strlcpy(filePath + offset, entry->d_name, _countof(filePath) - offset);
            if (env->CallIntMethod(callback, _onScanFileID, JNI::jstringRef_t(env, filePath).get(), (jint)entry->d_type, cookie) != SC_CONTINUE)
                break;
        }
    }

    return errnum;
}

__STATIC_INLINE__ void buildUniqueFileName(char (&path)[MAX_PATH], const stdutil::char_sequence& dirPath, const char* name)
{
    assert(name);
    assert(!dirPath.empty());

    ::snprintf(path, _countof(path), "%.*s/%s", dirPath.size, dirPath.data, name);
    if (::access(path, F_OK) == 0)
    {
        // Builds the unique filename.
        char format[MAX_PATH];
        if (const char* ext = ::strrchr(name, '.'))
            ::snprintf(format, _countof(format), "%.*s/%.*s-%%d%s", dirPath.size, dirPath.data, (uint32_t)(ext - name), name, ext);
        else
            ::snprintf(format, _countof(format), "%.*s/%s-%%d", dirPath.size, dirPath.data, name);

        int index = 0;
        do
        {
            ::snprintf(path, _countof(path), format, ++index);
        } while (::access(path, F_OK) == 0);
    }
}

#ifdef __NDK_STLP__
__STATIC_INLINE__ jlong computeFileBytes(const char* path, size_t /*length*/)
{
    assert(path);

    std::list<std::string> dirPaths;
    dirPaths.push_back(path);

    jlong result = 0;
    do
    {
        // Retrieves the dirPath from the dirPaths front.
        std::string dirPath(std::move(dirPaths.front()));
        dirPaths.pop_front();

        __NS::Directory<> dir;
        if (dir.open(dirPath.c_str()) == 0)
        {
            struct stat buf;
            char filePath[MAX_PATH];
            const int offset = buildPath(filePath, dirPath.c_str(), dirPath.size());

            for (struct dirent* entry; dir.read(entry) == 0 && entry != NULL; )
            {
                ::strlcpy(filePath + offset, entry->d_name, _countof(filePath) - offset);
                if (entry->d_type == DT_DIR) {
                    // If filePath is a directory adds it to dirPaths.
                    dirPaths.push_back(filePath);
                } else if (::stat(filePath, &buf) == 0) {
                    result += (jlong)buf.st_size;
                }
            }
        }
    } while (!dirPaths.empty());

    return result;
}

template <typename _Filter>
__STATIC_INLINE__ jint scanDescendentFiles(JNIEnv* env, const char* path, size_t /*length*/, jobject callback, jobject cookie, jint& result)
{
    assert(env);
    assert(path);
    assert(callback);

    std::list<std::string> dirPaths;
    dirPaths.push_back(path);

    jint errnum = 0;
    do
    {
        // Retrieves the dirPath from the dirPaths front.
        std::string dirPath(std::move(dirPaths.front()));
        dirPaths.pop_front();

        __NS::Directory<_Filter> dir;
        if ((errnum = dir.open(dirPath.c_str())) == 0)
        {
            char filePath[MAX_PATH];
            const int offset = buildPath(filePath, dirPath.c_str(), dirPath.size());

            for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
            {
                ::strlcpy(filePath + offset, entry->d_name, _countof(filePath) - offset);
                result = env->CallIntMethod(callback, _onScanFileID, JNI::jstringRef_t(env, filePath).get(), (jint)entry->d_type, cookie);
                if (result == SC_STOP) {
                    dirPaths.clear();
                    break;
                } else if (result == SC_BREAK_PARENT) {
                    break;
                } else if (result == SC_BREAK) {
                    continue;
                } else if (entry->d_type == DT_DIR) {
                    // If filePath is a directory adds it to dirPaths.
                    dirPaths.push_back(filePath);
                }
            }
        }
    } while (errnum == 0 && !dirPaths.empty());

    return errnum;
}
#else
static inline jlong computeFileBytes(const char* dirPath, size_t length)
{
    assert(dirPath);

    jlong result = 0;
    __NS::Directory<> dir;
    if (dir.open(dirPath) == 0)
    {
        struct stat buf;
        char filePath[MAX_PATH];
        const int offset = buildPath(filePath, dirPath, length);

        for (struct dirent* entry; dir.read(entry) == 0 && entry != NULL; )
        {
            const size_t size = ::strlcpy(filePath + offset, entry->d_name, _countof(filePath) - offset);
            if (entry->d_type == DT_DIR) {
                assert(::strlen(filePath) == (size + offset));
                result += computeFileBytes(filePath, size + offset);
            } else if (::stat(filePath, &buf) == 0) {
                result += (jlong)buf.st_size;
            }
        }
    }

    return result;
}

template <typename _Filter>
static inline jint scanDescendentFiles(JNIEnv* env, const char* dirPath, size_t length, jobject callback, jobject cookie, jint& result)
{
    assert(env);
    assert(dirPath);
    assert(callback);

    __NS::Directory<_Filter> dir;
    jint errnum = dir.open(dirPath);
    if (errnum == 0)
    {
        char filePath[MAX_PATH];
        const int offset = buildPath(filePath, dirPath, length);

        for (struct dirent* entry; (errnum = dir.read(entry)) == 0 && entry != NULL; )
        {
            const size_t size = ::strlcpy(filePath + offset, entry->d_name, _countof(filePath) - offset);
            result = env->CallIntMethod(callback, _onScanFileID, JNI::jstringRef_t(env, filePath).get(), (jint)entry->d_type, cookie);
            if (result == SC_BREAK) {
                continue;
            } else if (result == SC_STOP || result == SC_BREAK_PARENT) {
                break;
            } else if (entry->d_type == DT_DIR) {
                // Scans the sub directory.
                assert(::strlen(filePath) == (size + offset));
                errnum = scanDescendentFiles<_Filter>(env, filePath, size + offset, callback, cookie, result);
                if (errnum != 0 || result == SC_STOP)
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
        errnum = __verify((::rename(jsrc, jdst) == 0 ? 0 : errno), "Couldn't move '%s' to '%s'", jsrc.str(), jdst.str());
    }

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    listFiles
// Signature: (Ljava/lang/String;ILjava/util/Collection;)I

JNIEXPORT_METHOD(jint) listFiles(JNIEnv* env, jclass clazz, jstring dirPath, jint flags, jobject outDirents)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, dirPath) == 0 || outDirents == NULL, "dirPath == null || dirPath.length() == 0 || outDirents == null", EINVAL);

    const JNI::jstring_t jdirPath(env, dirPath);
    return ((flags & FLAG_IGNORE_HIDDEN_FILE) ? listFilesImpl<__NS::IgnoreHiddenFilter>(env, clazz, jdirPath, outDirents) : listFilesImpl<__NS::DefaultFilter>(env, clazz, jdirPath, outDirents));
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    scanFiles
// Signature: (Ljava/lang/String;L PACKAGE_UTILITIES FileUtils$ScanCallback;ILjava/lang/Object;)I

JNIEXPORT_METHOD(jint) scanFiles(JNIEnv* env, jclass /*clazz*/, jstring dirPath, jobject callback, jint flags, jobject cookie)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, dirPath) == 0 || callback == NULL, "dirPath == null || dirPath.length() == 0 || callback == null", EINVAL);

    jint result;
    const JNI::jstring_t jdirPath(env, dirPath);
    return ((flags & FLAG_SCAN_FOR_DESCENDENTS)
            ? ((flags & FLAG_IGNORE_HIDDEN_FILE) ? scanDescendentFiles<__NS::IgnoreHiddenFilter>(env, jdirPath, jdirPath.length, callback, cookie, result) : scanDescendentFiles<__NS::DefaultFilter>(env, jdirPath, jdirPath.length, callback, cookie, result))
            : ((flags & FLAG_IGNORE_HIDDEN_FILE) ? scanFilesImpl<__NS::IgnoreHiddenFilter>(env, jdirPath, jdirPath.length, callback, cookie) : scanFilesImpl<__NS::DefaultFilter>(env, jdirPath, jdirPath.length, callback, cookie)));
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
// Method:    compareFile
// Signature: (Ljava/lang/String;Ljava/lang/String;)Z

JNIEXPORT_METHOD(jboolean) compareFile(JNIEnv* env, jclass /*clazz*/, jstring file1, jstring file2)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, file1) == 0 || JNI::getLength(env, file2) == 0, "file1 == null || file1.length() == 0 || file2 == null || file2.length() == 0", JNI_FALSE);

    const JNI::jstring_t jfile1(env, file1);
    const JNI::jstring_t jfile2(env, file2);
    jboolean result = compareLength(jfile1, jfile2);
    LOGD("compare length = %s\n", (result ? "true" : "false"));
    if (result)
    {
        __NS::File f1, f2;
        result = (f1.open(jfile1, O_RDONLY) == 0 && f2.open(jfile2, O_RDONLY) == 0);
        if (result)
        {
            ssize_t count1, count2;
            uint8_t buffer1[_BUF_SIZE], buffer2[_BUF_SIZE];

            while ((count1 = readFile(f1, buffer1)) != 0 && (count2 = readFile(f2, buffer2)) != 0)
            {
                if (count1 == -1 || count2 == -1 || count1 != count2 || ::memcmp(buffer1, buffer2, count1) != 0)
                {
                    LOGD("compare contents = false (count1 = %zd, count2 = %zd)\n", count1, count2);
                    result = JNI_FALSE;
                    break;
                }
            }
        }
    }

    return result;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    stat
// Signature: (Ljava/lang/String;L PACKAGE_UTILITIES FileUtils$Stat;)I

JNIEXPORT_METHOD(jint) getFileStatus(JNIEnv* env, jclass clazz, jstring path, jobject outStat)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, path) == 0 || outStat == NULL, "path == null || path.length() == 0 || outStat == null", EINVAL);

    struct stat buf;
    const JNI::jstring_t jpath(env, path);

    const jint errnum = __verify((::stat(jpath, &buf) == 0 ? 0 : errno), "Couldn't get file '%s' status", jpath.str());
    if (errnum == 0)
        env->CallStaticVoidMethod(clazz, _setStatID, outStat, (jint)buf.st_mode, (jint)buf.st_uid, (jint)buf.st_gid, (jlong)buf.st_size, (jlong)buf.st_blocks, (jlong)buf.st_blksize, (jlong)buf.st_mtime * MILLISECONDS);

    return errnum;
}

///////////////////////////////////////////////////////////////////////////////
// Class:     FileUtils
// Method:    computeFileSizes
// Signature: (Ljava/lang/String;)J

JNIEXPORT_METHOD(jlong) computeFileSizes(JNIEnv* env, jclass /*clazz*/, jstring file)
{
    assert(env);
    AssertThrowErrnoException(env, JNI::getLength(env, file) == 0, "file == null || file.length() == 0", 0);

    struct stat buf;
    const JNI::jstring_t jfile(env, file);
    return (::stat(jfile, &buf) == 0 ? ((S_ISDIR(buf.st_mode) ? computeFileBytes(jfile, jfile.length) : (jlong)buf.st_size)) : 0);
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
        buildUniqueFileName(path, dirPath, name);

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
    _onScanFileID = JNI::jclass_t(env, PACKAGE_UTILITIES "FileUtils$ScanCallback").getMethodID("onScanFile", "(Ljava/lang/String;ILjava/lang/Object;)I");
    assert(_onScanFileID != NULL);

    // Initializes class FileUtils method IDs.
    const JNI::jclass_t clazz(env, PACKAGE_UTILITIES "FileUtils");
    _setStatID   = clazz.getStaticMethodID("setStat", "(L" PACKAGE_UTILITIES "FileUtils$Stat;IIIJJJJ)V");
    _addDirentID = clazz.getStaticMethodID("addDirent", "(Ljava/util/Collection;Ljava/lang/String;I)V");
    assert(_setStatID != NULL && _addDirentID != NULL);

    // Registers class FileUtils native methods.
    const JNINativeMethod methods[] =
    {
        { "mkdirs", "(Ljava/lang/String;I)I", (void*)mkdirs },
        { "createFile", "(Ljava/lang/String;J)I", (void*)createFile },
        { "deleteFiles", "(Ljava/lang/String;Z)I", (void*)deleteFiles },
        { "computeFileSizes", "(Ljava/lang/String;)J", (void*)computeFileSizes },
        { "moveFile", "(Ljava/lang/String;Ljava/lang/String;)I", (void*)moveFile },
        { "compareFile", "(Ljava/lang/String;Ljava/lang/String;)Z", (void*)compareFile },
        { "listFiles", "(Ljava/lang/String;ILjava/util/Collection;)I", (void*)listFiles },
        { "createUniqueFile", "(Ljava/lang/String;J)Ljava/lang/String;", (void*)createUniqueFile },
        { "stat", "(Ljava/lang/String;L" PACKAGE_UTILITIES "FileUtils$Stat;)I", (void*)getFileStatus },
        { "scanFiles", "(Ljava/lang/String;L" PACKAGE_UTILITIES "FileUtils$ScanCallback;ILjava/lang/Object;)I", (void*)scanFiles },
    };

    return clazz.registerNatives(methods, _countof(methods));
}

}  // namespace FileUtils

#endif  // __FILEUTILS_H__
