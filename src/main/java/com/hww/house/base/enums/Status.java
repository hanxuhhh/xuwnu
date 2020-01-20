package com.hww.house.base.enums;

import lombok.Data;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 13:54
 * Description:
 */

public enum Status {
    /**
     * 请求失败
     */
    REQUESTSUCCESS(200, "success"),
    /**
     * 请求成功
     */
    SUCCESS(200, "OK"),
    /**
     * 错误请求
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * 未找到资源
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * 内部错误
     */
    INTERNAL_SERVER_ERROR(500, "Unknown Internal Error"),
    /**
     * 参数错误
     */
    NOT_VALID_PARAM(40005, "Not valid Params"),
    /**
     * 不支持造作
     */
    NOT_SUPPORTED_OPERATION(40006, "Operation not supported"),
    /**
     * 未登录
     */
    NOT_LOGIN(50000, "Not Login");

    private int code;
    private String standardMessage;

    Status(int code, String standardMessage) {
        this.code = code;
        this.standardMessage = standardMessage;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStandardMessage() {
        return standardMessage;
    }

    public void setStandardMessage(String standardMessage) {
        this.standardMessage = standardMessage;
    }
}
