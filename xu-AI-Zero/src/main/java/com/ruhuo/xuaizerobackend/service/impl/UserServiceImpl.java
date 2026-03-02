package com.ruhuo.xuaizerobackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.dto.user.UserQueryRequest;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.mapper.UserMapper;
import com.ruhuo.xuaizerobackend.model.enums.UserRoleEnum;
import com.ruhuo.xuaizerobackend.model.vo.LoginUserVO;
import com.ruhuo.xuaizerobackend.model.vo.UserVO;
import com.ruhuo.xuaizerobackend.service.UserService;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    /**
     * 用户注册方法
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 新注册用户的ID
     * @throws BusinessException 当参数校验失败或注册过程中出现错误时抛出
     */
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        //1.校验
        // 检查参数是否为空
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 检查用户账号长度是否足够
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        // 检查密码长度是否足够
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 检查两次输入的密码是否一致
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        //2.检查是否重复
        // 创建查询条件，检查账号是否已存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        //3.加密
        // 对用户密码进行加密处理
        String encryptPassword = getEncryptPassword(userPassword);

        //4.插入数据
        // 创建新用户对象并设置基本信息
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserRole(UserRoleEnum.USER.getValue());
        // 保存用户信息到数据库
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        // 返回新注册用户的ID
        return user.getId();
    }

    @Override
    /**
     * 获取加密后的密码
     * @param userPassword 用户原始密码
     * @return 返回经过MD5加密后的密码字符串
     */
    public String getEncryptPassword(String userPassword) {
        //加盐+混淆密码，使用固定盐值"ruhuo"增加密码安全性
        final String SALT = "ruhuo";
        //将盐值与用户密码拼接后进行MD5加密，并返回十六进制格式的加密结果
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 用于 将 User 实体转换为 LoginUserVO 视图对象，
     * 通过 BeanUtil.copyProperties 快速拷贝同名属性，
     * 避免返回敏感字段，提升安全性和代码可维护性。
     * 该方法为接口实现方法，用于获取用户登录视图对象
     */
    @Override  // 标记该方法实现了接口中的抽象方法
    public LoginUserVO getLoginUserVO(User user) {
        // 参数校验：如果传入的user对象为null，则直接返回null
        if (user == null) {
            return null;
        }
        // 创建LoginUserVO视图对象实例
        LoginUserVO loginUserVO = new LoginUserVO();
        // 使用BeanUtil工具类将user对象的属性拷贝到loginUserVO对象中
        // 只拷贝名称相同的属性，避免手动设置每个属性
        BeanUtil.copyProperties(user, loginUserVO);
        // 返回转换后的视图对象
        return loginUserVO;
    }

    @Override
    /**
     * 用户登录方法
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request HTTP请求对象，用于获取session
     * @return LoginUserVO 脱敏后的用户信息
     */
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        // 检查参数是否为空
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 检查用户账号长度是否符合要求
        // 检查用户账号长度是否小于4
        if (userAccount.length() < 4) {
            // 如果账号长度小于4，抛出业务异常
            // 异常错误码为PARAMS_ERROR，错误信息为"账号错误"
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }

        // 检查用户密码长度是否小于8位
        if (userPassword.length() < 8) {
            // 如果密码长度不足8位，抛出业务异常
            // 异常错误码为PARAMS_ERROR，错误信息为"密码错误"
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        //2.加密
        String encryptPassword = getEncryptPassword(userPassword);
        //查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);

        User user = this.mapper.selectOneByQuery(queryWrapper);

        //用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        //3.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        //4.获取脱敏后的用户信息
        return this.getLoginUserVO(user);

    }

    @Override
    /**
     * 获取当前登录用户信息
     * @param request HttpServletRequest对象，用于获取会话信息
     * @return User 当前登录用户对象
     * @throws BusinessException 当用户未登录或用户不存在时抛出
     */
    public User getLoginUser(HttpServletRequest request) {
        //先判断是否已登录，从会话中获取用户对象
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        //如果用户对象为空或用户ID为空，则抛出未登录异常
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //从数据库查询最新用户信息
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        //如果数据库中不存在该用户，则抛出未登录异常
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    /**
     * 用户登出方法
     * @param request HttpServletRequest对象，包含请求相关信息
     * @return 登出成功返回true
     * @throws BusinessException 当用户未登录时抛出异常
     */
    public boolean userLogout(HttpServletRequest request) {
        //先判断是否已登录
        //从session中获取用户登录状态信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        //如果用户对象为null，表示未登录
        if (userObj == null) {
            //抛出业务异常，提示未登录
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }

        //移除登录态
        //从session中移除用户登录状态信息，实现登出
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取用户视图对象
     * 该方法将User实体对象转换为UserVO视图对象，用于前端展示
     *
     * @param user 用户实体对象
     * @return 转换后的用户视图对象，如果输入参数为null则返回null
     */
    @Override
    public UserVO getUserVO(User user) {
        // 检查用户对象是否为空
        if (user == null) {
            return null;
        }

        // 创建用户视图对象实例
        UserVO userVO = new UserVO();
        /**
         * 属性拷贝
         * 这里的 BeanUtil (通常来自 Hutool 工具包) 会自动对比两个类。
         * 它发现 user 有 "userName"，userVO 也有 "userName"，就把值拷过去。
         * 它发现 user 有 "password"，但 userVO 没有，它就直接忽略（实现了自动脱敏）。
         */
        // 使用BeanUtil工具将user对象的属性拷贝到userVO对象中，实现数据转换
        BeanUtil.copyProperties(user, userVO);
        // 返回转换后的用户视图对象
        return userVO;
    }

    /**
     * 获取用户视图对象列表
     * 该方法将用户实体列表转换为用户视图对象列表
     *
     * @param userList 用户实体列表，包含完整的用户信息
     * @return 返回用户视图对象列表，通常用于前端展示
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        // 1. 判空：如果列表是空的，直接返回一个空的新列表，防止报空指针错误
        if (CollUtil.isEmpty(userList)) {
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
     *
     * @param userQueryRequest 包含查询条件的请求对象
     * @return 返回一个构建好的 QueryWrapper 对象，用于数据库查询
     */
    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 检查请求参数是否为空，为空则抛出参数异常
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }


        // 从请求对象中提取各个查询参数
        Long id = userQueryRequest.getId();                    // 用户ID
        String userAccount = userQueryRequest.getUserAccount(); // 用户账号
        String userName = userQueryRequest.getUserName();       // 用户名
        String userProfile = userQueryRequest.getUserProfile(); // 用户简介
        String userRole = userQueryRequest.getUserRole();      // 用户角色
        String sortField = userQueryRequest.getSortField();    // 排序字段
        String sortOrder = userQueryRequest.getSortOrder();    // 排序方式

        // 构建查询条件
        // 1. 使用 QueryWrapper 创建查询条件
        // 2. 添加各个字段的查询条件
        //    - id 和 userRole 使用精确匹配 (eq)
        //    - userAccount、userName 和 userProfile 使用模糊匹配 (like)
        // 3. 根据排序字段和排序方式设置排序规则
        return QueryWrapper.create()
                .eq("id", id)                           // 精确匹配id
                .eq("userRole", userRole)               // 精确匹配用户角色
                .like("userAccount", userAccount)       // 模糊匹配用户账号
                .like("userName", userName)             // 模糊匹配用户名
                .like("userProfile", userProfile)       // 模糊匹配用户简介
                .orderBy(sortField, "ascend".equals(sortOrder)); // 根据字段和排序方式排序
    }
}
