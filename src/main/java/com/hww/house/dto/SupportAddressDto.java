package com.hww.house.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 15:28
 * Description: @JsonProperty
 */
@Data
public class SupportAddressDto {

    private Long id;
    /**
     * 上一级行政单位
     */
    @JsonProperty(value = "belong_to")
    private String belongTo;
    private String enName;
    private String cnName;

    private String level;

    private double baiDuMapLongitude;

    private double baiDuMapLatitude;

}
