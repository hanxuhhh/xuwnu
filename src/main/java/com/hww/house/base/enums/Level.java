package com.hww.house.base.enums;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 14:52
 * Description:行政级别定义
 */
public enum Level {
    /**
     * 城市
     */
    CITY("city"),
    /**
     * 地区
     */
    REGION("region");

    private String value;

    Level(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Level getLevel(String value) {
        for (Level level : Level.values()) {
            if (level.getValue().equals(value)) {
                return level;
            }
        }
        return null;
    }
}
