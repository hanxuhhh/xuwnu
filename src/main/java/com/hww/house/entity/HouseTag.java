package com.hww.house.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:46
 * Description:
 */
@Data
@AllArgsConstructor
public class HouseTag {

    private Long id;

    private Long houseId;

    private String name;

}
