package com.ruhuo.xuaizerobackend.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ruhuo.xuaizerobackend.model.dto.app.AppQueryRequest;
import com.ruhuo.xuaizerobackend.model.entity.App;
import com.ruhuo.xuaizerobackend.model.vo.AppVO;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
public interface AppService extends IService<App> {

    /**
     * 获取脱敏后的应用信息
     *
     * @param app 应用信息
     * @return 脱敏后的应用信息
     */
    AppVO getAppVO(App app);

    /**
     * 获取脱敏后的应用信息（批量）
     *
     * @param appList 应用列表
     * @return 脱敏后的应用信息列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 根据查询条件构造数据查询参数
     *
     * @param appQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}
