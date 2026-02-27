package com.ruhuo.xuaizerobackend.common;

import lombok.Data;

/**
 * 分页请求参数类
 * 用于封装前端传递的分页、排序相关参数
 * 使用@Data注解自动生成getter、setter等方法
 */
@Data
public class PageRequest {
    /**
     * 当前页号
     * 默认值为1
     */
    private int pageNum = 1;

    /**
     * 页面大小
     * 默认值为10，表示每页显示10条数据
     */
    private int pageSize = 10;

    /**
     * 排序字段
     * 用于指定排序依据的属性名
     */
    private String sortField;

    /**
     * 排序顺序（默认排序）
     * 默认值为"descend"，表示降序排序
     * 可选值："ascend"（升序）或"descend"（降序）
     */
    private String sortOrder = "descend";
}
