package com.ruhuo.xuaizerobackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
//省去了手写 getText() 和 getValue() 方法的麻烦。
public enum CodeGenTypeEnum {

    HTML("原生HTML模式","html"),
    MULTI_FILE("原生多文件模式","multi_file"),
    VUE_PROJECT("Vue 工程模式","vue_project");

    private final String text;  // 给用户看的中文字符串，如 "原生HTML模式"
    private final String value; // 给程序/数据库用的标识，如 "html"

    CodeGenTypeEnum(String text,String value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static CodeGenTypeEnum getEnumByValue(String value){
        if(ObjUtil.isEmpty(value)){
            return null;
        }
        for (CodeGenTypeEnum anEnum:CodeGenTypeEnum.values()){
            if(anEnum.value.equals(value)){
                return anEnum;
            }
        }
        return null;
    }
}
