package com.hww.house.base.service.response;

import com.hww.house.base.enums.Status;
import lombok.Data;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 14:08
 * Description:
 */
@Data
public class AppResponse {

    private int code;
    private String message;
    private Object data;
    //更多
    private boolean more;

    public AppResponse() {

    }

    public AppResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 请求成功
     *
     * @param data
     * @return
     */
    public static AppResponse requestSuccess(Object data) {
        return new AppResponse(Status.SUCCESS.getCode(), Status.SUCCESS.getStandardMessage(), data);
    }


    /**
     * 请求失败
     *
     * @param message
     * @return
     */
    public static AppResponse requestError(String message) {
        return new AppResponse(Status.BAD_REQUEST.getCode(), message, null);
    }


}
