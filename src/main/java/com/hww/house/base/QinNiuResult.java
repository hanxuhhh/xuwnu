package com.hww.house.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/9
 * @Time: 16:17
 * Description:
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QinNiuResult {
    public String key;
    public String hash;
    public String bucket;
    public int width;
    public int height;

}
