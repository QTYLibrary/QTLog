package com.qty.log.handler;

import android.content.Context;
import android.util.Log;

import com.qty.log.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 日志文件管理类
 */
public class QTLogFileManager {

    /**
     * TAG
     */
    private static final String TAG = QTLogFileManager.class.getSimpleName();
    /**
     * 默认最大日志文件存储天数, 默认：7天
     */
    public static final int DEFAULT_MAX_SAVE_DAYS = 7;
    /**
     * 默认总日志文件保存空间大小，默认：100MB
     */
    public static final long DEFAULT_MAX_SAVE_SIZE = 100 * 1024 * 1024;
    /**
     * 清除过期日志文件时间间隔
     */
    private static final long CLEAN_TIME_INTERVAL = 60 * 60 * 1000;
    /**
     * 日志文件后缀
     */
    private static final String LOG_FILE_SUFFIX = ".log";
    /**
     * QTLogFileManager 对象
     */
    private static final QTLogFileManager INSTANCE = QTLogFileManagerInstance.sInstance;

    /**
     * Context 对象
     */
    private Context mContext;
    /**
     * 日志文件保存天数，单位：天，小于或等于 0 表示不设置
     */
    private int mMaxSaveDays;
    /**
     * 日志文件保存的总文件大小，单位：Byte，小于或等于 0 表示不设置
     */
    private long mMaxSaveSize;
    /**
     * 是否将日志写入文件中
     */
    private boolean writeToFile;
    /**
     * 日志队列
     */
    private Queue<LogData> mQueue;
    /**
     * 日志写入线程
     */
    private WriteThread mWriteThread;
    /**
     * 日志清理定时器
     */
    private Timer mCleanTimer;
    /**
     * 日志清除任务
     */
    private TimerTask mCleanTask;
    /**
     * 是否停止向日志队列中添加日志
     */
    private boolean stopAddLogToQueue;

    /**
     * 单例方法
     * @return 返回 QTLogFileManager
     */
    public static QTLogFileManager getInstance() {
        return INSTANCE;
    }

    /**
     * 内部构造方法
     */
    private QTLogFileManager() {}

    /**
     * 初始化方法
     * @param context Context 对象
     * @param maxSaveDays 最大保存天数
     * @param maxSaveSize 最大保存空间大小
     * @param writeToFile   是否将日志写入文件
     */
    public void init(Context context, int maxSaveDays, long maxSaveSize, boolean writeToFile) {
        mContext = context;
        mMaxSaveDays = maxSaveDays;
        mMaxSaveSize = maxSaveSize;
        this.writeToFile = writeToFile;
        mQueue = new LinkedList<>();
        if (writeToFile) {
            startCleanExpiredFilesTimer();
        }
    }

    /**
     * 停止向日志队列中添加日志
     */
    public void stopAddLogToQueue() {
        stopAddLogToQueue = true;
    }

    /**
     * 判断日志队列是否为空
     * @return 如果日志队列为空，返回 true；否则返回 false
     */
    public boolean isLogQueueEmpty() {
        return mQueue.isEmpty();
    }

    /**
     * 清除过期日志文件
     */
    public void clearExpiredFiles() {
        Log.i(TAG, "clearExpiredFiles()...");
        if (mMaxSaveDays > 0) {
            clearExpiredFilesByDay();
        } else if (mMaxSaveSize > 0) {
            clearExpiredFilesBySize();
        }
    }

    /**
     * 根据时间清除过期日志文件
     */
    public void clearExpiredFilesByDay() {
        String logDir = FileUtils.getLogFileDirectory(mContext);
        if (logDir != null) {
            File dir = new File(logDir);
            File[] files = dir.listFiles();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar earyDay = Calendar.getInstance();
            earyDay.add(Calendar.DAY_OF_MONTH, -mMaxSaveDays);
            String earyLogFileName = sdf.format(earyDay.getTime());
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                fileName = fileName.substring(0, fileName.indexOf(LOG_FILE_SUFFIX));
                if (fileName.compareTo(earyLogFileName) <= 0) {
                    try {
                        files[i].delete();
                    } catch (Exception e) {
                        Log.e(TAG, "clearExpiredFilesByDay=>Delete file "
                                + fileName + LOG_FILE_SUFFIX + " error: ", e);
                    }
                }
            }
        } else {
            Log.e(TAG, "clearExpiredFilesByDay=>Can't get log file directory.");
        }
    }

    /**
     * 根据存储空间清除过期日志文件
     */
    public void clearExpiredFilesBySize() {
        String logDir = FileUtils.getLogFileDirectory(mContext);
        if (logDir != null) {
            File dir = new File(logDir);
            long size = FileUtils.getTotalSizeOfFilesInDir(dir);
            if (size > mMaxSaveSize) {
                File[] logs = dir.listFiles();
                Arrays.sort(logs, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                ArrayList<String> files = new ArrayList<>();
                for (int i = 0; i < logs.length; i++) {
                    files.add(logs[i].getAbsolutePath());
                }
                File file = null;
                while (size > mMaxSaveSize / 2 && files.size() > 0) {
                    file = new File(files.get(0));
                    long deleteSize = file.length();
                    try {
                        file.delete();
                        size -= deleteSize;
                        files.remove(0);
                    } catch (Exception e) {
                        Log.e(TAG, "clearExpiredFilesBySize=>Delete file "
                                + file.getName() + " error: ", e);
                        break;
                    }
                }
            }
        } else {
            Log.e(TAG, "clearExpiredFilesBySize=>Can't get log file directory.");
        }
    }

    /**
     * 启动清除过期日志文件定时器
     */
    public void startCleanExpiredFilesTimer() {
        if (mCleanTimer != null) {
            mCleanTimer.cancel();
        }
        if (mCleanTask != null) {
            mCleanTask.cancel();
        }
        mCleanTimer = new Timer();
        mCleanTask = new TimerTask() {
            @Override
            public void run() {
                clearExpiredFiles();
                startCleanExpiredFilesTimer();
            }
        };
        mCleanTimer.schedule(mCleanTask, CLEAN_TIME_INTERVAL);
    }

    /**
     * 将要打印的日志添加到日志打印队列中
     * @param time  日志时间
     * @param msg   日志信息
     */
    public void addLogToQueue(Calendar time, String msg) {
        if (writeToFile && !stopAddLogToQueue) {
            if (mWriteThread == null || mWriteThread.isStop()) {
                mWriteThread = new WriteThread();
                mWriteThread.start();
            }
            synchronized (mQueue) {
                mQueue.add(new LogData(time, msg));
            }
        } else {
            Log.w(TAG, "addLogToQueue=>unabled add, writeToFile = "
                    + writeToFile + ", isStop: " + stopAddLogToQueue);
        }
    }

    private class WriteThread extends Thread {

        /**
         * 在没有日志写入时，线程休眠时间
         */
        private static final int SLEEP_TIME = 500;

        /**
         * 是否停止线程
         */
        private boolean isStop;

        @Override
        public void run() {
            Log.d(TAG, "run=>Write thread start....");
            BufferedWriter writer = null;
            try {
                Calendar logFileTime = Calendar.getInstance();
                File logFile = getLogFile(logFileTime);
                if (logFile == null) {
                    Log.e(TAG, "run=>Unabled open log file.");
                    isStop = true;
                    return;
                }
                writer = new BufferedWriter(new FileWriter(logFile, true));
                while (!isStop) {
                    if (!mQueue.isEmpty()) {
                        LogData log = mQueue.poll();
                        if (needSwitchLogFile(logFileTime, log.getTime())) {
                            try {
                                writer.flush();
                                writer.close();
                            } catch (Exception ignore) {}
                            logFileTime = log.getTime();
                            logFile = getLogFile(logFileTime);
                            if (logFile == null) {
                                Log.e(TAG, "run=>Unabled open log file.");
                                isStop = true;
                                return;
                            }
                            writer = new BufferedWriter(new FileWriter(logFile, true));
                        }
                        writer.write(log.getMessage());
                        writer.flush();
                    } else {
                        try {
                            Thread.sleep(SLEEP_TIME);
                        } catch (Exception ignore) {}
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "run=>error: ", e);
            } finally {
                try {
                    if (writer != null) {
                        writer.flush();
                        writer.close();
                    }
                } catch (Exception ignore) {}
            }
            isStop = true;
            Log.i(TAG, "run=>Write log thread end.");
        }

        /**
         * 停止线程
         */
        public void stopThread() {
            try {
                isStop = true;
                notify();
            } catch (Exception ignore) {}
        }

        /**
         * 判断线程是否停止
         * @return 如果线程已经停止，返回 true；否则返回 false
         */
        public boolean isStop() {
            return isStop;
        }

        /**
         * 获取日志文件对象
         * @param time 日志时间
         * @return 返回日志文件对象，如果获取失败，则返回 null.
         */
        private File getLogFile(Calendar time) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String logDir = FileUtils.getLogFileDirectory(mContext);
            if (logDir == null) {
                Log.e(TAG, "getLogFile=>Log file directory is null.");
                return null;
            }
            String filePath = logDir + File.separator + sdf.format(time.getTime()) + LOG_FILE_SUFFIX;
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    Log.e(TAG, "getLogFile=>error: ", e);
                    file = null;
                }
            }
            return file;
        }

        /**
         * 判断是否需要切换日志文件
         * 当当前日志文件与日志信息中的时间不在同一天时，将会切换用于记录日志的文件。
         * @param older 上次日志文件时间
         * @param newer 当前日志时间
         * @return 如果需要切换日志文件，则返回 true；否则返回 false
         */
        private boolean needSwitchLogFile(Calendar older, Calendar newer) {
            if (older.get(Calendar.YEAR) != newer.get(Calendar.YEAR)
                    || older.get(Calendar.MONTH) != newer.get(Calendar.MONTH)
                    || older.get(Calendar.DAY_OF_MONTH) != newer.get(Calendar.DAY_OF_MONTH)) {
                return true;
            }
            return false;
        }
    }

    private class LogData {
        /**
         * 日志时间
         */
        private Calendar mTime;
        /**
         * 日志信息
         */
        private String mMsg;

        /**
         * 构造方法
         * @param time  日志时间
         * @param msg   日志信息
         */
        LogData(Calendar time, String msg) {
            mTime = time;
            mMsg = msg;
        }

        /**
         * 获取日志时间
         * @return  返回日志时间
         */
        public Calendar getTime() {
            return mTime;
        }

        /**
         * 获取日志信息
         * @return  返回日志信息
         */
        public String getMessage() {
            return mMsg;
        }
    }

    /**
     * 内部类，单例实现辅助类
     */
    private static class QTLogFileManagerInstance {
        public static final QTLogFileManager sInstance = new QTLogFileManager();
    }
}
