package com.qty.log.crash;

import androidx.annotation.NonNull;

import com.qty.log.QTLog;
import com.qty.log.handler.QTLogFileManager;

/**
 * 崩溃异常捕获类
 * @hide
 */
public class QTCrashHandler implements Thread.UncaughtExceptionHandler {

    /**
     * 默认的崩溃异常处理对象
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /**
     * 日志打印类
     */
    private QTLog Log = new QTLog(QTCrashHandler.class);
    /**
     * 日志文件管理类
     */
    private QTLogFileManager mFileManager = QTLogFileManager.getInstance();

    /**
     * 构造方法
     * @param handler 默认崩溃处理对象
     */
    public QTCrashHandler(Thread.UncaughtExceptionHandler handler) {
        mDefaultHandler = handler;
    }

    /**
     * 将捕获的异常添加到日志打印，然后停止向日志打印队列添加日志信息。
     * 通过循环判断当前打印队列是否为空，如果为空就结束应用；否则休眠 100 毫秒。
     * @param t 异常线程
     * @param e 异常信息
     */
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e("uncaughtException=>Thread: " + t.toString());
        Log.e("uncaughtException=>Exception: ", e);
        mFileManager.stopAddLogToQueue();
        while (!mFileManager.isLogQueueEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {}
        }
        android.util.Log.i(QTCrashHandler.class.getSimpleName(), "uncaughtException=>start exit.");
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        } else {
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
