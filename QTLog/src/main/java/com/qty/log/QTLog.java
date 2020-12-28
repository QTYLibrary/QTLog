package com.qty.log;

import android.text.TextUtils;
import android.util.Log;

import com.qty.log.bean.QTLogLevel;
import com.qty.log.handler.QTLogConfig;
import com.qty.log.handler.QTLogFileManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * 日志打印类
 *
 * 日志格式如下：
 *  %T TAG
 *  %d 日期，如果设置了日志，则需要给出日志格式，例如：%d -format="yyyy-MM-dd HH:mm:ss.sss"
 *  %c 类名
 *  %C 完整类名
 *  %p 包名
 *  %t 线程名
 *  %L 日志类型
 *  %f 文件名
 *  %M 方法名
 *  %l 行号
 *  %m 日志内容
 *  %n 换行
 */
public class QTLog {

    /**
     * TAG
     */
    private static final String TAG = QTLog.class.getSimpleName();

    /**
     * 日志配置对象
     */
    private QTLogConfig mConfig;
    /**
     * 日志文件管理器
     */
    private QTLogFileManager mFileManager;
    /**
     * 当前使用日志的类
     */
    private Class mClazz;
    /**
     * 日志 TAG
     */
    private String mTag;
    /**
     * 日志级别
     */
    private QTLogLevel mLevel;
    /**
     * 终端日志格式
     */
    private String mTerminalLogFormat;
    /**
     * 文件日志格式
     */
    private String mFileLogFormat;

    /**
     * 构造方法
     * @param clazz 类对象
     */
    public QTLog(Class clazz) {
        this(clazz, null, null, null, null);
    }

    /**
     * 构造方法
     * @param clazz 类对象
     * @param tag   日志 TAG
     * @param level 日志级别
     * @param termFormat   终端日志格式
     * @param fileFormat  文件日志格式
     */
    private QTLog(Class clazz, String tag, QTLogLevel level, String termFormat, String fileFormat) {
        mConfig = QTLogConfig.getInstance();
        mFileManager = QTLogFileManager.getInstance();
        mClazz = clazz;
        mTag = tag;
        if (mTag == null) {
            mTag = mConfig.getTag();
        }
        mLevel = level;
        if (mLevel == null) {
            mLevel = mConfig.getLevel(clazz);
        }
        mTerminalLogFormat = termFormat;
        if (mTerminalLogFormat == null) {
            mTerminalLogFormat = mConfig.getTerminalLogFormat();
        }
        mFileLogFormat = fileFormat;
        if (mFileLogFormat == null) {
            mFileLogFormat = mConfig.getFileLogFormat();
        }
    }

    /**
     * 打印错误日志
     * @param msg 错误日志信息
     */
    public void e(String msg) {
        print(QTLogLevel.ERROR_LEVEL, msg, null);
    }

    /**
     * 打印错误日志
     * @param msg   错误日志信息
     * @param tr    错误跟踪对象
     */
    public void e(String msg, Throwable tr) {
        print(QTLogLevel.ERROR_LEVEL, msg, tr);
    }

    /**
     * 打印警告日志信息
     * @param msg   警告日志信息
     */
    public void w(String msg) {
        print(QTLogLevel.WARN_LEVEL, msg, null);
    }

    /**
     * 打印警告日志信息
     * @param msg   警告日志信息
     * @param tr    错误跟踪对象
     */
    public void w(String msg, Throwable tr) {
        print(QTLogLevel.WARN_LEVEL, msg, tr);
    }

    /**
     * 打印信息日志信息
     * @param msg 信息日志信息
     */
    public void i(String msg) {
        print(QTLogLevel.INFO_LEVEL, msg, null);
    }

    /**
     * 打印信息日志信息
     * @param msg   信息日志信息
     * @param tr    错误跟踪对象
     */
    public void i(String msg, Throwable tr) {
        print(QTLogLevel.INFO_LEVEL, msg, tr);
    }

    /**
     * 打印调试日志信息
     * @param msg   调试日志信息
     */
    public void d(String msg) {
        print(QTLogLevel.DEBUG_LEVEL, msg, null);
    }

    /**
     * 打印调试日志信息
     * @param msg 调试日志信息
     * @param tr    错误跟踪对象
     */
    public void d(String msg, Throwable tr) {
        print(QTLogLevel.DEBUG_LEVEL, msg, tr);
    }

    /**
     * 打印详情日志信息
     * @param msg 详情日志信息
     */
    public void v(String msg) {
        print(QTLogLevel.VERBOSE_LEVEL, msg, null);
    }

    /**
     * 打印详情日志信息
     * @param msg 详情日志信息
     * @param tr 错误跟踪对象
     */
    public void v(String msg, Throwable tr) {
        print(QTLogLevel.VERBOSE_LEVEL, msg, tr);
    }

    /**
     * 日志打印方法
     * @param level 日志级别
     * @param msg   日志信息
     * @param tr    日志错误跟踪对象
     */
    private void print(QTLogLevel level, String msg, Throwable tr) {
        if (!QTLogManager.getInstance().isInited()) {
            Log.e(TAG, "print=>QTLogManager is not inited.");
            return;
        }
        if (level.rawValue() > mLevel.rawValue()) {
            Calendar time = Calendar.getInstance();
            String fileMsg = formatMessage(level, time, mFileLogFormat, msg, tr);
            mFileManager.addLogToQueue(time, fileMsg);
            String logMsg = formatMessage(level, time, mTerminalLogFormat, msg, tr);
            Log.println(level.rawValue(), mTag, logMsg);
        }
    }

    private String formatMessage(QTLogLevel level, Calendar time, String format, String msg, Throwable t) {
        StringBuffer message = new StringBuffer();
        StackTraceElement ste = getStackTraceElement();
        int startIndex = 0;
        int index = format.indexOf("%", startIndex);
        if (index == -1) {
            return format;
        }
        while (startIndex != format.length()) {
            message.append(format.substring(startIndex, index));
            startIndex = index;
            String flag = format.substring(startIndex, index + 2);
//            Log.d(TAG, "formatMessage=>flag: " + flag);
            switch (flag) {
                case "%d":	// 日期时间
                    SimpleDateFormat sdf = new SimpleDateFormat(mConfig.getTimeFormat());
                    message.append(sdf.format(time.getTime()));
                    startIndex = index + 2;
                    break;

                case "%T":	// TAG
                    String tag = String.format("%-15s", mTag);
                    message.append(tag);
                    startIndex = index + 2;
                    break;

                case "%c":	// 类名
                    String simpleName = "Unknow";
                    if (ste != null) {
                        simpleName = ste.getClassName().substring(ste.getClassName().lastIndexOf(".") + 1);
                    }
                    message.append(simpleName);
                    startIndex = index + 2;
                    break;

                case "%C":	// 类名
                    String name = "Unknow";
                    if (ste != null) {
                        simpleName = ste.getClassName();
                    }
                    message.append(name);
                    startIndex = index + 2;
                    break;

                case "%p":	// 包名
                    String packageName = "Unknow";
                    if (QTLogManager.getInstance().getContext() != null) {
                        packageName = QTLogManager.getInstance().getContext().getPackageName();
                    }
                    message.append(packageName);
                    startIndex = index + 2;
                    break;

                case "%t":	// 线程名
                    String thread = "Unknow";
                    if (ste != null) {
                        thread = Thread.currentThread().getName();
                    }
                    message.append(thread);
                    startIndex = index + 2;
                    break;

                case "%L":	// 日志级别
                    message.append(level.nameValue());
                    startIndex = index + 2;
                    break;

                case "%f":	// 文件名
                    String file = "Unknow";
                    if (ste != null) {
                        file = ste.getFileName();
                    }
                    message.append(file);
                    startIndex = index + 2;
                    break;

                case "%M":	// 方法名
                    String method = "Unknow";
                    if (ste != null) {
                        method = ste.getMethodName();
                    }
                    message.append(method);
                    startIndex = index + 2;
                    break;

                case "%l":	// 行号
                    String line = "Unknow";
                    if (ste != null) {
                        line = String.format("%4d", ste.getLineNumber());
                    }
                    message.append(line);
                    startIndex = index + 2;
                    break;

                case "%m":	// 日志内容
                    String header = message.toString();
                    message.append(msg);
                    if (t != null) {
                        message.append(System.getProperty("line.separator"));
                        String[] errs = Log.getStackTraceString(t).split("\n");
                        for (int i = 0; i < errs.length; i++) {
                            if (i == 0) {
                                message.append(errs[i]);
                            } else {
                                message.append(header + errs[i]);
                            }
                            if (i + 1 < errs.length) {
                                message.append(System.getProperty("line.separator"));
                            }

                        }
                    }
                    startIndex = index + 2;
                    break;

                case "%n":	// 换行
                    message.append(System.getProperty("line.separator"));
                    startIndex = index + 2;
                    break;
            }

            index = format.indexOf("%", startIndex);
            if (index == -1) {
                message.append(format.substring(startIndex, format.length()));
                startIndex = format.length();
            }
        }
        return message.toString();
    }

    /**
     * 获取运行环境的堆栈信息
     * @return 返回堆栈信息
     */
    private StackTraceElement getStackTraceElement() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        StackTraceElement ele = null;
        if (stackElements != null)
            for (int i = 0; i < stackElements.length; ) {
                if (stackElements[i].getClassName().equals(getClass().getName())) {
                    i++;
                    continue;
                }
                ele = stackElements[i];
                break;
            }
        return ele;
    }
}
