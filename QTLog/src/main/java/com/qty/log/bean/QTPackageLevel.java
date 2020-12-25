package com.qty.log.bean;

/**
 * 包的日志级别
 */
public class QTPackageLevel {

    /**
     * 包名
     */
    private String mPackageName;
    /**
     * 日志级别
     */
    private QTLogLevel mLevel;

    /**
     * 构造方法
     * @param packageName 包名
     * @param level 日志级别
     */
    public QTPackageLevel(String packageName, QTLogLevel level) {
        mPackageName = packageName;
        mLevel = level;
    }

    /**
     * 获取包名
     * @return 返回包名
     */
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * 获取日志级别
     * @return 返回日志级别
     */
    public QTLogLevel getLevel() {
        return mLevel;
    }
}
