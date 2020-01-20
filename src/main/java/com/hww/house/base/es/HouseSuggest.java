package com.hww.house.base.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/17
 * @Time: 10:34
 * Description: 自动补全
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseSuggest {
    private String input;
    private int weight = 10;
}
