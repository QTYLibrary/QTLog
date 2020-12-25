package com.qty.log;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.qty.log.bean.QTLogLevel;
import com.qty.log.crash.QTCrashHandler;
import com.qty.log.handler.QTLogConfig;
import com.qty.log.handler.QTLogFileManager;

import org.w3c.dom.Text;

/**
 * 日志管理类
 *
 * 用于初始化日志相关信息
 */
public class QTLogManager {

    /**
     * TAG
     */
    private static final String TAG = QTLogManager.class.getSimpleName();
    /**
     * QTLogManager 实例
     */
    private static final QTLogManager INSTANCE = QTLogManagerInstance.sInstance;
    /**
     * Context 对象
     */
    private Context mContext;
    /**
     * 是否已经初始化
     */
    private boolean isInited;

    /**
     * 单例方法
     */
    public static QTLogManager getInstance() {
        return INSTANCE;
    }

    /**
     * 内部构造方法
     */
    private QTLogManager() {}

    /**
     * 初始化方法
     * @param context Context 对象
     * @param tag   日志 TAG
     * @param level 日志 Level
     */
    public void init(Context context, String tag, QTLogLevel level) {
        this.init(context, tag, level, QTLogConfig.DEFAULT_TIME_FORMAT, QTLogConfig.DEFAULT_TERMINAL_LOG_FORMAT,
                QTLogConfig.DEFAULT_FILE_LOG_FORMAT, QTLogFileManager.DEFAULT_MAX_SAVE_DAYS, -1,
                true, true, true);
    }

    /**
     * 初始化方法
     * @param context Context 对象
     * @param tag   日志 TAG
     * @param level 日志级别
     * @param maxSaveDays   日志文件最大存储天数
     * @param catchCrash    是否捕获崩溃异常
     * @param writeToFile   是否将日志写入文件
     * @param enableConfig  是否允许使用 log.properties 配置文件
     */
    public void init(Context context, String tag, QTLogLevel level, int maxSaveDays,
                     boolean catchCrash, boolean writeToFile, boolean enableConfig) {
        this.init(context, tag, level, QTLogConfig.DEFAULT_TIME_FORMAT, QTLogConfig.DEFAULT_TERMINAL_LOG_FORMAT,
                QTLogConfig.DEFAULT_FILE_LOG_FORMAT,maxSaveDays, -1,
                catchCrash, writeToFile, enableConfig);
    }

    /**
     * 初始化方法
     * @param context Context 对象
     * @param tag   日志 TAG
     * @param level 日志级别
     * @param maxSaveSize   日志文件最大存储空间大小
     * @param catchCrash    是否捕获崩溃异常
     * @param writeToFile   是否将日志写入文件
     * @param enableConfig  是否允许使用 log.properties 配置文件
     */
    public void init(Context context, String tag, QTLogLevel level, long maxSaveSize,
                     boolean catchCrash, boolean writeToFile, boolean enableConfig) {
        this.init(context, tag, level, QTLogConfig.DEFAULT_TIME_FORMAT, QTLogConfig.DEFAULT_TERMINAL_LOG_FORMAT,
                QTLogConfig.DEFAULT_FILE_LOG_FORMAT,-1, maxSaveSize,
                catchCrash, writeToFile, enableConfig);
    }

    /**
     * 初始化方法
     * @param context Context 对象
     * @param tag   日志 TAG
     * @param level 日志级别
     * @param timeFormat 日志日期格式
     * @param termLogFormat 终端日志格式
     * @param fileLogFormat 文件日志格式
     * @param maxSaveDays   日志文件最大存储天数
     * @param maxSaveSize   日志文件最大存储空间大小
     * @param catchCrash    是否捕获崩溃异常
     * @param writeToFile   是否将日志写入文件
     * @param enableConfig  是否允许使用 log.properties 配置文件
     */
    public void init(Context context, String tag, QTLogLevel level, String timeFormat, String termLogFormat,
                     String fileLogFormat, int maxSaveDays, long maxSaveSize, boolean catchCrash,
                     boolean writeToFile, boolean enableConfig) {
        if (TextUtils.isEmpty(tag) || level == null || TextUtils.isEmpty(timeFormat) || TextUtils.isEmpty(termLogFormat) || TextUtils.isEmpty(fileLogFormat)) {
            Log.e(TAG, "init fail, Parameter error.");
            return;
        }
        isInited = true;
        mContext = context;
        QTLogConfig.getInstance().init(context, tag, level, timeFormat, termLogFormat, fileLogFormat, enableConfig);
        QTLogFileManager.getInstance().init(context, maxSaveDays, maxSaveSize, writeToFile);
        QTLogFileManager.getInstance().clearExpiredFiles();
        if (catchCrash) {
            Thread.setDefaultUncaughtExceptionHandler(new QTCrashHandler(Thread.getDefaultUncaughtExceptionHandler()));
        }
    }

    /**
     * 获取 Context 对象
     * @return 返回 Context 对象
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 判断是否已经初始化
     * @return 如果已经初始化，返回 true；否则返回 false;
     */
    public boolean isInited() {
        return isInited;
    }

    /**
     * 内部静态类
     */
    private static class QTLogManagerInstance {
        private static final QTLogManager sInstance = new QTLogManager();
    }
}
