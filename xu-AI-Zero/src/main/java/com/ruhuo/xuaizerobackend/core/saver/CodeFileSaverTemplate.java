package com.ruhuo.xuaizerobackend.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ruhuo.xuaizerobackend.ai.model.HtmlCodeResult;
import com.ruhuo.xuaizerobackend.ai.model.MultiFileCodeResult;
import com.ruhuo.xuaizerobackend.constant.AppConstant;
import com.ruhuo.xuaizerobackend.exception.BusinessException;
import com.ruhuo.xuaizerobackend.exception.ErrorCode;
import com.ruhuo.xuaizerobackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.prefs.BackingStoreException;

/**
 * 抽象代码文件保存器 - 模板方法模式
 * <p>
 * public：对外开放，谁都能调。
 * private：私有，只有自己能用，儿子（子类）都不能用。
 * protected：传家宝。不对外人开放，但专门留给“儿子”（子类）使用或重写。
 * <p>
 * 抽象类 CodeFileSaverTemplate，用于保存代码文件的模板类
 *
 * @param <T> 泛型参数，表示要保存的代码结果类型
 */
public abstract class CodeFileSaverTemplate<T> {

    // 保存代码文件的根目录路径
    protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 保存代码文件的最终方法，按照固定流程执行
     *
     * @param result 要保存的代码结果
     * @param appId  应用ID，用于构建唯一目录
     * @return 创建的目录文件对象
     */
    public final File saveCode(T result, Long appId) {
        // 1. 验证输入参数
        validateInput(result);

        // 2. 构建基于 appId 的目录
        String baseDirPath = buildUniqueDir(appId);

        // 3. 保存文件，具体实现由子类提供
        saveFiles(result, baseDirPath);

        // 4. 返回目录文件对象
        return new File(baseDirPath);
    }


    /**
     * 验证输入结果对象的合法性
     *
     * @param result 需要验证的结果对象
     * @throws BusinessException 当结果对象为空时抛出业务异常
     */
    protected void validateInput(T result) {
        // 检查结果对象是否为null
        if (result == null) {
            // 如果结果对象为空，抛出业务异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }


    /**
     * 构建唯一的目录路径
     *
     * @param appId 应用的唯一标识ID
     * @return 返回构建好的目录路径字符串
     * @throws BusinessException 当应用ID为空时抛出参数异常
     */
    protected final String buildUniqueDir(Long appId) {
        // 检查应用ID是否为空，如果为空则抛出业务异常
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        }

        // 获取代码类型的值，并与应用ID组合成唯一的目录名称
        String codeType = getCodeType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, appId);
        // 组合完整的目录路径
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;

        // 真正去硬盘上创建这个文件夹
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 将字符串内容写入到指定文件中
     *
     * @param dirPath  目录路径
     * @param filename 文件名
     * @param content  要写入的文件内容
     */
    protected final void writeToFile(String dirPath, String filename, String content) {
        // 检查内容是否为空或空白字符串
        if (StrUtil.isNotBlank(content)) {
            // 拼接完整的文件路径
            String filePath = dirPath + File.separator + filename;
            // 使用 UTF-8 编码写入，防止中文乱码
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 获取代码生成类型的抽象方法
     * 该方法由子类实现，用于返回具体的代码生成类型
     *
     * @return CodeGenTypeEnum 枚举类型的代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();


    /**
     * 这是一个抽象方法，用于保存文件
     *
     * @param result      需要保存的文件数据，类型为泛型T
     * @param baseDirPath 文件保存的基础目录路径
     *                    该方法需要在子类中实现具体逻辑
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}
