// IFile.aidl
package com.tencent.test;

import android.ext.util.FileUtils;

interface IFile {
    int stat(in String path, out FileUtils.Stat outStat);
    FileUtils.Dirent getEntry(in String path);
}
