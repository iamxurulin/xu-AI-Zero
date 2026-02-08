package com.ruhuo.xuaizerouser.service.impl;

import com.ruhuo.xuaizerobackend.innerservice.InnerUserService;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.vo.UserVO;
import com.ruhuo.xuaizerouser.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@DubboService
public class InnerUserServiceImpl implements InnerUserService {
    @Resource
    private UserService userService;
    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids){
        return userService.listByIds(ids);
    }

    @Override
    public User getById(Serializable id){
        return userService.getById(id);
    }

    @Override
    public UserVO getUserVO(User user){
        return userService.getUserVO(user);
    }
}
