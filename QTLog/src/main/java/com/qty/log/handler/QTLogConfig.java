package com.qty.log.handler;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.qty.log.bean.QTClassLevel;
import com.qty.log.bean.QTLogLevel;
import com.qty.log.bean.QTPackageLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * 日志配置信息类
 *
 * log.properties 日志配置文件格式如下：
 *
 * # 日志 TAG
 * LOG_TAG="TAG"
 *
 * # 日志级别优先级（从低到高） none > error > wran > info > debug > verbose
 * # 设置当前日志级别
 * LOG_LEVEL=debug
 *
 * # 日志日期格式
 * TIME_FORMAT=yyyy-MM-dd HH:mm:ss.sss
 *
 * # 日志格式
 * # %T TAG
 * # %d 日期，如果设置了日志，则需要给出日志格式，例如：%d -format="yyyy-MM-dd HH:mm:ss.sss"
 * # %c 类名
 * # %C 完整类名
 * # %p 包名
 * # %t 线程名
 * # %L 日志类型
 * # %f 文件名
 * # %M 方法名
 * # %l 行号
 * # %m 日志内容
 * # %n 换行
 * # 终端中的日志格式
 * TERMINAL_LOG_FORMAT=[%c][%M]%m
 * # 文件中的日志格式
 * FILE_LOG_FORMAT=%d %p %L/%T(%l): [%c][%M]%m%n
 *
 * # %n 指定包名的级别（包名和日志级别使用冒号隔开，中间不能有空格）
 * PACKAGE_LOG_LEVEL=com.qty.log:info
 *
 * # 类的日志级别（类名需要是完整的类名（包含包名），类名与日志级别使用冒号隔开，中间不能有空格）
 * CLASS_LOG_LEVEL=com.qty.log.Log:wran
 */
public class QTLogConfig {

    /**
     * TAG
     */
    private static final String TAG = QTLogConfig.class.getSimpleName();
    /**
     * 日志配置文件名
     */
    private static final String LOG_CONFIG_FILE_NAME = "log.config";
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.sss";
    /**
     * 默认终端打印日志格式
     */
    public static final String DEFAULT_TERMINAL_LOG_FORMAT = "[%c][%M]%m%n";
    /**
     * 默认文件打印日志格式
     */
    public static final String DEFAULT_FILE_LOG_FORMAT = "%d  %p  %L  %T(%l): [%c][%M]%m%n";
    /**
     * 日志配置文件中 TAG 的标签名
     */
    private static final String LOG_TAG = "LOG_TAG";
    /**
     * 日志配置文件中日志级别的标签名
     */
    private static final String LEVEL_TAG = "LOG_LEVEL";
    /**
     * 日志配置文件中时间格式的标签名
     */
    private static final String TIME_FORMAT_TAG = "TIME_FORMAT";
    /**
     * 日志配置文件中终端打印格式的标签名
     */
    private static final String TERMINAL_LOG_FORMAT_TAG = "TERMINAL_LOG_FORMAT";
    /**
     * 日志配置文件中文件打印格式的标签名
     */
    private static final String FILE_LOG_FORMAT_TAG = "FILE_LOG_FORMAT";
    /**
     * 日志配置文件中包日志级别的标签名
     */
    private static final String PACKAGE_LEVEL_TAG = "PACKAGE_LOG_LEVEL";
    /**
     * 日志配置文件中类日志级别的标签名
     */
    private static final String CLASS_LEVEL_TAG = "CLASS_LOG_LEVEL";
    /**
     * QTLogConfig实例
     */
    private static final QTLogConfig INSTANCE = QTLogConfigInstance.sInstance;

    /**
     * Context 对象
     */
    private Context mContext;
    /**
     * 日志 TAG
     */
    private String mTag;
    /**
     * 日志级别
     */
    private QTLogLevel mLevel;
    /**
     * 是否允许使用 log.properties 的配置
     */
    private boolean enableConfig;
    /**
     * 时间格式
     */
    private String mTimeFormat;
    /**
     * 终端日志打印格式
     */
    private String mTerminalLogFormat;
    /**
     * 文件日志打印格式
     */
    private String mFileLogFormat;
    /**
     * 类的日志级别集合
     */
    private ArrayList<QTClassLevel> mClassLevels;
    /**
     * 包名的日志级别集合
     */
    private ArrayList<QTPackageLevel> mPackageLevels;

    /**
     * 单例实现
     * @return 返回 QTLogConfig 对象
     */
    public static QTLogConfig getInstance() {
        return INSTANCE;
    }

    /**
     * 内部构造方法
     */
    private QTLogConfig() {}

    /**
     * 初始化方法
     * @param context Context 对象
     * @param tag   日志 tag
     * @param level 日志级别
     * @param timeFormat 日志日期格式
     * @param termLogFormat 终端日志格式
     * @param fileLogFormat 文件日志格式
     * @param enableConfig  是否允许使用 log.properties 的配置
     */
    public void init(Context context, String tag, QTLogLevel level, String timeFormat,
                     String termLogFormat, String fileLogFormat, boolean enableConfig) {
        mContext = context;
        mTag = tag;
        mLevel = level;
        this.enableConfig = enableConfig;
        mTimeFormat = timeFormat;
        mTerminalLogFormat = termLogFormat;
        mFileLogFormat = fileLogFormat;
        mClassLevels = new ArrayList<>();
        mPackageLevels = new ArrayList<>();
        if (enableConfig) {
            parserConfigFile();
        } else {
            Log.i(TAG, "init=>Disabled use log config file.");
        }
    }

    /**
     * 获取日志 TAG
     * @return 返回 TAG
     */
    public String getTag() {
        return mTag;
    }

    /**
     * 获取日志级别
     *
     * 根据 clazz 对象获取日志级别，如果 clazz 为 null，则直接返回顶级日志级别。
     * 如果在类日志级别集合中找到该类，则使用该类的日志级别；如果没有找到，
     * 则在包日志级别集合中查找，如果找到则使用包的日志级别，否则使用顶级的日志级别
     * @param clazz 当前的类
     * @return  返回日志级别
     */
    public QTLogLevel getLevel(Class clazz) {
        if (clazz == null) {
            return mLevel;
        }
        for (int i = 0; i < mClassLevels.size(); i++) {
            QTClassLevel level = mClassLevels.get(i);
            if (level.getClassName().equals(clazz.getName())) {
                return level.getLevel();
            }
        }
        for (int i = 0; i < mPackageLevels.size(); i++) {
            QTPackageLevel level = mPackageLevels.get(i);
            if (level.getPackageName().equals(clazz.getPackage())) {
                return level.getLevel();
            }
        }
        return mLevel;
    }

    /**
     * 获取时间格式
     * @return 返回时间格式
     */
    public String getTimeFormat() {
        return mTimeFormat;
    }

    /**
     * 获取终端日志打印格式
     * @return 返回终端日志打印格式
     */
    public String getTerminalLogFormat() {
        return mTerminalLogFormat;
    }

    /**
     * 获取文件日志打印格式
     * @return 返回文件日志打印格式
     */
    public String getFileLogFormat() {
        return mFileLogFormat;
    }

    /**
     * 解析日志配置文件，日志文件位于 Android/data/应用包名/files/Documents/ 目录下
     */
    private void parserConfigFile() {
        File file = mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File configFile = new File(file.getAbsolutePath() + File.separator + LOG_CONFIG_FILE_NAME);
        Log.d(TAG, "parserConfigFile=>config file: " + configFile.getAbsolutePath());
        if (configFile.exists() && configFile.isFile()) {
            try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                mClassLevels.clear();
                mPackageLevels.clear();
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (TextUtils.isEmpty(line) || line.startsWith("#")) {
                        continue;
                    }
                    String[] strs = line.split("=");
                    if (strs.length == 2 && !TextUtils.isEmpty(strs[1]) && !TextUtils.isEmpty(strs[0])) {
                        switch (strs[0].trim()) {
                            case LOG_TAG:
                                mTag = strs[1].trim();
                                break;

                            case LEVEL_TAG:
                                try {
                                    mLevel = QTLogLevel.valueOf(strs[1].trim().toUpperCase());
                                } catch (Exception e) {
                                    Log.e(TAG, "parserConfigFile=>Switching log level error: ", e);
                                }
                                break;

                            case TIME_FORMAT_TAG:
                                mTimeFormat = strs[1].trim();
                                break;

                            case TERMINAL_LOG_FORMAT_TAG:
                                mTerminalLogFormat = strs[1].trim();
                                break;

                            case FILE_LOG_FORMAT_TAG:
                                mFileLogFormat = strs[1].trim();
                                break;

                            case PACKAGE_LEVEL_TAG:
                                try {
                                    String[] info = strs[1].trim().split(":");
                                    if (info.length == 2 && !TextUtils.isEmpty(info[0]) && !TextUtils.isEmpty(info[1])) {
                                        String packageName = info[0].trim();
                                        QTLogLevel level = QTLogLevel.valueOf(info[1].trim());
                                        QTPackageLevel pl = new QTPackageLevel(packageName, level);
                                        mPackageLevels.add(pl);
                                    } else {
                                        Log.e(TAG, "parserConfigFile=>\"" + line + "\" is not a package level config.");
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "parserConfigFile=>Switching package log level error: ", e);
                                }
                                break;

                            case CLASS_LEVEL_TAG:
                                try {
                                    String[] info = strs[1].trim().split(":");
                                    if (info.length == 2 && !TextUtils.isEmpty(info[0]) && !TextUtils.isEmpty(info[1])) {
                                        String className = info[0].trim();
                                        QTLogLevel level = QTLogLevel.valueOf(info[1].trim());
                                        QTClassLevel pl = new QTClassLevel(className, level);
                                        mClassLevels.add(pl);
                                    } else {
                                        Log.e(TAG, "parserConfigFile=>\"" + line + "\" is not a class level config.");
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "parserConfigFile=>Switching class log level error: ", e);
                                }
                                break;

                            default:
                                Log.e(TAG, "parserConfigFile=>Unknown configuration \"" + line + "\".");
                                break;
                        }
                    } else {
                        Log.e(TAG, "parserConfigFile=>\"" + line  + "\" incorrect format");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "parserConfigFile=>Read config file error: ", e);
            }
        } else {
            Log.e(TAG, "parserConfigFile=>Config file is not exist!!!");
        }
    }

    /**
     * 内部类，单例实现
     */
    private static class QTLogConfigInstance {
        private static final QTLogConfig sInstance = new QTLogConfig();
    }
}
