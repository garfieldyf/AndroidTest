package com.tencent.temp;

import android.content.Context;
import android.ext.cache.SimpleFileCache;
import android.ext.util.FileUtils;
import java.io.File;

public class SimpleFileCache2 extends SimpleFileCache {
    public SimpleFileCache2(File cacheDir, int maxSize) {
        super(cacheDir, maxSize);
    }

    public SimpleFileCache2(Context context, String name, int maxSize) {
        super(context, name, maxSize);
    }

    @Override
    protected int sizeOf(String cacheFile) {
        return (int)FileUtils.getFileLength(cacheFile);
    }
}
