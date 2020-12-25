package com.qty.log.bean;

/**
 * 日志级别枚举类
 */
public enum QTLogLevel {

    /**
     * 不打印日志
     */
    NONE(0, ""),
    /**
     * 打印所有日志
     */
    ALL_LEVEL(1, "A"),
    /**
     * 打印详情日志
     */
    VERBOSE_LEVEL(2, "V"),
    /**
     * 打印调试日志
     */
    DEBUG_LEVEL(3, "D"),
    /**
     * 打印信息日志
     */
    INFO_LEVEL(4, "I"),
    /**
     * 打印警告日志
     */
    WARN_LEVEL(5, "W"),
    /**
     * 打印错误日志
     */
    ERROR_LEVEL(6, "E");

    /**
     * 枚举的原始值
     */
    private int raw;
    /**
     * 枚举名称
     */
    private String name;

    /**
     * 构造函数
     */
    private QTLogLevel(int raw, String name) {
        this.raw = raw;
        this.name = name;
    }

    /**
     * 获取枚举的原始值
     *
     * @return 返回枚举的原始值
     */
    public int rawValue() {
        return this.raw;
    }

    /**
     * 获取枚举的名称
     *
     * @return 返回枚举的名称
     */
    public String nameValue() {
        return this.name;
    }
}
