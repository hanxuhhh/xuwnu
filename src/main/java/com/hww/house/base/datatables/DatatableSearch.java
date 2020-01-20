package com.hww.house.base.datatables;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/10
 * @Time: 9:47
 * Description: dataTables搜索结构
 */
@Data
public class DatatableSearch {

    /**
     * Datatables要求回显字段
     */
    private int draw;

    /**
     * Datatables规定分页字段
     */
    private int start;
    private int length;


    /********************************自定义值********************/

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMin;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createTimeMax;

    private String city;
    private String title;
    private String direction;
    private String orderBy;


}
