package com.hww.house.base.kafkamessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/14
 * @Time: 16:34
 * Description: kafka消息结构体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KafKaMessage {

    public static final String INDEX = "index";

    public static final String REMOVE = "remove";

    public  static final int MAX_RETRY=3;

    private long houseId;

    private String operation;
    /**
     * 重试
     */
    private int retry = 0;

}
