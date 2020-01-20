package com.hww.house.base.service.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 18:05
 * Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse<T> {

    private boolean success;
    private String message;
    private T result;

    public ServiceResponse(boolean success) {
        this.success = success;
    }

    public ServiceResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ServiceResponse(T result) {
        this.result = result;
    }
}
