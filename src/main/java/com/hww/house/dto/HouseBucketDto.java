package com.hww.house.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/18
 * @Time: 20:15
 * Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseBucketDto {
    private String key;
    private long count;
}
