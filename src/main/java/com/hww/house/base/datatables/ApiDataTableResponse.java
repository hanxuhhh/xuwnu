package com.hww.house.base.datatables;

import com.hww.house.base.service.response.AppResponse;
import com.hww.house.base.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/10
 * @Time: 9:42
 * Description: dataTables响应结构
 */
@Data
@NoArgsConstructor
public class ApiDataTableResponse extends AppResponse {
    /**
     * draw===回传给dataBable就ok
     */
    private int draw;
    private long recordsTotal;
    private long recordsFiltered;

    public ApiDataTableResponse(int code, String message, Object data) {
        super(code, message, data);
    }

    public ApiDataTableResponse(Status status) {
        this(status.getCode(), status.getStandardMessage(), null);
    }
}
