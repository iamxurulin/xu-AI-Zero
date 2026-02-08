package com.ruhuo.xuaizerobackend.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存Key生成工具类
 *
 */
public class CacheKeyUtils {

    /**
     * 根据对象生成缓存Key （JSON + MD5）
     * @param obj   要生成Key的对象
     * @return      MD5哈希后的缓存Key
     */
    public static String generateKey(Object obj){
        if(obj == null){
            return DigestUtil.md5Hex("null");
        }

        //先转JSON，再MD5
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
