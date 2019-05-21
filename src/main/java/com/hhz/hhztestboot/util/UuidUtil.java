package com.hhz.hhztestboot.util;

import java.util.UUID;

/**
 * @Author: 肖冠阳
 * @Description:
 * @Date: Create in 20:02 2019/3/25 0025
 */
public class UuidUtil {
    /**
     * 功能：去掉UUID中的“-”
     */
    public static String uuid() {
        String s = UUID.randomUUID().toString();
        return s.replace("-", "");
    }
}
