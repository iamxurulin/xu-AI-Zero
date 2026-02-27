package com.ruhuo.xuaizerobackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.ruhuo.xuaizerobackend.annotation.AuthCheck;
import com.ruhuo.xuaizerobackend.common.BaseResponse;
import com.ruhuo.xuaizerobackend.common.DeleteRequest;
import com.ruhuo.xuaizerobackend.common.ResultUtils;
import com.ruhuo.xuaizerobackend.constant.UserConstant;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.exception.ThrowUtils;
import com.ruhuo.xuaizerobackend.model.dto.user.*;
import com.ruhuo.xuaizerobackend.model.entity.User;
import com.ruhuo.xuaizerobackend.model.vo.LoginUserVO;
import com.ruhuo.xuaizerobackend.model.vo.UserVO;
import com.ruhuo.xuaizerobackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 用户 控制层。
 * 提供用户相关的HTTP接口，包括注册、登录、用户信息管理等操作。
 *
 * @author <a href="https://github.com/iamxurulin">iamxurulin</a>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService; // 用户服务层接口，用于处理用户相关的业务逻辑

    /**
     * 用户注册接口
     * 处理用户注册请求，验证参数并调用服务层完成注册
     *
     * @param userRegisterRequest 用户注册请求对象，包含用户账号、密码和确认密码信息
     * @return BaseResponse<Long> 返回注册结果，包含用户ID
     */
    @PostMapping("register")  // HTTP POST请求映射到"/register"路径
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 检查请求参数是否为空，若为空则抛出参数错误异常
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        // 从请求对象中获取用户账号
        String userAccount = userRegisterRequest.getUserAccount();
        // 从请求对象中获取用户密码
        String userPassword = userRegisterRequest.getUserPassword();
        // 从请求对象中获取确认密码
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 调用用户服务进行用户注册，获取注册结果
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        // 返回注册成功的响应，包含用户ID
        return ResultUtils.success(result);
    }

    /**
     * 用户登录接口
     * 处理用户登录请求，验证账号密码并返回登录信息
     *
     * @param userLoginRequest 用户登录请求参数，包含用户名和密码
     * @param request          HTTP请求对象，用于获取请求相关信息
     * @return BaseResponse<LoginUserVO> 返回登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 检查请求参数是否为空，若为空则抛出参数错误异常
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取用户名
        String userAccount = userLoginRequest.getUserAccount();
        // 获取密码
        String userPassword = userLoginRequest.getUserPassword();

        // 调用用户服务进行登录验证，获取登录用户信息
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        // 返回成功响应，包含登录用户信息
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户信息接口
     * 从会话中获取当前登录用户信息
     *
     * @param request HTTP请求对象，用于获取当前会话信息
     * @return BaseResponse<LoginUserVO> 返回包含用户登录信息的响应对象
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        // 通过userService获取当前登录用户实体
        User loginUser = userService.getLoginUser(request);
        // 将用户实体转换为VO对象并封装为成功响应返回
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销接口
     * 处理用户注销请求，清除会话信息
     *
     * @param request HTTP请求对象，用于获取会话信息
     * @return 返回操作结果，包含登出是否成功的布尔值
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 参数校验：如果request为null，则抛出参数错误异常
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        // 调用用户服务层的登出方法，执行登出操作
        boolean result = userService.userLogout(request);
        // 返回操作成功的结果，包含登出操作的结果
        return ResultUtils.success(result);
    }

    /**
     * 创建用户接口
     * 仅管理员可调用，创建新用户账号
     *
     * @param userAddRequest 添加用户的请求参数，包含用户信息
     * @return 返回新添加用户的ID
     */
    @PostMapping("/add") // HTTP POST请求，映射到/add路径
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) //  只有管理员能调
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        // 检查请求参数是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 创建一个新的User对象
        User user = new User();

        // 将请求参数中的属性复制到User对象中
        BeanUtil.copyProperties(userAddRequest, user);
        //默认密码12345678
        // 定义默认密码常量
        final String DEFAULT_PASSWORD = "12345678";
        // 获取加密后的密码
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        // 设置用户的加密密码
        user.setUserPassword(encryptPassword);
        // 保存用户信息
        boolean result = userService.save(user);
        // 如果保存失败，抛出操作异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回用户ID，表示操作成功
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户接口
     *
     * @param deleteRequest 删除请求参数，包含要删除的用户ID
     * @return 返回操作结果，成功返回true，失败返回false
     * 只有管理员角色才能调用此接口
     */
    @PostMapping("/delete") // HTTP POST请求，映射到/delete路径
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)// 只有管理员能调
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        // 检查请求参数是否为空或ID是否小于等于0
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            // 参数无效，抛出业务异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 调用服务层方法根据ID删除用户
        boolean b = userService.removeById(deleteRequest.getId());
        // 返回操作成功结果，包含删除操作是否成功的布尔值
        return ResultUtils.success(b);
    }

    /**
     * 更新用户信息接口
     * 仅管理员可调用，更新用户信息
     *
     * @param userUpdateRequest 包含用户更新信息的请求对象，必须包含用户ID
     * @return BaseResponse<Boolean> 更新成功返回true，失败抛出异常
     * @throws BusinessException 当请求参数为空或用户ID为空时抛出参数错误异常
     *
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)// 只有管理员能调

    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 检查请求参数是否为空或用户ID是否为空
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 创建新的User对象并复制请求中的属性
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        // 执行更新操作，如果更新失败则抛出异常
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回更新成功的响应
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取用户（仅管理员）
     * <p>
     * 该接口仅允许管理员角色访问，通过id参数获取指定用户信息
     *
     * @param id 用户ID，必须为大于0的正整数
     * @return BaseResponse<User> 包含用户信息的响应结果，如果用户不存在则返回错误信息
     */
    @GetMapping("/get")  // HTTP GET请求映射到/get路径
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，要求用户必须具有管理员角色
    public BaseResponse<User> getUserById(long id) {  // 方法：根据用户ID获取用户信息
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);  // 参数校验，如果id小于等于0则抛出参数错误异常
        User user = userService.getById(id);  // 调用服务层方法根据ID获取用户
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);  // 检查用户是否存在，不存在则抛出未找到错误异常
        return ResultUtils.success(user);  // 返回成功响应，包含用户信息
    }

    /**
     * 根据id获取包装类
     * // 注意：这里没有 @AuthCheck，说明允许普通用户查看（比如查看他人主页）
     *
     * @param id 用户ID
     * @return 返回包装后的用户视图对象(BaseResponse<UserVO>)
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        // 先调用基础方法获取用户对象
        BaseResponse<User> response = getUserById(id);
        // 从响应中获取用户数据
        User user = response.getData();
        // 使用用户服务将用户对象转换为视图对象并返回成功响应
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     * @return 返回分页后的用户视图对象列表
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，只有管理员可以访问
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        // 参数校验，如果请求参数为空则抛出参数错误异常
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取分页参数
        long pageNum = userQueryRequest.getPageNum();
        long pageSize = userQueryRequest.getPageSize();
        // 1. 第一步：查数据库（拿到的是包含密码的 User Page）
        // 使用分页查询，获取用户原始数据
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize), userService.getQueryWrapper(userQueryRequest));

        // 2. 第二步：脱敏（把 User Page 转换成 UserVO Page）
        // 创建新的视图对象分页，保持原始分页信息
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        // 将用户列表转换为视图对象列表，进行数据脱敏处理
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        // 设置转换后的记录列表
        userVOPage.setRecords(userVOList);
        // 返回成功响应，包含脱敏后的用户数据
        return ResultUtils.success(userVOPage);
    }
}
