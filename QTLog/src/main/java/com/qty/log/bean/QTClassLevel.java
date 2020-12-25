package com.qty.log.bean;

/**
 * 类的日志级别类
 */
public class QTClassLevel {

    /**
     * 类的全名
     */
    private String mClassName;
    /**
     * 类的日志级别
     */
    private QTLogLevel mLevel;

    /**
     * 构造方法
     * @param className 类的全名
     * @param level 类的日志打印级别
     */
    public QTClassLevel(String className, QTLogLevel level) {
        mClassName = className;
        mLevel = level;
    }

    /**
     * 获取类名
     * @return 返回类名
     */
    public String getClassName() {
        return mClassName;
    }

    /**
     * 获取类的日志级别
     * @return 返回日志级别
     */
    public QTLogLevel getLevel() {
        return mLevel;
    }
}
