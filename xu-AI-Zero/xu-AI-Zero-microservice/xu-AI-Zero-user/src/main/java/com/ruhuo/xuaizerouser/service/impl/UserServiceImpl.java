package com.ruhuo.xuaizerouser.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerouser.mapper.UserMapper;
import com.ruhuo.xuaizerobackend.model.dto.user.UserQueryRequest;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.enums.UserRoleEnum;
import com.ruhuo.xuaizerobackend.model.vo.LoginUserVO;
import com.ruhuo.xuaizerobackend.model.vo.UserVO;
import com.ruhuo.xuaizerouser.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ruhuo.xuaizerobackend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Override
    public long userRegister(String userAccount,String userPassword,String checkPassword){
        //1.校验
        if(StrUtil.hasBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }

        if(userPassword.length()<8||checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }

        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }

        //2.检查是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }

        //3.加密
        String encryptPassword = getEncryptPassword(userPassword);

        //4.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword){
        //加盐+混淆密码
        final String SALT = "ruhuo";
        return DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
    }

    /**
     * 用于 将 User 实体转换为 LoginUserVO 视图对象，
     * 通过 BeanUtil.copyProperties 快速拷贝同名属性，
     * 避免返回敏感字段，提升安全性和代码可维护性。
     *
     */
    @Override
    public LoginUserVO getLoginUserVO(User user){
        if(user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request){
        //1.校验
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }

        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"张好错误");
        }

        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }

        //2.加密
        String encryptPassword = getEncryptPassword(userPassword);
        //查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);

        User user = this.mapper.selectOneByQuery(queryWrapper);

        //用户不存在
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //3.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);

        //4.获取脱敏后的用户信息
        return this.getLoginUserVO(user);

    }

    @Override
    public User getLoginUser(HttpServletRequest request){
        //先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser==null||currentUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //从数据库查询
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if(currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request){
        //先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj==null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"未登录");
        }

        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user){
        if(user == null){
            return null;
        }

        UserVO userVO = new UserVO();
        /**
         * 属性拷贝
         * 这里的 BeanUtil (通常来自 Hutool 工具包) 会自动对比两个类。
         * 它发现 user 有 "userName"，userVO 也有 "userName"，就把值拷过去。
         * 它发现 user 有 "password"，但 userVO 没有，它就直接忽略（实现了自动脱敏）。
         */
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList){
        // 1. 判空：如果列表是空的，直接返回一个空的新列表，防止报空指针错误
        if(CollUtil.isEmpty(userList)){
            return new ArrayList<>();
        }
        // 2. 流式处理
        /**
         * .stream()：
         * 想象 userList 是一个装满原材料（User）的箱子。
         * .stream() 就是把箱子倒在传送带上，让它们一个个排队流过来。
         *
         * map：映射/转换的意思。它的作用是“进来一个A，出去一个B”。
         * this::getUserVO：
         * 对于传送带上传过来的每一个 User 对象，都调用一下当前类（this）里的 getUserVO 方法。
         *
         * .collect(Collectors.toList())：
         * 这是传送带的尽头。
         * 把传送带上处理好的 UserVO 一个个拿下来，重新收集（Collect）到一个新的 List 里面
         */
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    /**
     * 负责把前端传来的那个大杂烩对象 UserQueryRequest，
     * 智能地翻译成一条精准的 SQL 查询条件，
     * 而且还能自动处理哪些条件该加，哪些条件该扔（为空时）
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest){
        if(userQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请求参数为空");
        }

        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id",id)
                .eq("userRole",userRole)
                .like("userAccount",userAccount)
                .like("userName",userName)
                .like("userProfile",userProfile)
                .orderBy(sortField,"ascend".equals(sortOrder));
    }
}
