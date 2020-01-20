package com.hww.house.base.service.response;

import lombok.Data;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 15:48
 * Description:
 */
@Data
public class BaseServiceResponse<T> {

    private long total;
    private List<T> result;

    public BaseServiceResponse(long total, List<T> result) {
        this.total = total;
        this.result = result;
    }

    public int getResultSize() {
        if (this.result == null) {
            return 0;
        }
        return this.result.size();
    }

}
