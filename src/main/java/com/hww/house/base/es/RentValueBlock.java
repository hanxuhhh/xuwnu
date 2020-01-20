package com.hww.house.base.es;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/11
 * @Time: 14:32
 * Description:带用区间的常用数值定义
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RentValueBlock {

    /**
     * 价格区间定义
     */
    public static final Map<String, RentValueBlock> PRICE_BLOCK;
    /**
     * 面积区间定义
     */
    public static final Map<String, RentValueBlock> AREA_BLOCK;

    /**
     * 无限制区间
     */
    public static final RentValueBlock ALL = new RentValueBlock("*", -1, -1);

    private String key;
    private int min;
    private int max;

    static {
        PRICE_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-1000", new RentValueBlock("*-1000", -1, 1000))
                .put("1000-3000", new RentValueBlock("1000-3000", 1000, 3000))
                .put("3000-*", new RentValueBlock("3000-*", 3000, -1))
                .build();
        AREA_BLOCK = ImmutableMap.<String, RentValueBlock>builder()
                .put("*-30", new RentValueBlock("*-30", -1, 30))
                .put("30-50", new RentValueBlock("30-50", 30, 50))
                .put("50-*", new RentValueBlock("50-*", 50, -1))
                .build();
    }

    /**
     * 匹配价格区间
     *
     * @param key
     * @return
     */
    public static RentValueBlock matchPrice(String key) {
        RentValueBlock block = PRICE_BLOCK.get(key);
        if (block == null) {
            return ALL;
        }
        return block;
    }

    /**
     * 匹配地区区间
     *
     * @param key
     * @return
     */
    public static RentValueBlock matchArea(String key) {
        RentValueBlock block = AREA_BLOCK.get(key);
        if (block == null) {
            return ALL;
        }
        return block;
    }

}
