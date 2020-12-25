package com.qty.log.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FileUtils {

    /**
     * TAG
     */
    private static final String TAG = FileUtils.class.getSimpleName();
    /**
     * 用于存放日志文件的目录名称
     */
    private static final String LOG_DIR_NAME = "logs";

    /**
     * 获取日志文件存储目录
     * @param context Context 对象
     * @return 返回日志文件存储的目录路径，如果获取失败，则返回 null
     */
    public static String getLogFileDirectory(Context context) {
        File file = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        file = new File(file.getAbsolutePath() + File.separator + LOG_DIR_NAME);
        if (!file.exists() || !file.isDirectory()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                Log.e(TAG, "getLogFileDirectory=>error: ", e);
                return null;
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取文件夹空间大小
     * @param file 文件夹路径
     */
    public static long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSizeOfFilesInDir(child);
        return total;
    }
}
