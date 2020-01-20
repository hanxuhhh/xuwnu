package com.hww.house.entity;

import lombok.Data;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:34
 * Description:
 */
@Data
public class HousePicture {

    private Long id;
    private Long houseId;
    /**
     * 图片路径
     */
    private String cdnPrefix;
    /**
     * 宽
     */
    private int width;
    /**
     * 高
     */
    private int height;
    /**
     * 所属房屋位置
     */
    private String location;
    /**
     * 文件名
     */
    private String path;

}
