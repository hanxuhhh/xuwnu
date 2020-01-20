package com.hww.house.base.bmap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/24
 * @Time: 9:42
 * Description: 百度位置信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaiDuMapLocation {


    @JsonProperty("lon")
    private double lon;


    @JsonProperty("lat")
    private double lat;
}
