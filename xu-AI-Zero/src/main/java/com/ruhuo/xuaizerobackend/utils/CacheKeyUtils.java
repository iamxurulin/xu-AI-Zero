package com.ruhuo.xuaizerobackend.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存Key生成工具类
 * 该工具类用于生成对象的缓存Key，通过将对象转换为JSON字符串后再进行MD5哈希处理
 *
 */
public class CacheKeyUtils {

    /**
     * 根据对象生成缓存Key （JSON + MD5）

     * 该方法将任意对象转换为JSON字符串，然后通过MD5算法生成哈希值作为缓存Key
     * @param obj   要生成Key的对象，可以是任意类型的Java对象
     * @return      MD5哈希后的缓存Key，如果输入对象为null，则返回"null"字符串的MD5哈希值
     */
    public static String generateKey(Object obj){
        // 处理null对象的情况
        if(obj == null){
            return DigestUtil.md5Hex("null");
        }

        //先转JSON，再MD5
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
